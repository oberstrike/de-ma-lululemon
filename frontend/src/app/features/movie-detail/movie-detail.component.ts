import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService, Movie, DownloadProgress } from '../../services/api.service';
import { MoviesStore } from '../../store/movies.store';
import { WebSocketService } from '../../services/websocket.service';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="movie-detail">
      <a routerLink="/" class="back-btn">&larr; Back</a>

      @if (movie) {
        <div class="content">
          <div class="poster">
            @if (movie.thumbnailUrl) {
              <img [src]="movie.thumbnailUrl" [alt]="movie.title" />
            } @else {
              <div class="placeholder">{{ movie.title.charAt(0) }}</div>
            }
          </div>

          <div class="info">
            <h1>{{ movie.title }}</h1>

            <div class="meta">
              @if (movie.year) { <span>{{ movie.year }}</span> }
              @if (movie.duration) { <span>{{ movie.duration }}</span> }
              @if (movie.categoryName) { <span>{{ movie.categoryName }}</span> }
            </div>

            @if (movie.description) {
              <p class="description">{{ movie.description }}</p>
            }

            <div class="status">
              <span class="badge" [class]="movie.status.toLowerCase()">
                {{ movie.status }}
              </span>
              @if (movie.fileSize) {
                <span class="file-size">{{ formatSize(movie.fileSize) }}</span>
              }
            </div>

            @if (downloadProgress && movie.status === 'DOWNLOADING') {
              <div class="progress-bar">
                <div class="progress" [style.width.%]="downloadProgress.progress"></div>
                <span>{{ downloadProgress.progress }}%</span>
              </div>
            }

            <div class="actions">
              @if (movie.status === 'READY') {
                <button class="btn primary" [routerLink]="['/play', movie.id]">
                  ▶ Play
                </button>
              } @else if (movie.status === 'PENDING') {
                <button class="btn primary" (click)="startDownload()">
                  ⬇ Download
                </button>
              } @else if (movie.status === 'DOWNLOADING') {
                <button class="btn" disabled>Downloading...</button>
              } @else if (movie.status === 'ERROR') {
                <button class="btn primary" (click)="startDownload()">
                  ↻ Retry Download
                </button>
              }

              <button class="btn danger" (click)="deleteMovie()">
                🗑 Delete
              </button>
            </div>
          </div>
        </div>
      } @else {
        <div class="loading">Loading...</div>
      }
    </div>
  `,
  styles: [`
    .movie-detail {
      padding: 2rem;
      max-width: 1000px;
      margin: 0 auto;
    }

    .back-btn {
      display: inline-block;
      color: var(--text-secondary);
      margin-bottom: 1.5rem;

      &:hover { color: #fff; }
    }

    .content {
      display: grid;
      grid-template-columns: 300px 1fr;
      gap: 2rem;
    }

    .poster {
      aspect-ratio: 2/3;
      background: var(--bg-card);
      border-radius: 8px;
      overflow: hidden;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      .placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 5rem;
        color: #555;
      }
    }

    .info {
      h1 {
        font-size: 2rem;
        margin-bottom: 1rem;
      }
    }

    .meta {
      display: flex;
      gap: 1rem;
      color: var(--text-secondary);
      margin-bottom: 1rem;
    }

    .description {
      color: var(--text-secondary);
      line-height: 1.6;
      margin-bottom: 1.5rem;
    }

    .status {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1.5rem;
    }

    .badge {
      padding: 4px 12px;
      border-radius: 4px;
      font-size: 0.875rem;
      font-weight: 600;

      &.ready { background: #22c55e; }
      &.downloading { background: #3b82f6; }
      &.pending { background: #f59e0b; }
      &.error { background: #ef4444; }
    }

    .file-size {
      color: var(--text-secondary);
    }

    .progress-bar {
      height: 24px;
      background: #333;
      border-radius: 4px;
      margin-bottom: 1.5rem;
      position: relative;
      overflow: hidden;

      .progress {
        height: 100%;
        background: var(--primary);
        transition: width 0.3s;
      }

      span {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        font-size: 0.875rem;
      }
    }

    .actions {
      display: flex;
      gap: 1rem;
    }

    .btn {
      padding: 0.75rem 1.5rem;
      border-radius: 4px;
      font-weight: 600;
      transition: background 0.2s;

      &.primary {
        background: var(--primary);
        &:hover { background: var(--primary-hover); }
      }

      &.danger {
        background: #333;
        &:hover { background: #ef4444; }
      }

      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
      }
    }

    .loading {
      text-align: center;
      padding: 3rem;
      color: var(--text-secondary);
    }

    @media (max-width: 768px) {
      .content {
        grid-template-columns: 1fr;
      }

      .poster {
        max-width: 250px;
      }
    }
  `]
})
export class MovieDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private api = inject(ApiService);
  private moviesStore = inject(MoviesStore);
  private ws = inject(WebSocketService);

  movie: Movie | null = null;
  downloadProgress: DownloadProgress | null = null;

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.api.getMovie(id).subscribe({
      next: movie => this.movie = movie,
      error: () => this.router.navigate(['/'])
    });

    this.ws.connect();
    this.ws.getDownloadProgress().subscribe(progress => {
      if (this.movie && progress.movieId === this.movie.id) {
        this.downloadProgress = progress;
        if (progress.status === 'COMPLETED') {
          this.movie = { ...this.movie, status: 'READY', cached: true };
        } else if (progress.status === 'FAILED') {
          this.movie = { ...this.movie, status: 'ERROR' };
        }
      }
    });
  }

  startDownload() {
    if (!this.movie) return;
    this.movie = { ...this.movie, status: 'DOWNLOADING' };
    this.moviesStore.startDownload(this.movie.id);
  }

  deleteMovie() {
    if (!this.movie || !confirm('Delete this movie?')) return;
    this.moviesStore.deleteMovie(this.movie.id);
    this.router.navigate(['/']);
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
  }
}
