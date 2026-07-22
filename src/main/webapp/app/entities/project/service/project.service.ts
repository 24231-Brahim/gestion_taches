import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { ProjectRole } from 'app/entities/enumerations/project-role.model';
import { IProject, IProjectMember, NewProject } from '../project.model';

export type PartialUpdateProject = Partial<IProject> & Pick<IProject, 'id'>;

type RestOf<T extends IProject | NewProject> = Omit<T, 'createdAt'> & {
  createdAt?: string | null;
};

export type RestProject = RestOf<IProject>;

export type NewRestProject = RestOf<NewProject>;

export type PartialUpdateRestProject = RestOf<PartialUpdateProject>;

@Injectable()
export class ProjectsService {
  readonly projectsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly projectsResource = httpResource<RestProject[]>(() => {
    const params = this.projectsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of project that have been fetched. It is updated when the projectsResource emits a new value.
   * In case of error while fetching the projects, the signal is set to an empty array.
   */
  readonly projects = computed(() =>
    (this.projectsResource.hasValue() ? this.projectsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/projects');

  refresh(): void {
    this.projectsResource.reload();
  }

  protected convertValueFromServer(restProject: RestProject): IProject {
    return {
      ...restProject,
      createdAt: restProject.createdAt ? dayjs(restProject.createdAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class ProjectService extends ProjectsService {
  protected readonly http = inject(HttpClient);

  create(project: NewProject): Observable<IProject> {
    const copy = this.convertValueFromClient(project);
    return this.http.post<RestProject>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(project: IProject): Observable<IProject> {
    const copy = this.convertValueFromClient(project);
    return this.http
      .put<RestProject>(`${this.resourceUrl}/${encodeURIComponent(this.getProjectIdentifier(project))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(project: PartialUpdateProject): Observable<IProject> {
    const copy = this.convertValueFromClient(project);
    return this.http
      .patch<RestProject>(`${this.resourceUrl}/${encodeURIComponent(this.getProjectIdentifier(project))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IProject> {
    return this.http
      .get<RestProject>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  findByKey(key: string): Observable<IProject> {
    return this.http
      .get<RestProject>(`${this.resourceUrl}/by-key/${encodeURIComponent(key)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IProject[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestProject[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getMembers(id: number): Observable<IProjectMember[]> {
    return this.http
      .get<IProjectMember[]>(`${this.resourceUrl}/${encodeURIComponent(id)}/members`)
      .pipe(map(members => members.map(m => this.convertMemberFromServer(m))));
  }

  getMyRoles(): Observable<IProjectMember[]> {
    return this.http
      .get<IProjectMember[]>(`${this.resourceUrl}/my-roles`)
      .pipe(map(members => members.map(m => this.convertMemberFromServer(m))));
  }

  addMember(projectId: number, userId: number): Observable<undefined> {
    return this.http.post<undefined>(`${this.resourceUrl}/${encodeURIComponent(projectId)}/members/${encodeURIComponent(userId)}`, null);
  }

  removeMember(projectId: number, userId: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(projectId)}/members/${encodeURIComponent(userId)}`);
  }

  updateMemberRole(projectId: number, userId: number, role: ProjectRole | string): Observable<undefined> {
    return this.http.patch<undefined>(`${this.resourceUrl}/${encodeURIComponent(projectId)}/members/${encodeURIComponent(userId)}`, {
      role,
    });
  }

  getProjectIdentifier(project: Pick<IProject, 'id'>): number {
    return project.id;
  }

  compareProject(o1: Pick<IProject, 'id'> | null, o2: Pick<IProject, 'id'> | null): boolean {
    return o1 && o2 ? this.getProjectIdentifier(o1) === this.getProjectIdentifier(o2) : o1 === o2;
  }

  addProjectToCollectionIfMissing<Type extends Pick<IProject, 'id'>>(
    projectCollection: Type[],
    ...projectsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const projects: Type[] = projectsToCheck.filter(isPresent);
    if (projects.length > 0) {
      const projectCollectionIdentifiers = projectCollection.map(projectItem => this.getProjectIdentifier(projectItem));
      const projectsToAdd = projects.filter(projectItem => {
        const projectIdentifier = this.getProjectIdentifier(projectItem);
        if (projectCollectionIdentifiers.includes(projectIdentifier)) {
          return false;
        }
        projectCollectionIdentifiers.push(projectIdentifier);
        return true;
      });
      return [...projectsToAdd, ...projectCollection];
    }
    return projectCollection;
  }

  protected convertValueFromClient<T extends IProject | NewProject | PartialUpdateProject>(project: T): RestOf<T> {
    return {
      ...project,
      createdAt: project.createdAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestProject): IProject {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestProject[]): IProject[] {
    return res.map(item => this.convertValueFromServer(item));
  }

  protected convertMemberFromServer(member: IProjectMember): IProjectMember {
    return {
      ...member,
      joinedAt: member.joinedAt ? dayjs(member.joinedAt) : null,
    };
  }
}
