/**
 * TypeScript mirrors of the API's DTO records (package pl.hubert.liberumveto.api.dto).
 * They describe the JSON shape only; they are compile-time types and do not exist at runtime.
 */

export type Faction = 'ROYAL' | 'MAGNATE' | 'INDEPENDENT';

export type BillType = 'TAX' | 'CONSCRIPTION' | 'NOBLE_PRIVILEGE' | 'TREASURY_REFORM';

export type GameResult = 'KING_WINS' | 'MAGNATES_WIN' | 'DRAW';

export type DeputySort = 'id' | 'support' | 'corruptibility';

export interface Bill {
  id: number;
  title: string;
  type: BillType;
  impact: Partial<Record<Faction, number>>;
}

export interface Game {
  id: string;
  sejmNumber: number;
  sejmCount: number;
  kingBudget: number;
  passedCount: number;
  dissolvedCount: number;
  over: boolean;
  result: GameResult | null;
  selectedBill: Bill | null;
}

export interface Deputy {
  id: number;
  name: string;
  coatOfArms: string;
  fullName: string;
  faction: Faction;
  loyaltyToKing: number;
  corruptibility: number;
  /** Support for the selected bill (0-100); null before a bill is selected. */
  support: number | null;
  vetoChancePercent: number | null;
}

export interface BillSelection {
  bill: Bill;
  bribedDeputies: number;
}

export interface SupportChange {
  deputy: Deputy;
  supportBefore: number;
  supportAfter: number;
  kingBudget: number;
}

export interface SejmResult {
  number: number;
  bill: Bill;
  votesFor: number;
  votesAgainst: number;
  vetoes: number;
  passed: boolean;
  dissolved: boolean;
  vetoer: Deputy | null;
}

export interface VoteResponse {
  sejm: SejmResult;
  game: Game;
}

/** The API's error format (RFC 9457). `detail` is Polish text meant for the player. */
export interface ProblemDetail {
  title: string;
  status: number;
  detail: string;
  errors?: Record<string, string>;
}
