import { HttpClient, HttpResponse, httpResource } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { isPresent } from 'app/core/util/operators';
import { IAttachment, NewAttachment } from '../attachment.model';

export type PartialUpdateAttachment = Partial<IAttachment> & Pick<IAttachment, 'id'>;

type RestOf<T extends IAttachment | NewAttachment> = Omit<T, 'uploadedAt'> & {
  uploadedAt?: string | null;
};

export type RestAttachment = RestOf<IAttachment>;

export type NewRestAttachment = RestOf<NewAttachment>;

export type PartialUpdateRestAttachment = RestOf<PartialUpdateAttachment>;

@Injectable()
export class AttachmentsService {
  readonly attachmentsParams = signal<Record<string, string | number | boolean | readonly (string | number | boolean)[]> | undefined>(
    undefined,
  );
  readonly attachmentsResource = httpResource<RestAttachment[]>(() => {
    const params = this.attachmentsParams();
    if (!params) {
      return undefined;
    }
    return { url: this.resourceUrl, params };
  });
  /**
   * This signal holds the list of attachment that have been fetched. It is updated when the attachmentsResource emits a new value.
   * In case of error while fetching the attachments, the signal is set to an empty array.
   */
  readonly attachments = computed(() =>
    (this.attachmentsResource.hasValue() ? this.attachmentsResource.value() : []).map(item => this.convertValueFromServer(item)),
  );
  protected readonly applicationConfigService = inject(ApplicationConfigService);
  protected readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/attachments');

  protected convertValueFromServer(restAttachment: RestAttachment): IAttachment {
    return {
      ...restAttachment,
      uploadedAt: restAttachment.uploadedAt ? dayjs(restAttachment.uploadedAt) : undefined,
    };
  }
}

@Injectable({ providedIn: 'root' })
export class AttachmentService extends AttachmentsService {
  protected readonly http = inject(HttpClient);

  create(attachment: NewAttachment): Observable<IAttachment> {
    const copy = this.convertValueFromClient(attachment);
    return this.http.post<RestAttachment>(this.resourceUrl, copy).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(attachment: IAttachment): Observable<IAttachment> {
    const copy = this.convertValueFromClient(attachment);
    return this.http
      .put<RestAttachment>(`${this.resourceUrl}/${encodeURIComponent(this.getAttachmentIdentifier(attachment))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(attachment: PartialUpdateAttachment): Observable<IAttachment> {
    const copy = this.convertValueFromClient(attachment);
    return this.http
      .patch<RestAttachment>(`${this.resourceUrl}/${encodeURIComponent(this.getAttachmentIdentifier(attachment))}`, copy)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<IAttachment> {
    return this.http
      .get<RestAttachment>(`${this.resourceUrl}/${encodeURIComponent(id)}`)
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<HttpResponse<IAttachment[]>> {
    const options = createRequestOption(req);
    return this.http
      .get<RestAttachment[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => res.clone({ body: this.convertResponseArrayFromServer(res.body!) })));
  }

  delete(id: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl}/${encodeURIComponent(id)}`);
  }

  getAttachmentIdentifier(attachment: Pick<IAttachment, 'id'>): number {
    return attachment.id;
  }

  compareAttachment(o1: Pick<IAttachment, 'id'> | null, o2: Pick<IAttachment, 'id'> | null): boolean {
    return o1 && o2 ? this.getAttachmentIdentifier(o1) === this.getAttachmentIdentifier(o2) : o1 === o2;
  }

  addAttachmentToCollectionIfMissing<Type extends Pick<IAttachment, 'id'>>(
    attachmentCollection: Type[],
    ...attachmentsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const attachments: Type[] = attachmentsToCheck.filter(isPresent);
    if (attachments.length > 0) {
      const attachmentCollectionIdentifiers = attachmentCollection.map(attachmentItem => this.getAttachmentIdentifier(attachmentItem));
      const attachmentsToAdd = attachments.filter(attachmentItem => {
        const attachmentIdentifier = this.getAttachmentIdentifier(attachmentItem);
        if (attachmentCollectionIdentifiers.includes(attachmentIdentifier)) {
          return false;
        }
        attachmentCollectionIdentifiers.push(attachmentIdentifier);
        return true;
      });
      return [...attachmentsToAdd, ...attachmentCollection];
    }
    return attachmentCollection;
  }

  protected convertValueFromClient<T extends IAttachment | NewAttachment | PartialUpdateAttachment>(attachment: T): RestOf<T> {
    return {
      ...attachment,
      uploadedAt: attachment.uploadedAt?.toJSON() ?? null,
    };
  }

  protected convertResponseFromServer(res: RestAttachment): IAttachment {
    return this.convertValueFromServer(res);
  }

  protected convertResponseArrayFromServer(res: RestAttachment[]): IAttachment[] {
    return res.map(item => this.convertValueFromServer(item));
  }
}
