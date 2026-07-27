import { Pipe, PipeTransform } from '@angular/core';

import dayjs from 'dayjs/esm';

@Pipe({
  name: 'formatMediumDate',
})
export default class FormatMediumDatePipe implements PipeTransform {
  transform(day: dayjs.Dayjs | string | Date | null | undefined): string {
    if (day == null) {
      return '';
    }
    const d = dayjs(day);
    return d.isValid() ? d.format('D MMM YYYY') : '';
  }
}
