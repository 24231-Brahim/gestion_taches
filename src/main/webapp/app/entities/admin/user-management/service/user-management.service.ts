import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Pagination } from 'app/core/request/request.model';
import { IUserManagement } from '../user-management.model';

export interface IUserAdminDetail {
  id?: number | null;
  login?: string;
  firstName?: string | null;
  lastName?: string | null;
  email?: string;
  activated?: boolean;
  langKey?: string;
  createdBy?: string;
  createdDate?: Date;
  authorities?: string[];
  tasks?: ITaskSummary[];
  projects?: IProjectMembership[];
  recentActivity?: ITaskHistoryEntry[];
}

export interface ITaskSummary {
  id?: number | null;
  title?: string;
  status?: string;
  priority?: string;
  createdAt?: Date;
  projectId?: number | null;
  projectName?: string;
  projectKey?: string;
  sprintId?: number | null;
  sprintName?: string;
  epicId?: number | null;
  epicTitle?: string;
}

export interface IProjectMembership {
  projectId?: number | null;
  projectName?: string;
  projectKey?: string;
  role?: string;
  joinedAt?: Date;
}

export interface ITaskHistoryEntry {
  id?: number | null;
  action?: string;
  oldValue?: string;
  newValue?: string;
  createdAt?: Date;
  taskId?: number | null;
  taskTitle?: string;
}

@Injectable({ providedIn: 'root' })
export class UserManagementService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);

  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/admin/users');

  create(user: IUserManagement): Observable<IUserManagement> {
    return this.http.post<IUserManagement>(this.resourceUrl, user);
  }

  update(user: IUserManagement): Observable<IUserManagement> {
    return this.http.put<IUserManagement>(this.resourceUrl, user);
  }

  find(login: string): Observable<IUserManagement> {
    return this.http.get<IUserManagement>(`${this.resourceUrl}/${encodeURIComponent(login)}`);
  }

  findDetail(login: string): Observable<IUserAdminDetail> {
    return this.http.get<IUserAdminDetail>(`${this.resourceUrl}/${encodeURIComponent(login)}/detail`);
  }

  query(req?: Pagination): Observable<HttpResponse<IUserManagement[]>> {
    const options = createRequestOption(req);
    return this.http.get<IUserManagement[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(login: string): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(login)}`);
  }
}
