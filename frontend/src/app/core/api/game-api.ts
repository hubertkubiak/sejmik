import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Bill,
  BillSelection,
  Deputy,
  DeputySort,
  Game,
  SejmResult,
  SupportChange,
  VoteResponse,
} from './models';

/**
 * Thin, typed wrapper around the REST API: one method per endpoint, no state.
 * HttpClient returns cold Observables, so nothing is sent until someone subscribes.
 */
@Service()
export class GameApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/api/games`;

  create(): Observable<Game> {
    return this.http.post<Game>(this.baseUrl, null);
  }

  get(gameId: string): Observable<Game> {
    return this.http.get<Game>(`${this.baseUrl}/${gameId}`);
  }

  deputies(gameId: string, sort: DeputySort = 'id'): Observable<Deputy[]> {
    const params = new HttpParams().set('sort', sort);
    return this.http.get<Deputy[]>(`${this.baseUrl}/${gameId}/deputies`, { params });
  }

  bills(gameId: string): Observable<Bill[]> {
    return this.http.get<Bill[]>(`${this.baseUrl}/${gameId}/bills`);
  }

  selectBill(gameId: string, billId: number): Observable<BillSelection> {
    return this.http.put<BillSelection>(`${this.baseUrl}/${gameId}/selected-bill`, { billId });
  }

  bribe(gameId: string, deputyId: number, amount: number): Observable<SupportChange> {
    return this.http.post<SupportChange>(`${this.baseUrl}/${gameId}/bribes`, { deputyId, amount });
  }

  vote(gameId: string): Observable<VoteResponse> {
    return this.http.post<VoteResponse>(`${this.baseUrl}/${gameId}/votes`, null);
  }

  history(gameId: string): Observable<SejmResult[]> {
    return this.http.get<SejmResult[]>(`${this.baseUrl}/${gameId}/history`);
  }
}
