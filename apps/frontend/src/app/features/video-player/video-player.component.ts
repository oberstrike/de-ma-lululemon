import {
  ChangeDetectionStrategy,
  Component,
  effect,
  ElementRef,
  inject,
  OnDestroy,
  OnInit,
  signal,
  ViewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Slider } from 'primeng/slider';

import { PlayerStore } from '../../store/player.store';

@Component({
  selector: 'app-video-player',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ButtonModule, Slider, ProgressSpinner, FormsModule],
  template: `
    <div
      class="player-wrapper"
      [class.fullscreen]="player.isFullscreen()"
      (mousemove)="onMouseMove()"
      (click)="player.togglePlay()"
      (keyup.space)="player.togglePlay()"
      tabindex="0"
      role="application"
      aria-label="Video player"
    >
      @if (player.loading()) {
        <div class="loading-overlay">
          <div class="loading-content">
            <div class="loading-spinner">
              <p-progressspinner strokeWidth="3" />
            </div>
            <p class="loading-text">Loading video...</p>
          </div>
        </div>
      }

      @if (player.error()) {
        <div
          class="error-overlay"
          (click)="$event.stopPropagation()"
          (keydown)="$event.stopPropagation()"
          tabindex="-1"
          role="dialog"
          aria-label="Video error"
        >
          <div class="error-content">
            <div class="error-icon">
              <i class="pi pi-exclamation-circle"></i>
            </div>
            <h3>Unable to Play Video</h3>
            <p>{{ player.error() }}</p>
            <div class="error-actions">
              <p-button
                icon="pi pi-refresh"
                label="Try Again"
                (click)="retry($event)"
                styleClass="retry-btn"
              />
              <p-button
                icon="pi pi-arrow-left"
                label="Back to Movies"
                routerLink="/"
                [outlined]="true"
              />
            </div>
          </div>
        </div>
      }

      <video
        #videoElement
        [src]="player.streamUrl()"
        (loadedmetadata)="onLoaded()"
        (timeupdate)="onTimeUpdate()"
        (ended)="onEnded()"
        (play)="player.play()"
        (pause)="player.pause()"
        (waiting)="isBuffering.set(true)"
        (canplay)="isBuffering.set(false)"
        playsinline
      ></video>

      @if (isBuffering() && player.isPlaying()) {
        <div class="buffering-indicator">
          <p-progressspinner strokeWidth="3" />
        </div>
      }

      <div
        class="controls"
        [class.visible]="player.controlsVisible()"
        (click)="$event.stopPropagation()"
        (keydown)="$event.stopPropagation()"
        role="toolbar"
        tabindex="-1"
        aria-label="Video controls"
      >
        <div
          class="progress-container"
          (click)="seekTo($event)"
          (keydown.arrowRight)="seekRelative(5)"
          (keydown.arrowLeft)="seekRelative(-5)"
          tabindex="0"
          role="slider"
          [attr.aria-valuenow]="player.progress()"
          aria-valuemin="0"
          aria-valuemax="100"
          aria-label="Video progress"
        >
          <div class="buffered" [style.width.%]="player.bufferedPercent()"></div>
          <div class="progress" [style.width.%]="player.progress()"></div>
          <div class="scrubber" [style.left.%]="player.progress()"></div>
        </div>

        <div class="controls-row">
          <div class="left">
            <p-button
              [icon]="player.isPlaying() ? 'pi pi-pause' : 'pi pi-play'"
              [rounded]="true"
              [text]="true"
              (click)="togglePlay()"
              [attr.aria-label]="player.isPlaying() ? 'Pause' : 'Play'"
            />
            <p-button
              icon="pi pi-step-backward"
              [rounded]="true"
              [text]="true"
              (click)="seekRelative(-10)"
              aria-label="Rewind 10 seconds"
            />
            <p-button
              icon="pi pi-step-forward"
              [rounded]="true"
              [text]="true"
              (click)="seekRelative(10)"
              aria-label="Forward 10 seconds"
            />

            <div class="volume-control">
              <p-button
                [icon]="
                  player.muted() || player.volume() === 0 ? 'pi pi-volume-off' : 'pi pi-volume-up'
                "
                [rounded]="true"
                [text]="true"
                (click)="player.toggleMute()"
                [attr.aria-label]="player.muted() ? 'Unmute' : 'Mute'"
              />
              <p-slider
                [(ngModel)]="volumeValue"
                [min]="0"
                [max]="100"
                (onChange)="onVolumeSliderChange($event)"
                styleClass="volume-slider"
                aria-label="Volume"
              />
            </div>

            <span class="time">
              {{ player.formattedTime() }} / {{ player.formattedDuration() }}
            </span>
          </div>

          <div class="right">
            <p-button
              icon="pi pi-times"
              [rounded]="true"
              [text]="true"
              routerLink="/"
              aria-label="Close player"
            />
            <p-button
              [icon]="player.isFullscreen() ? 'pi pi-window-minimize' : 'pi pi-window-maximize'"
              [rounded]="true"
              [text]="true"
              (click)="toggleFullscreen()"
              [attr.aria-label]="player.isFullscreen() ? 'Exit fullscreen' : 'Enter fullscreen'"
            />
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [
    `
      .player-wrapper {
        position: fixed;
        inset: 0;
        background: var(--black);
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: none;

        &:hover {
          cursor: default;
        }
      }

      video {
        max-width: 100%;
        max-height: 100%;
        width: 100%;
        height: 100%;
        object-fit: contain;
      }

      /* Loading Overlay */
      .loading-overlay {
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--overlay-dark);
        z-index: 10;
        animation: fade-in var(--transition-default);
      }

      .loading-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        gap: var(--space-lg);
      }

      .loading-spinner {
        :host ::ng-deep .p-progress-spinner {
          width: 60px;
          height: 60px;

          .p-progress-spinner-circle {
            stroke: var(--primary);
          }
        }
      }

      .loading-text {
        font-size: 1.1rem;
        color: var(--text-secondary);
        margin: 0;
      }

      /* Error Overlay */
      .error-overlay {
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--overlay-dark);
        z-index: 10;
        animation: fade-in var(--transition-default);
      }

      .error-content {
        display: flex;
        flex-direction: column;
        align-items: center;
        text-align: center;
        gap: var(--space-lg);
        max-width: 400px;
        padding: var(--space-xl);

        h3 {
          font-size: 1.5rem;
          font-weight: 600;
          margin: 0;
          color: var(--text-primary);
        }

        p {
          color: var(--text-secondary);
          margin: 0;
          line-height: 1.5;
        }
      }

      .error-icon {
        width: 80px;
        height: 80px;
        border-radius: var(--radius-full);
        background: var(--primary-alpha-15);
        display: flex;
        align-items: center;
        justify-content: center;

        i {
          font-size: 2.5rem;
          color: var(--primary);
        }
      }

      .error-actions {
        display: flex;
        gap: var(--space-md);
        margin-top: var(--space-sm);

        :host ::ng-deep .retry-btn {
          background: var(--primary);
          border-color: var(--primary);

          &:hover {
            background: var(--primary-hover) !important;
            border-color: var(--primary-hover) !important;
          }
        }
      }

      /* Buffering Indicator */
      .buffering-indicator {
        position: absolute;
        inset: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        background: var(--overlay-medium);
        z-index: 5;

        :host ::ng-deep .p-progress-spinner {
          width: 50px;
          height: 50px;

          .p-progress-spinner-circle {
            stroke: var(--primary);
          }
        }
      }

      /* Controls */
      .controls {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: var(--space-lg) var(--space-xl);
        background: linear-gradient(
          transparent,
          var(--overlay-dark-70) 30%,
          var(--overlay-dark-95)
        );
        opacity: 0;
        transform: translateY(10px);
        transition:
          opacity var(--transition-default),
          transform var(--transition-default);

        &.visible {
          opacity: 1;
          transform: translateY(0);
        }
      }

      /* Progress Bar */
      .progress-container {
        height: 5px;
        background: var(--white-15);
        border-radius: var(--radius-full);
        cursor: pointer;
        position: relative;
        margin-bottom: var(--space-md);
        transition: height var(--transition-fast);

        &:hover {
          height: 8px;

          .scrubber {
            transform: translateX(-50%) translateY(-50%) scale(1);
            opacity: 1;
          }
        }
      }

      .buffered {
        position: absolute;
        height: 100%;
        background: var(--white-25);
        border-radius: var(--radius-full);
        transition: width 0.1s linear;
      }

      .progress {
        position: absolute;
        height: 100%;
        background: var(--primary);
        border-radius: var(--radius-full);
        transition: width 0.1s linear;
      }

      .scrubber {
        position: absolute;
        top: 50%;
        width: 16px;
        height: 16px;
        background: var(--primary);
        border: 2px solid var(--text-primary);
        border-radius: var(--radius-full);
        transform: translateX(-50%) translateY(-50%) scale(0);
        opacity: 0;
        transition: all var(--transition-fast);
        box-shadow: 0 2px 8px var(--overlay-medium-50);
      }

      /* Controls Row */
      .controls-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .left,
      .right {
        display: flex;
        align-items: center;
        gap: var(--space-xs);
      }

      .volume-control {
        display: flex;
        align-items: center;
        gap: var(--space-sm);
        opacity: 0.8;
        transition: opacity var(--transition-fast);

        &:hover {
          opacity: 1;
        }

        :host ::ng-deep .volume-slider {
          width: 100px;

          .p-slider {
            background: var(--white-20);
          }

          .p-slider-range {
            background: var(--primary);
          }

          .p-slider-handle {
            width: 14px;
            height: 14px;
            background: var(--text-primary);
            border: none;
            box-shadow: 0 2px 4px var(--overlay-light-30);
            transition: transform var(--transition-fast);

            &:hover {
              transform: scale(1.2);
            }
          }
        }
      }

      .time {
        font-size: 0.9rem;
        color: var(--text-secondary);
        margin-left: var(--space-md);
        font-variant-numeric: tabular-nums;
        letter-spacing: 0.5px;
      }

      :host ::ng-deep .p-button {
        color: var(--text-primary);
        width: 44px;
        height: 44px;
        border-radius: var(--radius-full);
        transition: all var(--transition-fast);

        &:hover {
          background: var(--white-15) !important;
          transform: scale(1.1);
        }

        &:active {
          transform: scale(1);
        }

        .p-button-icon {
          font-size: 1.2rem;
        }
      }

      /* Responsive */
      @media (max-width: 768px) {
        .controls {
          padding: var(--space-md);
        }

        .volume-control {
          display: none;
        }

        :host ::ng-deep .p-button {
          width: 40px;
          height: 40px;
        }
      }
    `,
  ],
})
export class VideoPlayerComponent implements OnInit, OnDestroy {
  @ViewChild('videoElement') videoRef!: ElementRef<HTMLVideoElement>;

  readonly player = inject(PlayerStore);
  private readonly route = inject(ActivatedRoute);

  readonly isBuffering = signal(false);
  volumeValue = 100;
  private controlsTimer: ReturnType<typeof setTimeout> | null = null;

  private readonly syncEffect = effect(() => {
    const video = this.videoRef?.nativeElement;
    if (video === undefined) return;

    if (this.player.isPlaying()) {
      video.play().catch(() => {
        this.player.pause();
      });
    } else {
      video.pause();
    }
  });

  async ngOnInit(): Promise<void> {
    const movieId = this.route.snapshot.paramMap.get('id');
    if (!movieId) return;
    await this.player.loadMovie(movieId);
    this.startControlsTimer();
    this.volumeValue = this.player.volume() * 100;
  }

  ngOnDestroy(): void {
    this.player.reset();
    if (this.controlsTimer !== null) clearTimeout(this.controlsTimer);
  }

  onLoaded(): void {
    this.player.setDuration(this.videoRef.nativeElement.duration);
    void this.videoRef.nativeElement.play();
  }

  onTimeUpdate(): void {
    const video = this.videoRef.nativeElement;
    this.player.setCurrentTime(video.currentTime);
    if (video.buffered.length > 0) {
      this.player.setBuffered(video.buffered.end(video.buffered.length - 1));
    }
  }

  onEnded(): void {
    this.player.pause();
    this.player.showControls();
  }

  onMouseMove(): void {
    this.player.showControls();
    this.startControlsTimer();
  }

  startControlsTimer(): void {
    if (this.controlsTimer !== null) clearTimeout(this.controlsTimer);
    this.controlsTimer = setTimeout(() => {
      if (this.player.isPlaying()) {
        this.player.hideControls();
      }
    }, 3000);
  }

  togglePlay(): void {
    this.player.togglePlay();
  }

  seekTo(event: MouseEvent): void {
    const rect = (event.target as HTMLElement).getBoundingClientRect();
    const percent = (event.clientX - rect.left) / rect.width;
    const time = percent * this.player.duration();
    this.videoRef.nativeElement.currentTime = time;
    this.player.setCurrentTime(time);
  }

  seekRelative(seconds: number): void {
    const newTime = this.player.currentTime() + seconds;
    const clampedTime = Math.max(0, Math.min(this.player.duration(), newTime));
    this.videoRef.nativeElement.currentTime = clampedTime;
    this.player.setCurrentTime(clampedTime);
  }

  onVolumeSliderChange(event: { value?: number }): void {
    const value = (event.value ?? 0) / 100;
    this.player.setVolume(value);
    this.videoRef.nativeElement.volume = value;
  }

  toggleFullscreen(): void {
    const wrapper = this.videoRef.nativeElement.parentElement;
    if (!wrapper) return;
    if (!document.fullscreenElement) {
      void wrapper.requestFullscreen?.();
    } else {
      void document.exitFullscreen?.();
    }
    this.player.toggleFullscreen();
  }

  retry(event: Event): void {
    event.stopPropagation();
    const movieId = this.route.snapshot.paramMap.get('id');
    if (movieId) {
      void this.player.loadMovie(movieId);
    }
  }
}
