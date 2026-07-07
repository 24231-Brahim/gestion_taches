import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IEpic } from '../epic.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../epic.test-samples';

import { EpicService, RestEpic } from './epic.service';

const requireRestSample: RestEpic = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
  updatedAt: sampleWithRequiredData.updatedAt?.toJSON(),
};

describe('Epic Service', () => {
  let service: EpicService;
  let httpMock: HttpTestingController;
  let expectedResult: IEpic | IEpic[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(EpicService);
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

    it('should create a Epic', () => {
      const epic = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(epic).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Epic', () => {
      const epic = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(epic).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Epic', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Epic', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Epic', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addEpicToCollectionIfMissing', () => {
      it('should add a Epic to an empty array', () => {
        const epic: IEpic = sampleWithRequiredData;
        expectedResult = service.addEpicToCollectionIfMissing([], epic);
        expect(expectedResult).toEqual([epic]);
      });

      it('should not add a Epic to an array that contains it', () => {
        const epic: IEpic = sampleWithRequiredData;
        const epicCollection: IEpic[] = [
          {
            ...epic,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addEpicToCollectionIfMissing(epicCollection, epic);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Epic to an array that doesn't contain it", () => {
        const epic: IEpic = sampleWithRequiredData;
        const epicCollection: IEpic[] = [sampleWithPartialData];
        expectedResult = service.addEpicToCollectionIfMissing(epicCollection, epic);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(epic);
      });

      it('should add only unique Epic to an array', () => {
        const epicArray: IEpic[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const epicCollection: IEpic[] = [sampleWithRequiredData];
        expectedResult = service.addEpicToCollectionIfMissing(epicCollection, ...epicArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const epic: IEpic = sampleWithRequiredData;
        const epic2: IEpic = sampleWithPartialData;
        expectedResult = service.addEpicToCollectionIfMissing([], epic, epic2);
        expect(expectedResult).toEqual([epic, epic2]);
      });

      it('should accept null and undefined values', () => {
        const epic: IEpic = sampleWithRequiredData;
        expectedResult = service.addEpicToCollectionIfMissing([], null, epic, undefined);
        expect(expectedResult).toEqual([epic]);
      });

      it('should return initial array if no Epic is added', () => {
        const epicCollection: IEpic[] = [sampleWithRequiredData];
        expectedResult = service.addEpicToCollectionIfMissing(epicCollection, undefined, null);
        expect(expectedResult).toEqual(epicCollection);
      });
    });

    describe('compareEpic', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareEpic(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 5106 };
        const entity2 = null;

        const compareResult1 = service.compareEpic(entity1, entity2);
        const compareResult2 = service.compareEpic(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 5106 };
        const entity2 = { id: 11594 };

        const compareResult1 = service.compareEpic(entity1, entity2);
        const compareResult2 = service.compareEpic(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 5106 };
        const entity2 = { id: 5106 };

        const compareResult1 = service.compareEpic(entity1, entity2);
        const compareResult2 = service.compareEpic(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
