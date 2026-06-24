import { HttpResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';
import { Observable, finalize, map } from 'rxjs';

import { IssueStatus } from 'app/entities/enumerations/issue-status.model';
import { IssueType } from 'app/entities/enumerations/issue-type.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { IEpic } from 'app/entities/epic/epic.model';
import { SprintService } from 'app/entities/sprint/service/sprint.service';
import { ISprint } from 'app/entities/sprint/sprint.model';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';

import { IIssue } from '../issue.model';
import { IssueService } from '../service/issue.service';

import { IssueFormGroup, IssueFormService } from './issue-form.service';
import { EpicService } from 'app/entities/epic/service/epic.service';
import { IProject } from 'app/entities/project/project.model';
import { ProjectService } from 'app/entities/project/service/project.service';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-issue-update',
  templateUrl: './issue-update.html',
  imports: [TranslateDirective, TranslateModule, FontAwesomeModule, AlertError, ReactiveFormsModule],
})
export class IssueUpdate implements OnInit {
  readonly isSaving = signal(false);
  issue: IIssue | null = null;
  issueTypeValues = Object.keys(IssueType);
  issueStatusValues = Object.keys(IssueStatus);
  priorityValues = Object.keys(Priority);

  sprintsSharedCollection = signal<ISprint[]>([]);
  epicsSharedCollection = signal<IEpic[]>([]);
  projectsSharedCollection = signal<IProject[]>([]);

  protected issueService = inject(IssueService);
  protected issueFormService = inject(IssueFormService);
  protected sprintService = inject(SprintService);
  protected epicService = inject(EpicService);
  protected projectService = inject(ProjectService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: IssueFormGroup = this.issueFormService.createIssueFormGroup();

  compareSprint = (o1: ISprint | null, o2: ISprint | null): boolean => this.sprintService.compareSprint(o1, o2);

  compareEpic = (o1: IEpic | null, o2: IEpic | null): boolean => this.epicService.compareEpic(o1, o2);

  compareProject = (o1: IProject | null, o2: IProject | null): boolean => this.projectService.compareProject(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ issue }) => {
      this.issue = issue;
      if (issue) {
        this.updateForm(issue);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    globalThis.history.back();
  }

  save(): void {
    this.isSaving.set(true);
    const issue = this.issueFormService.getIssue(this.editForm);
    if (issue.id === null) {
      this.subscribeToSaveResponse(this.issueService.create(issue));
    } else {
      this.subscribeToSaveResponse(this.issueService.update(issue));
    }
  }

  protected subscribeToSaveResponse(result: Observable<IIssue | null>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving.set(false);
  }

  protected updateForm(issue: IIssue): void {
    this.issue = issue;
    this.issueFormService.resetForm(this.editForm, issue);

    this.sprintsSharedCollection.update(sprints => this.sprintService.addSprintToCollectionIfMissing<ISprint>(sprints, issue.sprint));
    this.epicsSharedCollection.update(epics => this.epicService.addEpicToCollectionIfMissing<IEpic>(epics, issue.epic));
    this.projectsSharedCollection.update(projects =>
      this.projectService.addProjectToCollectionIfMissing<IProject>(projects, issue.project),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.sprintService
      .query()
      .pipe(map((res: HttpResponse<ISprint[]>) => res.body ?? []))
      .pipe(map((sprints: ISprint[]) => this.sprintService.addSprintToCollectionIfMissing<ISprint>(sprints, this.issue?.sprint)))
      .subscribe((sprints: ISprint[]) => this.sprintsSharedCollection.set(sprints));

    this.epicService
      .query()
      .pipe(map((res: HttpResponse<IEpic[]>) => res.body ?? []))
      .pipe(map((epics: IEpic[]) => this.epicService.addEpicToCollectionIfMissing<IEpic>(epics, this.issue?.epic)))
      .subscribe((epics: IEpic[]) => this.epicsSharedCollection.set(epics));

    this.projectService
      .query()
      .pipe(map((res: HttpResponse<IProject[]>) => res.body ?? []))
      .pipe(map((projects: IProject[]) => this.projectService.addProjectToCollectionIfMissing<IProject>(projects, this.issue?.project)))
      .subscribe((projects: IProject[]) => this.projectsSharedCollection.set(projects));
  }
}
