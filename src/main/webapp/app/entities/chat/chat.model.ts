import dayjs from 'dayjs/esm';

import { ConversationType } from 'app/entities/enumerations/conversation-type.model';
import { ProjectRole } from 'app/entities/enumerations/project-role.model';

export interface IChatUser {
  id: number | null;
  login: string | null;
}

export interface IConversation {
  id: number;
  type: ConversationType;
  name?: string | null;
  projectId?: number | null;
  createdAt?: dayjs.Dayjs | null;
  lastMessageAt?: dayjs.Dayjs | null;
  lastMessagePreview?: string | null;
  unreadCount?: number;
  participants?: IChatMember[] | null;
}

export interface IConversationMember {
  id: number | null;
  conversationId?: number | null;
  userId: number | null;
  userLogin?: string | null;
  joinedAt?: dayjs.Dayjs | null;
  lastReadAt?: dayjs.Dayjs | null;
}

export interface IChatMember {
  userId: number | null;
  userLogin?: string | null;
  role?: ProjectRole | null;
  joinedAt?: dayjs.Dayjs | null;
  lastReadAt?: dayjs.Dayjs | null;
  online?: boolean;
  lastActiveAt?: dayjs.Dayjs | null;
}

export interface IUserPresence {
  userId: number | null;
  online?: boolean;
  lastActiveAt?: dayjs.Dayjs | null;
}

export interface IChatMessage {
  id: number;
  content: string;
  createdAt?: dayjs.Dayjs | null;
  editedAt?: dayjs.Dayjs | null;
  deleted?: boolean;
  conversationId?: number | null;
  sender?: IChatUser | null;
  parentMessageId?: number | null;
  mentions?: Set<number> | null;
}
