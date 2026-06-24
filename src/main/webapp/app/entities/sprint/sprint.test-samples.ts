import dayjs from 'dayjs/esm';

import { ISprint, NewSprint } from './sprint.model';

export const sampleWithRequiredData: ISprint = {
  id: 4944,
  name: 'autant insipide',
  status: 'PLANNED',
};

export const sampleWithPartialData: ISprint = {
  id: 21300,
  name: 'par suite de de façon à ce que étant donné que',
  status: 'ACTIVE',
};

export const sampleWithFullData: ISprint = {
  id: 15912,
  name: 'sale à bas de envers',
  goal: 'sans doute pour que touriste',
  startDate: dayjs('2026-06-23'),
  endDate: dayjs('2026-06-23'),
  status: 'CANCELLED',
};

export const sampleWithNewData: NewSprint = {
  name: 'dring',
  status: 'ACTIVE',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
