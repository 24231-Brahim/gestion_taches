import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../epic.test-samples';

import { EpicFormService } from './epic-form.service';

describe('Epic Form Service', () => {
  let service: EpicFormService;

  beforeEach(() => {
    service = TestBed.inject(EpicFormService);
  });

  describe('Service methods', () => {
    describe('createEpicFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createEpicFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            description: expect.any(Object),
            status: expect.any(Object),
            priority: expect.any(Object),
            createdAt: expect.any(Object),
            updatedAt: expect.any(Object),
            project: expect.any(Object),
          }),
        );
      });

      it('passing IEpic should create a new form with FormGroup', () => {
        const formGroup = service.createEpicFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            description: expect.any(Object),
            status: expect.any(Object),
            priority: expect.any(Object),
            createdAt: expect.any(Object),
            updatedAt: expect.any(Object),
            project: expect.any(Object),
          }),
        );
      });
    });

    describe('getEpic', () => {
      it('should return NewEpic for default Epic initial value', () => {
        const formGroup = service.createEpicFormGroup(sampleWithNewData);

        const epic = service.getEpic(formGroup);

        expect(epic).toMatchObject(sampleWithNewData);
      });

      it('should return NewEpic for empty Epic initial value', () => {
        const formGroup = service.createEpicFormGroup();

        const epic = service.getEpic(formGroup);

        expect(epic).toMatchObject({});
      });

      it('should return IEpic', () => {
        const formGroup = service.createEpicFormGroup(sampleWithRequiredData);

        const epic = service.getEpic(formGroup);

        expect(epic).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IEpic should not enable id FormControl', () => {
        const formGroup = service.createEpicFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewEpic should disable id FormControl', () => {
        const formGroup = service.createEpicFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
