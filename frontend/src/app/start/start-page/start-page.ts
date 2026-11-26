import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { errorMessage } from '../../core/api/error-message';
import { GameApi } from '../../core/api/game-api';

const LAST_GAME_KEY = 'liberum-veto.last-game';

/**
 * Landing page: start a new game or continue the last one (its id is remembered in localStorage).
 */
@Component({
  imports: [RouterLink],
  selector: 'lv-start-page',
  styleUrl: './start-page.scss',
  templateUrl: './start-page.html',
})
export class StartPage {
  private readonly api = inject(GameApi);
  private readonly router = inject(Router);

  protected readonly lastGameId = signal(localStorage.getItem(LAST_GAME_KEY));
  protected readonly starting = signal(false);
  protected readonly error = signal<string | undefined>(undefined);

  protected async startNewGame(): Promise<void> {
    this.starting.set(true);
    this.error.set(undefined);
    try {
      const game = await firstValueFrom(this.api.create());
      localStorage.setItem(LAST_GAME_KEY, game.id);
      await this.router.navigate(['/games', game.id]);
    } catch (error) {
      this.error.set(errorMessage(error));
    } finally {
      this.starting.set(false);
    }
  }
}
