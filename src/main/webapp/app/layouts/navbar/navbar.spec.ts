import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TranslateModule } from '@ngx-translate/core';

import { Account } from 'app/core/auth/account.model';
import { AccountService } from 'app/core/auth/account.service';
import { NotificationService } from 'app/core/util/notification.service';

import Navbar from './navbar';

const account: Account = {
  activated: true,
  authorities: [],
  email: '',
  firstName: 'John',
  langKey: '',
  lastName: 'Doe',
  login: 'john.doe',
  imageUrl: '',
};

describe('Navbar Component', () => {
  let comp: Navbar;
  let fixture: ComponentFixture<Navbar>;
  let mockAccountService: AccountService;
  let mockNotificationService: NotificationService;

  beforeEach(() => {
    const accountSignal = signal<Account | null>(null);

    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideRouter([]),
        provideHttpClientTesting(),
        {
          provide: AccountService,
          useValue: {
            account: accountSignal.asReadonly(),
            authenticate: vitest.fn((identity: Account | null) => accountSignal.set(identity)),
          },
        },
        {
          provide: NotificationService,
          useValue: {
            startPolling: vitest.fn(),
            stopPolling: vitest.fn(),
            markAsRead: vitest.fn(),
            refresh: vitest.fn(),
          },
        },
      ],
    });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Navbar);
    comp = fixture.componentInstance;
    mockAccountService = TestBed.inject(AccountService);
    mockNotificationService = TestBed.inject(NotificationService);
  });

  it('should create', () => {
    expect(comp).toBeTruthy();
  });

  it('should have account signal null by default', () => {
    expect(comp.account()).toBeNull();
  });

  it('should start notification polling when account is set', () => {
    fixture.detectChanges();
    mockAccountService.authenticate(account);
    fixture.detectChanges();

    expect(mockNotificationService.startPolling).toHaveBeenCalled();
  });
});
