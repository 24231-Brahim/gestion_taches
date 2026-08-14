export interface StatusBadge {
  label: string;
  color: string;
  bg: string;
}

export const PRIORITY_ICONS: Record<string, string> = {
  LOWEST: 'arrow-down',
  LOW: 'arrow-down',
  MEDIUM: 'flag',
  HIGH: 'arrow-up',
  HIGHEST: 'exclamation-triangle',
};

export const PRIORITY_COLORS: Record<string, string> = {
  LOWEST: 'var(--color-priority-lowest)',
  LOW: 'var(--color-priority-low)',
  MEDIUM: 'var(--color-priority-medium)',
  HIGH: 'var(--color-priority-high)',
  HIGHEST: 'var(--color-priority-highest)',
};

export const STATUS_BADGES: Record<string, StatusBadge> = {
  NEW: { label: 'gestionTachesApp.TaskStatus.NEW', color: 'var(--color-status-backlog)', bg: 'var(--color-status-backlog-bg)' },
  TODO: { label: 'gestionTachesApp.TaskStatus.TODO', color: 'var(--color-status-backlog)', bg: 'var(--color-status-backlog-bg)' },
  IN_PROGRESS: {
    label: 'gestionTachesApp.TaskStatus.IN_PROGRESS',
    color: 'var(--color-status-in-progress)',
    bg: 'var(--color-status-in-progress-bg)',
  },
  READY_FOR_TEST: {
    label: 'gestionTachesApp.TaskStatus.READY_FOR_TEST',
    color: 'var(--color-status-in-review)',
    bg: 'var(--color-status-in-review-bg)',
  },
  IN_REVIEW: {
    label: 'gestionTachesApp.TaskStatus.IN_REVIEW',
    color: 'var(--color-status-in-review)',
    bg: 'var(--color-status-in-review-bg)',
  },
  DONE: { label: 'gestionTachesApp.TaskStatus.DONE', color: 'var(--color-status-done)', bg: 'var(--color-status-done-bg)' },
  NEEDS_INFO: {
    label: 'gestionTachesApp.TaskStatus.NEEDS_INFO',
    color: 'var(--color-status-cancelled)',
    bg: 'var(--color-status-cancelled-bg)',
  },
};
