import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import { Observable, Subject } from 'rxjs';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export interface INotification {
  id: number;
  message: string;
  taskId?: number;
  taskTitle?: string;
  projectKey?: string;
  userId: number;
  userLogin?: string;
  isRead: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  readonly unreadCount = signal(0);
  readonly notifications = signal<INotification[]>([]);
  readonly notificationReceived$: Observable<INotification>;

  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly stateStorageService = inject(StateStorageService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/notifications');
  private stompClient: Client | null = null;
  private readonly notificationReceived = new Subject<INotification>();

  constructor() {
    this.notificationReceived$ = this.notificationReceived.asObservable();
  }

  startPolling(): void {
    this.refresh();
    this.connectWebSocket();
  }

  stopPolling(): void {
    this.disconnectWebSocket();
    this.unreadCount.set(0);
    this.notifications.set([]);
  }

  refresh(): void {
    this.getUnreadCount().subscribe(count => this.unreadCount.set(count));
    this.getNotificationsPaginated(0, 5).subscribe(resp => this.notifications.set(resp.body ?? []));
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

  markAllAsRead(): Observable<unknown> {
    return this.http.patch(`${this.resourceUrl}/read-all`, {});
  }

  private connectWebSocket(): void {
    if (this.stompClient?.connected) {
      return;
    }
    const url = this.applicationConfigService.getEndpointFor('websocket/tracker');
    const token = this.stateStorageService.getAuthenticationToken();

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(url),
      connectHeaders: {
        Authorization: token ? `Bearer ${token}` : '',
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.stompClient.onConnect = () => {
      this.stompClient?.subscribe('/user/queue/notifications', message => {
        try {
          const notification: INotification = JSON.parse(message.body);
          this.unreadCount.update(c => c + 1);
          this.notifications.update(list => [notification, ...list].slice(0, 5));
          this.notificationReceived.next(notification);
        } catch {
          // ignore parse errors
        }
      });
    };

    this.stompClient.activate();
  }

  private disconnectWebSocket(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }
}
