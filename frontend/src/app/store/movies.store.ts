import { computed, inject } from '@angular/core';
import { tapResponse } from '@ngrx/operators';
import { patchState, signalStore, withComputed, withMethods, withState } from '@ngrx/signals';
import { rxMethod } from '@ngrx/signals/rxjs-interop';
import { pipe, switchMap, tap } from 'rxjs';

import { ApiService, Movie, MovieCreateRequest } from '../services/api.service';

interface MoviesState {
  movies: Movie[];
  selectedMovieId: string | null;
  filter: string;
  loading: boolean;
  error: string | null;
}

interface MovieGroup {
  name: string;
  movies: Movie[];
}

function groupMoviesByCategory(movies: Movie[], filter: string): MovieGroup[] {
  const lowerFilter = filter.toLowerCase();
  const filtered = lowerFilter
    ? movies.filter((m) => m.title.toLowerCase().includes(lowerFilter))
    : movies;

  const groups: MovieGroup[] = [];
  const categorized = new Map<string, Movie[]>();
  const uncategorized: Movie[] = [];

  for (const movie of filtered) {
    if (movie.categoryName) {
      const existing = categorized.get(movie.categoryName) ?? [];
      existing.push(movie);
      categorized.set(movie.categoryName, existing);
    } else {
      uncategorized.push(movie);
    }
  }

  // Add favorites row first
  const favorites = filtered.filter((m) => m.favorite);
  if (favorites.length > 0) {
    groups.push({ name: 'My Favorites', movies: favorites });
  }

  // Add cached movies row second
  const cached = filtered.filter((m) => m.cached);
  if (cached.length > 0) {
    groups.push({ name: 'Downloaded on Server', movies: cached });
  }

  // Add category rows
  for (const [name, categoryMovies] of categorized) {
    groups.push({ name, movies: categoryMovies });
  }

  // Add uncategorized at the end
  if (uncategorized.length > 0) {
    groups.push({ name: 'Other Movies', movies: uncategorized });
  }

  return groups;
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

    moviesByCategory: computed(() => groupMoviesByCategory(state.movies(), state.filter())),

    featuredMovie: computed(() => selectFeaturedMovie(state.movies())),
  })),

  withMethods((store, api = inject(ApiService)) => {
    const loadMovies = rxMethod<void>(
      pipe(
        tap(() => patchState(store, { loading: true, error: null })),
        switchMap(() =>
          api.getMovies().pipe(
            tapResponse({
              next: (movies) => patchState(store, { movies, loading: false }),
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
