import { afterEach, beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { CUSTOM_ELEMENTS_SCHEMA, signal } from '@angular/core';
import { of, Subject } from 'rxjs';

import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import {
  faCheck,
  faClipboardList,
  faFolder,
  faPencil,
  faPlay,
  faPlus,
  faChartLine,
  faTableColumns,
  faListCheck,
} from '@fortawesome/free-solid-svg-icons';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap/modal';
import { TranslateModule } from '@ngx-translate/core';

import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/util/alert.service';
import { TaskService } from 'app/entities/task/service/task.service';
import { ProjectService } from 'app/entities/project/service/project.service';
import { SprintService } from '../service/sprint.service';
import { ISprint } from '../sprint.model';
import { Sprint } from './sprint';

vitest.useFakeTimers();

describe('Sprint Component', () => {
  let comp: Sprint;
  let fixture: ComponentFixture<Sprint>;
  let modalService: NgbModal;

  const mockSprints = signal<ISprint[]>([]);
  const mockRefresh = vitest.fn();

  const mockSprintService = {
    sprints: mockSprints,
    sprintsParams: signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(undefined),
    refresh: mockRefresh,
  } as unknown as SprintService;

  const mockTaskService = {
    tasks: signal([]),
    tasksParams: signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(undefined),
    tasksResource: { hasValue: () => false },
    refresh: vitest.fn(),
    partialUpdate: vitest.fn().mockReturnValue(of({})),
  } as unknown as TaskService;

  const mockProjectService = {
    findByKey: vitest.fn().mockReturnValue(of({ id: 1, key: 'test', name: 'Test Project' })),
  } as unknown as ProjectService;

  const mockAlertService = {
    addAlert: vitest.fn(),
  } as unknown as AlertService;

  const mockAccountService = {
    account: signal({ authorities: ['ROLE_USER'], login: 'testuser' }),
  } as unknown as AccountService;

  const parentParamMap = of(convertToParamMap({ key: 'test-key' }));

  beforeEach(() => {
    mockSprints.set([]);
    mockRefresh.mockClear();

    TestBed.configureTestingModule({
      imports: [TranslateModule.forRoot()],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      providers: [
        { provide: ActivatedRoute, useValue: { parent: { paramMap: parentParamMap }, paramMap: parentParamMap } },
        { provide: SprintService, useValue: mockSprintService },
        { provide: TaskService, useValue: mockTaskService },
        { provide: ProjectService, useValue: mockProjectService },
        { provide: AlertService, useValue: mockAlertService },
        { provide: AccountService, useValue: mockAccountService },
      ],
    });

    fixture = TestBed.createComponent(Sprint);
    comp = fixture.componentInstance;
    modalService = TestBed.inject(NgbModal);

    const library = TestBed.inject(FaIconLibrary);
    library.addIcons(faCheck, faClipboardList, faFolder, faPencil, faPlay, faPlus, faChartLine, faTableColumns, faListCheck);

    fixture.detectChanges();
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should create the component', () => {
    expect(comp).toBeTruthy();
  });

  it('should have board tab as default', () => {
    expect(comp.activeTab()).toBe('board');
  });

  it('should switch tabs', () => {
    comp.setTab('planning');
    expect(comp.activeTab()).toBe('planning');

    comp.setTab('burndown');
    expect(comp.activeTab()).toBe('burndown');

    comp.setTab('board');
    expect(comp.activeTab()).toBe('board');
  });

  it('should select sprint on change', () => {
    const sprint: ISprint = { id: 1, name: 'Sprint 1', status: 'ACTIVE' };
    mockSprints.set([sprint]);
    fixture.detectChanges();

    comp.onSprintChange(1);
    expect(comp.selectedSprintId()).toBe(1);
    expect(comp.selectedSprint()).toEqual(sprint);
  });

  it('should open create sprint modal', () => {
    const modalOpen = vitest.spyOn(modalService, 'open').mockReturnValue({
      componentInstance: {},
      closed: new Subject(),
    } as any);

    comp.openCreateSprintModal();
    expect(modalOpen).toHaveBeenCalled();
  });

  it('should open edit sprint modal', () => {
    const modalOpen = vitest.spyOn(modalService, 'open').mockReturnValue({
      componentInstance: {},
      closed: new Subject(),
    } as any);

    const sprint: ISprint = { id: 1, name: 'Sprint 1', status: 'ACTIVE' };
    comp.openEditSprintModal(sprint);
    expect(modalOpen).toHaveBeenCalled();
  });

  it('should close velocity report modal', () => {
    comp.velocityReport.set({ tachesPrevues: 10, tachesTerminees: 8, pourcentage: 80, tachesReportees: 2 });
    comp.showVelocityModal.set(true);
    expect(comp.showVelocityModal()).toBe(true);

    comp.closeVelocityModal();
    expect(comp.showVelocityModal()).toBe(false);
    expect(comp.velocityReport()).toBeNull();
  });

  it('should compute hasActiveSprint correctly', () => {
    expect(comp.hasActiveSprint()).toBe(false);

    mockSprints.set([{ id: 1, name: 'Sprint 1', status: 'PLANNED' }]);
    fixture.detectChanges();
    expect(comp.hasActiveSprint()).toBe(false);

    mockSprints.set([
      { id: 1, name: 'Sprint 1', status: 'PLANNED' },
      { id: 2, name: 'Sprint 2', status: 'ACTIVE' },
    ]);
    fixture.detectChanges();
    expect(comp.hasActiveSprint()).toBe(true);
  });

  it('should compute canStartSprint correctly', () => {
    expect(comp.canStartSprint()).toBe(false);

    const plannedSprint: ISprint = { id: 1, name: 'Sprint 1', status: 'PLANNED' };
    mockSprints.set([plannedSprint]);
    fixture.detectChanges();
    comp.onSprintChange(1);
    fixture.detectChanges();
    expect(comp.canStartSprint()).toBe(true);

    mockSprints.set([plannedSprint, { id: 2, name: 'Sprint 2', status: 'ACTIVE' }]);
    comp.onSprintChange(1);
    fixture.detectChanges();
    expect(comp.canStartSprint()).toBe(false);
  });

  it('should compute canCloseSprint correctly', () => {
    expect(comp.canCloseSprint()).toBe(false);

    const activeSprint: ISprint = { id: 1, name: 'Sprint 1', status: 'ACTIVE' };
    mockSprints.set([activeSprint]);
    fixture.detectChanges();
    comp.onSprintChange(1);
    fixture.detectChanges();
    expect(comp.canCloseSprint()).toBe(true);
  });

  it('should compute sprintProgress correctly', () => {
    expect(comp.sprintProgress()).toBe(0);

    comp.tasks.set([
      { id: 1, title: 'Task 1', status: 'DONE' },
      { id: 2, title: 'Task 2', status: 'TODO' },
    ]);
    fixture.detectChanges();
    expect(comp.sprintProgress()).toBe(50);

    comp.tasks.set([
      { id: 1, title: 'Task 1', status: 'DONE' },
      { id: 2, title: 'Task 2', status: 'DONE' },
    ]);
    fixture.detectChanges();
    expect(comp.sprintProgress()).toBe(100);
  });
});
