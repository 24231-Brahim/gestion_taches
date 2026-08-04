import { afterEach, beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';

import { registerAllIcons } from 'src/test/javascript/mocks/fa-icon-library';

import { TaskService } from '../service/task.service';
import { AlertService } from 'app/core/util/alert.service';
import { TaskDetailPanel } from './task-detail-panel';
import { sampleWithRequiredData } from '../task.test-samples';
import { ITask } from '../task.model';

describe('TaskDetailPanel', () => {
  let fixture: ComponentFixture<TaskDetailPanel>;
  let comp: TaskDetailPanel;
  let taskServiceMock: { partialUpdate: ReturnType<typeof vitest.fn>; getAssignableUsers: ReturnType<typeof vitest.fn> };
  let alertServiceMock: { addAlert: ReturnType<typeof vitest.fn> };

  let mockTask: ITask;

  beforeEach(() => {
    mockTask = { ...sampleWithRequiredData, status: 'NEEDS_INFO' };
    taskServiceMock = { partialUpdate: vitest.fn().mockReturnValue(of({})), getAssignableUsers: vitest.fn().mockReturnValue(of([])) };
    alertServiceMock = { addAlert: vitest.fn() };

    TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([]), FontAwesomeModule, TranslateModule.forRoot(), TaskDetailPanel],
      providers: [
        { provide: TaskService, useValue: taskServiceMock },
        { provide: AlertService, useValue: alertServiceMock },
      ],
    });

    const library = TestBed.inject(FaIconLibrary);
    registerAllIcons(library);

    fixture = TestBed.createComponent(TaskDetailPanel);
    comp = fixture.componentInstance;
    fixture.componentRef.setInput('task', mockTask);
    fixture.componentRef.setInput('visible', true);
    fixture.componentRef.setInput('close', () => {});
    fixture.detectChanges();
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should create', () => {
    expect(comp).toBeTruthy();
  });

  it('onStatusChange should not update when status is unchanged', () => {
    comp.onStatusChange(mockTask, 'NEEDS_INFO');
    expect(taskServiceMock.partialUpdate).not.toHaveBeenCalled();
  });

  it('onStatusChange should call partialUpdate when status differs', () => {
    comp.onStatusChange(mockTask, 'DONE');
    expect(taskServiceMock.partialUpdate).toHaveBeenCalledWith({ id: mockTask.id, status: 'DONE' });
  });

  it('onStatusChange should update task on success', () => {
    const updated = { ...mockTask, status: 'DONE' };
    taskServiceMock.partialUpdate.mockReturnValue(of(updated));
    const emitSpy = vitest.spyOn(comp.taskChanged, 'emit');
    comp.onStatusChange(mockTask, 'DONE');
    expect(emitSpy).toHaveBeenCalledWith({ ...mockTask, ...updated });
    expect(comp.isSaving()).toBe(false);
  });

  it('onStatusChange should show alert on error', () => {
    taskServiceMock.partialUpdate.mockReturnValue(throwError(() => ({ error: { detail: 'fail' }, message: 'err' })));
    comp.onStatusChange(mockTask, 'DONE');
    expect(alertServiceMock.addAlert).toHaveBeenCalledWith({ type: 'danger', message: 'fail' });
    expect(comp.isSaving()).toBe(false);
  });

  it('onStatusChange should set isSaving during request', () => {
    taskServiceMock.partialUpdate.mockReturnValue(of(mockTask));
    comp.onStatusChange(mockTask, 'DONE');
    expect(comp.isSaving()).toBe(false);
  });

  it('getInitials should return first letter uppercase', () => {
    expect(comp.getInitials('alice')).toBe('A');
    expect(comp.getInitials('')).toBe('?');
    expect(comp.getInitials(undefined)).toBe('?');
  });
});
