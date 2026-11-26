import { TestBed } from '@angular/core/testing';
import { Observable, of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { GameApi } from '../core/api/game-api';
import { Bill, Deputy, Game, SejmResult } from '../core/api/models';
import { GameStore } from './game-store';

const bill: Bill = { id: 7, title: 'Reforma skarbu koronnego', type: 'TREASURY_REFORM', impact: {} };

function game(overrides: Partial<Game> = {}): Game {
  return {
    id: 'g1',
    sejmNumber: 1,
    sejmCount: 5,
    kingBudget: 100,
    passedCount: 0,
    dissolvedCount: 0,
    over: false,
    result: null,
    selectedBill: null,
    ...overrides,
  };
}

function deputy(id: number, support: number | null): Deputy {
  return {
    id,
    name: `Poseł ${id}`,
    coatOfArms: 'Jelita',
    fullName: `Poseł ${id} h. Jelita`,
    faction: 'INDEPENDENT',
    loyaltyToKing: 40,
    corruptibility: 0.5,
    support,
    vetoChancePercent: support === null ? null : Math.max(0, Math.floor(((15 - support) * 100) / 15)),
  };
}

/** A hand-written fake: only the methods the store uses, returning canned answers. */
function fakeApi(overrides: Partial<Record<keyof GameApi, () => Observable<unknown>>> = {}) {
  return {
    get: () => of(game()),
    bills: () => of([bill]),
    deputies: () => of([deputy(1, null), deputy(2, null)]),
    history: () => of([] as SejmResult[]),
    selectBill: () => of({ bill, bribedDeputies: 3 }),
    bribe: () => of({ deputy: deputy(1, 60), supportBefore: 45, supportAfter: 60, kingBudget: 70 }),
    vote: () => of({}),
    ...overrides,
  };
}

function createStore(api = fakeApi()): GameStore {
  TestBed.configureTestingModule({ providers: [GameStore, { provide: GameApi, useValue: api }] });
  return TestBed.inject(GameStore);
}

describe('GameStore', () => {
  it('loads the game, bills, deputies and history', async () => {
    const store = createStore();

    await store.load('g1');

    expect(store.game()?.sejmNumber).toBe(1);
    expect(store.bills()).toEqual([bill]);
    expect(store.deputies()).toHaveLength(2);
    expect(store.busy()).toBe(false);
  });

  it('updates one deputy and the budget after a bribe', async () => {
    const store = createStore();
    await store.load('g1');

    const ok = await store.bribe(1, 30);

    expect(ok).toBe(true);
    expect(store.game()?.kingBudget).toBe(70);
    expect(store.deputies().find((d) => d.id === 1)?.support).toBe(60);
    expect(store.lastBribe()).toContain('45 → 60');
  });

  it('counts deputies at risk of a veto', async () => {
    const store = createStore(fakeApi({ deputies: () => of([deputy(1, 3), deputy(2, 14), deputy(3, 50)]) }));

    await store.load('g1');

    expect(store.atRiskCount()).toBe(2);
  });

  it('shows the Polish error from the API and stays usable', async () => {
    const problem = new HttpErrorResponse({ status: 422, error: { detail: 'Niewystarczający budżet' } });
    const store = createStore(fakeApi({ bribe: () => throwError(() => problem) }));
    await store.load('g1');

    const ok = await store.bribe(1, 1000);

    expect(ok).toBe(false);
    expect(store.error()).toBe('Niewystarczający budżet');
    expect(store.busy()).toBe(false);
  });

  it('remembers the last vote result', async () => {
    const result: SejmResult = {
      number: 1, bill, votesFor: 10, votesAgainst: 8, vetoes: 2,
      passed: false, dissolved: true, vetoer: deputy(5, 0),
    };
    const store = createStore(fakeApi({
      get: () => of(game({ selectedBill: bill })),
      vote: () => of({ sejm: result, game: game({ sejmNumber: 2, dissolvedCount: 1 }) }),
    }));
    await store.load('g1');

    await store.vote();

    expect(store.lastResult()?.dissolved).toBe(true);
    expect(store.game()?.sejmNumber).toBe(2);
  });
});
