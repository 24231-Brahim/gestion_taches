import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { ITaskHistory } from '../task-history.model';

type RestTaskHistory = Omit<ITaskHistory, 'createdAt'> & { createdAt?: string | null };

@Injectable({ providedIn: 'root' })
export class TaskHistoryService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/task-histories');

  findByTask(taskId: number): Observable<ITaskHistory[]> {
    return this.http
      .get<RestTaskHistory[]>(`${this.resourceUrl}/by-task/${taskId}`)
      .pipe(map(histories => histories.map(history => this.convertValueFromServer(history))));
  }

  protected convertValueFromServer(restTaskHistory: RestTaskHistory): ITaskHistory {
    return {
      ...restTaskHistory,
      createdAt: restTaskHistory.createdAt ? dayjs(restTaskHistory.createdAt) : undefined,
    };
  }
}
