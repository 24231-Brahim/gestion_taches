import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { Observable, interval, switchMap, tap } from 'rxjs';

export interface INotification {
  id: number;
  message: string;
  issueId?: number;
  issueTitle?: string;
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
    this.refresh();
    this.pollingSubscription = interval(30000).subscribe(() => this.refresh());
  }

  stopPolling(): void {
    if (this.pollingSubscription) {
      this.pollingSubscription.unsubscribe();
    }
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

  markAsRead(id: number): Observable<any> {
    return this.http.patch(`${this.resourceUrl}/${id}/read`, {});
  }
}
