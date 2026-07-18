import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IEpic, NewEpic } from '../epic.model';

export type PartialUpdateEpic = Partial<IEpic> & Pick<IEpic, 'id'>;

type RestOf<T extends IEpic | NewEpic> = Omit<T, 'createdAt' | 'updatedAt' | 'startDate' | 'endDate'> & {
  createdAt?: string | null;
  updatedAt?: string | null;
  startDate?: string | null;
  endDate?: string | null;
};

export type RestEpic = RestOf<IEpic>;

export type NewRestEpic = RestOf<NewEpic>;

export type PartialUpdateRestEpic = RestOf<PartialUpdateEpic>;

@Injectable()
export class EpicsService {
  readonly epicsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(undefined);
  readonly epicsResource = httpResource<RestEpic[]>(() => {
    const params = this.epicsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of epic that have been fetched. It is updated when the epicsResource emits a new value.
   * In case of error while fetching the epics, the signal is set to an empty array.
   */
  readonly epics = computed(() =>
    (this.epicsResource.hasValue() ? this.epicsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/epics');

  refresh(): void {
    this.epicsResource.reload();
  }

  protected convertValueFromServer(restEpic: RestEpic): IEpic {
    return {
      ...restEpic,
      createdAt: restEpic.createdAt ? dayjs(restEpic.createdAt) : undefined,
      updatedAt: restEpic.updatedAt ? dayjs(restEpic.updatedAt) : undefined,
      startDate: restEpic.startDate ? dayjs(restEpic.startDate) : undefined,
      endDate: restEpic.endDate ? dayjs(restEpic.endDate) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class EpicService extends EpicsService {
  protected readonly http = inject(HttpClient);

  create(epic: NewEpic): Observable<IEpic> {
    const copy = this.convertValueFromClient(epic);
    return this.http.post<RestEpic>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(epic: IEpic): Observable<IEpic> {
    const copy = this.convertValueFromClient(epic);
    return this.http
      .put<RestEpic>(`${this.resourceUrl}/${encodeURIComponent(this.getEpicIdentifier(epic))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(epic: PartialUpdateEpic): Observable<IEpic> {
    const copy = this.convertValueFromClient(epic);
    return this.http
      .patch<RestEpic>(`${this.resourceUrl}/${encodeURIComponent(this.getEpicIdentifier(epic))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IEpic> {
    return this.http.get<RestEpic>(`${this.resourceUrl}/${encodeURIComponent(id)}`).pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IEpic[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestEpic[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getEpicIdentifier(epic: Pick<IEpic, 'id'>): number {
    return epic.id;
  }

  compareEpic(o1: Pick<IEpic, 'id'> | null, o2: Pick<IEpic, 'id'> | null): boolean {
    return o1 && o2 ? this.getEpicIdentifier(o1) === this.getEpicIdentifier(o2) : o1 === o2;
  }

  addEpicToCollectionIfMissing<Type extends Pick<IEpic, 'id'>>(
    epicCollection: Type[],
    ...epicsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const epics: Type[] = epicsToCheck.filter(isPresent);
    if (epics.length > 0) {
      const epicCollectionIdentifiers = epicCollection.map(epicItem => this.getEpicIdentifier(epicItem));
      const epicsToAdd = epics.filter(epicItem => {
        const epicIdentifier = this.getEpicIdentifier(epicItem);
        if (epicCollectionIdentifiers.includes(epicIdentifier)) {
          return false;
        }
        epicCollectionIdentifiers.push(epicIdentifier);
        return true;
      });
      return [...epicsToAdd, ...epicCollection];
    }
    return epicCollection;
  }

  protected convertValueFromClient<T extends IEpic | NewEpic | PartialUpdateEpic>(epic: T): RestOf<T> {
    return {
      ...epic,
      createdAt: epic.createdAt?.toJSON() ?? null,
      updatedAt: epic.updatedAt?.toJSON() ?? null,
      startDate: epic.startDate?.toJSON() ?? null,
      endDate: epic.endDate?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestEpic): IEpic {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestEpic[]): IEpic[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
