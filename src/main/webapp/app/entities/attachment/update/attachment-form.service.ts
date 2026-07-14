import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';

import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IAttachment, NewAttachment } from '../attachment.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAttachment for edit and NewAttachmentFormGroupInput for create.
 */
type AttachmentFormGroupInput = IAttachment | PartialWithRequiredKeyOf<NewAttachment>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IAttachment | NewAttachment> = Omit<T, 'uploadedAt'> & {
  uploadedAt?: string | null;
};

type AttachmentFormRawValue = FormValueOf<IAttachment>;

type NewAttachmentFormRawValue = FormValueOf<NewAttachment>;

type AttachmentFormDefaults = Pick<NewAttachment, 'id' | 'uploadedAt'>;

type AttachmentFormGroupContent = {
  id: FormControl<AttachmentFormRawValue['id'] | NewAttachment['id']>;
  fileName: FormControl<AttachmentFormRawValue['fileName']>;
  filePath: FormControl<AttachmentFormRawValue['filePath']>;
  uploadedAt: FormControl<AttachmentFormRawValue['uploadedAt']>;
  task: FormControl<AttachmentFormRawValue['task']>;
};

export type AttachmentFormGroup = FormGroup<AttachmentFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AttachmentFormService {
  createAttachmentFormGroup(attachment?: AttachmentFormGroupInput): AttachmentFormGroup {
    const attachmentRawValue = this.convertAttachmentToAttachmentRawValue({
      ...this.getFormDefaults(),
      ...(attachment ?? { id: null }),
    });
    return new FormGroup<AttachmentFormGroupContent>({
      id: new FormControl(
        { value: attachmentRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      fileName: new FormControl(attachmentRawValue.fileName, {
        validators: [Validators.required, Validators.minLength(1), Validators.maxLength(255)],
      }),
      filePath: new FormControl(attachmentRawValue.filePath, {
        validators: [Validators.required, Validators.maxLength(1000)],
      }),
      uploadedAt: new FormControl(attachmentRawValue.uploadedAt, {
        validators: [Validators.required],
      }),
      task: new FormControl(attachmentRawValue.task, {
        validators: [Validators.required],
      }),
    });
  }

  getAttachment(form: AttachmentFormGroup): IAttachment | NewAttachment {
    return this.convertAttachmentRawValueToAttachment(form.getRawValue());
  }

  resetForm(form: AttachmentFormGroup, attachment: AttachmentFormGroupInput): void {
    const attachmentRawValue = this.convertAttachmentToAttachmentRawValue({ ...this.getFormDefaults(), ...attachment });
    form.reset({
      ...attachmentRawValue,
      id: { value: attachmentRawValue.id, disabled: true },
    });
  }

  private getFormDefaults(): AttachmentFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      uploadedAt: currentTime,
    };
  }

  private convertAttachmentRawValueToAttachment(
    rawAttachment: AttachmentFormRawValue | NewAttachmentFormRawValue,
  ): IAttachment | NewAttachment {
    return {
      ...rawAttachment,
      uploadedAt: dayjs(rawAttachment.uploadedAt, DATE_TIME_FORMAT),
    };
  }

  private convertAttachmentToAttachmentRawValue(
    attachment: IAttachment | (Partial<NewAttachment> & AttachmentFormDefaults),
  ): AttachmentFormRawValue | PartialWithRequiredKeyOf<NewAttachmentFormRawValue> {
    return {
      ...attachment,
      uploadedAt: attachment.uploadedAt ? attachment.uploadedAt.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
