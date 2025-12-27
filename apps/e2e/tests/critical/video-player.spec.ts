import { test, expect } from '../../fixtures/base';
import { mockBackend } from '../../fixtures/mocked';

test.describe('Video Player @critical', () => {
  test.beforeEach(async ({ page }) => {
    await mockBackend(page);
  });

  test('loads video player page', async ({ videoPlayerPage }) => {
    await videoPlayerPage.goto('1');
    await videoPlayerPage.expectVideoVisible();
  });

  test('can play and pause video', async ({ videoPlayerPage }) => {
    await videoPlayerPage.goto('1');
    await videoPlayerPage.expectVideoVisible();

    await videoPlayerPage.play();
    await videoPlayerPage.expectPlaying();

    await videoPlayerPage.pause();
    await videoPlayerPage.expectPaused();
  });

  test('shows controls on hover', async ({ page, videoPlayerPage }) => {
    await videoPlayerPage.goto('1');
    await page.locator('.player-wrapper').hover();
    await expect(videoPlayerPage.controls).toBeVisible();
  });

  test('can close player and return to list', async ({
    page,
    videoPlayerPage,
  }) => {
    await videoPlayerPage.goto('1');
    await page.locator('.player-wrapper').hover();
    await videoPlayerPage.close();
    await expect(page).toHaveURL('/');
  });
});

test.describe('Video Player Navigation @critical', () => {
  test.beforeEach(async ({ page }) => {
    await mockBackend(page);
  });

  test('navigates from movie list to player', async ({
    movieListPage,
    page,
  }) => {
    await movieListPage.goto();
    await movieListPage.clickMovieCard(0);
    await page.getByRole('button', { name: /play/i }).click();
    await expect(page).toHaveURL(/\/player\/\d+/);
  });
});
