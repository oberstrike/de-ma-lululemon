import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/movie-list/movie-list.component')
      .then(m => m.MovieListComponent)
  },
  {
    path: 'movie/:id',
    loadComponent: () => import('./features/movie-detail/movie-detail.component')
      .then(m => m.MovieDetailComponent)
  },
  {
    path: 'play/:id',
    loadComponent: () => import('./features/video-player/video-player.component')
      .then(m => m.VideoPlayerComponent)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
