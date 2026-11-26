import { Component, input } from '@angular/core';
import { SejmResult } from '../../core/api/models';

/**
 * The outcome of the last vote. A dissolved Sejm gets the big "Nie pozwalam!" banner with a CSS
 * animation; role="status" makes screen readers announce the result.
 */
@Component({
  selector: 'lv-vote-result',
  styleUrl: './vote-result.scss',
  templateUrl: './vote-result.html',
})
export class VoteResult {
  readonly result = input.required<SejmResult>();
}
