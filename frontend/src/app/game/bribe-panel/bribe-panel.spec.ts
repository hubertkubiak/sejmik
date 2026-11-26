import { TestBed } from '@angular/core/testing';
import { BribePanel, BribeRequest } from './bribe-panel';
import { testDeputy } from '../test-data';

describe('BribePanel', () => {
  async function render(budget: number) {
    const fixture = TestBed.createComponent(BribePanel);
    fixture.componentRef.setInput('deputy', testDeputy());
    fixture.componentRef.setInput('budget', budget);
    const sent: BribeRequest[] = [];
    fixture.componentInstance.bribe.subscribe((r) => sent.push(r));
    await fixture.whenStable();
    return { fixture, sent };
  }

  async function typeAmount(fixture: Awaited<ReturnType<typeof render>>['fixture'], value: string) {
    const input: HTMLInputElement = fixture.nativeElement.querySelector('input');
    input.value = value;
    input.dispatchEvent(new Event('input'));
    input.dispatchEvent(new Event('blur'));
    await fixture.whenStable();
  }

  it('emits the bribe for the selected deputy', async () => {
    const { fixture, sent } = await render(100);
    await typeAmount(fixture, '40');

    fixture.nativeElement.querySelector('form').dispatchEvent(new Event('submit'));
    await fixture.whenStable();

    expect(sent).toEqual([{ deputyId: 3, amount: 40 }]);
  });

  it('blocks an amount above the treasury and says why', async () => {
    const { fixture, sent } = await render(30);
    await typeAmount(fixture, '50');

    expect(fixture.nativeElement.querySelector('button[type="submit"]').disabled).toBe(true);
    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('Masz tylko 30 dukatów');
    expect(sent).toEqual([]);
  });

  it('blocks a non-positive amount', async () => {
    const { fixture } = await render(100);
    await typeAmount(fixture, '0');

    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('Kwota musi być dodatnia');
  });
});
