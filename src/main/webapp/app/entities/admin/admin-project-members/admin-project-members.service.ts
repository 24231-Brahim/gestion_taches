import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { IProjectMember } from 'app/entities/project/project.model';

export type RestProjectMember = Omit<IProjectMember, 'joinedAt'> & {
  joinedAt?: string | null;
};

@Injectable({ providedIn: 'root' })
export class AdminProjectMemberService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/admin/project-members');

  query(req?: any): Observable<HttpResponse<IProjectMember[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestProjectMember[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  protected convertDateFromServer(restProjectMember: RestProjectMember): IProjectMember {
    return {
      ...restProjectMember,
      joinedAt: restProjectMember.joinedAt ? dayjs(restProjectMember.joinedAt) : undefined,
    };
  }

  protected convertResponseArrayFromServer(restProjectMembers: RestProjectMember[]): IProjectMember[] {
    return restProjectMembers.map(restProjectMember => this.convertDateFromServer(restProjectMember));
  }
}
