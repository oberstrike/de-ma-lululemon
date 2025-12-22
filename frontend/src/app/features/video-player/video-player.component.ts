import { Component, OnInit, OnDestroy, ViewChild, ElementRef, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PlayerStore } from '../../store/player.store';
import { ButtonModule } from 'primeng/button';
import { Slider } from 'primeng/slider';
import { ProgressSpinner } from 'primeng/progressspinner';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-video-player',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonModule, Slider, ProgressSpinner, FormsModule],
  template: `
    <div
      class="player-wrapper"
      [class.fullscreen]="player.isFullscreen()"
      (mousemove)="onMouseMove()"
      (click)="player.togglePlay()"
    >
      @if (player.loading()) {
        <div class="loading-overlay">
          <p-progressspinner strokeWidth="4" />
          <p>Loading video...</p>
        </div>
      }

      @if (player.error()) {
        <div class="error-overlay" (click)="$event.stopPropagation()">
          <i class="pi pi-exclamation-triangle" style="font-size: 3rem; color: var(--p-red-500)"></i>
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
        (waiting)="isBuffering = true"
        (canplay)="isBuffering = false"
        playsinline
      ></video>

      @if (isBuffering && player.isPlaying()) {
        <div class="buffering-indicator">
          <p-progressspinner strokeWidth="4" />
        </div>
      }

      <div class="controls" [class.visible]="player.controlsVisible()" (click)="$event.stopPropagation()">
        <div class="progress-container" (click)="seekTo($event)">
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
              pTooltip="-10s"
            />
            <p-button
              icon="pi pi-step-forward"
              [rounded]="true"
              [text]="true"
              (click)="seekRelative(10)"
              pTooltip="+10s"
            />

            <div class="volume-control">
              <p-button
                [icon]="player.muted() || player.volume() === 0 ? 'pi pi-volume-off' : 'pi pi-volume-up'"
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
            <p-button
              icon="pi pi-times"
              [rounded]="true"
              [text]="true"
              routerLink="/"
            />
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
  styles: [`
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

    .loading-overlay, .error-overlay, .buffering-indicator {
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

    .left, .right {
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
  `]
})
export class VideoPlayerComponent implements OnInit, OnDestroy {
  @ViewChild('videoElement') videoRef!: ElementRef<HTMLVideoElement>;

  readonly player = inject(PlayerStore);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  isBuffering = false;
  volumeValue = 100;
  private controlsTimer: ReturnType<typeof setTimeout> | null = null;
  private syncEffect = effect(() => {
    const video = this.videoRef?.nativeElement;
    if (!video) return;

    if (this.player.isPlaying()) {
      video.play().catch(() => this.player.pause());
    } else {
      video.pause();
    }
  });

  async ngOnInit() {
    const movieId = this.route.snapshot.paramMap.get('id')!;
    await this.player.loadMovie(movieId);
    this.startControlsTimer();
    this.volumeValue = this.player.volume() * 100;
  }

  ngOnDestroy() {
    this.player.reset();
    if (this.controlsTimer) clearTimeout(this.controlsTimer);
  }

  onLoaded() {
    this.player.setDuration(this.videoRef.nativeElement.duration);
    this.videoRef.nativeElement.play();
  }

  onTimeUpdate() {
    const video = this.videoRef.nativeElement;
    this.player.setCurrentTime(video.currentTime);
    if (video.buffered.length > 0) {
      this.player.setBuffered(video.buffered.end(video.buffered.length - 1));
    }
  }

  onEnded() {
    this.player.pause();
    this.player.showControls();
  }

  onMouseMove() {
    this.player.showControls();
    this.startControlsTimer();
  }

  startControlsTimer() {
    if (this.controlsTimer) clearTimeout(this.controlsTimer);
    this.controlsTimer = setTimeout(() => {
      if (this.player.isPlaying()) {
        this.player.hideControls();
      }
    }, 3000);
  }

  togglePlay() {
    this.player.togglePlay();
  }

  seekTo(event: MouseEvent) {
    const rect = (event.target as HTMLElement).getBoundingClientRect();
    const percent = (event.clientX - rect.left) / rect.width;
    const time = percent * this.player.duration();
    this.videoRef.nativeElement.currentTime = time;
    this.player.setCurrentTime(time);
  }

  seekRelative(seconds: number) {
    const newTime = this.player.currentTime() + seconds;
    this.videoRef.nativeElement.currentTime = Math.max(0, Math.min(this.player.duration(), newTime));
  }

  onVolumeSliderChange(event: { value: number }) {
    const value = event.value / 100;
    this.player.setVolume(value);
    this.videoRef.nativeElement.volume = value;
  }

  toggleFullscreen() {
    const wrapper = this.videoRef.nativeElement.parentElement!;
    if (!document.fullscreenElement) {
      wrapper.requestFullscreen?.();
    } else {
      document.exitFullscreen?.();
    }
    this.player.toggleFullscreen();
  }

  retry(event: Event) {
    event.stopPropagation();
    const movieId = this.route.snapshot.paramMap.get('id')!;
    this.player.loadMovie(movieId);
  }
}
