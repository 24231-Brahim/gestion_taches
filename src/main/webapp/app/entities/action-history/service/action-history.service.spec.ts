import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IActionHistory } from '../action-history.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../action-history.test-samples';

import { ActionHistoryService, RestActionHistory } from './action-history.service';

const requireRestSample: RestActionHistory = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
};

describe('ActionHistory Service', () => {
  let service: ActionHistoryService;
  let httpMock: HttpTestingController;
  let expectedResult: IActionHistory | IActionHistory[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(ActionHistoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  describe('Service methods', () => {
    it('should find an element', () => {
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.find(123).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should create a ActionHistory', () => {
      const actionHistory = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(actionHistory).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a ActionHistory', () => {
      const actionHistory = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(actionHistory).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a ActionHistory', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of ActionHistory', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a ActionHistory', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addActionHistoryToCollectionIfMissing', () => {
      it('should add a ActionHistory to an empty array', () => {
        const actionHistory: IActionHistory = sampleWithRequiredData;
        expectedResult = service.addActionHistoryToCollectionIfMissing([], actionHistory);
        expect(expectedResult).toEqual([actionHistory]);
      });

      it('should not add a ActionHistory to an array that contains it', () => {
        const actionHistory: IActionHistory = sampleWithRequiredData;
        const actionHistoryCollection: IActionHistory[] = [
          {
            ...actionHistory,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addActionHistoryToCollectionIfMissing(actionHistoryCollection, actionHistory);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a ActionHistory to an array that doesn't contain it", () => {
        const actionHistory: IActionHistory = sampleWithRequiredData;
        const actionHistoryCollection: IActionHistory[] = [sampleWithPartialData];
        expectedResult = service.addActionHistoryToCollectionIfMissing(actionHistoryCollection, actionHistory);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(actionHistory);
      });

      it('should add only unique ActionHistory to an array', () => {
        const actionHistoryArray: IActionHistory[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const actionHistoryCollection: IActionHistory[] = [sampleWithRequiredData];
        expectedResult = service.addActionHistoryToCollectionIfMissing(actionHistoryCollection, ...actionHistoryArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const actionHistory: IActionHistory = sampleWithRequiredData;
        const actionHistory2: IActionHistory = sampleWithPartialData;
        expectedResult = service.addActionHistoryToCollectionIfMissing([], actionHistory, actionHistory2);
        expect(expectedResult).toEqual([actionHistory, actionHistory2]);
      });

      it('should accept null and undefined values', () => {
        const actionHistory: IActionHistory = sampleWithRequiredData;
        expectedResult = service.addActionHistoryToCollectionIfMissing([], null, actionHistory, undefined);
        expect(expectedResult).toEqual([actionHistory]);
      });

      it('should return initial array if no ActionHistory is added', () => {
        const actionHistoryCollection: IActionHistory[] = [sampleWithRequiredData];
        expectedResult = service.addActionHistoryToCollectionIfMissing(actionHistoryCollection, undefined, null);
        expect(expectedResult).toEqual(actionHistoryCollection);
      });
    });

    describe('compareActionHistory', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareActionHistory(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 2047 };
        const entity2 = null;

        const compareResult1 = service.compareActionHistory(entity1, entity2);
        const compareResult2 = service.compareActionHistory(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 2047 };
        const entity2 = { id: 6945 };

        const compareResult1 = service.compareActionHistory(entity1, entity2);
        const compareResult2 = service.compareActionHistory(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 2047 };
        const entity2 = { id: 2047 };

        const compareResult1 = service.compareActionHistory(entity1, entity2);
        const compareResult2 = service.compareActionHistory(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
