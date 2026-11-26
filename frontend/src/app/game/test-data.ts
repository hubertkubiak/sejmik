import { Bill, Deputy, SejmResult } from '../core/api/models';

/** Small builders for component tests. */
export const testBill: Bill = { id: 7, title: 'Reforma skarbu koronnego', type: 'TREASURY_REFORM', impact: {} };

export function testDeputy(overrides: Partial<Deputy> = {}): Deputy {
  return {
    id: 3,
    name: 'Jakub Sobieski',
    coatOfArms: 'Lubicz',
    fullName: 'Jakub Sobieski h. Lubicz',
    faction: 'INDEPENDENT',
    loyaltyToKing: 40,
    corruptibility: 0.8,
    support: 0,
    vetoChancePercent: 100,
    ...overrides,
  };
}

export function testResult(overrides: Partial<SejmResult> = {}): SejmResult {
  return {
    number: 1,
    bill: testBill,
    votesFor: 9,
    votesAgainst: 8,
    vetoes: 3,
    passed: false,
    dissolved: true,
    vetoer: testDeputy({ fullName: 'Kazimierz Sapieha h. Ostoja' }),
    ...overrides,
  };
}
