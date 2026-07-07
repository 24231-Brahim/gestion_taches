import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IIssue, NewIssue } from '../issue.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IIssue for edit and NewIssueFormGroupInput for create.
 */
type IssueFormGroupInput = IIssue | PartialWithRequiredKeyOf<NewIssue>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IIssue | NewIssue> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

type IssueFormRawValue = FormValueOf<IIssue>;

type NewIssueFormRawValue = FormValueOf<NewIssue>;

type IssueFormDefaults = Pick<NewIssue, 'id' | 'createdAt' | 'updatedAt'>;

type IssueFormGroupContent = {
  id: FormControl<IssueFormRawValue['id'] | NewIssue['id']>;
  title: FormControl<IssueFormRawValue['title']>;
  description: FormControl<IssueFormRawValue['description']>;
  type: FormControl<IssueFormRawValue['type']>;
  status: FormControl<IssueFormRawValue['status']>;
  priority: FormControl<IssueFormRawValue['priority']>;
  createdAt: FormControl<IssueFormRawValue['createdAt']>;
  updatedAt: FormControl<IssueFormRawValue['updatedAt']>;
  sprint: FormControl<IssueFormRawValue['sprint']>;
  epic: FormControl<IssueFormRawValue['epic']>;
  project: FormControl<IssueFormRawValue['project']>;
  assignee: FormControl<IssueFormRawValue['assignee']>;
};

export type IssueFormGroup = FormGroup<IssueFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class IssueFormService {
  createIssueFormGroup(issue?: IssueFormGroupInput): IssueFormGroup {
    const issueRawValue = this.convertIssueToIssueRawValue({
      ...this.getFormDefaults(),
      ...(issue ?? { id: null }),
    });
    return new FormGroup<IssueFormGroupContent>({
      id: new FormControl(
        { value: issueRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      title: new FormControl(issueRawValue.title, {
        validators: [Validators.required, Validators.minLength(1), Validators.maxLength(200)],
      }),
      description: new FormControl(issueRawValue.description, {
        validators: [Validators.maxLength(5000)],
      }),
      type: new FormControl(issueRawValue.type, {
        validators: [Validators.required],
      }),
      status: new FormControl(issueRawValue.status, {
        validators: [Validators.required],
      }),
      priority: new FormControl(issueRawValue.priority, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(issueRawValue.createdAt, {
        validators: [Validators.required],
      }),
      updatedAt: new FormControl(issueRawValue.updatedAt),
      sprint: new FormControl(issueRawValue.sprint),
      epic: new FormControl(issueRawValue.epic),
      project: new FormControl(issueRawValue.project, {
        validators: [Validators.required],
      }),
      assignee: new FormControl(issueRawValue.assignee),
    });
  }

  getIssue(form: IssueFormGroup): IIssue | NewIssue {
    return this.convertIssueRawValueToIssue(form.getRawValue());
  }

  resetForm(form: IssueFormGroup, issue: IssueFormGroupInput): void {
    const issueRawValue = this.convertIssueToIssueRawValue({ ...this.getFormDefaults(), ...issue });
    form.reset({
      ...issueRawValue,
      id: { value: issueRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): IssueFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
      updatedAt: currentTime,
    };
  }

  private convertIssueRawValueToIssue(rawIssue: IssueFormRawValue | NewIssueFormRawValue): IIssue | NewIssue {
    return {
      ...rawIssue,
      createdAt: dayjs(rawIssue.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawIssue.updatedAt, DATE_TIME_FORMAT),
    };
  }

  private convertIssueToIssueRawValue(
    issue: IIssue | (Partial<NewIssue> & IssueFormDefaults),
  ): IssueFormRawValue | PartialWithRequiredKeyOf<NewIssueFormRawValue> {
    return {
      ...issue,
      createdAt: issue.createdAt ? issue.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: issue.updatedAt ? issue.updatedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
