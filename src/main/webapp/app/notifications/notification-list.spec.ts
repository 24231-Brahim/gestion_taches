import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';
import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { FontAwesomeModule, FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { registerAllIcons } from 'src/test/javascript/mocks/fa-icon-library';
import { NotificationService } from 'app/core/util/notification.service';
import NotificationListComponent from './notification-list';
import { of } from 'rxjs';

describe('Notification List Component', () => {
  let comp: NotificationListComponent;
  let fixture: ComponentFixture<NotificationListComponent>;
  let mockNotificationService: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot(), FontAwesomeModule, NotificationListComponent],
      providers: [
        provideRouter([]),
        provideHttpClientTesting(),
        {
          provide: NotificationService,
          useValue: {
            getNotificationsPaginated: vitest.fn(() => of({ body: [], headers: { get: () => '0' } })),
            markAsRead: vitest.fn(() => of({})),
            markAllAsRead: vitest.fn(() => of({})),
            refresh: vitest.fn(() => of({})),
            notificationReceived$: of(null),
            unreadCount: signal(0),
            notifications: signal([]),
          },
        },
        {
          provide: NgbModal,
          useValue: {
            open: vitest.fn(),
          },
        },
      ],
    });

    const library = TestBed.inject(FaIconLibrary);
    registerAllIcons(library);

    fixture = TestBed.createComponent(NotificationListComponent);
    comp = fixture.componentInstance;
    mockNotificationService = TestBed.inject(NotificationService);
  });

  it('should load notifications on init', () => {
    comp.ngOnInit();
    expect(mockNotificationService.getNotificationsPaginated).toHaveBeenCalled();
  });

  it('should mark notification as read', () => {
    const notification = {
      id: 1,
      message: 'Test message',
      isRead: false,
      createdAt: '2026-08-06T00:00:00Z',
      userId: 1,
    };
    comp.markAsRead(notification);
    expect(mockNotificationService.markAsRead).toHaveBeenCalledWith(1);
  });
});
