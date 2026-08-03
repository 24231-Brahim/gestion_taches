import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { INotification } from 'app/core/util/notification.service';

@Injectable({ providedIn: 'root' })
export class AdminNotificationService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/admin/notifications');

  query(req?: any): Observable<HttpResponse<INotification[]>> {
    const options = createRequestOption(req);
    return this.http.get<INotification[]>(this.resourceUrl, { params: options, observe: 'response' });
  }
}
