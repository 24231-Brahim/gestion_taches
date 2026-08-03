import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { ITaskHistory, NewTaskHistory } from '../task-history.model';

export type PartialUpdateTaskHistory = Partial<ITaskHistory> & Pick<ITaskHistory, 'id'>;

type RestOf<T extends ITaskHistory | NewTaskHistory> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestTaskHistory = RestOf<ITaskHistory>;

export type NewRestTaskHistory = RestOf<NewTaskHistory>;

export type PartialUpdateRestTaskHistory = RestOf<PartialUpdateTaskHistory>;

@Injectable({ providedIn: 'root' })
export class TaskHistoryService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/task-histories');

  create(taskHistory: NewTaskHistory): Observable<ITaskHistory> {
    const copy = this.convertValueFromClient(taskHistory);
    return this.http.post<RestTaskHistory>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(taskHistory: ITaskHistory): Observable<ITaskHistory> {
    const copy = this.convertValueFromClient(taskHistory);
    return this.http
      .put<RestTaskHistory>(`${this.resourceUrl}/${encodeURIComponent(this.getTaskHistoryIdentifier(taskHistory))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(taskHistory: PartialUpdateTaskHistory): Observable<ITaskHistory> {
    const copy = this.convertValueFromClient(taskHistory);
    return this.http
      .patch<RestTaskHistory>(`${this.resourceUrl}/${encodeURIComponent(this.getTaskHistoryIdentifier(taskHistory))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<ITaskHistory> {
    return this.http
      .get<RestTaskHistory>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<ITaskHistory[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestTaskHistory[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  findByTask(taskId: number): Observable<HttpResponse<ITaskHistory[]>> {
    return this.http
      .get<RestTaskHistory[]>(`${this.resourceUrl}/by-task/${encodeURIComponent(taskId)}`, { observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getTaskHistoryIdentifier(taskHistory: Pick<ITaskHistory, 'id'>): number {
    return taskHistory.id;
  }

  compareTaskHistory(o1: Pick<ITaskHistory, 'id'> | null, o2: Pick<ITaskHistory, 'id'> | null): boolean {
    return o1 && o2 ? this.getTaskHistoryIdentifier(o1) === this.getTaskHistoryIdentifier(o2) : o1 === o2;
  }

  protected convertValueFromClient<T extends ITaskHistory | NewTaskHistory | PartialUpdateTaskHistory>(taskHistory: T): RestOf<T> {
    return {
      ...taskHistory,
      createdAt: taskHistory.createdAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestTaskHistory): ITaskHistory {
    return {
      ...res,
      createdAt: res.createdAt ? dayjs(res.createdAt) : undefined,
    };
  }

  protected convertResponseArrayFromServer(res: RestTaskHistory[]): ITaskHistory[] {
    return res.map(item => this.convertResponseFromServer(item));
  }
}
