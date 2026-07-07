import { IconDefinition } from '@fortawesome/fontawesome-svg-core';

export interface StatusBadge {
  label: string;
  color: string;
}

export const ISSUE_TYPE_LABELS: Record<string, string> = {
  STORY: 'gestionTachesApp.IssueType.STORY',
  BUG: 'gestionTachesApp.IssueType.BUG',
  TASK: 'gestionTachesApp.IssueType.TASK',
  SUBTASK: 'gestionTachesApp.IssueType.SUBTASK',
  IMPROVEMENT: 'gestionTachesApp.IssueType.IMPROVEMENT',
};

export const ISSUE_TYPE_ICONS: Record<string, string> = {
  STORY: 'th-list',
  BUG: 'bug',
  TASK: 'check-circle',
  SUBTASK: 'plus',
  IMPROVEMENT: 'arrow-up',
};

export const ISSUE_TYPE_COLORS: Record<string, string> = {
  STORY: 'var(--color-story, #4caf50)',
  BUG: 'var(--color-bug, #f44336)',
  TASK: 'var(--color-task, #2196f3)',
  SUBTASK: 'var(--color-subtask, #9e9e9e)',
  IMPROVEMENT: 'var(--color-improvement, #ff9800)',
};

export const PRIORITY_ICONS: Record<string, string> = {
  LOWEST: 'arrow-down',
  LOW: 'arrow-down',
  MEDIUM: 'flag',
  HIGH: 'arrow-up',
  HIGHEST: 'exclamation-triangle',
};

export const PRIORITY_COLORS: Record<string, string> = {
  LOWEST: 'var(--color-priority-lowest, #9e9e9e)',
  LOW: 'var(--color-priority-low, #607d8b)',
  MEDIUM: 'var(--color-priority-medium, #2196f3)',
  HIGH: 'var(--color-priority-high, #ff9800)',
  HIGHEST: 'var(--color-priority-highest, #f44336)',
};

export const STATUS_BADGES: Record<string, StatusBadge> = {
  BACKLOG: { label: 'gestionTachesApp.IssueStatus.BACKLOG', color: 'var(--color-status-backlog, #9e9e9e)' },
  TODO: { label: 'gestionTachesApp.IssueStatus.TODO', color: 'var(--color-status-todo, #2196f3)' },
  IN_PROGRESS: { label: 'gestionTachesApp.IssueStatus.IN_PROGRESS', color: 'var(--color-status-in-progress, #ff9800)' },
  IN_REVIEW: { label: 'gestionTachesApp.IssueStatus.IN_REVIEW', color: 'var(--color-status-in-review, #9c27b0)' },
  DONE: { label: 'gestionTachesApp.IssueStatus.DONE', color: 'var(--color-status-done, #4caf50)' },
  CANCELLED: { label: 'gestionTachesApp.IssueStatus.CANCELLED', color: 'var(--color-status-cancelled, #f44336)' },
};

export type ViewMode = 'list' | 'kanban';
