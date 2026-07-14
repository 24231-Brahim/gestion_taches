import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ITask, NewTask } from '../task.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ITask for edit and NewTaskFormGroupInput for create.
 */
type TaskFormGroupInput = ITask | PartialWithRequiredKeyOf<NewTask>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ITask | NewTask> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

type TaskFormRawValue = FormValueOf<ITask>;

type NewTaskFormRawValue = FormValueOf<NewTask>;

type TaskFormDefaults = Pick<NewTask, 'id' | 'createdAt' | 'updatedAt'>;

type TaskFormGroupContent = {
  id: FormControl<TaskFormRawValue['id'] | NewTask['id']>;
  title: FormControl<TaskFormRawValue['title']>;
  description: FormControl<TaskFormRawValue['description']>;
  type: FormControl<TaskFormRawValue['type']>;
  status: FormControl<TaskFormRawValue['status']>;
  priority: FormControl<TaskFormRawValue['priority']>;
  createdAt: FormControl<TaskFormRawValue['createdAt']>;
  updatedAt: FormControl<TaskFormRawValue['updatedAt']>;
  sprint: FormControl<TaskFormRawValue['sprint']>;
  epic: FormControl<TaskFormRawValue['epic']>;
  project: FormControl<TaskFormRawValue['project']>;
  assignee: FormControl<TaskFormRawValue['assignee']>;
};

export type TaskFormGroup = FormGroup<TaskFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class TaskFormService {
  createTaskFormGroup(task?: TaskFormGroupInput): TaskFormGroup {
    const taskRawValue = this.convertTaskToTaskRawValue({
      ...this.getFormDefaults(),
      ...(task ?? { id: null }),
    });
    return new FormGroup<TaskFormGroupContent>({
      id: new FormControl(
        { value: taskRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      title: new FormControl(taskRawValue.title, {
        validators: [Validators.required, Validators.minLength(1), Validators.maxLength(200)],
      }),
      description: new FormControl(taskRawValue.description, {
        validators: [Validators.maxLength(5000)],
      }),
      type: new FormControl(taskRawValue.type, {
        validators: [Validators.required],
      }),
      status: new FormControl(taskRawValue.status, {
        validators: [Validators.required],
      }),
      priority: new FormControl(taskRawValue.priority, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(taskRawValue.createdAt, {
        validators: [Validators.required],
      }),
      updatedAt: new FormControl(taskRawValue.updatedAt),
      sprint: new FormControl(taskRawValue.sprint),
      epic: new FormControl(taskRawValue.epic),
      project: new FormControl(taskRawValue.project, {
        validators: [Validators.required],
      }),
      assignee: new FormControl(taskRawValue.assignee),
    });
  }

  getTask(form: TaskFormGroup): ITask | NewTask {
    return this.convertTaskRawValueToTask(form.getRawValue());
  }

  resetForm(form: TaskFormGroup, task: TaskFormGroupInput): void {
    const taskRawValue = this.convertTaskToTaskRawValue({ ...this.getFormDefaults(), ...task });
    form.reset({
      ...taskRawValue,
      id: { value: taskRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): TaskFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
      updatedAt: currentTime,
    };
  }

  private convertTaskRawValueToTask(rawIssue: TaskFormRawValue | NewTaskFormRawValue): ITask | NewTask {
    return {
      ...rawIssue,
      createdAt: dayjs(rawIssue.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawIssue.updatedAt, DATE_TIME_FORMAT),
    };
  }

  private convertTaskToTaskRawValue(
    task: ITask | (Partial<NewTask> & TaskFormDefaults),
  ): TaskFormRawValue | PartialWithRequiredKeyOf<NewTaskFormRawValue> {
    return {
      ...task,
      createdAt: task.createdAt ? task.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: task.updatedAt ? task.updatedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
