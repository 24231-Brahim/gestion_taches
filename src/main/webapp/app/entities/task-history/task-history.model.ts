import dayjs from 'dayjs/esm';

import { ITask } from 'app/entities/task/task.model';

export interface ITaskHistory {
  id: number;
  action?: string | null;
  oldValue?: string | null;
  newValue?: string | null;
  createdAt?: dayjs.Dayjs | null;
  task?: Pick<ITask, 'id'> | null;
  user?: { id: number; login: string } | null;
}
