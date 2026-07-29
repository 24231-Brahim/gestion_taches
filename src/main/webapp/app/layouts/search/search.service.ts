import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { Observable } from 'rxjs';

export interface SearchResult {
  type: string;
  id: number;
  title: string;
  description: string;
  projectKey: string;
  status: string;
  link: string;
}

@Injectable({ providedIn: 'root' })
export class SearchService {
  readonly searchOpen = signal(false);
  readonly searchQuery = signal('');
  readonly searchResults = signal<SearchResult[]>([]);
  readonly searchLoading = signal(false);

  private readonly http = inject(HttpClient);
  private readonly applicationConfigService = inject(ApplicationConfigService);

  search(query: string): Observable<SearchResult[]> {
    return this.http.get<SearchResult[]>(this.applicationConfigService.getEndpointFor('api/search'), { params: { q: query } });
  }

  open(): void {
    this.searchOpen.set(true);
    this.searchQuery.set('');
    this.searchResults.set([]);
  }

  close(): void {
    this.searchOpen.set(false);
    this.searchQuery.set('');
    this.searchResults.set([]);
  }
}
