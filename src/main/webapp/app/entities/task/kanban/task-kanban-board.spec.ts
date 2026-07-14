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
import { TaskKanbanBoard } from './task-kanban-board';
import { sampleWithRequiredData, sampleWithPartialData, sampleWithFullData } from '../task.test-samples';
import { ITask } from '../task.model';

describe('TaskKanbanBoard', () => {
  let fixture: ComponentFixture<TaskKanbanBoard>;
  let comp: TaskKanbanBoard;
  let issueServiceMock: { partialUpdate: ReturnType<typeof vitest.fn> };
  let alertServiceMock: { addAlert: ReturnType<typeof vitest.fn> };
  let translateServiceMock: { instant: ReturnType<typeof vitest.fn> };

  const mockIssues: ITask[] = [
    { ...sampleWithRequiredData, status: 'TODO' },
    { ...sampleWithPartialData, status: 'IN_PROGRESS' },
    { ...sampleWithFullData, status: 'DONE' },
  ];

  beforeEach(() => {
    issueServiceMock = { partialUpdate: vitest.fn().mockReturnValue(of({})) };
    alertServiceMock = { addAlert: vitest.fn() };
    translateServiceMock = { instant: vitest.fn().mockReturnValue('error') };

    TestBed.configureTestingModule({
      imports: [FontAwesomeModule, TranslateModule.forRoot(), TaskKanbanBoard],
      providers: [
        { provide: TaskService, useValue: issueServiceMock },
        { provide: AlertService, useValue: alertServiceMock },
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
    fixture.componentRef.setInput('tasks', mockIssues);
    fixture.detectChanges();
    const cols = comp.getColumns();
    expect(cols.length).toBe(Object.keys(TaskStatus).length);
    expect(cols.find(c => c.status === 'TODO')!.tasks.length).toBe(1);
    expect(cols.find(c => c.status === 'IN_PROGRESS')!.tasks.length).toBe(1);
    expect(cols.find(c => c.status === 'DONE')!.tasks.length).toBe(1);
    expect(cols.find(c => c.status === 'BACKLOG')!.tasks.length).toBe(0);
    expect(cols.find(c => c.status === 'CANCELLED')!.tasks.length).toBe(0);
  });

  it('onDragStart should set dragTaskId', () => {
    comp.onDragStart(mockIssues[0]);
    expect(comp.dragTaskId).toBe(mockIssues[0].id);
  });

  it('onDragOver should set dragOverStatus and call preventDefault', () => {
    const event = new DragEvent('dragover');
    vitest.spyOn(event, 'preventDefault');
    comp.onDragOver(event, 'IN_PROGRESS');
    expect(event.preventDefault).toHaveBeenCalled();
    expect(comp.dragOverStatus).toBe('IN_PROGRESS');
  });

  it('onDragLeave should reset dragOverStatus', () => {
    comp.dragOverStatus = 'TODO';
    comp.onDragLeave();
    expect(comp.dragOverStatus).toBeNull();
  });

  it('onDrop should call partialUpdate when status differs', () => {
    fixture.componentRef.setInput('tasks', mockIssues);
    fixture.detectChanges();
    comp.dragTaskId = mockIssues[0].id;
    const event = new DragEvent('drop');
    vitest.spyOn(event, 'preventDefault');
    comp.onDrop(event, 'DONE');
    expect(event.preventDefault).toHaveBeenCalled();
    expect(issueServiceMock.partialUpdate).toHaveBeenCalledWith({ id: mockIssues[0].id, status: 'DONE' });
  });

  it('onDrop should not call partialUpdate when status is same', () => {
    fixture.componentRef.setInput('tasks', mockIssues);
    fixture.detectChanges();
    comp.dragTaskId = mockIssues[0].id;
    comp.onDrop(new DragEvent('drop'), 'TODO');
    expect(issueServiceMock.partialUpdate).not.toHaveBeenCalled();
  });

  it('onDrop should no-op when dragTaskId is null', () => {
    comp.dragTaskId = null;
    comp.onDrop(new DragEvent('drop'), 'DONE');
    expect(issueServiceMock.partialUpdate).not.toHaveBeenCalled();
  });

  it('onDrop should show alert on error', () => {
    fixture.componentRef.setInput('tasks', mockIssues);
    fixture.detectChanges();
    issueServiceMock.partialUpdate.mockReturnValue(throwError(() => ({ error: { detail: 'fail' }, message: 'err' })));
    comp.dragTaskId = mockIssues[0].id;
    comp.onDrop(new DragEvent('drop'), 'DONE');
    expect(alertServiceMock.addAlert).toHaveBeenCalledWith({ type: 'danger', message: 'fail' });
  });

  it('onDragEnd should reset drag state', () => {
    comp.dragTaskId = 42;
    comp.dragOverStatus = 'TODO';
    comp.onDragEnd();
    expect(comp.dragTaskId).toBeNull();
    expect(comp.dragOverStatus).toBeNull();
  });

  it('onTaskClick should emit the task', () => {
    const spy = vitest.fn();
    comp.selectTask.subscribe(spy);
    comp.onTaskClick(mockIssues[0]);
    expect(spy).toHaveBeenCalledWith(mockIssues[0]);
  });

  it('getInitials should return first letter uppercase', () => {
    expect(comp.getInitials('john')).toBe('J');
    expect(comp.getInitials('')).toBe('?');
    expect(comp.getInitials(undefined)).toBe('?');
  });
});
