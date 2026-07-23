import dayjs from 'dayjs/esm';

import { IProject } from 'app/entities/project/project.model';

export interface IGroupMessage {
  id: number;
  content?: string | null;
  createdAt?: dayjs.Dayjs | null;
  sender?: { id: number; login: string } | null;
  recipient?: { id: number; login: string } | null;
  project?: Pick<IProject, 'id'> | null;
}

export type NewGroupMessage = Omit<IGroupMessage, 'id'> & { id: null };
