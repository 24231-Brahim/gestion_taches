import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { HttpResponse } from '@angular/common/http';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { NgbPagination } from '@ng-bootstrap/ng-bootstrap/pagination';
import { TranslateModule } from '@ngx-translate/core';

import { ITEMS_PER_PAGE } from 'app/config/pagination.constants';
import { Alert } from 'app/shared/alert/alert';
import { AlertError } from 'app/shared/alert/alert-error';
import { TranslateDirective } from 'app/shared/language';
import { ItemCount } from 'app/shared/pagination';
import { TaskService } from 'app/entities/task/service/task.service';
import { ITask } from 'app/entities/task/task.model';
import { STATUS_BADGES, PRIORITY_COLORS, StatusBadge } from 'app/entities/task/task-helper';

@Component({
  selector: 'jhi-admin-tasks',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './admin-tasks.html',
  imports: [RouterLink, FontAwesomeModule, AlertError, Alert, NgbPagination, TranslateDirective, TranslateModule, ItemCount, DatePipe],
})
export class AdminTasks implements OnInit {
  readonly tasks = signal<ITask[]>([]);
  readonly isLoading = signal(false);
  readonly totalItems = signal(0);
  readonly page = signal(0);
  readonly itemsPerPage = signal(ITEMS_PER_PAGE);
  readonly searchQuery = signal('');
  readonly statusBadges = STATUS_BADGES;
  readonly priorityColors = PRIORITY_COLORS;

  private readonly taskService = inject(TaskService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading.set(true);
    const req = {
      page: this.page() - 1,
      size: this.itemsPerPage(),
      sort: 'createdAt,desc',
    };
    this.taskService.query(req).subscribe({
      next: (res: HttpResponse<ITask[]>) => {
        this.isLoading.set(false);
        this.totalItems.set(Number(res.headers.get('X-Total-Count')));
        this.tasks.set(res.body ?? []);
      },
      error: () => this.isLoading.set(false),
    });
  }

  transition(): void {
    this.loadAll();
  }

  getStatusColor(status: string | null | undefined): string {
    if (!status) return '#9e9e9e';
    const badge: StatusBadge | undefined = (STATUS_BADGES as Record<string, StatusBadge | undefined>)[status];
    return badge ? badge.color : '#9e9e9e';
  }

  getPriorityColor(priority: string | null | undefined): string {
    return PRIORITY_COLORS[priority ?? ''] ?? '#9e9e9e';
  }

  getStatusLabel(status: string | null | undefined): string {
    if (!status) return '';
    const map: Record<string, string> = {
      NEW: 'Nouveau',
      TODO: 'A faire',
      IN_PROGRESS: 'En cours',
      IN_REVIEW: 'En revue',
      DONE: 'Terminé',
      CANCELLED: 'Annulé',
    };
    return map[status] ?? status;
  }

  getPriorityLabel(priority: string | null | undefined): string {
    if (!priority) return '';
    const map: Record<string, string> = {
      LOWEST: 'Très bas',
      LOW: 'Bas',
      MEDIUM: 'Moyen',
      HIGH: 'Haut',
      HIGHEST: 'Très haut',
    };
    return map[priority] ?? priority;
  }
}
