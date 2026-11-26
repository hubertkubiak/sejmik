import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { GameApi } from './game-api';
import { Game } from './models';

describe('GameApi', () => {
  let api: GameApi;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      // provideHttpClientTesting swaps the real backend for a fake one we control
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    api = TestBed.inject(GameApi);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify()); // fails if a test left a request unanswered

  it('creates a game with POST /api/games', async () => {
    const result = firstValueFrom(api.create());

    const request = http.expectOne('http://localhost:8080/api/games');
    expect(request.request.method).toBe('POST');
    request.flush({ id: 'g1', sejmNumber: 1 } as Game);

    expect((await result).id).toBe('g1');
  });

  it('asks for deputies with a sort parameter', () => {
    api.deputies('g1', 'support').subscribe();

    const request = http.expectOne((r) => r.url === 'http://localhost:8080/api/games/g1/deputies');
    expect(request.request.params.get('sort')).toBe('support');
    request.flush([]);
  });

  it('sends the bribe as a JSON body', () => {
    api.bribe('g1', 7, 30).subscribe();

    const request = http.expectOne('http://localhost:8080/api/games/g1/bribes');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ deputyId: 7, amount: 30 });
    request.flush({});
  });

  it('selects a bill with PUT', () => {
    api.selectBill('g1', 3).subscribe();

    const request = http.expectOne('http://localhost:8080/api/games/g1/selected-bill');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ billId: 3 });
    request.flush({ bill: {}, bribedDeputies: 2 });
  });
});
