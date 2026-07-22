import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

@Injectable({ providedIn: 'root' })
export class CsvDownloadService {
  private readonly http = inject(HttpClient);
  private readonly appConfig = inject(ApplicationConfigService);

  download(apiPath: string, filename: string): void {
    const url = this.appConfig.getEndpointFor(apiPath);
    this.http.get(url, { responseType: 'blob' }).subscribe({
      next: blob => {
        const blobUrl = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = blobUrl;
        a.download = filename;
        a.click();
        URL.revokeObjectURL(blobUrl);
      },
      error: err => {
        console.error('CSV download failed', err);
      },
    });
  }
}
