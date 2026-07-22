import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import { Observable, Subject, interval } from 'rxjs';

export interface INotification {
  id: number;
  message: string;
  taskId?: number;
  taskTitle?: string;
  projectKey?: string;
  userId: number;
  isRead: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly stateStorageService = inject(StateStorageService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/notifications');

  readonly unreadCount = signal(0);
  readonly notifications = signal<INotification[]>([]);
  private pollingSubscription: any;
  private abortController: AbortController | null = null;
  private readonly notificationReceived = new Subject<INotification>();

  readonly notificationReceived$ = this.notificationReceived.asObservable();

  startPolling(): void {
    if (!this.pollingSubscription) {
      this.refresh();
      this.pollingSubscription = interval(60000).subscribe(() => this.refresh());
    }
    this.connectSSE();
  }

  stopPolling(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
      this.pollingSubscription = null;
    }
    this.disconnectSSE();
    this.unreadCount.set(0);
    this.notifications.set([]);
  }

  refresh(): void {
    this.getUnreadCount().subscribe(count => this.unreadCount.set(count));
    this.getNotificationsPaginated(0, 20).subscribe(resp => this.notifications.set(resp.body ?? []));
  }

  getUnreadCount(): Observable<number> {
    return this.http.get<number>(`${this.resourceUrl}/unread-count`);
  }

  getNotifications(): Observable<INotification[]> {
    return this.http.get<INotification[]>(this.resourceUrl);
  }

  getNotificationsPaginated(page: number, size: number): Observable<HttpResponse<INotification[]>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', 'createdAt,desc');
    return this.http.get<INotification[]>(this.resourceUrl, { params, observe: 'response' });
  }

  markAsRead(id: number): Observable<any> {
    return this.http.patch(`${this.resourceUrl}/${id}/read`, {});
  }

  markAllAsRead(): Observable<void> {
    return this.http.patch<void>(`${this.resourceUrl}/read-all`, {});
  }

  private connectSSE(): void {
    if (this.abortController) {
      return;
    }
    this.abortController = new AbortController();
    const url = `${this.resourceUrl}/stream`;
    const token = this.stateStorageService.getAuthenticationToken();

    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    fetch(url, {
      headers,
      signal: this.abortController.signal,
    })
      .then(response => {
        if (!response.ok || !response.body) {
          this.disconnectSSE();
          return;
        }
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        const processStream = (): void => {
          reader.read().then(({ done, value }) => {
            if (done) {
              this.disconnectSSE();
              return;
            }
            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');
            buffer = lines.pop() ?? '';

            let eventName = 'message';
            let eventData = '';
            for (const line of lines) {
              if (line.startsWith('event:')) {
                eventName = line.slice(6).trim();
              } else if (line.startsWith('data:')) {
                eventData = line.slice(5).trim();
              } else if (line === '' && eventData) {
                if (eventName === 'notification') {
                  try {
                    const notification: INotification = JSON.parse(eventData);
                    this.unreadCount.update(c => c + 1);
                    this.notifications.update(list => [notification, ...list].slice(0, 20));
                    this.notificationReceived.next(notification);
                  } catch {
                    // ignore parse errors
                  }
                }
                eventName = 'message';
                eventData = '';
              }
            }
            processStream();
          });
        };
        processStream();
      })
      .catch(() => {
        this.disconnectSSE();
      });
  }

  private disconnectSSE(): void {
    if (this.abortController) {
      this.abortController.abort();
      this.abortController = null;
    }
  }
}
