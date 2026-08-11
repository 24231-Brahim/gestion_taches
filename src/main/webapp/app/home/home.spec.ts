import { beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { signal } from '@angular/core';
import { Router } from '@angular/router';
import { provideRouter } from '@angular/router';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';

import { TranslateModule } from '@ngx-translate/core';
import { of } from 'rxjs';

import { registerAllIcons } from 'src/test/javascript/mocks/fa-icon-library';

import { AccountService } from 'app/core/auth/account.service';
import { Account } from 'app/core/auth/account.model';

import Home from './home';

const createAccount = (authorities: string[], login = 'user'): Account =>
  new Account(true, authorities, 'user@test.com', 'Test', 'fr', 'Test', login, '', 1);

describe('Home Component', () => {
  let comp: Home;
  let fixture: ComponentFixture<Home>;
  let mockAccountService: AccountService;
  let mockRouter: Router;
  let accountSignal: ReturnType<typeof signal<Account | null>>;

  beforeEach(() => {
    accountSignal = signal<Account | null>(null);

    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      providers: [
        provideRouter([]),
        provideHttpClientTesting(),
        {
          provide: AccountService,
          useValue: {
            account: accountSignal.asReadonly(),
            isAuthenticated: vitest.fn(),
          },
        },
      ],
    });
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(Home);
    comp = fixture.componentInstance;
    mockAccountService = TestBed.inject(AccountService);
    mockAccountService.identity = vitest.fn(() => of(null));

    registerAllIcons(TestBed.inject(FaIconLibrary));

    mockRouter = TestBed.inject(Router);
    vitest.spyOn(mockRouter, 'navigate');
  });

  describe('login', () => {
    it('should navigate to /login on login', () => {
      // WHEN
      comp.login();

      // THEN
      expect(mockRouter.navigate).toHaveBeenCalledWith(['/login']);
    });
  });

  describe('role-based dashboard rendering', () => {
    it('should render the admin dashboard for ROLE_ADMIN', () => {
      accountSignal.set(createAccount(['ROLE_ADMIN', 'ROLE_USER']));
      fixture.detectChanges();

      expect(comp.isManagerOrAdmin()).toBe(true);
      expect(fixture.debugElement.query(By.css('jhi-dashboard'))).toBeTruthy();
      expect(fixture.debugElement.query(By.css('jhi-developer-dashboard'))).toBeNull();
    });

    it('should render the developer dashboard without the projects list for ROLE_DEVELOPER', () => {
      accountSignal.set(createAccount(['ROLE_DEVELOPER', 'ROLE_USER']));
      fixture.detectChanges();

      expect(comp.isDeveloper()).toBe(true);
      const devDashboard = fixture.debugElement.query(By.css('jhi-developer-dashboard'));
      expect(devDashboard).toBeTruthy();
      expect(devDashboard.componentInstance.showRecentProjects()).toBe(false);
    });

    it('should render the developer dashboard with the projects list for ROLE_USER', () => {
      accountSignal.set(createAccount(['ROLE_USER']));
      fixture.detectChanges();

      expect(comp.isUser()).toBe(true);
      expect(comp.isDeveloper()).toBe(false);
      expect(comp.isManagerOrAdmin()).toBe(false);

      const devDashboard = fixture.debugElement.query(By.css('jhi-developer-dashboard'));
      expect(devDashboard).toBeTruthy();
      expect(devDashboard.componentInstance.showRecentProjects()).toBe(true);
      expect(devDashboard.componentInstance.hideTaskStats()).toBe(true);
    });

    it('should render the welcome message for anonymous users', () => {
      fixture.detectChanges();

      expect(fixture.debugElement.query(By.css('jhi-dashboard'))).toBeNull();
      expect(fixture.debugElement.query(By.css('jhi-developer-dashboard'))).toBeNull();
      expect(fixture.debugElement.query(By.css('.home-row'))).toBeTruthy();
    });
  });
});
