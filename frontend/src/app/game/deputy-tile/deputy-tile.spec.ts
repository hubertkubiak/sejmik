import { TestBed } from '@angular/core/testing';
import { DeputyTile } from './deputy-tile';
import { testDeputy } from '../test-data';

describe('DeputyTile', () => {
  function render(support: number | null) {
    const fixture = TestBed.createComponent(DeputyTile);
    fixture.componentRef.setInput('deputy', testDeputy({ support, vetoChancePercent: support === 0 ? 100 : 0 }));
    return fixture;
  }

  it('marks a deputy below 15 as a veto threat', async () => {
    const fixture = render(0);
    await fixture.whenStable();

    const button: HTMLButtonElement = fixture.nativeElement.querySelector('button');
    expect(button.classList).toContain('level-veto');
    expect(button.getAttribute('aria-label')).toContain('grozi VETO');
    expect(button.textContent).toContain('100%');
  });

  it('marks a deputy from 50 as voting FOR', async () => {
    const fixture = render(60);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('button').classList).toContain('level-for');
  });

  it('hides the support bar before a bill is selected', async () => {
    const fixture = render(null);
    await fixture.whenStable();

    expect(fixture.nativeElement.querySelector('.support-bar')).toBeNull();
  });

  it('emits its id when clicked', async () => {
    const fixture = render(10);
    const picked: number[] = [];
    fixture.componentInstance.picked.subscribe((id) => picked.push(id));
    await fixture.whenStable();

    fixture.nativeElement.querySelector('button').click();

    expect(picked).toEqual([3]);
  });
});
