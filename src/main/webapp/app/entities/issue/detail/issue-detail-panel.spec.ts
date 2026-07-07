import { afterEach, beforeEach, describe, expect, it, vitest } from 'vitest';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RouterModule } from '@angular/router';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { faTimes, faPencilAlt } from '@fortawesome/free-solid-svg-icons';
import { TranslateModule } from '@ngx-translate/core';
import { of, throwError } from 'rxjs';

import { IssueService } from '../service/issue.service';
import { AlertService } from 'app/core/util/alert.service';
import { IssueDetailPanel } from './issue-detail-panel';
import { sampleWithRequiredData } from '../issue.test-samples';
import { IIssue } from '../issue.model';

describe('IssueDetailPanel', () => {
  let fixture: ComponentFixture<IssueDetailPanel>;
  let comp: IssueDetailPanel;
  let issueServiceMock: { partialUpdate: ReturnType<typeof vitest.fn> };
  let alertServiceMock: { addAlert: ReturnType<typeof vitest.fn> };

  let mockIssue: IIssue;

  beforeEach(() => {
    mockIssue = { ...sampleWithRequiredData, status: 'TODO' };
    issueServiceMock = { partialUpdate: vitest.fn().mockReturnValue(of({})) };
    alertServiceMock = { addAlert: vitest.fn() };

    TestBed.configureTestingModule({
      imports: [RouterModule.forRoot([]), FontAwesomeModule, TranslateModule.forRoot(), IssueDetailPanel],
      providers: [
        { provide: IssueService, useValue: issueServiceMock },
        { provide: AlertService, useValue: alertServiceMock },
      ],
    });

    const library = TestBed.inject(FaIconLibrary);
    library.addIcons(faTimes, faPencilAlt);

    fixture = TestBed.createComponent(IssueDetailPanel);
    comp = fixture.componentInstance;
    fixture.componentRef.setInput('issue', mockIssue);
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
    comp.onStatusChange(mockIssue, 'TODO');
    expect(issueServiceMock.partialUpdate).not.toHaveBeenCalled();
  });

  it('onStatusChange should call partialUpdate when status differs', () => {
    comp.onStatusChange(mockIssue, 'DONE');
    expect(issueServiceMock.partialUpdate).toHaveBeenCalledWith({ id: mockIssue.id, status: 'DONE' });
  });

  it('onStatusChange should update issue on success', () => {
    const updated = { ...mockIssue, status: 'DONE' };
    issueServiceMock.partialUpdate.mockReturnValue(of(updated));
    comp.onStatusChange(mockIssue, 'DONE');
    expect(mockIssue.status).toBe('DONE');
    expect(comp.isSaving()).toBe(false);
  });

  it('onStatusChange should show alert on error', () => {
    issueServiceMock.partialUpdate.mockReturnValue(throwError(() => ({ error: { detail: 'fail' }, message: 'err' })));
    comp.onStatusChange(mockIssue, 'DONE');
    expect(alertServiceMock.addAlert).toHaveBeenCalledWith({ type: 'danger', message: 'fail' });
    expect(comp.isSaving()).toBe(false);
  });

  it('onStatusChange should set isSaving during request', () => {
    issueServiceMock.partialUpdate.mockReturnValue(of(mockIssue));
    comp.onStatusChange(mockIssue, 'DONE');
    expect(comp.isSaving()).toBe(false);
  });

  it('getInitials should return first letter uppercase', () => {
    expect(comp.getInitials('alice')).toBe('A');
    expect(comp.getInitials('')).toBe('?');
    expect(comp.getInitials(undefined)).toBe('?');
  });
});
