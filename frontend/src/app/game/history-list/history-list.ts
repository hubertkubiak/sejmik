import { Component, input } from '@angular/core';
import { SejmResult } from '../../core/api/models';

/**
 * Results of the finished Sejms.
 */
@Component({
  selector: 'sj-history-list',
  styleUrl: './history-list.scss',
  templateUrl: './history-list.html',
})
export class HistoryList {
  readonly history = input.required<SejmResult[]>();
}
