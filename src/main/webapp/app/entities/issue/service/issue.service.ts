import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IIssue, NewIssue } from '../issue.model';

export type PartialUpdateIssue = Partial<IIssue> & Pick<IIssue, 'id'>;

type RestOf<T extends IIssue | NewIssue> = Omit<T, 'createdAt' | 'updatedAt'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
};

export type RestIssue = RestOf<IIssue>;

export type NewRestIssue = RestOf<NewIssue>;

export type PartialUpdateRestIssue = RestOf<PartialUpdateIssue>;

@Injectable()
export class IssuesService {
  readonly issuesParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(undefined);
  readonly issuesResource = httpResource<RestIssue[]>(() => {
    const params = this.issuesParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of issue that have been fetched. It is updated when the issuesResource emits a new value.
   * In case of error while fetching the issues, the signal is set to an empty array.
   */
  readonly issues = computed(() =>
    (this.issuesResource.hasValue() ? this.issuesResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/issues');

  protected convertValueFromServer(restIssue: RestIssue): IIssue {
    return {
      ...restIssue,
      createdAt: restIssue.createdAt ? dayjs(restIssue.createdAt) : undefined,
      updatedAt: restIssue.updatedAt ? dayjs(restIssue.updatedAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class IssueService extends IssuesService {
  protected readonly http = inject(HttpClient);

  create(issue: NewIssue): Observable<IIssue> {
    const copy = this.convertValueFromClient(issue);
    return this.http.post<RestIssue>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(issue: IIssue): Observable<IIssue> {
    const copy = this.convertValueFromClient(issue);
    return this.http
      .put<RestIssue>(`${this.resourceUrl}/${encodeURIComponent(this.getIssueIdentifier(issue))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(issue: PartialUpdateIssue): Observable<IIssue> {
    const copy = this.convertValueFromClient(issue);
    return this.http
      .patch<RestIssue>(`${this.resourceUrl}/${encodeURIComponent(this.getIssueIdentifier(issue))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IIssue> {
    return this.http.get<RestIssue>(`${this.resourceUrl}/${encodeURIComponent(id)}`).pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IIssue[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestIssue[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getIssueIdentifier(issue: Pick<IIssue, 'id'>): number {
    return issue.id;
  }

  compareIssue(o1: Pick<IIssue, 'id'> | null, o2: Pick<IIssue, 'id'> | null): boolean {
    return o1 && o2 ? this.getIssueIdentifier(o1) === this.getIssueIdentifier(o2) : o1 === o2;
  }

  addIssueToCollectionIfMissing<Type extends Pick<IIssue, 'id'>>(
    issueCollection: Type[],
    ...issuesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const issues: Type[] = issuesToCheck.filter(isPresent);
    if (issues.length > 0) {
      const issueCollectionIdentifiers = issueCollection.map(issueItem => this.getIssueIdentifier(issueItem));
      const issuesToAdd = issues.filter(issueItem => {
        const issueIdentifier = this.getIssueIdentifier(issueItem);
        if (issueCollectionIdentifiers.includes(issueIdentifier)) {
          return false;
        }
        issueCollectionIdentifiers.push(issueIdentifier);
        return true;
      });
      return [...issuesToAdd, ...issueCollection];
    }
    return issueCollection;
  }

  protected convertValueFromClient<T extends IIssue | NewIssue | PartialUpdateIssue>(issue: T): RestOf<T> {
    return {
      ...issue,
      createdAt: issue.createdAt?.toJSON() ?? null,
      updatedAt: issue.updatedAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestIssue): IIssue {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestIssue[]): IIssue[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
