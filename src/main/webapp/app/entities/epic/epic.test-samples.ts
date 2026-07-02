import dayjs from 'dayjs/esm';

import { IEpic, NewEpic } from './epic.model';

export const sampleWithRequiredData: IEpic = {
  id: 24843,
  title: 'trop sédentaire par rapport à',
  status: 'IN_PROGRESS',
  priority: 'LOWEST',
  createdAt: dayjs('2026-06-23T16:10'),
};

export const sampleWithPartialData: IEpic = {
  id: 14193,
  title: 'près de afin de naguère',
  description: 'envers',
  status: 'DONE',
  priority: 'HIGH',
  createdAt: dayjs('2026-06-23T10:47'),
};

export const sampleWithFullData: IEpic = {
  id: 23662,
  title: 'du fait que',
  description: 'après que dense',
  status: 'TODO',
  priority: 'HIGH',
  createdAt: dayjs('2026-06-23T22:35'),
  updatedAt: dayjs('2026-06-23T15:19'),
  startDate: dayjs('2026-06-01'),
  endDate: dayjs('2026-06-30'),
};

export const sampleWithNewData: NewEpic = {
  title: 'tant concurrence oh',
  status: 'IN_PROGRESS',
  priority: 'HIGH',
  createdAt: dayjs('2026-06-23T16:02'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
