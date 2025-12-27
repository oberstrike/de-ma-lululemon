import { test, expect } from '../../fixtures/base';
import { mockBackend, mockScenarios } from '../../fixtures/mocked';

test.describe('Movie List @smoke', () => {
  test.beforeEach(async ({ page }) => {
    await mockBackend(page);
  });

  test('displays movie list on homepage', async ({ movieListPage }) => {
    await movieListPage.goto();
    await movieListPage.expectMoviesVisible();
    const count = await movieListPage.getMovieCount();
    expect(count).toBeGreaterThan(0);
  });

  test('shows empty state when no movies', async ({ page, movieListPage }) => {
    await mockScenarios.emptyMovies(page);
    await movieListPage.goto();
    await movieListPage.expectEmptyState();
  });

  test('can search for movies', async ({ movieListPage }) => {
    await movieListPage.goto();
    await movieListPage.searchMovies('Test');
    await expect(movieListPage.movieCards.first()).toBeVisible();
  });

  test('can navigate to movie detail', async ({ page, movieListPage }) => {
    await movieListPage.goto();
    await movieListPage.clickMovieCard(0);
    await expect(page).toHaveURL(/\/movie\/\d+/);
  });
});

test.describe('Movie List Error States @smoke', () => {
  test('handles server error gracefully', async ({ page, movieListPage }) => {
    await mockScenarios.serverError(page);
    await movieListPage.goto();
    await expect(page.getByText(/error|failed/i)).toBeVisible();
  });

  test('handles network failure gracefully', async ({
    page,
    movieListPage,
  }) => {
    await mockScenarios.networkFailure(page);
    await movieListPage.goto();
    await expect(page.getByText(/error|offline|failed/i)).toBeVisible();
  });
});
