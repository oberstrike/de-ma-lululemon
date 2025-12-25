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

    cachedMovies: computed(() =>
      state.movies().filter(m => m.cached)
    ),

    favoriteMovies: computed(() =>
      state.movies().filter(m => m.favorite)
    ),

    moviesByCategory: computed(() => {
      const movies = state.movies();
      const filter = state.filter().toLowerCase();
      const filtered = filter
        ? movies.filter(m => m.title.toLowerCase().includes(filter))
        : movies;

      const groups: { name: string; movies: Movie[] }[] = [];
      const categorized = new Map<string, Movie[]>();
      const uncategorized: Movie[] = [];

      for (const movie of filtered) {
        if (movie.categoryName) {
          const existing = categorized.get(movie.categoryName) || [];
          existing.push(movie);
          categorized.set(movie.categoryName, existing);
        } else {
          uncategorized.push(movie);
        }
      }

      // Add favorites row first
      const favorites = filtered.filter(m => m.favorite);
      if (favorites.length > 0) {
        groups.push({ name: 'My Favorites', movies: favorites });
      }

      // Add cached movies row second
      const cached = filtered.filter(m => m.cached);
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
    }),

    featuredMovie: computed(() => {
      const movies = state.movies();
      // Pick a random cached movie as featured, or first movie
      const cached = movies.filter(m => m.cached);
      if (cached.length > 0) {
        return cached[Math.floor(Math.random() * cached.length)];
      }
      return movies[0] ?? null;
    }),
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
              next: () => { /* Success - no action needed */ },
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
              next: () => { /* Success - no action needed */ },
              error: (error: Error) => patchState(store, { error: error.message }),
            })
          )
        )
      )
    ),

    addFavorite: rxMethod<string>(
      pipe(
        switchMap((movieId) =>
          api.addFavorite(movieId).pipe(
            tapResponse({
              next: (movie) => {
                const movies = store.movies().map(m =>
                  m.id === movieId ? movie : m
                );
                patchState(store, { movies });
              },
              error: (error: Error) => patchState(store, { error: error.message }),
            })
          )
        )
      )
    ),

    removeFavorite: rxMethod<string>(
      pipe(
        switchMap((movieId) =>
          api.removeFavorite(movieId).pipe(
            tapResponse({
              next: (movie) => {
                const movies = store.movies().map(m =>
                  m.id === movieId ? movie : m
                );
                patchState(store, { movies });
              },
              error: (error: Error) => patchState(store, { error: error.message }),
            })
          )
        )
      )
    ),

    toggleFavorite(movieId: string) {
      const movie = store.movies().find(m => m.id === movieId);
      if (movie?.favorite) {
        this.removeFavorite(movieId);
      } else {
        this.addFavorite(movieId);
      }
    },
  }))
);
