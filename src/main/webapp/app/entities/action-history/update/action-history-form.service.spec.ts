import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../action-history.test-samples';

import { ActionHistoryFormService } from './action-history-form.service';

describe('ActionHistory Form Service', () => {
  let service: ActionHistoryFormService;

  beforeEach(() => {
    service = TestBed.inject(ActionHistoryFormService);
  });

  describe('Service methods', () => {
    describe('createActionHistoryFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createActionHistoryFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            action: expect.any(Object),
            fieldChanged: expect.any(Object),
            oldValue: expect.any(Object),
            newValue: expect.any(Object),
            createdAt: expect.any(Object),
            task: expect.any(Object),
          }),
        );
      });

      it('passing IActionHistory should create a new form with FormGroup', () => {
        const formGroup = service.createActionHistoryFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            action: expect.any(Object),
            fieldChanged: expect.any(Object),
            oldValue: expect.any(Object),
            newValue: expect.any(Object),
            createdAt: expect.any(Object),
            task: expect.any(Object),
          }),
        );
      });
    });

    describe('getActionHistory', () => {
      it('should return NewActionHistory for default ActionHistory initial value', () => {
        const formGroup = service.createActionHistoryFormGroup(sampleWithNewData);

        const actionHistory = service.getActionHistory(formGroup);

        expect(actionHistory).toMatchObject(sampleWithNewData);
      });

      it('should return NewActionHistory for empty ActionHistory initial value', () => {
        const formGroup = service.createActionHistoryFormGroup();

        const actionHistory = service.getActionHistory(formGroup);

        expect(actionHistory).toMatchObject({});
      });

      it('should return IActionHistory', () => {
        const formGroup = service.createActionHistoryFormGroup(sampleWithRequiredData);

        const actionHistory = service.getActionHistory(formGroup);

        expect(actionHistory).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing IActionHistory should not enable id FormControl', () => {
        const formGroup = service.createActionHistoryFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewActionHistory should disable id FormControl', () => {
        const formGroup = service.createActionHistoryFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
