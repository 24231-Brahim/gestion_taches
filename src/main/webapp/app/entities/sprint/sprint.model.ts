import dayjs from 'dayjs/esm';

import { SprintStatus } from 'app/entities/enumerations/sprint-status.model';
import { IProject } from 'app/entities/project/project.model';

export interface ISprint {
  id: number;
  name?: string | null;
  goal?: string | null;
  startDate?: dayjs.Dayjs | null;
  endDate?: dayjs.Dayjs | null;
  status?: keyof typeof SprintStatus | null;
  project?: Pick<IProject, 'id' | 'name'> | null;
}

export type NewSprint = Omit<ISprint, 'id'> & { id: null };
