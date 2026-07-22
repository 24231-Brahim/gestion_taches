import { describe, expect, it } from 'vitest';

import dayjs from 'dayjs/esm';

import FormatMediumDatePipe from './format-medium-date.pipe';

describe('FormatMediumDatePipe', () => {
  const pipe = new FormatMediumDatePipe();

  it('should return an empty string when receive undefined', () => {
    expect(pipe.transform(undefined)).toBe('');
  });

  it('should return an empty string when receive null', () => {
    expect(pipe.transform(null)).toBe('');
  });

  it('should format a dayjs object', () => {
    expect(pipe.transform(dayjs('2020-11-16').locale('fr'))).toBe('16 Nov 2020');
  });

  it('should format an ISO string', () => {
    expect(pipe.transform('2020-11-16T14:30:00')).toBe('16 Nov 2020');
  });

  it('should format a Date object', () => {
    const date = new Date('2020-11-16T14:30:00');
    expect(pipe.transform(date)).toBe('16 Nov 2020');
  });

  it('should return an empty string for an invalid string', () => {
    expect(pipe.transform('not-a-date')).toBe('');
  });
});
