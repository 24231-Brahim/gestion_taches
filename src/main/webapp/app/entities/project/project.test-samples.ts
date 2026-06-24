import dayjs from 'dayjs/esm';

import { IProject, NewProject } from './project.model';

export const sampleWithRequiredData: IProject = {
  id: 22823,
  name: 'comment',
  key: 'à côté de ',
  createdAt: dayjs('2026-06-23T22:48'),
};

export const sampleWithPartialData: IProject = {
  id: 14992,
  name: 'population du Québec toc-toc',
  key: 'tandis que',
  createdAt: dayjs('2026-06-23T15:39'),
};

export const sampleWithFullData: IProject = {
  id: 1375,
  name: 'rectorat parer membre titulaire',
  description: 'de sorte que',
  key: 'avex',
  createdAt: dayjs('2026-06-24T00:14'),
};

export const sampleWithNewData: NewProject = {
  name: 'toc quant à',
  key: 'cot cot en',
  createdAt: dayjs('2026-06-24T03:18'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
