import dayjs from 'dayjs/esm';

import { IIssue } from 'app/entities/issue/issue.model';

export interface IAttachment {
  id: number;
  fileName?: string | null;
  filePath?: string | null;
  uploadedAt?: dayjs.Dayjs | null;
  issue?: Pick<IIssue, 'id'> | null;
}

export type NewAttachment = Omit<IAttachment, 'id'> & { id: null };
