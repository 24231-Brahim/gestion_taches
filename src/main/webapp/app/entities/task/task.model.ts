import dayjs from 'dayjs/esm';

import { TaskStatus } from 'app/entities/enumerations/task-status.model';
import { TaskType } from 'app/entities/enumerations/task-type.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { IEpic } from 'app/entities/epic/epic.model';
import { IProject } from 'app/entities/project/project.model';
import { ISprint } from 'app/entities/sprint/sprint.model';

export interface ITask {
  id: number;
  title?: string | null;
  description?: string | null;
  type?: keyof typeof TaskType | null;
  status?: keyof typeof TaskStatus | null;
  priority?: keyof typeof Priority | null;
  storyPoints?: number | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  sprint?: Pick<ISprint, 'id' | 'name'> | null;
  epic?: Pick<IEpic, 'id' | 'title'> | null;
  project?: Pick<IProject, 'id' | 'name' | 'key'> | null;
  assignee?: { id: number; login: string } | null;
  createdBy?: { id: number; login: string } | null;
}

export type NewTask = Omit<ITask, 'id'> & { id: null };
