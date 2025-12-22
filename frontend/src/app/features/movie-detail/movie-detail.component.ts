import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService, Movie, DownloadProgress } from '../../services/api.service';
import { MoviesStore } from '../../store/movies.store';
import { WebSocketService } from '../../services/websocket.service';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { ProgressBar } from 'primeng/progressbar';
import { ProgressSpinner } from 'primeng/progressspinner';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { ConfirmationService } from 'primeng/api';
import { Chip } from 'primeng/chip';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    ButtonModule,
    TagModule,
    ProgressBar,
    ProgressSpinner,
    ConfirmDialog,
    Chip
  ],
  providers: [ConfirmationService],
  template: `
    <div class="movie-detail">
      <p-button
        icon="pi pi-arrow-left"
        label="Back"
        [text]="true"
        routerLink="/"
        styleClass="back-btn"
      />

      @if (movie) {
        <div class="content">
          <div class="poster">
            @if (movie.thumbnailUrl) {
              <img [src]="movie.thumbnailUrl" [alt]="movie.title" />
            } @else {
              <div class="placeholder">
                <i class="pi pi-video" style="font-size: 5rem"></i>
              </div>
            }
          </div>

          <div class="info">
            <h1>{{ movie.title }}</h1>

            <div class="meta">
              @if (movie.year) {
                <p-chip [label]="movie.year.toString()" icon="pi pi-calendar" />
              }
              @if (movie.duration) {
                <p-chip [label]="movie.duration" icon="pi pi-clock" />
              }
              @if (movie.categoryName) {
                <p-chip [label]="movie.categoryName" icon="pi pi-tag" />
              }
            </div>

            @if (movie.description) {
              <p class="description">{{ movie.description }}</p>
            }

            <div class="status">
              <p-tag
                [value]="movie.status"
                [severity]="getStatusSeverity(movie.status)"
              />
              @if (movie.fileSize) {
                <span class="file-size">
                  <i class="pi pi-database"></i>
                  {{ formatSize(movie.fileSize) }}
                </span>
              }
            </div>

            @if (downloadProgress && movie.status === 'DOWNLOADING') {
              <div class="progress-section">
                <p-progressbar
                  [value]="downloadProgress.progress"
                  [showValue]="true"
                />
                <span class="progress-info">
                  {{ formatSize(downloadProgress.bytesDownloaded || 0) }} /
                  {{ formatSize(downloadProgress.totalBytes || 0) }}
                </span>
              </div>
            }

            <div class="actions">
              @if (movie.status === 'READY') {
                <p-button
                  icon="pi pi-play"
                  label="Play"
                  [routerLink]="['/play', movie.id]"
                />
              } @else if (movie.status === 'PENDING') {
                <p-button
                  icon="pi pi-download"
                  label="Download"
                  (click)="startDownload()"
                />
              } @else if (movie.status === 'DOWNLOADING') {
                <p-button
                  icon="pi pi-spin pi-spinner"
                  label="Downloading..."
                  [disabled]="true"
                />
              } @else if (movie.status === 'ERROR') {
                <p-button
                  icon="pi pi-refresh"
                  label="Retry Download"
                  (click)="startDownload()"
                />
              }

              <p-button
                icon="pi pi-trash"
                label="Delete"
                severity="danger"
                [outlined]="true"
                (click)="confirmDelete()"
              />
            </div>
          </div>
        </div>
      } @else {
        <div class="loading">
          <p-progressspinner strokeWidth="4" />
          <p>Loading movie details...</p>
        </div>
      }

      <p-confirmdialog />
    </div>
  `,
  styles: [`
    .movie-detail {
      padding: 2rem;
      max-width: 1000px;
      margin: 0 auto;
    }

    :host ::ng-deep .back-btn {
      margin-bottom: 1.5rem;
    }

    .content {
      display: grid;
      grid-template-columns: 300px 1fr;
      gap: 2rem;
    }

    .poster {
      aspect-ratio: 2/3;
      background: var(--p-surface-card);
      border-radius: var(--p-border-radius);
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
        color: var(--p-text-muted-color);
      }
    }

    .info {
      h1 {
        font-size: 2rem;
        margin: 0 0 1rem 0;
      }
    }

    .meta {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem;
      margin-bottom: 1.5rem;
    }

    .description {
      color: var(--p-text-muted-color);
      line-height: 1.6;
      margin-bottom: 1.5rem;
    }

    .status {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1.5rem;

      .file-size {
        color: var(--p-text-muted-color);
        display: flex;
        align-items: center;
        gap: 0.5rem;
      }
    }

    .progress-section {
      margin-bottom: 1.5rem;

      .progress-info {
        display: block;
        text-align: center;
        margin-top: 0.5rem;
        font-size: 0.875rem;
        color: var(--p-text-muted-color);
      }
    }

    .actions {
      display: flex;
      gap: 1rem;
    }

    .loading {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem;
      gap: 1rem;
      color: var(--p-text-muted-color);
    }

    @media (max-width: 768px) {
      .content {
        grid-template-columns: 1fr;
      }

      .poster {
        max-width: 250px;
        margin: 0 auto;
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
  private confirmationService = inject(ConfirmationService);

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

  confirmDelete() {
    this.confirmationService.confirm({
      message: 'Are you sure you want to delete this movie?',
      header: 'Delete Confirmation',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => this.deleteMovie()
    });
  }

  deleteMovie() {
    if (!this.movie) return;
    this.moviesStore.deleteMovie(this.movie.id);
    this.router.navigate(['/']);
  }

  getStatusSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    switch (status) {
      case 'READY': return 'success';
      case 'DOWNLOADING': return 'info';
      case 'PENDING': return 'warn';
      case 'ERROR': return 'danger';
      default: return 'secondary';
    }
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
  }
}
