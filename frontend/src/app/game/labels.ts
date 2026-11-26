import { Faction, GameResult } from '../core/api/models';

/** Polish names shown to the player; the API sends enum constants. */
export const FACTION_LABELS: Record<Faction, string> = {
  ROYAL: 'królewska',
  MAGNATE: 'magnacka',
  INDEPENDENT: 'niezależna',
};

export const RESULT_LABELS: Record<GameResult, string> = {
  KING_WINS: 'Wygrana Króla! Reformy przeszły, Rzeczpospolita rośnie w siłę.',
  MAGNATES_WIN: 'Wygrana magnatów. Złota wolność szlachecka zwyciężyła… na razie.',
  DRAW: 'Remis: Rzeczpospolita trwa.',
};

export const FOR_THRESHOLD = 50;
export const VETO_THRESHOLD = 15;

/** How a deputy will most likely vote, the same thresholds as Deputy.vote() in the domain. */
export type SupportLevel = 'for' | 'against' | 'veto' | 'unknown';

export function supportLevel(support: number | null): SupportLevel {
  if (support === null) {
    return 'unknown';
  }
  if (support >= FOR_THRESHOLD) {
    return 'for';
  }
  return support >= VETO_THRESHOLD ? 'against' : 'veto';
}

export const SUPPORT_LABELS: Record<SupportLevel, string> = {
  for: 'ZA',
  against: 'PRZECIW',
  veto: 'grozi VETO',
  unknown: 'brak ustawy',
};

/** "+20", "-30", "0" */
export function signed(value: number): string {
  return value > 0 ? `+${value}` : `${value}`;
}
