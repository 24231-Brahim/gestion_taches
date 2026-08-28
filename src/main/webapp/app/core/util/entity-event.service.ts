import { Injectable, inject } from '@angular/core';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import { Observable, Subject, debounceTime, filter } from 'rxjs';

export interface EntityChangeEvent {
  entityType: string;
  eventType: string;
  entityId: number;
  projectId: number | null;
}

export const EntityType = {
  TASK: 'TASK',
  SPRINT: 'SPRINT',
  EPIC: 'EPIC',
  PROJECT: 'PROJECT',
  PROJECT_MEMBER: 'PROJECT_MEMBER',
} as const;

export const EventType = {
  CREATED: 'CREATED',
  UPDATED: 'UPDATED',
  DELETED: 'DELETED',
} as const;

@Injectable({ providedIn: 'root' })
export class EntityEventService {
  readonly events$: Observable<EntityChangeEvent>;

  private readonly applicationConfigService = inject(ApplicationConfigService);
  private readonly stateStorageService = inject(StateStorageService);
  private readonly eventSubject = new Subject<EntityChangeEvent>();
  private abortController: AbortController | null = null;
  private connected = false;

  constructor() {
    this.events$ = this.eventSubject.asObservable();
  }

  connect(): void {
    if (this.connected) {
      return;
    }
    this.connected = true;
    this.connectSSE();
  }

  disconnect(): void {
    this.connected = false;
    if (this.abortController) {
      this.abortController.abort();
      this.abortController = null;
    }
  }

  onEntityType(entityType: string, debounceMs = 0): Observable<EntityChangeEvent> {
    return this.events$.pipe(
      debounceTime(debounceMs),
      filter(e => e.entityType === entityType),
    );
  }

  onEntityTypeAndProject(entityType: string, projectId: number | null): Observable<EntityChangeEvent> {
    return this.events$.pipe(filter(e => e.entityType === entityType && (projectId === null || e.projectId === projectId)));
  }

  onProject(projectId: number | null): Observable<EntityChangeEvent> {
    return this.events$.pipe(filter(e => projectId === null || e.projectId === projectId));
  }

  private connectSSE(): void {
    if (this.abortController) {
      return;
    }
    this.abortController = new AbortController();
    const url = this.applicationConfigService.getEndpointFor('api/events/stream');
    const token = this.stateStorageService.getAuthenticationToken();

    const headers: Record<string, string> = {};
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    fetch(url, {
      headers,
      signal: this.abortController.signal,
    })
      .then(response => {
        if (!response.ok || !response.body) {
          this.disconnect();
          return;
        }
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        const processStream = (): void => {
          reader.read().then(({ done, value }) => {
            if (done) {
              this.disconnect();
              return;
            }
            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');
            buffer = lines.pop() ?? '';

            let eventName = 'message';
            let eventData = '';
            for (const line of lines) {
              if (line.startsWith('event:')) {
                eventName = line.slice(6).trim();
              } else if (line.startsWith('data:')) {
                eventData = line.slice(5).trim();
              } else if (line === '' && eventData) {
                if (eventName === 'entity-change') {
                  try {
                    const event: EntityChangeEvent = JSON.parse(eventData);
                    this.eventSubject.next(event);
                  } catch {
                    // ignore parse errors
                  }
                }
                eventName = 'message';
                eventData = '';
              }
            }
            processStream();
          });
        };
        processStream();
      })
      .catch(() => {
        this.disconnect();
      });
  }
}
