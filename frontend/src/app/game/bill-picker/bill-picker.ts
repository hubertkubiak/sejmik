import { Component, input, output } from '@angular/core';
import { Bill, Faction } from '../../core/api/models';
import { FACTION_LABELS, signed } from '../labels';

/**
 * The bills available this turn, with their impact on each faction.
 */
@Component({
  selector: 'sj-bill-picker',
  styleUrl: './bill-picker.scss',
  templateUrl: './bill-picker.html',
})
export class BillPicker {
  readonly bills = input.required<Bill[]>();
  readonly disabled = input(false);
  readonly picked = output<number>();

  protected readonly factions = Object.keys(FACTION_LABELS) as Faction[];
  protected readonly factionLabels = FACTION_LABELS;
  protected readonly signed = signed;

  protected impact(bill: Bill, faction: Faction): number {
    return bill.impact[faction] ?? 0;
  }
}
