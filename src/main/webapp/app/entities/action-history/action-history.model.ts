import dayjs from 'dayjs/esm';

import { IIssue } from 'app/entities/issue/issue.model';

export interface IActionHistory {
  id: number;
  action?: string | null;
  fieldChanged?: string | null;
  oldValue?: string | null;
  newValue?: string | null;
  createdAt?: dayjs.Dayjs | null;
  issue?: Pick<IIssue, 'id'> | null;
  user?: { id: number; login: string } | null;
}

export type NewActionHistory = Omit<IActionHistory, 'id'> & { id: null };
