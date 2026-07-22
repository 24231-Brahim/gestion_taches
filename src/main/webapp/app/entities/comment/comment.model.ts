import dayjs from 'dayjs/esm';

import { ITask } from 'app/entities/task/task.model';

export interface IComment {
  id: number;
  content?: string | null;
  createdAt?: dayjs.Dayjs | null;
  task?: Pick<ITask, 'id'> | null;
  author?: { id: number; login: string } | null;
}

export type NewComment = Omit<IComment, 'id'> & { id: null };
