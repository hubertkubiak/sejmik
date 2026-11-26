import { Component, input } from '@angular/core';
import { Game } from '../../core/api/models';

/**
 * The top bar of the game screen: which Sejm, the treasury and the score.
 */
@Component({
  selector: 'lv-game-status',
  styleUrl: './game-status.scss',
  templateUrl: './game-status.html',
})
export class GameStatus {
  readonly game = input.required<Game>();
  readonly atRisk = input(0);
}
