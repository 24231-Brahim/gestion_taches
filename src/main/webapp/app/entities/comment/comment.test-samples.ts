import dayjs from 'dayjs/esm';

import { IComment, NewComment } from './comment.model';

export const sampleWithRequiredData: IComment = {
  id: 20452,
  content: 'afin que amorphe',
  createdAt: dayjs('2026-06-23T19:38'),
};

export const sampleWithPartialData: IComment = {
  id: 12398,
  content: 'peu',
  createdAt: dayjs('2026-06-23T22:51'),
};

export const sampleWithFullData: IComment = {
  id: 28427,
  content: 'population du Québec oh',
  createdAt: dayjs('2026-06-23T23:48'),
};

export const sampleWithNewData: NewComment = {
  content: 'isoler tic-tac infime',
  createdAt: dayjs('2026-06-23T22:31'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
