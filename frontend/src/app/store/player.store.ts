import { computed, inject } from '@angular/core';
import { patchState, signalStore, withComputed, withMethods, withState } from '@ngrx/signals';
import { firstValueFrom } from 'rxjs';

import { ApiService, StreamInfo } from '../services/api.service';

interface PlayerState {
  currentMovieId: string | null;
  streamInfo: StreamInfo | null;
  isPlaying: boolean;
  currentTime: number;
  duration: number;
  buffered: number;
  volume: number;
  muted: boolean;
  isFullscreen: boolean;
  controlsVisible: boolean;
  loading: boolean;
  error: string | null;
}

export const PlayerStore = signalStore(
  { providedIn: 'root' },

  withState<PlayerState>({
    currentMovieId: null,
    streamInfo: null,
    isPlaying: false,
    currentTime: 0,
    duration: 0,
    buffered: 0,
    volume: 1,
    muted: false,
    isFullscreen: false,
    controlsVisible: true,
    loading: false,
    error: null,
  }),

  withComputed((state, api = inject(ApiService)) => ({
    progress: computed(() =>
      state.duration() > 0 ? (state.currentTime() / state.duration()) * 100 : 0
    ),

    bufferedPercent: computed(() =>
      state.duration() > 0 ? (state.buffered() / state.duration()) * 100 : 0
    ),

    formattedTime: computed(() => formatTime(state.currentTime())),
    formattedDuration: computed(() => formatTime(state.duration())),

    streamUrl: computed(() => {
      const movieId = state.currentMovieId();
      return movieId ? api.getStreamUrl(movieId) : null;
    }),
  })),

  withMethods((store, api = inject(ApiService)) => ({
    async loadMovie(movieId: string) {
      patchState(store, { loading: true, error: null, currentMovieId: movieId });

      try {
        const streamInfo = await firstValueFrom(api.getStreamInfo(movieId));
        patchState(store, { streamInfo, loading: false });
      } catch (error) {
        patchState(store, {
          error: error instanceof Error ? error.message : 'Failed to load stream',
          loading: false,
        });
      }
    },

    play() {
      patchState(store, { isPlaying: true });
    },
    pause() {
      patchState(store, { isPlaying: false });
    },
    togglePlay() {
      patchState(store, { isPlaying: !store.isPlaying() });
    },

    setCurrentTime(time: number) {
      patchState(store, { currentTime: time });
    },
    setDuration(duration: number) {
      patchState(store, { duration });
    },
    setBuffered(buffered: number) {
      patchState(store, { buffered });
    },

    seekTo(time: number) {
      patchState(store, { currentTime: Math.max(0, Math.min(store.duration(), time)) });
    },

    seekRelative(seconds: number) {
      const newTime = Math.max(0, Math.min(store.duration(), store.currentTime() + seconds));
      patchState(store, { currentTime: newTime });
    },

    setVolume(volume: number) {
      patchState(store, { volume: Math.max(0, Math.min(1, volume)), muted: false });
    },

    toggleMute() {
      patchState(store, { muted: !store.muted() });
    },
    toggleFullscreen() {
      patchState(store, { isFullscreen: !store.isFullscreen() });
    },
    showControls() {
      patchState(store, { controlsVisible: true });
    },
    hideControls() {
      patchState(store, { controlsVisible: false });
    },

    reset() {
      patchState(store, {
        currentMovieId: null,
        streamInfo: null,
        isPlaying: false,
        currentTime: 0,
        duration: 0,
        buffered: 0,
        isFullscreen: false,
        controlsVisible: true,
        error: null,
      });
    },
  }))
);

function formatTime(seconds: number): string {
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);
  return h > 0
    ? `${h}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
    : `${m}:${s.toString().padStart(2, '0')}`;
}
