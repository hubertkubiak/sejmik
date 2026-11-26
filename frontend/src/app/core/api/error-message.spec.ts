import { HttpErrorResponse } from '@angular/common/http';
import { errorMessage } from './error-message';

describe('errorMessage', () => {
  it('uses the Polish detail from a ProblemDetail', () => {
    const error = new HttpErrorResponse({
      status: 422,
      error: { title: 'Insufficient budget', status: 422, detail: 'Niewystarczający budżet' },
    });

    expect(errorMessage(error)).toBe('Niewystarczający budżet');
  });

  it('prefers per-field validation messages', () => {
    const error = new HttpErrorResponse({
      status: 400,
      error: {
        title: 'Validation failed',
        status: 400,
        detail: 'Popraw dane',
        errors: { amount: 'Kwota łapówki musi być dodatnia' },
      },
    });

    expect(errorMessage(error)).toBe('Kwota łapówki musi być dodatnia');
  });

  it('explains a missing connection', () => {
    expect(errorMessage(new HttpErrorResponse({ status: 0 }))).toContain('Brak połączenia');
  });

  it('handles non-HTTP errors', () => {
    expect(errorMessage(new Error('boom'))).toBe('Wystąpił nieoczekiwany błąd');
  });
});
