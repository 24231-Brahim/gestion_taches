import { afterEach, beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import {
  faThList,
  faBug,
  faCheckCircle,
  faPlus,
  faArrowUp,
  faArrowDown,
  faFlag,
  faExclamationTriangle,
} from '@fortawesome/free-solid-svg-icons';
import { TranslateModule } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';

import { TaskStatus } from 'app/entities/enumerations/task-status.model';
import { TaskService } from '../service/task.service';
import { AlertService } from 'app/core/util/alert.service';
import { AccountService } from 'app/core/auth/account.service';
import { TaskKanbanBoard } from './task-kanban-board';
import { sampleWithRequiredData, sampleWithPartialData, sampleWithFullData } from '../task.test-samples';
import { ITask } from '../task.model';

describe('TaskKanbanBoard', () => {
  let fixture: ComponentFixture<TaskKanbanBoard>;
  let comp: TaskKanbanBoard;
  let taskServiceMock: { partialUpdate: ReturnType<typeof vitest.fn> };
  let alertServiceMock: { addAlert: ReturnType<typeof vitest.fn> };
  let translateServiceMock: { instant: ReturnType<typeof vitest.fn> };
  let accountServiceMock: { account: ReturnType<typeof vitest.fn> };

  const mockTasks: ITask[] = [
    { ...sampleWithRequiredData, status: 'NEEDS_INFO' },
    { ...sampleWithPartialData, status: 'IN_PROGRESS' },
    { ...sampleWithFullData, status: 'DONE' },
  ];

  beforeEach(() => {
    taskServiceMock = { partialUpdate: vitest.fn().mockReturnValue(of({})) };
    alertServiceMock = { addAlert: vitest.fn() };
    translateServiceMock = { instant: vitest.fn().mockReturnValue('error') };
    accountServiceMock = { account: vitest.fn().mockReturnValue({ authorities: ['ROLE_ADMIN'], login: 'admin' }) };

    TestBed.configureTestingModule({
      imports: [FontAwesomeModule, TranslateModule.forRoot(), TaskKanbanBoard],
      providers: [
        { provide: TaskService, useValue: taskServiceMock },
        { provide: AlertService, useValue: alertServiceMock },
        { provide: AccountService, useValue: accountServiceMock },
      ],
    });

    const library = TestBed.inject(FaIconLibrary);
    library.addIcons(faThList, faBug, faCheckCircle, faPlus, faArrowUp, faArrowDown, faFlag, faExclamationTriangle);

    fixture = TestBed.createComponent(TaskKanbanBoard);
    comp = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should create', () => {
    expect(comp).toBeTruthy();
  });

  it('should compute columns filtering tasks by status', () => {
    fixture.componentRef.setInput('tasks', mockTasks);
    fixture.detectChanges();
    const cols = comp.getColumns();
    expect(cols.length).toBe(Object.keys(TaskStatus).length);
    expect(cols.find(c => c.status === 'NEEDS_INFO')!.tasks.length).toBe(1);
    expect(cols.find(c => c.status === 'IN_PROGRESS')!.tasks.length).toBe(1);
    expect(cols.find(c => c.status === 'DONE')!.tasks.length).toBe(1);
    expect(cols.find(c => c.status === 'NEW')!.tasks.length).toBe(0);
    expect(cols.find(c => c.status === 'READY_FOR_TEST')!.tasks.length).toBe(0);
  });

  it('onDragStart should set dragTaskId', () => {
    comp.onDragStart(mockTasks[0], new DragEvent('dragstart'));
    expect(comp.dragTaskId).toBe(mockTasks[0].id);
  });

  it('onDragOver should set dragOverStatus and call preventDefault', () => {
    const event = new DragEvent('dragover');
    vitest.spyOn(event, 'preventDefault');
    comp.onDragOver(event, 'IN_PROGRESS');
    expect(event.preventDefault).toHaveBeenCalled();
    expect(comp.dragOverStatus).toBe('IN_PROGRESS');
  });

  it('onDragLeave should reset dragOverStatus', () => {
    comp.dragOverStatus = 'NEEDS_INFO';
    comp.onDragLeave();
    expect(comp.dragOverStatus).toBeNull();
  });

  it('onDrop should call partialUpdate when status differs', () => {
    fixture.componentRef.setInput('tasks', mockTasks);
    fixture.detectChanges();
    comp.dragTaskId = mockTasks[0].id;
    const event = new DragEvent('drop');
    vitest.spyOn(event, 'preventDefault');
    comp.onDrop(event, 'DONE');
    expect(event.preventDefault).toHaveBeenCalled();
    expect(taskServiceMock.partialUpdate).toHaveBeenCalledWith({ id: mockTasks[0].id, status: 'DONE' });
  });

  it('onDrop should not call partialUpdate when status is same', () => {
    fixture.componentRef.setInput('tasks', mockTasks);
    fixture.detectChanges();
    comp.dragTaskId = mockTasks[0].id;
    comp.onDrop(new DragEvent('drop'), 'NEEDS_INFO');
    expect(taskServiceMock.partialUpdate).not.toHaveBeenCalled();
  });

  it('onDrop should no-op when dragTaskId is null', () => {
    comp.dragTaskId = null;
    comp.onDrop(new DragEvent('drop'), 'DONE');
    expect(taskServiceMock.partialUpdate).not.toHaveBeenCalled();
  });

  it('onDrop should show alert on error', () => {
    fixture.componentRef.setInput('tasks', mockTasks);
    fixture.detectChanges();
    taskServiceMock.partialUpdate.mockReturnValue(throwError(() => ({ error: { detail: 'fail' }, message: 'err' })));
    comp.dragTaskId = mockTasks[0].id;
    comp.onDrop(new DragEvent('drop'), 'DONE');
    expect(alertServiceMock.addAlert).toHaveBeenCalledWith({ type: 'danger', message: 'fail' });
  });

  it('onDragEnd should reset drag state', () => {
    comp.dragTaskId = 42;
    comp.dragOverStatus = 'NEEDS_INFO';
    comp.onDragEnd();
    expect(comp.dragTaskId).toBeNull();
    expect(comp.dragOverStatus).toBeNull();
  });

  it('onTaskClick should emit the task', () => {
    const spy = vitest.fn();
    comp.selectTask.subscribe(spy);
    comp.onTaskClick(mockTasks[0]);
    expect(spy).toHaveBeenCalledWith(mockTasks[0]);
  });

  it('getInitials should return first letter uppercase', () => {
    expect(comp.getInitials('john')).toBe('J');
    expect(comp.getInitials('')).toBe('?');
    expect(comp.getInitials(undefined)).toBe('?');
  });
});
