import dayjs from 'dayjs/esm';

import { IActionHistory, NewActionHistory } from './action-history.model';

export const sampleWithRequiredData: IActionHistory = {
  id: 23505,
  action: 'si bien que séculaire',
  createdAt: dayjs('2026-06-23T21:01'),
};

export const sampleWithPartialData: IActionHistory = {
  id: 1894,
  action: 'sympathique',
  oldValue: 'intrépide incognito concernant',
  newValue: 'insolite',
  createdAt: dayjs('2026-06-24T07:46'),
};

export const sampleWithFullData: IActionHistory = {
  id: 32665,
  action: 'séculaire',
  fieldChanged: 'lorsque',
  oldValue: 'broum',
  newValue: 'âcre concernant',
  createdAt: dayjs('2026-06-23T10:34'),
};

export const sampleWithNewData: NewActionHistory = {
  action: 'tellement',
  createdAt: dayjs('2026-06-24T05:23'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
