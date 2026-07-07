import dayjs from 'dayjs/esm';

import { IssueStatus } from 'app/entities/enumerations/issue-status.model';
import { IssueType } from 'app/entities/enumerations/issue-type.model';
import { Priority } from 'app/entities/enumerations/priority.model';
import { IEpic } from 'app/entities/epic/epic.model';
import { IProject } from 'app/entities/project/project.model';
import { ISprint } from 'app/entities/sprint/sprint.model';

export interface IIssue {
  id: number;
  title?: string | null;
  description?: string | null;
  type?: keyof typeof IssueType | null;
  status?: keyof typeof IssueStatus | null;
  priority?: keyof typeof Priority | null;
  createdAt?: dayjs.Dayjs | null;
  updatedAt?: dayjs.Dayjs | null;
  sprint?: Pick<ISprint, 'id' | 'name'> | null;
  epic?: Pick<IEpic, 'id' | 'title'> | null;
  project?: Pick<IProject, 'id' | 'name' | 'key'> | null;
  assignee?: { id: number; login: string } | null;
  createdBy?: { id: number; login: string } | null;
}

export type NewIssue = Omit<IIssue, 'id'> & { id: null };
