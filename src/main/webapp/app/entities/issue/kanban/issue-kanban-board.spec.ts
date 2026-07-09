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

import { IssueStatus } from 'app/entities/enumerations/issue-status.model';
import { IssueService } from '../service/issue.service';
import { AlertService } from 'app/core/util/alert.service';
import { IssueKanbanBoard } from './issue-kanban-board';
import { sampleWithRequiredData, sampleWithPartialData, sampleWithFullData } from '../issue.test-samples';
import { IIssue } from '../issue.model';

describe('IssueKanbanBoard', () => {
  let fixture: ComponentFixture<IssueKanbanBoard>;
  let comp: IssueKanbanBoard;
  let issueServiceMock: { partialUpdate: ReturnType<typeof vitest.fn> };
  let alertServiceMock: { addAlert: ReturnType<typeof vitest.fn> };
  let translateServiceMock: { instant: ReturnType<typeof vitest.fn> };

  const mockIssues: IIssue[] = [
    { ...sampleWithRequiredData, status: 'TODO' },
    { ...sampleWithPartialData, status: 'IN_PROGRESS' },
    { ...sampleWithFullData, status: 'DONE' },
  ];

  beforeEach(() => {
    issueServiceMock = { partialUpdate: vitest.fn().mockReturnValue(of({})) };
    alertServiceMock = { addAlert: vitest.fn() };
    translateServiceMock = { instant: vitest.fn().mockReturnValue('error') };

    TestBed.configureTestingModule({
      imports: [FontAwesomeModule, TranslateModule.forRoot(), IssueKanbanBoard],
      providers: [
        { provide: IssueService, useValue: issueServiceMock },
        { provide: AlertService, useValue: alertServiceMock },
      ],
    });

    const library = TestBed.inject(FaIconLibrary);
    library.addIcons(faThList, faBug, faCheckCircle, faPlus, faArrowUp, faArrowDown, faFlag, faExclamationTriangle);

    fixture = TestBed.createComponent(IssueKanbanBoard);
    comp = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    TestBed.resetTestingModule();
  });

  it('should create', () => {
    expect(comp).toBeTruthy();
  });

  it('should compute columns filtering issues by status', () => {
    fixture.componentRef.setInput('issues', mockIssues);
    fixture.detectChanges();
    const cols = comp.getColumns();
    expect(cols.length).toBe(Object.keys(IssueStatus).length);
    expect(cols.find(c => c.status === 'TODO')!.issues.length).toBe(1);
    expect(cols.find(c => c.status === 'IN_PROGRESS')!.issues.length).toBe(1);
    expect(cols.find(c => c.status === 'DONE')!.issues.length).toBe(1);
    expect(cols.find(c => c.status === 'BACKLOG')!.issues.length).toBe(0);
    expect(cols.find(c => c.status === 'CANCELLED')!.issues.length).toBe(0);
  });

  it('onDragStart should set dragIssueId', () => {
    comp.onDragStart(mockIssues[0]);
    expect(comp.dragIssueId).toBe(mockIssues[0].id);
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

  it('onDrop should call partialUpdate and emit updated issue when status differs', () => {
    fixture.componentRef.setInput('issues', mockIssues);
    fixture.detectChanges();
    const updatedIssue = { ...mockIssues[0], status: 'DONE' as const };
    const spy = vitest.fn();
    issueServiceMock.partialUpdate.mockReturnValue(of(updatedIssue));
    comp.issueMoved.subscribe(spy);
    comp.dragIssueId = mockIssues[0].id;
    const event = new DragEvent('drop');
    vitest.spyOn(event, 'preventDefault');
    comp.onDrop(event, 'DONE');
    expect(event.preventDefault).toHaveBeenCalled();
    expect(issueServiceMock.partialUpdate).toHaveBeenCalledWith({ id: mockIssues[0].id, status: 'DONE' });
    expect(spy).toHaveBeenCalledWith(updatedIssue);
  });

  it('onDrop should not call partialUpdate when status is same', () => {
    fixture.componentRef.setInput('issues', mockIssues);
    fixture.detectChanges();
    comp.dragIssueId = mockIssues[0].id;
    comp.onDrop(new DragEvent('drop'), 'TODO');
    expect(issueServiceMock.partialUpdate).not.toHaveBeenCalled();
  });

  it('onDrop should no-op when dragIssueId is null', () => {
    comp.dragIssueId = null;
    comp.onDrop(new DragEvent('drop'), 'DONE');
    expect(issueServiceMock.partialUpdate).not.toHaveBeenCalled();
  });

  it('onDrop should show alert on error', () => {
    fixture.componentRef.setInput('issues', mockIssues);
    fixture.detectChanges();
    issueServiceMock.partialUpdate.mockReturnValue(throwError(() => ({ error: { detail: 'fail' }, message: 'err' })));
    comp.dragIssueId = mockIssues[0].id;
    comp.onDrop(new DragEvent('drop'), 'DONE');
    expect(alertServiceMock.addAlert).toHaveBeenCalledWith({ type: 'danger', message: 'fail' });
  });

  it('onDragEnd should reset drag state', () => {
    comp.dragIssueId = 42;
    comp.dragOverStatus = 'TODO';
    comp.onDragEnd();
    expect(comp.dragIssueId).toBeNull();
    expect(comp.dragOverStatus).toBeNull();
  });

  it('onIssueClick should emit the issue', () => {
    const spy = vitest.fn();
    comp.selectIssue.subscribe(spy);
    comp.onIssueClick(mockIssues[0]);
    expect(spy).toHaveBeenCalledWith(mockIssues[0]);
  });

  it('getInitials should return first letter uppercase', () => {
    expect(comp.getInitials('john')).toBe('J');
    expect(comp.getInitials('')).toBe('?');
    expect(comp.getInitials(undefined)).toBe('?');
  });
});
