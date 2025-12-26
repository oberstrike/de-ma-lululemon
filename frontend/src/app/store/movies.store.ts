import { computed, inject } from '@angular/core';
import {
  patchState,
  signalStore,
  withComputed,
  withHooks,
  withMethods,
  withState,
} from '@ngrx/signals';
import {
  addEntity,
  removeEntity,
  setAllEntities,
  updateEntity,
  withEntities,
} from '@ngrx/signals/entities';
import { firstValueFrom } from 'rxjs';

import { ApiService, Movie, MovieCreateRequest, MovieGroup } from '../services/api.service';

interface MoviesState {
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

  // Entity management for movies collection
  withEntities<Movie>(),

  // State
  withState<MoviesState>({
    movieGroups: [],
    selectedMovieId: null,
    filter: '',
    loading: false,
    error: null,
  }),

  withComputed((state) => ({
    // Alias for entities
    movies: computed(() => state.entities()),

    filteredMovies: computed(() => {
      const filter = state.filter().toLowerCase();
      if (!filter) return state.entities();
      return state.entities().filter((m) => m.title.toLowerCase().includes(filter));
    }),

    selectedMovie: computed(
      () => state.entities().find((m) => m.id === state.selectedMovieId()) ?? null
    ),

    readyMovies: computed(() => state.entities().filter((m) => m.status === 'READY')),

    downloadingMovies: computed(() => state.entities().filter((m) => m.status === 'DOWNLOADING')),

    cachedMovies: computed(() => state.entities().filter((m) => m.cached)),

    favoriteMovies: computed(() => state.entities().filter((m) => m.favorite)),

    moviesByCategory: computed(() => filterGroupsBySearch(state.movieGroups(), state.filter())),

    featuredMovie: computed(() => selectFeaturedMovie(state.entities())),

    isLoading: computed(() => state.loading()),
    hasError: computed(() => state.error() !== null),
  })),

  withMethods((store, api = inject(ApiService)) => ({
    async loadMovies(): Promise<void> {
      patchState(store, { loading: true, error: null });
      try {
        const groups = await firstValueFrom(api.getMoviesGrouped());
        const movies = groups.flatMap((g) => g.movies);
        patchState(store, setAllEntities(movies), { movieGroups: groups, loading: false });
      } catch (err) {
        patchState(store, {
          error: err instanceof Error ? err.message : 'Failed to load movies',
          loading: false,
        });
      }
    },

    async createMovie(request: MovieCreateRequest): Promise<void> {
      try {
        const movie = await firstValueFrom(api.createMovie(request));
        patchState(store, addEntity(movie));
      } catch (err) {
        patchState(store, {
          error: err instanceof Error ? err.message : 'Failed to create movie',
        });
      }
    },

    async startDownload(movieId: string): Promise<void> {
      // Optimistic update
      patchState(store, updateEntity({ id: movieId, changes: { status: 'DOWNLOADING' as const } }));
      try {
        await firstValueFrom(api.startDownload(movieId));
      } catch (err) {
        // Revert on error
        patchState(store, updateEntity({ id: movieId, changes: { status: 'PENDING' as const } }));
        patchState(store, {
          error: err instanceof Error ? err.message : 'Failed to start download',
        });
      }
    },

    async deleteMovie(id: string): Promise<void> {
      try {
        await firstValueFrom(api.deleteMovie(id));
        patchState(store, removeEntity(id));
        if (store.selectedMovieId() === id) {
          patchState(store, { selectedMovieId: null });
        }
      } catch (err) {
        patchState(store, {
          error: err instanceof Error ? err.message : 'Failed to delete movie',
        });
      }
    },

    async addFavorite(movieId: string): Promise<void> {
      try {
        const movie = await firstValueFrom(api.addFavorite(movieId));
        patchState(store, updateEntity({ id: movieId, changes: movie }));
      } catch (err) {
        patchState(store, {
          error: err instanceof Error ? err.message : 'Failed to add favorite',
        });
      }
    },

    async removeFavorite(movieId: string): Promise<void> {
      try {
        const movie = await firstValueFrom(api.removeFavorite(movieId));
        patchState(store, updateEntity({ id: movieId, changes: movie }));
      } catch (err) {
        patchState(store, {
          error: err instanceof Error ? err.message : 'Failed to remove favorite',
        });
      }
    },

    updateMovieStatus(movieId: string, status: Movie['status'], cached = false): void {
      patchState(store, updateEntity({ id: movieId, changes: { status, cached } }));
    },

    selectMovie(id: string | null): void {
      patchState(store, { selectedMovieId: id });
    },

    setFilter(filter: string): void {
      patchState(store, { filter });
    },

    toggleFavorite(movieId: string): void {
      const movie = store.entities().find((m) => m.id === movieId);
      if (movie?.favorite) {
        void this.removeFavorite(movieId);
      } else {
        void this.addFavorite(movieId);
      }
    },

    clearError(): void {
      patchState(store, { error: null });
    },
  })),

  // Lifecycle hooks - auto-load on store initialization
  withHooks({
    onInit(store) {
      void store.loadMovies();
    },
  })
);
