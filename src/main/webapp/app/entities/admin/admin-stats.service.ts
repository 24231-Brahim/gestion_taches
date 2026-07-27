import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApplicationConfigService } from 'app/core/config/application-config.service';

export interface IAdminStats {
  totalUsers: number;
  totalProjects: number;
  totalTasks: number;
  usersByRole: Record<string, number>;
}

@Injectable({ providedIn: 'root' })
export class AdminStatsService {
  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly resourceUrl = this.applicationConfigService.getEndpointFor('api/admin/stats');

  getStats(): Observable<IAdminStats> {
    return this.http.get<IAdminStats>(this.resourceUrl);
  }
}
