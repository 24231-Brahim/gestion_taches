import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import dayjs from 'dayjs/esm';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { IGroupMessage, NewGroupMessage } from '../group-message.model';

export type PartialUpdateGroupMessage = Partial<IGroupMessage> & Pick<IGroupMessage, 'id'>;

type RestOf<T extends IGroupMessage | NewGroupMessage> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestGroupMessage = RestOf<IGroupMessage>;
export type NewRestGroupMessage = RestOf<NewGroupMessage>;
export type PartialUpdateRestGroupMessage = RestOf<PartialUpdateGroupMessage>;

@Injectable({ providedIn: 'root' })
export class GroupMessageService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  protected getProjectResourceUrl(projectId: number): string {
    return this.applicationConfigService.getEndpointFor(`api/projects/${projectId}/group-messages`);
  }

  create(projectId: number, groupMessage: NewGroupMessage): Observable<IGroupMessage> {
    const copy = this.convertValueFromClient(groupMessage);
    return this.http
      .post<RestGroupMessage>(this.getProjectResourceUrl(projectId), copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(projectId: number): Observable<IGroupMessage[]> {
    return this.http
      .get<RestGroupMessage[]>(this.getProjectResourceUrl(projectId))
      .pipe(map(res => res.map(item => this.convertValueFromServer(item))));
  }

  protected convertValueFromServer(restGroupMessage: RestGroupMessage): IGroupMessage {
    return {
      ...restGroupMessage,
      createdAt: restGroupMessage.createdAt ? dayjs(restGroupMessage.createdAt) : undefined,
    };
  }

  protected convertValueFromClient<T extends IGroupMessage | NewGroupMessage | PartialUpdateGroupMessage>(groupMessage: T): RestOf<T> {
    return {
      ...groupMessage,
      createdAt: groupMessage.createdAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestGroupMessage): IGroupMessage {
    return this.convertValueFromServer(res);
  }
}
