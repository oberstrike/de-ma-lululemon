import { Page, Locator, expect } from '@playwright/test';

export class MovieListPage {
  readonly page: Page;
  readonly movieCards: Locator;
  readonly searchInput: Locator;
  readonly categoryFilter: Locator;
  readonly emptyState: Locator;
  readonly loadingSpinner: Locator;
  readonly navBar: Locator;

  constructor(page: Page) {
    this.page = page;
    this.movieCards = page.locator('[data-testid="movie-card"]');
    this.searchInput = page.getByPlaceholder('Search movies');
    this.categoryFilter = page.locator('[data-testid="category-filter"]');
    this.emptyState = page.locator('[data-testid="empty-state"]');
    this.loadingSpinner = page.locator('p-progressspinner');
    this.navBar = page.locator('nav');
  }

  async goto(): Promise<void> {
    await this.page.goto('/');
  }

  async searchMovies(query: string): Promise<void> {
    await this.searchInput.fill(query);
  }

  async clickMovieCard(index = 0): Promise<void> {
    await this.movieCards.nth(index).click();
  }

  async getMovieCount(): Promise<number> {
    return this.movieCards.count();
  }

  async expectMoviesVisible(): Promise<void> {
    await expect(this.movieCards.first()).toBeVisible();
  }

  async expectEmptyState(): Promise<void> {
    await expect(this.emptyState).toBeVisible();
  }

  async expectLoading(): Promise<void> {
    await expect(this.loadingSpinner).toBeVisible();
  }

  async toggleFavorite(movieIndex = 0): Promise<void> {
    const card = this.movieCards.nth(movieIndex);
    await card.hover();
    await card.getByRole('button', { name: /favorite/i }).click();
  }

  async downloadMovie(movieIndex = 0): Promise<void> {
    const card = this.movieCards.nth(movieIndex);
    await card.hover();
    await card.getByRole('button', { name: /download/i }).click();
  }
}
