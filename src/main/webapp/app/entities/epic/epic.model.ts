import dayjs from 'dayjs/esm';

import { EpicStatus } from 'app/entities/enumerations/epic-status.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { IProject } from 'app/entities/project/project.model';

export interface IEpic {
  id: number;
  title?: string | null;
  description?: string | null;
  status?: keyof typeof EpicStatus | null;
  priority?: keyof typeof Priority | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  project?: Pick<IProject, 'id' | 'name'> | null;
}

export type NewEpic = Omit<IEpic, 'id'> & { id: null };
