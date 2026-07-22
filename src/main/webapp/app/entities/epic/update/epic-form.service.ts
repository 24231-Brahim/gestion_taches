import { Injectable } from '@angular/core';
import { AbstractControl, FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IEpic, NewEpic } from '../epic.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IEpic for edit and NewEpicFormGroupInput for create.
 */
type EpicFormGroupInput = IEpic | PartialWithRequiredKeyOf<NewEpic>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IEpic | NewEpic> = Omit<T, 'createdAt' | 'updatedAt' | 'startDate' | 'endDate'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
  startDate?: string | null;
  endDate?: string | null;
};

type EpicFormRawValue = FormValueOf<IEpic>;

type NewEpicFormRawValue = FormValueOf<NewEpic>;

type EpicFormDefaults = Pick<NewEpic, 'id' | 'createdAt' | 'updatedAt'>;

type EpicFormGroupContent = {
  id: FormControl<EpicFormRawValue['id'] | NewEpic['id']>;
  title: FormControl<EpicFormRawValue['title']>;
  description: FormControl<EpicFormRawValue['description']>;
  status: FormControl<EpicFormRawValue['status']>;
  priority: FormControl<EpicFormRawValue['priority']>;
  createdAt: FormControl<EpicFormRawValue['createdAt']>;
  updatedAt: FormControl<EpicFormRawValue['updatedAt']>;
  startDate: FormControl<EpicFormRawValue['startDate']>;
  endDate: FormControl<EpicFormRawValue['endDate']>;
  project: FormControl<EpicFormRawValue['project']>;
};

export type EpicFormGroup = FormGroup<EpicFormGroupContent>;

function dateRangeValidator(group: AbstractControl): Record<string, boolean> | null {
  const start = group.get('startDate')?.value;
  const end = group.get('endDate')?.value;
  if (start && end && start > end) {
    return { dateRange: true };
  }
  return null;
}

@Injectable({ providedIn: 'root' })
export class EpicFormService {
  createEpicFormGroup(epic?: EpicFormGroupInput): EpicFormGroup {
    const epicRawValue = this.convertEpicToEpicRawValue({
      ...this.getFormDefaults(),
      ...(epic ?? { id: null }),
    });
    const form = new FormGroup<EpicFormGroupContent>({
      id: new FormControl(
        { value: epicRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      title: new FormControl(epicRawValue.title, {
        validators: [Validators.required, Validators.minLength(1), Validators.maxLength(200)],
      }),
      description: new FormControl(epicRawValue.description, {
        validators: [Validators.maxLength(1000)],
      }),
      status: new FormControl(epicRawValue.status, {
        validators: [Validators.required],
      }),
      priority: new FormControl(epicRawValue.priority, {
        validators: [Validators.required],
      }),
      createdAt: new FormControl(epicRawValue.createdAt, {
        validators: [Validators.required],
      }),
      updatedAt: new FormControl(epicRawValue.updatedAt),
      startDate: new FormControl(epicRawValue.startDate),
      endDate: new FormControl(epicRawValue.endDate),
      project: new FormControl(epicRawValue.project, {
        validators: [Validators.required],
      }),
    });
    form.addValidators(dateRangeValidator);
    return form;
  }

  getEpic(form: EpicFormGroup): IEpic | NewEpic {
    return this.convertEpicRawValueToEpic(form.getRawValue());
  }

  resetForm(form: EpicFormGroup, epic: EpicFormGroupInput): void {
    const epicRawValue = this.convertEpicToEpicRawValue({ ...this.getFormDefaults(), ...epic });
    form.reset({
      ...epicRawValue,
      id: { value: epicRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): EpicFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
      updatedAt: currentTime,
    };
  }

  private convertEpicRawValueToEpic(rawEpic: EpicFormRawValue | NewEpicFormRawValue): IEpic | NewEpic {
    return {
      ...rawEpic,
      createdAt: dayjs(rawEpic.createdAt, DATE_TIME_FORMAT),
      updatedAt: dayjs(rawEpic.updatedAt, DATE_TIME_FORMAT),
      startDate: rawEpic.startDate ? dayjs(rawEpic.startDate) : undefined,
      endDate: rawEpic.endDate ? dayjs(rawEpic.endDate) : undefined,
    };
  }

  private convertEpicToEpicRawValue(
    epic: IEpic | (Partial<NewEpic> & EpicFormDefaults),
  ): EpicFormRawValue | PartialWithRequiredKeyOf<NewEpicFormRawValue> {
    return {
      ...epic,
      createdAt: epic.createdAt ? epic.createdAt.format(DATE_TIME_FORMAT) : undefined,
      updatedAt: epic.updatedAt ? epic.updatedAt.format(DATE_TIME_FORMAT) : undefined,
      startDate: epic.startDate ? epic.startDate.format('YYYY-MM-DD') : undefined,
      endDate: epic.endDate ? epic.endDate.format('YYYY-MM-DD') : undefined,
    };
  }
}
