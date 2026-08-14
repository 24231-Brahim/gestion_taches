import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import dayjs from 'dayjs/esm';
import { Observable, map } from 'rxjs';

import { ApplicationConfigService } from 'app/core/config/application-config.service';

import { IChatMember, IChatMessage, IConversation } from '../chat.model';

type RestChatMessage = Omit<IChatMessage, 'createdAt' | 'editedAt'> & {
  createdAt?: string | null;
  editedAt?: string | null;
};

type RestChatMember = Omit<IChatMember, 'joinedAt' | 'lastReadAt'> & {
  joinedAt?: string | null;
  lastReadAt?: string | null;
};

type RestConversation = Omit<IConversation, 'createdAt' | 'lastMessageAt' | 'participants'> & {
  createdAt?: string | null;
  lastMessageAt?: string | null;
  participants?: RestChatMember[] | null;
};

@Injectable({ providedIn: 'root' })
export class ChatService {
  protected readonly http = inject(HttpClient);
  protected readonly applicationConfigService = inject(ApplicationConfigService);

  getConversations(projectId: number): Observable<IConversation[]> {
    return this.http
      .get<RestConversation[]>(`${this.resourceUrl(projectId)}/conversations`)
      .pipe(map(items => items.map(item => this.convertConversationFromServer(item))));
  }

  openDirectConversation(projectId: number, userId: number): Observable<IConversation> {
    return this.http
      .post<RestConversation>(`${this.resourceUrl(projectId)}/conversations/direct/${encodeURIComponent(userId)}`, null)
      .pipe(map(item => this.convertConversationFromServer(item)));
  }

  getMessages(projectId: number, conversationId: number, beforeId?: number | null, limit = 30): Observable<IChatMessage[]> {
    const params: Record<string, string | number> = { limit };
    if (beforeId != null) {
      params['beforeId'] = beforeId;
    }
    return this.http
      .get<RestChatMessage[]>(`${this.resourceUrl(projectId)}/conversations/${encodeURIComponent(conversationId)}/messages`, { params })
      .pipe(map(items => items.map(item => this.convertMessageFromServer(item))));
  }

  sendMessage(projectId: number, conversationId: number, content: string): Observable<IChatMessage> {
    return this.http
      .post<RestChatMessage>(`${this.resourceUrl(projectId)}/conversations/${encodeURIComponent(conversationId)}/messages`, { content })
      .pipe(map(item => this.convertMessageFromServer(item)));
  }

  updateMessage(projectId: number, messageId: number, content: string): Observable<IChatMessage> {
    return this.http
      .patch<RestChatMessage>(`${this.resourceUrl(projectId)}/messages/${encodeURIComponent(messageId)}`, { content })
      .pipe(map(item => this.convertMessageFromServer(item)));
  }

  deleteMessage(projectId: number, messageId: number): Observable<undefined> {
    return this.http.delete<undefined>(`${this.resourceUrl(projectId)}/messages/${encodeURIComponent(messageId)}`);
  }

  markConversationRead(projectId: number, conversationId: number): Observable<undefined> {
    return this.http.post<undefined>(`${this.resourceUrl(projectId)}/conversations/${encodeURIComponent(conversationId)}/read`, null);
  }

  getMembers(projectId: number): Observable<IChatMember[]> {
    return this.http
      .get<RestChatMember[]>(`${this.resourceUrl(projectId)}/members`)
      .pipe(map(items => items.map(item => this.convertMemberFromServer(item))));
  }

  searchMessages(projectId: number, query: string, limit = 30): Observable<IChatMessage[]> {
    const params: Record<string, string | number> = { q: query, limit };
    return this.http
      .get<RestChatMessage[]>(`${this.resourceUrl(projectId)}/search`, { params })
      .pipe(map(items => items.map(item => this.convertMessageFromServer(item))));
  }

  protected convertMessageFromServer(rest: RestChatMessage): IChatMessage {
    return {
      ...rest,
      createdAt: rest.createdAt ? dayjs(rest.createdAt) : undefined,
      editedAt: rest.editedAt ? dayjs(rest.editedAt) : undefined,
    };
  }

  protected convertConversationFromServer(rest: RestConversation): IConversation {
    return {
      ...rest,
      createdAt: rest.createdAt ? dayjs(rest.createdAt) : undefined,
      lastMessageAt: rest.lastMessageAt ? dayjs(rest.lastMessageAt) : undefined,
      participants: rest.participants?.map(item => this.convertMemberFromServer(item)),
    };
  }

  protected convertMemberFromServer(rest: RestChatMember): IChatMember {
    return {
      ...rest,
      joinedAt: rest.joinedAt ? dayjs(rest.joinedAt) : undefined,
      lastReadAt: rest.lastReadAt ? dayjs(rest.lastReadAt) : undefined,
    };
  }

  private resourceUrl(projectId: number): string {
    return `${this.applicationConfigService.getEndpointFor('api/projects')}/${encodeURIComponent(projectId)}/chat`;
  }
}
