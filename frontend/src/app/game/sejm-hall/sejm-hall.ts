import { Component, computed, input, model, signal } from '@angular/core';
import { Deputy, Faction } from '../../core/api/models';
import { DeputyTile } from '../deputy-tile/deputy-tile';
import { FACTION_LABELS } from '../labels';

type FactionFilter = Faction | 'ALL';

/**
 * The Sejm hall: all deputies as tiles, with a faction filter.
 * `selectedId` is a model(): the parent binds it with [(selectedId)] (two-way binding).
 */
@Component({
  selector: 'sj-sejm-hall',
  imports: [DeputyTile],
  styleUrl: './sejm-hall.scss',
  templateUrl: './sejm-hall.html',
})
export class SejmHall {
  readonly deputies = input.required<Deputy[]>();
  readonly selectedId = model<number | undefined>(undefined);

  protected readonly filter = signal<FactionFilter>('ALL');
  protected readonly filters: { value: FactionFilter; label: string }[] = [
    { value: 'ALL', label: 'wszyscy' },
    ...(Object.keys(FACTION_LABELS) as Faction[]).map((f) => ({ value: f, label: FACTION_LABELS[f] })),
  ];

  protected readonly visible = computed(() => {
    const filter = this.filter();
    return filter === 'ALL' ? this.deputies() : this.deputies().filter((d) => d.faction === filter);
  });

  protected toggle(id: number): void {
    this.selectedId.update((current) => (current === id ? undefined : id));
  }
}
