import { Routes } from '@angular/router';

/**
 * Both pages are lazy-loaded: their code is downloaded only when the route is visited.
 */
export const routes: Routes = [
  {
    path: '',
    title: 'Liberum Veto',
    loadComponent: () => import('./start/start-page/start-page').then((m) => m.StartPage),
  },
  {
    path: 'games/:id',
    title: 'Liberum Veto: obrady sejmu',
    loadComponent: () => import('./game/game-page/game-page').then((m) => m.GamePage),
  },
  { path: '**', redirectTo: '' },
];
