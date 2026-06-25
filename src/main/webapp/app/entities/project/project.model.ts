import dayjs from 'dayjs/esm';

export interface IProject {
  id: number;
  name?: string | null;
  description?: string | null;
  key?: string | null;
  createdAt?: dayjs.Dayjs | null;
  ownerId?: number | null;
  ownerLogin?: string | null;
  memberIds?: number[] | null;
}

export type NewProject = Omit<IProject, 'id'> & { id: null };
