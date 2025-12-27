import { Page, Locator, expect } from '@playwright/test';

export class VideoPlayerPage {
  readonly page: Page;
  readonly video: Locator;
  readonly playPauseButton: Locator;
  readonly progressBar: Locator;
  readonly volumeSlider: Locator;
  readonly fullscreenButton: Locator;
  readonly closeButton: Locator;
  readonly controls: Locator;
  readonly loadingOverlay: Locator;
  readonly errorOverlay: Locator;

  constructor(page: Page) {
    this.page = page;
    this.video = page.locator('video');
    this.playPauseButton = page.getByRole('button', { name: /play|pause/i });
    this.progressBar = page.locator('[data-testid="progress-bar"]');
    this.volumeSlider = page.locator('[data-testid="volume-slider"]');
    this.fullscreenButton = page.getByRole('button', {
      name: /fullscreen/i,
    });
    this.closeButton = page.getByRole('button', { name: /close/i });
    this.controls = page.locator('.controls');
    this.loadingOverlay = page.locator('.loading-overlay');
    this.errorOverlay = page.locator('.error-overlay');
  }

  async goto(movieId: string): Promise<void> {
    await this.page.goto(`/player/${movieId}`);
  }

  async play(): Promise<void> {
    await this.playPauseButton.click();
  }

  async pause(): Promise<void> {
    await this.playPauseButton.click();
  }

  async toggleFullscreen(): Promise<void> {
    await this.fullscreenButton.click();
  }

  async close(): Promise<void> {
    await this.closeButton.click();
  }

  async seekTo(percent: number): Promise<void> {
    const box = await this.progressBar.boundingBox();
    if (box) {
      const x = box.x + box.width * (percent / 100);
      await this.page.mouse.click(x, box.y + box.height / 2);
    }
  }

  async expectPlaying(): Promise<void> {
    await expect(this.video).toHaveJSProperty('paused', false);
  }

  async expectPaused(): Promise<void> {
    await expect(this.video).toHaveJSProperty('paused', true);
  }

  async expectLoading(): Promise<void> {
    await expect(this.loadingOverlay).toBeVisible();
  }

  async expectError(): Promise<void> {
    await expect(this.errorOverlay).toBeVisible();
  }

  async expectVideoVisible(): Promise<void> {
    await expect(this.video).toBeVisible();
  }
}
