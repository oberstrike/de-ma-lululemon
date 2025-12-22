import { Component, OnInit, OnDestroy, ViewChild, ElementRef, inject, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { PlayerStore } from '../../store/player.store';

@Component({
  selector: 'app-video-player',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div
      class="player-wrapper"
      [class.fullscreen]="player.isFullscreen()"
      (mousemove)="onMouseMove()"
      (click)="player.togglePlay()"
    >
      @if (player.loading()) {
        <div class="loading-overlay">
          <div class="spinner"></div>
          <p>Loading video...</p>
        </div>
      }

      @if (player.error()) {
        <div class="error-overlay">
          <p>{{ player.error() }}</p>
          <button (click)="retry($event)">Retry</button>
          <a routerLink="/" (click)="$event.stopPropagation()">Back to Movies</a>
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
          <div class="spinner"></div>
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
            <button class="control-btn" (click)="togglePlay()">
              {{ player.isPlaying() ? '⏸' : '▶' }}
            </button>
            <button class="control-btn" (click)="seekRelative(-10)">⏪</button>
            <button class="control-btn" (click)="seekRelative(10)">⏩</button>

            <div class="volume-control">
              <button class="control-btn" (click)="player.toggleMute()">
                {{ player.muted() || player.volume() === 0 ? '🔇' : '🔊' }}
              </button>
              <input
                type="range"
                min="0"
                max="1"
                step="0.1"
                [value]="player.muted() ? 0 : player.volume()"
                (input)="onVolumeChange($event)"
              />
            </div>

            <span class="time">
              {{ player.formattedTime() }} / {{ player.formattedDuration() }}
            </span>
          </div>

          <div class="right">
            <a class="control-btn" routerLink="/">✕</a>
            <button class="control-btn" (click)="toggleFullscreen()">⛶</button>
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
      background: rgba(0, 0, 0, 0.7);
      z-index: 10;
    }

    .spinner {
      width: 48px;
      height: 48px;
      border: 4px solid #333;
      border-top-color: var(--primary);
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      to { transform: rotate(360deg); }
    }

    .error-overlay {
      p { margin-bottom: 1rem; }
      button, a {
        padding: 0.5rem 1rem;
        background: var(--primary);
        border-radius: 4px;
        margin: 0.5rem;
      }
    }

    .controls {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      padding: 1rem;
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
      background: var(--primary);
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
      gap: 0.5rem;
    }

    .control-btn {
      width: 40px;
      height: 40px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
      border-radius: 4px;
      transition: background 0.2s;

      &:hover {
        background: rgba(255, 255, 255, 0.1);
      }
    }

    .volume-control {
      display: flex;
      align-items: center;

      input[type="range"] {
        width: 80px;
        accent-color: var(--primary);
      }
    }

    .time {
      font-size: 0.875rem;
      color: var(--text-secondary);
      margin-left: 1rem;
    }
  `]
})
export class VideoPlayerComponent implements OnInit, OnDestroy {
  @ViewChild('videoElement') videoRef!: ElementRef<HTMLVideoElement>;

  readonly player = inject(PlayerStore);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  isBuffering = false;
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

  onVolumeChange(event: Event) {
    const value = parseFloat((event.target as HTMLInputElement).value);
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
