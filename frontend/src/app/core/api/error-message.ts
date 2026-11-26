import { HttpErrorResponse } from '@angular/common/http';
import { ProblemDetail } from './models';

/**
 * Turns any error into a Polish sentence for the player. The API already sends Polish text in
 * ProblemDetail.detail (and per-field messages in `errors`), so we prefer that.
 */
export function errorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Wystąpił nieoczekiwany błąd';
  }
  if (error.status === 0) {
    return 'Brak połączenia z serwerem. Czy API działa na porcie 8080?';
  }
  const problem = error.error as Partial<ProblemDetail> | null;
  const fieldMessages = problem?.errors ? Object.values(problem.errors) : [];
  if (fieldMessages.length > 0) {
    return fieldMessages.join('. ');
  }
  return problem?.detail ?? `Błąd serwera (${error.status})`;
}
