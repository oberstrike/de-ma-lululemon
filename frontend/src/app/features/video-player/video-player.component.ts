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
    >
      @if (player.loading()) {
        <div class="loading-overlay">
          <p-progressspinner strokeWidth="4" />
          <p>Loading video...</p>
        </div>
      }

      @if (player.error()) {
        <div
          class="error-overlay"
          (click)="$event.stopPropagation()"
          (keydown)="$event.stopPropagation()"
          tabindex="-1"
          role="dialog"
        >
          <i
            class="pi pi-exclamation-triangle"
            style="font-size: 3rem; color: var(--p-red-500)"
          ></i>
          <p>{{ player.error() }}</p>
          <div class="error-actions">
            <p-button icon="pi pi-refresh" label="Retry" (click)="retry($event)" />
            <p-button icon="pi pi-home" label="Back to Movies" routerLink="/" [outlined]="true" />
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
          <p-progressspinner strokeWidth="4" />
        </div>
      }

      <div
        class="controls"
        [class.visible]="player.controlsVisible()"
        (click)="$event.stopPropagation()"
        (keydown)="$event.stopPropagation()"
        role="toolbar"
        tabindex="-1"
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
            />
            <p-button
              icon="pi pi-step-backward"
              [rounded]="true"
              [text]="true"
              (click)="seekRelative(-10)"
            />
            <p-button
              icon="pi pi-step-forward"
              [rounded]="true"
              [text]="true"
              (click)="seekRelative(10)"
            />

            <div class="volume-control">
              <p-button
                [icon]="
                  player.muted() || player.volume() === 0 ? 'pi pi-volume-off' : 'pi pi-volume-up'
                "
                [rounded]="true"
                [text]="true"
                (click)="player.toggleMute()"
              />
              <p-slider
                [(ngModel)]="volumeValue"
                [min]="0"
                [max]="100"
                (onChange)="onVolumeSliderChange($event)"
                styleClass="volume-slider"
              />
            </div>

            <span class="time">
              {{ player.formattedTime() }} / {{ player.formattedDuration() }}
            </span>
          </div>

          <div class="right">
            <p-button icon="pi pi-times" [rounded]="true" [text]="true" routerLink="/" />
            <p-button
              [icon]="player.isFullscreen() ? 'pi pi-window-minimize' : 'pi pi-window-maximize'"
              [rounded]="true"
              [text]="true"
              (click)="toggleFullscreen()"
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
        background: #000;
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

      .loading-overlay,
      .error-overlay,
      .buffering-indicator {
        position: absolute;
        inset: 0;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        background: rgba(0, 0, 0, 0.8);
        z-index: 10;
        gap: 1rem;
      }

      .error-overlay {
        p {
          margin: 1rem 0;
          font-size: 1.25rem;
        }
      }

      .error-actions {
        display: flex;
        gap: 1rem;
      }

      .controls {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        padding: 1rem 1.5rem;
        background: linear-gradient(transparent, rgba(0, 0, 0, 0.9));
        opacity: 0;
        transition: opacity 0.3s;

        &.visible {
          opacity: 1;
        }
      }

      .progress-container {
        height: 6px;
        background: rgba(255, 255, 255, 0.2);
        border-radius: 3px;
        cursor: pointer;
        position: relative;
        margin-bottom: 1rem;

        &:hover {
          height: 10px;

          .scrubber {
            transform: translateX(-50%) scale(1.5);
          }
        }
      }

      .buffered {
        position: absolute;
        height: 100%;
        background: rgba(255, 255, 255, 0.3);
        border-radius: 3px;
      }

      .progress {
        position: absolute;
        height: 100%;
        background: var(--p-primary-color);
        border-radius: 3px;
      }

      .scrubber {
        position: absolute;
        top: 50%;
        width: 14px;
        height: 14px;
        background: #fff;
        border-radius: 50%;
        transform: translateX(-50%) translateY(-50%);
        transition: transform 0.1s;
      }

      .controls-row {
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .left,
      .right {
        display: flex;
        align-items: center;
        gap: 0.25rem;
      }

      .volume-control {
        display: flex;
        align-items: center;
        gap: 0.5rem;

        :host ::ng-deep .volume-slider {
          width: 80px;

          .p-slider-handle {
            width: 12px;
            height: 12px;
          }
        }
      }

      .time {
        font-size: 0.875rem;
        color: var(--p-text-muted-color);
        margin-left: 1rem;
      }

      :host ::ng-deep .p-button {
        color: white;

        &:hover {
          background: rgba(255, 255, 255, 0.1) !important;
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
    this.videoRef.nativeElement.play();
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
    this.videoRef.nativeElement.currentTime = Math.max(
      0,
      Math.min(this.player.duration(), newTime)
    );
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
      wrapper.requestFullscreen?.();
    } else {
      document.exitFullscreen?.();
    }
    this.player.toggleFullscreen();
  }

  retry(event: Event): void {
    event.stopPropagation();
    const movieId = this.route.snapshot.paramMap.get('id');
    if (movieId) {
      this.player.loadMovie(movieId);
    }
  }
}
