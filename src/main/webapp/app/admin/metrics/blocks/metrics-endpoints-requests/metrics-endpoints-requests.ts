import { DecimalPipe, KeyValuePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, input } from '@angular/core';

import { TranslateDirective, TranslateModule } from '@ngx-translate/core';

import { Services } from 'app/admin/metrics/metrics.model';

@Component({
  selector: 'jhi-metrics-endpoints-requests',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './metrics-endpoints-requests.html',
  imports: [KeyValuePipe, DecimalPipe, TranslateDirective, TranslateModule],
})
export class MetricsEndpointsRequests {
  /**
   * Object containing service related metrics
   */
  readonly endpointsRequestsMetrics = input<Services>();

  /**
   * Boolean field saying if the metrics are in the process of being updated
   */
  readonly updating = input<boolean>();
}
