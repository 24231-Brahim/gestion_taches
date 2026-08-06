import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { NgOptimizedImage } from '@angular/common';

@Component({
  selector: 'jhi-error',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [NgOptimizedImage],
  templateUrl: './error.html',
  styleUrl: './error.scss',
})
export default class Error {}
