import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { IIssue } from '../issue.model';
import { sampleWithFullData, sampleWithNewData, sampleWithPartialData, sampleWithRequiredData } from '../issue.test-samples';

import { IssueService, RestIssue } from './issue.service';

const requireRestSample: RestIssue = {
  ...sampleWithRequiredData,
  createdAt: sampleWithRequiredData.createdAt?.toJSON(),
  updatedAt: sampleWithRequiredData.updatedAt?.toJSON(),
};

describe('Issue Service', () => {
  let service: IssueService;
  let httpMock: HttpTestingController;
  let expectedResult: IIssue | IIssue[] | boolean | null;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClientTesting()],
    });
    expectedResult = null;
    service = TestBed.inject(IssueService);
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

    it('should create a Issue', () => {
      const issue = { ...sampleWithNewData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.create(issue).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'POST' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should update a Issue', () => {
      const issue = { ...sampleWithRequiredData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.update(issue).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PUT' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should partial update a Issue', () => {
      const patchObject = { ...sampleWithPartialData };
      const returnedFromService = { ...requireRestSample };
      const expected = { ...sampleWithRequiredData };

      service.partialUpdate(patchObject).subscribe(resp => (expectedResult = resp));

      const req = httpMock.expectOne({ method: 'PATCH' });
      req.flush(returnedFromService);
      expect(expectedResult).toMatchObject(expected);
    });

    it('should return a list of Issue', () => {
      const returnedFromService = { ...requireRestSample };

      const expected = { ...sampleWithRequiredData };

      service.query().subscribe(resp => (expectedResult = resp.body));

      const req = httpMock.expectOne({ method: 'GET' });
      req.flush([returnedFromService]);
      httpMock.verify();
      expect(expectedResult).toMatchObject([expected]);
    });

    it('should delete a Issue', () => {
      service.delete(123).subscribe();

      const requests = httpMock.match({ method: 'DELETE' });
      expect(requests.length).toBe(1);
    });

    describe('addIssueToCollectionIfMissing', () => {
      it('should add a Issue to an empty array', () => {
        const issue: IIssue = sampleWithRequiredData;
        expectedResult = service.addIssueToCollectionIfMissing([], issue);
        expect(expectedResult).toEqual([issue]);
      });

      it('should not add a Issue to an array that contains it', () => {
        const issue: IIssue = sampleWithRequiredData;
        const issueCollection: IIssue[] = [
          {
            ...issue,
          },
          sampleWithPartialData,
        ];
        expectedResult = service.addIssueToCollectionIfMissing(issueCollection, issue);
        expect(expectedResult).toHaveLength(2);
      });

      it("should add a Issue to an array that doesn't contain it", () => {
        const issue: IIssue = sampleWithRequiredData;
        const issueCollection: IIssue[] = [sampleWithPartialData];
        expectedResult = service.addIssueToCollectionIfMissing(issueCollection, issue);
        expect(expectedResult).toHaveLength(2);
        expect(expectedResult).toContain(issue);
      });

      it('should add only unique Issue to an array', () => {
        const issueArray: IIssue[] = [sampleWithRequiredData, sampleWithPartialData, sampleWithFullData];
        const issueCollection: IIssue[] = [sampleWithRequiredData];
        expectedResult = service.addIssueToCollectionIfMissing(issueCollection, ...issueArray);
        expect(expectedResult).toHaveLength(3);
      });

      it('should accept varargs', () => {
        const issue: IIssue = sampleWithRequiredData;
        const issue2: IIssue = sampleWithPartialData;
        expectedResult = service.addIssueToCollectionIfMissing([], issue, issue2);
        expect(expectedResult).toEqual([issue, issue2]);
      });

      it('should accept null and undefined values', () => {
        const issue: IIssue = sampleWithRequiredData;
        expectedResult = service.addIssueToCollectionIfMissing([], null, issue, undefined);
        expect(expectedResult).toEqual([issue]);
      });

      it('should return initial array if no Issue is added', () => {
        const issueCollection: IIssue[] = [sampleWithRequiredData];
        expectedResult = service.addIssueToCollectionIfMissing(issueCollection, undefined, null);
        expect(expectedResult).toEqual(issueCollection);
      });
    });

    describe('compareIssue', () => {
      it('should return true if both entities are null', () => {
        const entity1 = null;
        const entity2 = null;

        const compareResult = service.compareIssue(entity1, entity2);

        expect(compareResult).toEqual(true);
      });

      it('should return false if one entity is null', () => {
        const entity1 = { id: 6256 };
        const entity2 = null;

        const compareResult1 = service.compareIssue(entity1, entity2);
        const compareResult2 = service.compareIssue(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey differs', () => {
        const entity1 = { id: 6256 };
        const entity2 = { id: 29374 };

        const compareResult1 = service.compareIssue(entity1, entity2);
        const compareResult2 = service.compareIssue(entity2, entity1);

        expect(compareResult1).toEqual(false);
        expect(compareResult2).toEqual(false);
      });

      it('should return false if primaryKey matches', () => {
        const entity1 = { id: 6256 };
        const entity2 = { id: 6256 };

        const compareResult1 = service.compareIssue(entity1, entity2);
        const compareResult2 = service.compareIssue(entity2, entity1);

        expect(compareResult1).toEqual(true);
        expect(compareResult2).toEqual(true);
      });
    });
  });

  afterEach(() => {
    httpMock.verify();
  });
});
