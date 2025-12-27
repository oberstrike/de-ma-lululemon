import { Page, Locator, expect } from '@playwright/test';

export class MovieDetailPage {
  readonly page: Page;
  readonly title: Locator;
  readonly description: Locator;
  readonly playButton: Locator;
  readonly downloadButton: Locator;
  readonly favoriteButton: Locator;
  readonly backButton: Locator;
  readonly poster: Locator;
  readonly metaInfo: Locator;

  constructor(page: Page) {
    this.page = page;
    this.title = page.locator('[data-testid="movie-title"]');
    this.description = page.locator('[data-testid="movie-description"]');
    this.playButton = page.getByRole('button', { name: /play/i });
    this.downloadButton = page.getByRole('button', { name: /download/i });
    this.favoriteButton = page.getByRole('button', { name: /favorite/i });
    this.backButton = page.getByRole('button', { name: /back/i });
    this.poster = page.locator('[data-testid="movie-poster"]');
    this.metaInfo = page.locator('[data-testid="movie-meta"]');
  }

  async goto(movieId: string): Promise<void> {
    await this.page.goto(`/movie/${movieId}`);
  }

  async play(): Promise<void> {
    await this.playButton.click();
  }

  async download(): Promise<void> {
    await this.downloadButton.click();
  }

  async toggleFavorite(): Promise<void> {
    await this.favoriteButton.click();
  }

  async goBack(): Promise<void> {
    await this.backButton.click();
  }

  async expectTitle(title: string): Promise<void> {
    await expect(this.title).toContainText(title);
  }

  async expectLoaded(): Promise<void> {
    await expect(this.title).toBeVisible();
    await expect(this.poster).toBeVisible();
  }
}
