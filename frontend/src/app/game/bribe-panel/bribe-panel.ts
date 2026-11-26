import { Component, computed, input, output, signal } from '@angular/core';
import { FormField, form, min, required, submit, validate } from '@angular/forms/signals';
import { Deputy } from '../../core/api/models';

export interface BribeRequest {
  deputyId: number;
  amount: number;
}

/**
 * The bribe form, built with Signal Forms: the form is derived from a signal model and validation
 * rules are declared in a schema function. The server validates again; this is only for comfort.
 */
@Component({
  selector: 'lv-bribe-panel',
  imports: [FormField],
  styleUrl: './bribe-panel.scss',
  templateUrl: './bribe-panel.html',
})
export class BribePanel {
  readonly deputy = input<Deputy | undefined>(undefined);
  readonly budget = input.required<number>();
  readonly busy = input(false);
  readonly bribe = output<BribeRequest>();

  protected readonly model = signal({ amount: 20 });

  protected readonly bribeForm = form(this.model, (path) => {
    required(path.amount, { message: 'Podaj kwotę' });
    min(path.amount, 1, { message: 'Kwota musi być dodatnia' });
    // a custom rule reading another signal: the budget changes after every bribe
    validate(path.amount, ({ value }) =>
      value() > this.budget()
        ? { kind: 'budget', message: `Masz tylko ${this.budget()} dukatów` }
        : undefined,
    );
  });

  protected readonly canSubmit = computed(
    () => this.deputy() !== undefined && this.bribeForm().valid() && !this.busy(),
  );

  protected onSubmit(event: Event): void {
    event.preventDefault();
    const deputy = this.deputy();
    if (!deputy) {
      return;
    }
    void submit(this.bribeForm, async () => {
      this.bribe.emit({ deputyId: deputy.id, amount: this.model().amount });
    });
  }
}
