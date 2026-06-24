import dayjs from 'dayjs/esm';

import { IAttachment, NewAttachment } from './attachment.model';

export const sampleWithRequiredData: IAttachment = {
  id: 15526,
  fileName: 'plouf',
  filePath: 'avant que tendre',
  uploadedAt: dayjs('2026-06-24T09:29'),
};

export const sampleWithPartialData: IAttachment = {
  id: 15256,
  fileName: 'en bas de vorace',
  filePath: 'vers',
  uploadedAt: dayjs('2026-06-24T05:42'),
};

export const sampleWithFullData: IAttachment = {
  id: 21727,
  fileName: 'juriste pff diététiste',
  filePath: 'trop',
  uploadedAt: dayjs('2026-06-23T23:11'),
};

export const sampleWithNewData: NewAttachment = {
  fileName: 'divinement équipe',
  filePath: 'fort assez à peu près',
  uploadedAt: dayjs('2026-06-23T17:57'),
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
