import { beforeEach, describe, expect, it } from 'vitest';
import { TestBed } from '@angular/core/testing';

import { sampleWithNewData, sampleWithRequiredData } from '../task.test-samples';

import { TaskFormService } from './task-form.service';

describe('Task Form Service', () => {
  let service: TaskFormService;

  beforeEach(() => {
    service = TestBed.inject(TaskFormService);
  });

  describe('Service methods', () => {
    describe('createIssueFormGroup', () => {
      it('should create a new form with FormControl', () => {
        const formGroup = service.createIssueFormGroup();

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            description: expect.any(Object),
            type: expect.any(Object),
            status: expect.any(Object),
            priority: expect.any(Object),
            createdAt: expect.any(Object),
            updatedAt: expect.any(Object),
            sprint: expect.any(Object),
            epic: expect.any(Object),
            project: expect.any(Object),
          }),
        );
      });

      it('passing ITask should create a new form with FormGroup', () => {
        const formGroup = service.createIssueFormGroup(sampleWithRequiredData);

        expect(formGroup.controls).toEqual(
          expect.objectContaining({
            id: expect.any(Object),
            title: expect.any(Object),
            description: expect.any(Object),
            type: expect.any(Object),
            status: expect.any(Object),
            priority: expect.any(Object),
            createdAt: expect.any(Object),
            updatedAt: expect.any(Object),
            sprint: expect.any(Object),
            epic: expect.any(Object),
            project: expect.any(Object),
          }),
        );
      });
    });

    describe('getTask', () => {
      it('should return NewTask for default Task initial value', () => {
        const formGroup = service.createIssueFormGroup(sampleWithNewData);

        const task = service.getTask(formGroup);

        expect(task).toMatchObject(sampleWithNewData);
      });

      it('should return NewTask for empty Task initial value', () => {
        const formGroup = service.createIssueFormGroup();

        const task = service.getTask(formGroup);

        expect(task).toMatchObject({});
      });

      it('should return ITask', () => {
        const formGroup = service.createIssueFormGroup(sampleWithRequiredData);

        const task = service.getTask(formGroup);

        expect(task).toMatchObject(sampleWithRequiredData);
      });
    });

    describe('resetForm', () => {
      it('passing ITask should not enable id FormControl', () => {
        const formGroup = service.createIssueFormGroup();
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, sampleWithRequiredData);

        expect(formGroup.controls.id.disabled).toBe(true);
      });

      it('passing NewTask should disable id FormControl', () => {
        const formGroup = service.createIssueFormGroup(sampleWithRequiredData);
        expect(formGroup.controls.id.disabled).toBe(true);

        service.resetForm(formGroup, { id: null });

        expect(formGroup.controls.id.disabled).toBe(true);
      });
    });
  });
});
