import { Component, computed, input, output } from '@angular/core';
import { Deputy } from '../../core/api/models';
import { FACTION_LABELS, SUPPORT_LABELS, supportLevel } from '../labels';

/**
 * One deputy as a tile coloured by support. A real <button>, so it works with keyboard and screen readers.
 */
@Component({
  selector: 'sj-deputy-tile',
  styleUrl: './deputy-tile.scss',
  templateUrl: './deputy-tile.html',
})
export class DeputyTile {
  readonly deputy = input.required<Deputy>();
  readonly selected = input(false);
  readonly picked = output<number>();

  protected readonly level = computed(() => supportLevel(this.deputy().support));
  protected readonly factionLabel = computed(() => FACTION_LABELS[this.deputy().faction]);
  protected readonly supportLabel = computed(() => SUPPORT_LABELS[this.level()]);
  protected readonly ariaLabel = computed(() => {
    const d = this.deputy();
    const support = d.support === null ? 'poparcie nieznane' : `poparcie ${d.support}, ${this.supportLabel()}`;
    return `${d.fullName}, frakcja ${this.factionLabel()}, ${support}`;
  });
}
