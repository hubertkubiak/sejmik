import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { BillPicker } from '../bill-picker/bill-picker';
import { BribePanel, BribeRequest } from '../bribe-panel/bribe-panel';
import { GameStatus } from '../game-status/game-status';
import { GameStore } from '../game-store';
import { HistoryList } from '../history-list/history-list';
import { RESULT_LABELS } from '../labels';
import { SejmHall } from '../sejm-hall/sejm-hall';
import { VoteResult } from '../vote-result/vote-result';

/**
 * The game screen: composes the smaller components and connects them to the GameStore.
 * It provides its own store, so every visit starts with fresh state.
 */
@Component({
  selector: 'sj-game-page',
  imports: [RouterLink, GameStatus, BillPicker, SejmHall, BribePanel, VoteResult, HistoryList],
  providers: [GameStore],
  styleUrl: './game-page.scss',
  templateUrl: './game-page.html',
})
export class GamePage {
  /** Filled from the ":id" route parameter (withComponentInputBinding). */
  readonly id = input.required<string>();

  protected readonly store = inject(GameStore);
  protected readonly selectedDeputyId = signal<number | undefined>(undefined);
  protected readonly selectedDeputy = computed(() =>
    this.store.deputies().find((d) => d.id === this.selectedDeputyId()),
  );
  protected readonly resultLabels = RESULT_LABELS;

  constructor() {
    // load the game whenever the id changes (also on the first render)
    effect(() => {
      void this.store.load(this.id());
    });
  }

  protected async selectBill(billId: number): Promise<void> {
    this.selectedDeputyId.set(undefined);
    await this.store.selectBill(billId);
  }

  protected async bribe(request: BribeRequest): Promise<void> {
    await this.store.bribe(request.deputyId, request.amount);
  }

  protected async vote(): Promise<void> {
    await this.store.vote();
    this.selectedDeputyId.set(undefined);
  }
}
