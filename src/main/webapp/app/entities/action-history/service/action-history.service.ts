import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IActionHistory, NewActionHistory } from '../action-history.model';

export type PartialUpdateActionHistory = Partial<IActionHistory> & Pick<IActionHistory, 'id'>;

type RestOf<T extends IActionHistory | NewActionHistory> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestActionHistory = RestOf<IActionHistory>;

export type NewRestActionHistory = RestOf<NewActionHistory>;

export type PartialUpdateRestActionHistory = RestOf<PartialUpdateActionHistory>;

@Injectable()
export class ActionHistoriesService {
  readonly actionHistoriesParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly actionHistoriesResource = httpResource<RestActionHistory[]>(() => {
    const params = this.actionHistoriesParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of actionHistory that have been fetched. It is updated when the actionHistoriesResource emits a new value.
   * In case of error while fetching the actionHistories, the signal is set to an empty array.
   */
  readonly actionHistories = computed(() =>
    (this.actionHistoriesResource.hasValue() ? this.actionHistoriesResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/action-histories');

  refresh(): void {
    this.actionHistoriesResource.reload();
  }

  protected convertValueFromServer(restActionHistory: RestActionHistory): IActionHistory {
    return {
      ...restActionHistory,
      createdAt: restActionHistory.createdAt ? dayjs(restActionHistory.createdAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class ActionHistoryService extends ActionHistoriesService {
  protected readonly http = inject(HttpClient);

  create(actionHistory: NewActionHistory): Observable<IActionHistory> {
    const copy = this.convertValueFromClient(actionHistory);
    return this.http.post<RestActionHistory>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(actionHistory: IActionHistory): Observable<IActionHistory> {
    const copy = this.convertValueFromClient(actionHistory);
    return this.http
      .put<RestActionHistory>(`${this.resourceUrl}/${encodeURIComponent(this.getActionHistoryIdentifier(actionHistory))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(actionHistory: PartialUpdateActionHistory): Observable<IActionHistory> {
    const copy = this.convertValueFromClient(actionHistory);
    return this.http
      .patch<RestActionHistory>(`${this.resourceUrl}/${encodeURIComponent(this.getActionHistoryIdentifier(actionHistory))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IActionHistory> {
    return this.http
      .get<RestActionHistory>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IActionHistory[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestActionHistory[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getActionHistoryIdentifier(actionHistory: Pick<IActionHistory, 'id'>): number {
    return actionHistory.id;
  }

  compareActionHistory(o1: Pick<IActionHistory, 'id'> | null, o2: Pick<IActionHistory, 'id'> | null): boolean {
    return o1 && o2 ? this.getActionHistoryIdentifier(o1) === this.getActionHistoryIdentifier(o2) : o1 === o2;
  }

  addActionHistoryToCollectionIfMissing<Type extends Pick<IActionHistory, 'id'>>(
    actionHistoryCollection: Type[],
    ...actionHistoriesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const actionHistories: Type[] = actionHistoriesToCheck.filter(isPresent);
    if (actionHistories.length > 0) {
      const actionHistoryCollectionIdentifiers = actionHistoryCollection.map(actionHistoryItem =>
        this.getActionHistoryIdentifier(actionHistoryItem),
      );
      const actionHistoriesToAdd = actionHistories.filter(actionHistoryItem => {
        const actionHistoryIdentifier = this.getActionHistoryIdentifier(actionHistoryItem);
        if (actionHistoryCollectionIdentifiers.includes(actionHistoryIdentifier)) {
          return false;
        }
        actionHistoryCollectionIdentifiers.push(actionHistoryIdentifier);
        return true;
      });
      return [...actionHistoriesToAdd, ...actionHistoryCollection];
    }
    return actionHistoryCollection;
  }

  protected convertValueFromClient<T extends IActionHistory | NewActionHistory | PartialUpdateActionHistory>(actionHistory: T): RestOf<T> {
    return {
      ...actionHistory,
      createdAt: actionHistory.createdAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestActionHistory): IActionHistory {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestActionHistory[]): IActionHistory[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
