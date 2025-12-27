import { test as base, expect } from '@playwright/test';
import { MovieListPage } from '../pages/movie-list.page';
import { MovieDetailPage } from '../pages/movie-detail.page';
import { VideoPlayerPage } from '../pages/video-player.page';

type Fixtures = {
  movieListPage: MovieListPage;
  movieDetailPage: MovieDetailPage;
  videoPlayerPage: VideoPlayerPage;
};

export const test = base.extend<Fixtures>({
  movieListPage: async ({ page }, use) => {
    await use(new MovieListPage(page));
  },
  movieDetailPage: async ({ page }, use) => {
    await use(new MovieDetailPage(page));
  },
  videoPlayerPage: async ({ page }, use) => {
    await use(new VideoPlayerPage(page));
  },
});

export { expect };
