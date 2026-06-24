import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IActionHistory, NewActionHistory } from '../action-history.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IActionHistory for edit and NewActionHistoryFormGroupInput for create.
 */
type ActionHistoryFormGroupInput = IActionHistory | PartialWithRequiredKeyOf<NewActionHistory>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IActionHistory | NewActionHistory> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

type ActionHistoryFormRawValue = FormValueOf<IActionHistory>;

type NewActionHistoryFormRawValue = FormValueOf<NewActionHistory>;

type ActionHistoryFormDefaults = Pick<NewActionHistory, 'id' | 'createdAt'>;

type ActionHistoryFormGroupContent = {
  id: FormControl<ActionHistoryFormRawValue['id'] | NewActionHistory['id']>;
  action: FormControl<ActionHistoryFormRawValue['action']>;
  fieldChanged: FormControl<ActionHistoryFormRawValue['fieldChanged']>;
  oldValue: FormControl<ActionHistoryFormRawValue['oldValue']>;
  newValue: FormControl<ActionHistoryFormRawValue['newValue']>;
  createdAt: FormControl<ActionHistoryFormRawValue['createdAt']>;
  issue: FormControl<ActionHistoryFormRawValue['issue']>;
};

export type ActionHistoryFormGroup = FormGroup<ActionHistoryFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ActionHistoryFormService {
  createActionHistoryFormGroup(actionHistory?: ActionHistoryFormGroupInput): ActionHistoryFormGroup {
    const actionHistoryRawValue = this.convertActionHistoryToActionHistoryRawValue({
      ...this.getFormDefaults(),
      ...(actionHistory ?? { id: null }),
    });
    return new FormGroup<ActionHistoryFormGroupContent>({
      id: new FormControl(
        { value: actionHistoryRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      action: new FormControl(actionHistoryRawValue.action, {
        validators: [Validators.required, Validators.minLength(1), Validators.maxLength(100)],
      }),
      fieldChanged: new FormControl(actionHistoryRawValue.fieldChanged, {
        validators: [Validators.maxLength(100)],
      }),
      oldValue: new FormControl(actionHistoryRawValue.oldValue, {
        validators: [Validators.maxLength(500)],
      }),
      newValue: new FormControl(actionHistoryRawValue.newValue, {
        validators: [Validators.maxLength(500)],
      }),
      createdAt: new FormControl(actionHistoryRawValue.createdAt, {
        validators: [Validators.required],
      }),
      issue: new FormControl(actionHistoryRawValue.issue, {
        validators: [Validators.required],
      }),
    });
  }

  getActionHistory(form: ActionHistoryFormGroup): IActionHistory | NewActionHistory {
    return this.convertActionHistoryRawValueToActionHistory(form.getRawValue());
  }

  resetForm(form: ActionHistoryFormGroup, actionHistory: ActionHistoryFormGroupInput): void {
    const actionHistoryRawValue = this.convertActionHistoryToActionHistoryRawValue({ ...this.getFormDefaults(), ...actionHistory });
    form.reset({
      ...actionHistoryRawValue,
      id: { value: actionHistoryRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): ActionHistoryFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      createdAt: currentTime,
    };
  }

  private convertActionHistoryRawValueToActionHistory(
    rawActionHistory: ActionHistoryFormRawValue | NewActionHistoryFormRawValue,
  ): IActionHistory | NewActionHistory {
    return {
      ...rawActionHistory,
      createdAt: dayjs(rawActionHistory.createdAt, DATE_TIME_FORMAT),
    };
  }

  private convertActionHistoryToActionHistoryRawValue(
    actionHistory: IActionHistory | (Partial<NewActionHistory> & ActionHistoryFormDefaults),
  ): ActionHistoryFormRawValue | PartialWithRequiredKeyOf<NewActionHistoryFormRawValue> {
    return {
      ...actionHistory,
      createdAt: actionHistory.createdAt ? actionHistory.createdAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
