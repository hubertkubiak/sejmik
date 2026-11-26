import { Service, computed, inject, signal } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { errorMessage } from '../core/api/error-message';
import { GameApi } from '../core/api/game-api';
import { Bill, Deputy, Game, SejmResult } from '../core/api/models';

/**
 * State of one game screen, kept in signals. Components read the read-only signals and call
 * the action methods; only this class talks to the API and changes the state.
 *
 * `autoProvided: false`: not an app-wide singleton. The game page provides its own instance,
 * so leaving the page throws the state away.
 */
@Service({ autoProvided: false })
export class GameStore {
  private readonly api = inject(GameApi);

  private readonly gameState = signal<Game | undefined>(undefined);
  private readonly billsState = signal<Bill[]>([]);
  private readonly deputiesState = signal<Deputy[]>([]);
  private readonly historyState = signal<SejmResult[]>([]);
  private readonly lastResultState = signal<SejmResult | undefined>(undefined);
  private readonly lastBribeState = signal<string | undefined>(undefined);
  private readonly errorState = signal<string | undefined>(undefined);
  private readonly busyState = signal(false);

  readonly game = this.gameState.asReadonly();
  readonly bills = this.billsState.asReadonly();
  readonly deputies = this.deputiesState.asReadonly();
  readonly history = this.historyState.asReadonly();
  readonly lastResult = this.lastResultState.asReadonly();
  readonly lastBribe = this.lastBribeState.asReadonly();
  readonly error = this.errorState.asReadonly();
  readonly busy = this.busyState.asReadonly();

  /** Derived state: recomputed automatically when the game signal changes. */
  readonly selectedBill = computed(() => this.game()?.selectedBill ?? undefined);
  readonly atRiskCount = computed(
    () => this.deputies().filter((d) => d.support !== null && d.support < 15).length,
  );

  async load(gameId: string): Promise<void> {
    await this.run(async () => {
      const game = await firstValueFrom(this.api.get(gameId));
      this.gameState.set(game);
      await this.refreshTurn(game);
    });
  }

  async selectBill(billId: number): Promise<void> {
    await this.run(async () => {
      const game = this.requireGame();
      const selection = await firstValueFrom(this.api.selectBill(game.id, billId));
      this.lastResultState.set(undefined);
      this.lastBribeState.set(
        `Magnaci rozdali łapówki ${selection.bribedDeputies} posłom. Czas na Twój ruch, Miłościwy Panie.`,
      );
      this.gameState.set(await firstValueFrom(this.api.get(game.id)));
      this.deputiesState.set(await firstValueFrom(this.api.deputies(game.id, 'support')));
    });
  }

  async bribe(deputyId: number, amount: number): Promise<boolean> {
    return this.run(async () => {
      const game = this.requireGame();
      const change = await firstValueFrom(this.api.bribe(game.id, deputyId, amount));
      // immutable updates: new array and new object, so signals notice the change
      this.deputiesState.update((list) =>
        list.map((d) => (d.id === change.deputy.id ? change.deputy : d)),
      );
      this.gameState.update((g) => (g ? { ...g, kingBudget: change.kingBudget } : g));
      this.lastBribeState.set(
        `${change.deputy.fullName}: poparcie ${change.supportBefore} → ${change.supportAfter}`,
      );
    });
  }

  async vote(): Promise<void> {
    await this.run(async () => {
      const game = this.requireGame();
      const response = await firstValueFrom(this.api.vote(game.id));
      this.lastResultState.set(response.sejm);
      this.lastBribeState.set(undefined);
      this.gameState.set(response.game);
      await this.refreshTurn(response.game);
    });
  }

  clearError(): void {
    this.errorState.set(undefined);
  }

  private async refreshTurn(game: Game): Promise<void> {
    const sort = game.selectedBill ? 'support' : 'id';
    const [bills, deputies, history] = await Promise.all([
      firstValueFrom(this.api.bills(game.id)),
      firstValueFrom(this.api.deputies(game.id, sort)),
      firstValueFrom(this.api.history(game.id)),
    ]);
    this.billsState.set(bills);
    this.deputiesState.set(deputies);
    this.historyState.set(history);
  }

  private requireGame(): Game {
    const game = this.game();
    if (!game) {
      throw new Error('No game loaded');
    }
    return game;
  }

  /** Common wrapper: busy flag, clearing and setting the error message. Returns success. */
  private async run(action: () => Promise<void>): Promise<boolean> {
    this.busyState.set(true);
    this.errorState.set(undefined);
    try {
      await action();
      return true;
    } catch (error) {
      this.errorState.set(errorMessage(error));
      return false;
    } finally {
      this.busyState.set(false);
    }
  }
}
