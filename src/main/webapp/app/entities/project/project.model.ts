import dayjs from 'dayjs/esm';
import { ProjectRole } from 'app/entities/enumerations/project-role.model';

export interface IProjectMember {
  id: number;
  projectId?: number | null;
  userId?: number | null;
  userLogin?: string | null;
  role?: ProjectRole | null;
  joinedAt?: dayjs.Dayjs | null;
}

export interface IProject {
  id: number;
  name?: string | null;
  description?: string | null;
  key?: string | null;
  createdAt?: dayjs.Dayjs | null;
  ownerId?: number | null;
  ownerLogin?: string | null;
  projectMembers?: IProjectMember[] | null;
}

export type NewProject = Omit<IProject, 'id'> & { id: null };
