import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { NotificationService } from './notification.service';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

describe('Notification Service', () => {
  let service: NotificationService;
  let httpMock: HttpTestingController;
  let mockStateStorageService: StateStorageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        NotificationService,
        {
          provide: StateStorageService,
          useValue: {
            getAuthenticationToken: vitest.fn(() => 'test-token'),
          },
        },
        {
          provide: ApplicationConfigService,
          useValue: {
            getEndpointFor: vitest.fn((path: string) => `/api/${path}`),
          },
        },
      ],
    });

    service = TestBed.inject(NotificationService);
    httpMock = TestBed.inject(HttpTestingController);
    mockStateStorageService = TestBed.inject(StateStorageService);
  });

  it('should call getUnreadCount', () => {
    service.getUnreadCount().subscribe(count => {
      expect(count).toBe(5);
    });

    const req = httpMock.expectOne({ method: 'GET', url: '/api/api/notifications/unread-count' });
    req.flush(5);
    httpMock.verify();
  });

  it('should call markAsRead', () => {
    service.markAsRead(123).subscribe();

    const req = httpMock.expectOne({ method: 'PATCH', url: '/api/api/notifications/123/read' });
    req.flush({});
    httpMock.verify();
  });

  it('should call markAllAsRead', () => {
    service.markAllAsRead().subscribe();

    const req = httpMock.expectOne({ method: 'PATCH', url: '/api/api/notifications/read-all' });
    req.flush({});
    httpMock.verify();
  });
});
