import { Component, effect, inject, input } from '@angular/core';
import { GameStore } from '../game-store';

/**
 * The game screen. Provides its own GameStore, so every visit starts with fresh state.
 */
@Component({
  selector: 'lv-game-page',
  providers: [GameStore],
  styleUrl: './game-page.scss',
  templateUrl: './game-page.html',
})
export class GamePage {
  /** Filled from the ":id" route parameter (withComponentInputBinding). */
  readonly id = input.required<string>();

  protected readonly store = inject(GameStore);

  constructor() {
    // load the game whenever the id changes (also on the first render)
    effect(() => {
      void this.store.load(this.id());
    });
  }
}
