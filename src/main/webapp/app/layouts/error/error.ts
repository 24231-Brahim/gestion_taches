import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

@Component({
  selector: 'jhi-error',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './error.html',
  styleUrl: './error.scss',
})
export default class Error {}
