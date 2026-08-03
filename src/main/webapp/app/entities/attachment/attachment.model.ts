import dayjs from 'dayjs/esm';

import { ITask } from 'app/entities/task/task.model';

export interface IAttachment {
  id: number;
  fileName?: string | null;
  filePath?: string | null;
  uploadedAt?: dayjs.Dayjs | null;
  task?: Pick<ITask, 'id'> | null;
  uploadedBy?: { id: number; login: string } | null;
}

export type NewAttachment = Omit<IAttachment, 'id'> & { id: null };
