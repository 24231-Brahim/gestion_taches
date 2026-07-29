import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import { ISprint, NewSprint } from '../sprint.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ISprint for edit and NewSprintFormGroupInput for create.
 */
type SprintFormGroupInput = ISprint | PartialWithRequiredKeyOf<NewSprint>;

type SprintFormDefaults = Pick<NewSprint, 'id' | 'status'>;

type SprintFormGroupContent = {
  id: FormControl<ISprint['id'] | NewSprint['id']>;
  name: FormControl<ISprint['name']>;
  goal: FormControl<ISprint['goal']>;
  startDate: FormControl<ISprint['startDate']>;
  endDate: FormControl<ISprint['endDate']>;
  status: FormControl<ISprint['status']>;
  project: FormControl<ISprint['project']>;
};

export type SprintFormGroup = FormGroup<SprintFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class SprintFormService {
  createSprintFormGroup(sprint?: SprintFormGroupInput): SprintFormGroup {
    const sprintRawValue = {
      ...this.getFormDefaults(),
      ...(sprint ?? { id: null }),
    };
    return new FormGroup<SprintFormGroupContent>({
      id: new FormControl(
        { value: sprintRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(sprintRawValue.name, {
        validators: [Validators.required, Validators.minLength(1), Validators.maxLength(100)],
      }),
      goal: new FormControl(sprintRawValue.goal, {
        validators: [Validators.maxLength(500)],
      }),
      startDate: new FormControl(sprintRawValue.startDate, { validators: [Validators.required] }),
      endDate: new FormControl(sprintRawValue.endDate, { validators: [Validators.required] }),
      status: new FormControl(sprintRawValue.status, {
        validators: [Validators.required],
      }),
      project: new FormControl(sprintRawValue.project, {
        validators: [Validators.required],
      }),
    });
  }

  getSprint(form: SprintFormGroup): ISprint | NewSprint {
    return form.getRawValue();
  }

  resetForm(form: SprintFormGroup, sprint: SprintFormGroupInput): void {
    const sprintRawValue = { ...this.getFormDefaults(), ...sprint };
    form.reset({
      ...sprintRawValue,
      id: { value: sprintRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): SprintFormDefaults {
    return {
      id: null,
      status: 'PLANNED',
    };
  }
}
