import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable, inject, signal } from '@angular/core';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { Observable, interval, map, switchMap, tap } from 'rxjs';

export interface INotification {
  id: number;
  message: string;
  taskId?: number;
  taskTitle?: string;
  userId: number;
  isRead: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/notifications');

  readonly unreadCount = signal(0);
  readonly notifications = signal<INotification[]>([]);
  private pollingSubscription: any;

  startPolling(): void {
    if (!this.pollingSubscription) {
      this.refresh();
      this.pollingSubscription = interval(30000).subscribe(() => this.refresh());
    }
  }

  stopPolling(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
      this.pollingSubscription = null;
    }
    this.unreadCount.set(0);
    this.notifications.set([]);
  }

  refresh(): void {
    this.getUnreadCount().subscribe(count => this.unreadCount.set(count));
    this.getNotifications().subscribe(notifs => this.notifications.set(notifs));
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
}
