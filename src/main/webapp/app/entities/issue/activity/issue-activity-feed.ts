import { HttpClient } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, OnInit, inject, input, signal } from '@angular/core';

import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { TranslateModule } from '@ngx-translate/core';

import { IActionHistory } from 'app/entities/action-history/action-history.model';
import { FormatMediumDatetimePipe } from 'app/shared/date';
import { TranslateDirective } from 'app/shared/language';

@Component({
  changeDetection: ChangeDetectionStrategy.OnPush,
  selector: 'jhi-issue-activity-feed',
  templateUrl: './issue-activity-feed.html',
  styles: [
    `
      .issue-activity {
        display: flex;
        flex-direction: column;
        gap: 12px;
      }
      .issue-section-title {
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.8rem;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        color: var(--color-text-muted, #6a8fac);
        margin: 0 0 4px;
      }
      .activity-feed {
        display: flex;
        flex-direction: column;
        gap: 0;
      }
      .activity-item {
        display: flex;
        gap: 12px;
        padding: 10px 0;
        border-bottom: 1px solid var(--color-outline-variant, #2a3038);
      }
      .activity-item:last-child {
        border-bottom: none;
      }
      .activity-dot {
        width: 10px;
        height: 10px;
        margin-top: 5px;
        flex-shrink: 0;
        background: var(--color-primary, #97cbff);
        border: 2px solid var(--color-primary-container, #25a7fd);
      }
      .activity-content {
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.8rem;
        color: var(--color-text, #dfe3ea);
        line-height: 1.5;
      }
      .activity-action {
        font-weight: 700;
        color: var(--color-primary, #97cbff);
      }
      .activity-detail {
        color: var(--color-text-muted, #6a8fac);
      }
      .activity-date {
        font-size: 0.7rem;
        color: var(--color-text-muted, #6a8fac);
        margin-top: 2px;
      }
      .text-muted {
        color: var(--color-text-muted, #6a8fac);
        font-family: 'JetBrains Mono', monospace;
        font-size: 0.8rem;
      }
    `,
  ],
  imports: [FontAwesomeModule, TranslateDirective, TranslateModule, FormatMediumDatetimePipe],
})
export class IssueActivityFeed implements OnInit {
  readonly issueId = input.required<number>();

  readonly histories = signal<IActionHistory[]>([]);

  protected readonly http = inject(HttpClient);

  ngOnInit(): void {
    this.loadHistories();
  }

  loadHistories(): void {
    this.http.get<IActionHistory[]>(`/api/action-histories/by-issue/${this.issueId()}`).subscribe({
      next: histories => this.histories.set(histories),
    });
  }
}
