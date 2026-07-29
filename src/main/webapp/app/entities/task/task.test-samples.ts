import dayjs from 'dayjs/esm';

import { ITask, NewTask } from './task.model';

export const sampleWithRequiredData: ITask = {
  id: 7569,
  title: 'camarade',
  type: 'STORY',
  status: 'IN_REVIEW',
  priority: 'LOWEST',
  createdAt: dayjs('2026-06-23T16:09'),
};

export const sampleWithPartialData: ITask = {
  id: 19511,
  title: 'favoriser',
  type: 'BUG',
  status: 'IN_REVIEW',
  priority: 'LOWEST',
  createdAt: dayjs('2026-06-24T00:47'),
};

export const sampleWithFullData: ITask = {
  id: 23509,
  title: 'gérer',
  description: 'au-dessus de',
  type: 'SUBTASK',
  status: 'CANCELLED',
  priority: 'HIGH',
  createdAt: dayjs('2026-06-24T09:04'),
  updatedAt: dayjs('2026-06-24T03:48'),
};

export const sampleWithNewData: NewTask = {
  title: 'devant pas mal pff',
  type: 'SUBTASK',
  status: 'IN_REVIEW',
  priority: 'LOW',
  createdAt: dayjs('2026-06-23T23:53'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
