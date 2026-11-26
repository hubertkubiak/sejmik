import { TestBed } from '@angular/core/testing';
import { VoteResult } from './vote-result';
import { testResult } from '../test-data';

describe('VoteResult', () => {
  it('shouts "Nie pozwalam!" and names the vetoer when the Sejm is dissolved', async () => {
    const fixture = TestBed.createComponent(VoteResult);
    fixture.componentRef.setInput('result', testResult());
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Nie pozwalam!');
    expect(text).toContain('Kazimierz Sapieha h. Ostoja');
    expect(text).toContain('VETO: 3');
    expect(fixture.nativeElement.querySelector('[role="status"]').classList).toContain('dissolved');
  });

  it('celebrates a passed bill', async () => {
    const fixture = TestBed.createComponent(VoteResult);
    fixture.componentRef.setInput('result', testResult({ dissolved: false, passed: true, vetoer: null, vetoes: 0 }));
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Vivat! Ustawa przyjęta');
    expect(fixture.nativeElement.textContent).not.toContain('Nie pozwalam!');
  });
});
