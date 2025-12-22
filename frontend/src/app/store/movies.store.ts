import { computed, inject } from '@angular/core';
import { signalStore, withState, withComputed, withMethods, patchState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap } from 'rxjs';
import { tapResponse } from '@ngrx/operators';
import { ApiService, Movie, MovieCreateRequest } from '../services/api.service';

interface MoviesState {
  movies: Movie[];
  selectedMovieId: string | null;
  filter: string;
  loading: boolean;
  error: string | null;
}

export const MoviesStore = signalStore(
  { providedIn: 'root' },

  withState<MoviesState>({
    movies: [],
    selectedMovieId: null,
    filter: '',
    loading: false,
    error: null,
  }),

  withComputed((state) => ({
    filteredMovies: computed(() => {
      const filter = state.filter().toLowerCase();
      if (!filter) return state.movies();
      return state.movies().filter(m => m.title.toLowerCase().includes(filter));
    }),

    selectedMovie: computed(() =>
      state.movies().find(m => m.id === state.selectedMovieId()) ?? null
    ),

    readyMovies: computed(() =>
      state.movies().filter(m => m.status === 'READY')
    ),

    downloadingMovies: computed(() =>
      state.movies().filter(m => m.status === 'DOWNLOADING')
    ),
  })),

  withMethods((store, api = inject(ApiService)) => ({
    loadMovies: rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() =>
          api.getMovies().pipe(
            tapResponse({
              next: (movies) => patchState(store, { movies, loading: false }),
              error: (error: Error) => patchState(store, { error: error.message, loading: false }),
            })
          )
        )
      )
    ),

    createMovie: rxMethod<MovieCreateRequest>(
      pipe(
        switchMap((request) =>
          api.createMovie(request).pipe(
            tapResponse({
              next: (movie) => patchState(store, { movies: [...store.movies(), movie] }),
              error: (error: Error) => patchState(store, { error: error.message }),
            })
          )
        )
      )
    ),

    startDownload: rxMethod<string>(
      pipe(
        switchMap((movieId) =>
          api.startDownload(movieId).pipe(
            tap(() => {
              const movies = store.movies().map(m =>
                m.id === movieId ? { ...m, status: 'DOWNLOADING' as const } : m
              );
              patchState(store, { movies });
            }),
            tapResponse({
              next: () => {},
              error: (error: Error) => patchState(store, { error: error.message }),
            })
          )
        )
      )
    ),

    updateMovieStatus(movieId: string, status: Movie['status'], cached = false) {
      const movies = store.movies().map(m =>
        m.id === movieId ? { ...m, status, cached } : m
      );
      patchState(store, { movies });
    },

    selectMovie(id: string | null) {
      patchState(store, { selectedMovieId: id });
    },

    setFilter(filter: string) {
      patchState(store, { filter });
    },

    deleteMovie: rxMethod<string>(
      pipe(
        switchMap((id) =>
          api.deleteMovie(id).pipe(
            tap(() => patchState(store, {
              movies: store.movies().filter(m => m.id !== id),
              selectedMovieId: store.selectedMovieId() === id ? null : store.selectedMovieId(),
            })),
            tapResponse({
              next: () => {},
              error: (error: Error) => patchState(store, { error: error.message }),
            })
          )
        )
      )
    ),
  }))
);
