import dayjs from 'dayjs/esm';

import { IIssue } from 'app/entities/issue/issue.model';

export interface IComment {
  id: number;
  content?: string | null;
  createdAt?: dayjs.Dayjs | null;
  issue?: Pick<IIssue, 'id'> | null;
  author?: { id: number; login: string } | null;
}

export type NewComment = Omit<IComment, 'id'> & { id: null };
