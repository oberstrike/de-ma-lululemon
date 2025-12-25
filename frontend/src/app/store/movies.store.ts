import { computed, inject } from '@angular/core';
import { tapResponse } from '@ngrx/operators';
import { patchState, signalStore, withComputed, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap } from 'rxjs';

import { ApiService, Movie, MovieCreateRequest, MovieGroup } from '../services/api.service';

interface MoviesState {
  movies: Movie[];
  movieGroups: MovieGroup[];
  selectedMovieId: string | null;
  filter: string;
  loading: boolean;
  error: string | null;
}

function filterGroupsBySearch(groups: MovieGroup[], filter: string): MovieGroup[] {
  if (!filter) return groups;
  const lowerFilter = filter.toLowerCase();
  return groups
    .map((group) => ({
      ...group,
      movies: group.movies.filter((m) => m.title.toLowerCase().includes(lowerFilter)),
    }))
    .filter((group) => group.movies.length > 0);
}

function selectFeaturedMovie(movies: Movie[]): Movie | null {
  const cached = movies.filter((m) => m.cached);
  if (cached.length > 0) {
    return cached[Math.floor(Math.random() * cached.length)];
  }
  return movies[0] ?? null;
}

export const MoviesStore = signalStore(
  { providedIn: 'root' },

  withState<MoviesState>({
    movies: [],
    movieGroups: [],
    selectedMovieId: null,
    filter: '',
    loading: false,
    error: null,
  }),

  withComputed((state) => ({
    filteredMovies: computed(() => {
      const filter = state.filter().toLowerCase();
      if (!filter) return state.movies();
      return state.movies().filter((m) => m.title.toLowerCase().includes(filter));
    }),

    selectedMovie: computed(
      () => state.movies().find((m) => m.id === state.selectedMovieId()) ?? null
    ),

    readyMovies: computed(() => state.movies().filter((m) => m.status === 'READY')),

    downloadingMovies: computed(() => state.movies().filter((m) => m.status === 'DOWNLOADING')),

    cachedMovies: computed(() => state.movies().filter((m) => m.cached)),

    favoriteMovies: computed(() => state.movies().filter((m) => m.favorite)),

    moviesByCategory: computed(() => filterGroupsBySearch(state.movieGroups(), state.filter())),

    featuredMovie: computed(() => selectFeaturedMovie(state.movies())),
  })),

  withMethods((store, api = inject(ApiService)) => {
    const loadMovies = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() =>
          api.getMoviesGrouped().pipe(
            tapResponse({
              next: (groups) => {
                const movies = groups.flatMap((g) => g.movies);
                patchState(store, { movies, movieGroups: groups, loading: false });
              },
              error: (err: Error) => patchState(store, { error: err.message, loading: false }),
            })
          )
        )
      )
    );

    const createMovie = rxMethod<MovieCreateRequest>(
      pipe(
        switchMap((request) =>
          api.createMovie(request).pipe(
            tapResponse({
              next: (movie) => patchState(store, { movies: [...store.movies(), movie] }),
              error: (err: Error) => patchState(store, { error: err.message }),
            })
          )
        )
      )
    );

    const startDownload = rxMethod<string>(
      pipe(
        switchMap((movieId) =>
          api.startDownload(movieId).pipe(
            tap(() => {
              const movies = store
                .movies()
                .map((m) => (m.id === movieId ? { ...m, status: 'DOWNLOADING' as const } : m));
              patchState(store, { movies });
            }),
            tapResponse({
              next: () => undefined,
              error: (err: Error) => patchState(store, { error: err.message }),
            })
          )
        )
      )
    );

    const deleteMovie = rxMethod<string>(
      pipe(
        switchMap((id) =>
          api.deleteMovie(id).pipe(
            tap(() => {
              patchState(store, {
                movies: store.movies().filter((m) => m.id !== id),
                selectedMovieId: store.selectedMovieId() === id ? null : store.selectedMovieId(),
              });
            }),
            tapResponse({
              next: () => undefined,
              error: (err: Error) => patchState(store, { error: err.message }),
            })
          )
        )
      )
    );

    const addFavorite = rxMethod<string>(
      pipe(
        switchMap((movieId) =>
          api.addFavorite(movieId).pipe(
            tapResponse({
              next: (movie) => {
                const movies = store.movies().map((m) => (m.id === movieId ? movie : m));
                patchState(store, { movies });
              },
              error: (err: Error) => patchState(store, { error: err.message }),
            })
          )
        )
      )
    );

    const removeFavorite = rxMethod<string>(
      pipe(
        switchMap((movieId) =>
          api.removeFavorite(movieId).pipe(
            tapResponse({
              next: (movie) => {
                const movies = store.movies().map((m) => (m.id === movieId ? movie : m));
                patchState(store, { movies });
              },
              error: (err: Error) => patchState(store, { error: err.message }),
            })
          )
        )
      )
    );

    return {
      loadMovies,
      createMovie,
      startDownload,
      deleteMovie,
      addFavorite,
      removeFavorite,

      updateMovieStatus(movieId: string, status: Movie['status'], cached = false): void {
        const movies = store.movies().map((m) => (m.id === movieId ? { ...m, status, cached } : m));
        patchState(store, { movies });
      },

      selectMovie(id: string | null): void {
        patchState(store, { selectedMovieId: id });
      },

      setFilter(filter: string): void {
        patchState(store, { filter });
      },

      toggleFavorite(movieId: string): void {
        const movie = store.movies().find((m) => m.id === movieId);
        if (movie?.favorite) {
          removeFavorite(movieId);
        } else {
          addFavorite(movieId);
        }
      },
    };
  })
);
