import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed, toObservable } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ConfirmationService } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { Chip } from 'primeng/chip';
import { ConfirmDialog } from 'primeng/confirmdialog';
import { ProgressBar } from 'primeng/progressbar';
import { Skeleton } from 'primeng/skeleton';
import { TagModule } from 'primeng/tag';
import { combineLatest, filter, switchMap } from 'rxjs';

import { ApiService } from '../../services/api.service';
import { CurrentUserService } from '../../services/current-user.service';
import { NotificationService } from '../../services/notification.service';
import { WebSocketService } from '../../services/websocket.service';
import { MoviesStore } from '../../store/movies.store';
import { DownloadProgressResponse, MovieResponse } from '../../types';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ButtonModule, TagModule, ProgressBar, Skeleton, ConfirmDialog, Chip],
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

      @if (movie(); as m) {
        <div class="content animate-slide-up">
          <div class="poster">
            @if (m.thumbnailUrl) {
              <img [src]="m.thumbnailUrl" [alt]="m.title" />
            } @else {
              <div class="placeholder">
                <i class="pi pi-video"></i>
              </div>
            }
          </div>

          <div class="info">
            <h1>{{ m.title }}</h1>

            <div class="meta">
              @if (m.year) {
                <p-chip [label]="m.year.toString()" icon="pi pi-calendar" />
              }
              @if (m.duration) {
                <p-chip [label]="m.duration" icon="pi pi-clock" />
              }
              @if (m.categoryName) {
                <p-chip [label]="m.categoryName" icon="pi pi-tag" />
              }
            </div>

            @if (m.description) {
              <p class="description">{{ m.description }}</p>
            }

            <div class="status">
              <p-tag [value]="getStatusLabel(m)" [severity]="getStatusSeverity(m.status)" />
              @if (m.cached) {
                <p-chip label="Cached on Server" icon="pi pi-download" styleClass="cached-chip" />
              }
              @if (m.fileSize) {
                <span class="file-size">
                  <i class="pi pi-database"></i>
                  {{ formatSize(m.fileSize) }}
                </span>
              }
            </div>

            @if (downloadProgress(); as progress) {
              @if (m.status === 'DOWNLOADING') {
                <div class="progress-section">
                  <p-progressbar [value]="progress.progress" [showValue]="true" />
                  <span class="progress-info">
                    {{ formatSize(progress.bytesDownloaded || 0) }} /
                    {{ formatSize(progress.totalBytes || 0) }}
                  </span>
                </div>
              }
            }

            <div class="actions">
              @if (m.cached) {
                <p-button icon="pi pi-play" label="Play" [routerLink]="['/play', m.id]" />
                <p-button
                  icon="pi pi-times"
                  label="Clear Cache"
                  severity="secondary"
                  [outlined]="true"
                  (click)="confirmClearCache()"
                />
              } @else {
                @switch (m.status) {
                  @case ('PENDING') {
                    <p-button
                      icon="pi pi-download"
                      label="Download to Server"
                      (click)="startDownload()"
                    />
                  }
                  @case ('DOWNLOADING') {
                    <p-button
                      icon="pi pi-spin pi-spinner"
                      label="Downloading..."
                      [disabled]="true"
                    />
                  }
                  @case ('ERROR') {
                    <p-button
                      icon="pi pi-refresh"
                      label="Retry Download"
                      (click)="startDownload()"
                    />
                  }
                }
              }

              <p-button
                [icon]="m.favorite ? 'pi pi-heart-fill' : 'pi pi-heart'"
                [label]="m.favorite ? 'Remove Favorite' : 'Add Favorite'"
                [severity]="m.favorite ? 'danger' : 'secondary'"
                [outlined]="true"
                (click)="toggleFavorite()"
              />
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
        <!-- Skeleton Loading State -->
        <div class="content skeleton-loading">
          <div class="poster">
            <p-skeleton width="100%" height="100%" styleClass="poster-skeleton" />
          </div>
          <div class="info">
            <p-skeleton width="60%" height="40px" styleClass="title-skeleton" />
            <div class="meta">
              <p-skeleton width="80px" height="32px" borderRadius="16px" />
              <p-skeleton width="100px" height="32px" borderRadius="16px" />
              <p-skeleton width="120px" height="32px" borderRadius="16px" />
            </div>
            <p-skeleton width="100%" height="80px" styleClass="desc-skeleton" />
            <div class="status">
              <p-skeleton width="100px" height="28px" borderRadius="4px" />
              <p-skeleton width="150px" height="28px" borderRadius="16px" />
            </div>
            <div class="actions">
              <p-skeleton width="120px" height="44px" borderRadius="8px" />
              <p-skeleton width="140px" height="44px" borderRadius="8px" />
            </div>
          </div>
        </div>
      }

      <p-confirmdialog />
    </div>
  `,
  styles: [
    `
      .movie-detail {
        padding: var(--space-xl) var(--space-2xl);
        max-width: 1200px;
        margin: 0 auto;
        min-height: 100vh;
        background: var(--bg-secondary);
      }

      :host ::ng-deep .back-btn {
        margin-bottom: var(--space-xl);
        color: var(--text-secondary);
        transition: all var(--transition-fast);

        &:hover {
          color: var(--text-primary);
          background: var(--bg-tertiary) !important;
        }

        .p-button-label {
          font-weight: 500;
        }
      }

      .content {
        display: grid;
        grid-template-columns: 350px 1fr;
        gap: var(--space-2xl);
        align-items: start;
      }

      .poster {
        position: sticky;
        top: var(--space-xl);
        aspect-ratio: 2/3;
        background: var(--bg-tertiary);
        border-radius: var(--radius-lg);
        overflow: hidden;
        box-shadow: var(--shadow-lg);
        transition:
          transform var(--transition-default),
          box-shadow var(--transition-default);

        &:hover {
          transform: scale(1.02);
          box-shadow: var(--shadow-glow);
        }

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
          color: var(--text-muted);
          background: linear-gradient(135deg, var(--bg-tertiary), var(--bg-elevated));

          i {
            font-size: 5rem;
          }
        }
      }

      :host ::ng-deep .poster-skeleton {
        border-radius: var(--radius-lg);
      }

      .info {
        h1 {
          font-size: 2.5rem;
          font-weight: 700;
          margin: 0 0 var(--space-lg) 0;
          line-height: 1.2;
          letter-spacing: -0.5px;
          color: var(--text-primary);
        }
      }

      :host ::ng-deep .title-skeleton {
        margin-bottom: var(--space-lg);
        border-radius: var(--radius-sm);
      }

      .meta {
        display: flex;
        flex-wrap: wrap;
        gap: 0.75rem;
        margin-bottom: var(--space-xl);

        :host ::ng-deep .p-chip {
          background: var(--bg-tertiary);
          border: 1px solid var(--border-subtle);
          padding: var(--space-sm) var(--space-md);
          border-radius: var(--radius-full);
          transition: all var(--transition-fast);

          &:hover {
            background: var(--bg-elevated);
            border-color: var(--border-default);
          }

          .p-chip-icon {
            color: var(--primary);
          }

          .p-chip-text {
            color: var(--text-primary);
          }
        }
      }

      .description {
        color: var(--text-secondary);
        line-height: 1.8;
        font-size: 1.1rem;
        margin-bottom: var(--space-xl);
        max-width: 600px;
      }

      :host ::ng-deep .desc-skeleton {
        margin-bottom: var(--space-xl);
        border-radius: var(--radius-sm);
      }

      .status {
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: var(--space-md);
        margin-bottom: var(--space-xl);
        padding: 1.25rem;
        background: var(--bg-tertiary);
        border-radius: var(--radius-md);
        border: 1px solid var(--border-subtle);

        .file-size {
          color: var(--text-secondary);
          display: flex;
          align-items: center;
          gap: var(--space-sm);
          font-size: 0.95rem;

          i {
            color: var(--primary);
          }
        }
      }

      :host ::ng-deep .cached-chip {
        background: linear-gradient(135deg, var(--primary), var(--primary-dark));
        color: white;
        font-weight: 500;
        border: none;
      }

      .progress-section {
        margin-bottom: var(--space-xl);
        padding: var(--space-lg);
        background: var(--bg-tertiary);
        border-radius: var(--radius-md);
        border: 1px solid var(--border-subtle);

        :host ::ng-deep .p-progressbar {
          height: 8px;
          border-radius: var(--radius-full);
          background: var(--bg-elevated);

          .p-progressbar-value {
            background: linear-gradient(90deg, var(--primary), var(--primary-hover));
          }

          .p-progressbar-label {
            color: var(--text-primary);
            font-weight: 500;
          }
        }

        .progress-info {
          display: block;
          text-align: center;
          margin-top: 0.75rem;
          font-size: 0.9rem;
          color: var(--text-secondary);
        }
      }

      .actions {
        display: flex;
        flex-wrap: wrap;
        gap: var(--space-md);

        :host ::ng-deep .p-button {
          padding: 0.875rem var(--space-lg);
          font-weight: 500;
          border-radius: var(--radius-md);
          transition: all var(--transition-fast);

          &:hover:not(:disabled) {
            transform: translateY(-2px);
            box-shadow: var(--shadow-sm);
          }

          &:active:not(:disabled) {
            transform: translateY(0);
          }
        }
      }

      /* Skeleton loading state */
      .skeleton-loading {
        .meta {
          display: flex;
          gap: 0.75rem;
          margin-bottom: var(--space-xl);
        }

        .status {
          background: transparent;
          border: none;
          padding: 0;
          display: flex;
          gap: var(--space-md);
          margin-bottom: var(--space-xl);
        }

        .actions {
          display: flex;
          gap: var(--space-md);
        }
      }

      /* Confirm Dialog Styling */
      :host ::ng-deep .p-confirmdialog {
        .p-dialog {
          background: var(--bg-elevated);
          border-radius: var(--radius-lg);
          border: 1px solid var(--border-subtle);
          box-shadow: var(--shadow-lg);
        }

        .p-dialog-header {
          background: transparent;
          border-bottom: 1px solid var(--border-subtle);
          padding: var(--space-lg);
          color: var(--text-primary);
        }

        .p-dialog-content {
          padding: var(--space-lg);
          color: var(--text-secondary);
        }

        .p-dialog-footer {
          background: transparent;
          border-top: 1px solid var(--border-subtle);
          padding: var(--space-md) var(--space-lg);
          gap: 0.75rem;
        }
      }

      @media (max-width: 768px) {
        .movie-detail {
          padding: var(--space-md);
        }

        .content {
          grid-template-columns: 1fr;
          gap: var(--space-xl);
        }

        .poster {
          position: static;
          max-width: 280px;
          margin: 0 auto;
        }

        .info h1 {
          font-size: 1.75rem;
          text-align: center;
        }

        .meta {
          justify-content: center;
        }

        .description {
          text-align: center;
        }

        .status {
          justify-content: center;
        }

        .actions {
          justify-content: center;
        }
      }
    `,
  ],
})
export class MovieDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ApiService);
  private readonly moviesStore = inject(MoviesStore);
  private readonly ws = inject(WebSocketService);
  private readonly confirmationService = inject(ConfirmationService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly currentUser = inject(CurrentUserService);
  private readonly notification = inject(NotificationService);

  readonly movie = signal<MovieResponse | null>(null);
  readonly downloadProgress = signal<DownloadProgressResponse | null>(null);
  private readonly movieId = signal<string | null>(null);

  private readonly userId$ = toObservable(this.currentUser.userId);
  private readonly movieId$ = toObservable(this.movieId);

  readonly isDownloading = computed(() => this.movie()?.status === 'DOWNLOADING');

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      void this.router.navigate(['/']);
      return;
    }

    this.movieId.set(id);
    combineLatest([this.userId$, this.movieId$])
      .pipe(
        filter(([userId, movieId]) => Boolean(userId) && Boolean(movieId)),
        switchMap(([, movieId]) => this.api.getMovie(movieId ?? '')),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe({
        next: (movie) => {
          this.movie.set(movie);
        },
        error: () => void this.router.navigate(['/']),
      });

    this.ws.connect();
    this.ws
      .getDownloadProgress()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((progress) => {
        const currentMovie = this.movie();
        if (currentMovie && progress.movieId === currentMovie.id) {
          this.downloadProgress.set(progress);
          if (progress.status === 'COMPLETED') {
            this.movie.set({ ...currentMovie, status: 'READY', cached: true });
            this.notification.success('Download Complete', `${currentMovie.title} is ready`);
          } else if (progress.status === 'FAILED') {
            this.movie.set({ ...currentMovie, status: 'ERROR' });
            this.notification.error('Download Failed', 'Please try again');
          }
        }
      });
  }

  startDownload(): void {
    const currentMovie = this.movie();
    if (!currentMovie) return;
    this.movie.set({ ...currentMovie, status: 'DOWNLOADING' });
    this.moviesStore.startDownload(currentMovie.id);
    this.notification.info('Download Started', `Downloading ${currentMovie.title}...`);
  }

  toggleFavorite(): void {
    const currentMovie = this.movie();
    if (!currentMovie) return;

    const newFavorite = !currentMovie.favorite;
    this.movie.set({ ...currentMovie, favorite: newFavorite });

    if (newFavorite) {
      this.moviesStore.addFavorite(currentMovie.id);
      this.notification.success('Added to Favorites', currentMovie.title);
    } else {
      this.moviesStore.removeFavorite(currentMovie.id);
      this.notification.info('Removed from Favorites', currentMovie.title);
    }
  }

  confirmDelete(): void {
    this.confirmationService.confirm({
      message: 'Are you sure you want to delete this movie?',
      header: 'Delete Confirmation',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.deleteMovie();
      },
    });
  }

  deleteMovie(): void {
    const currentMovie = this.movie();
    if (!currentMovie) return;
    this.moviesStore.deleteMovie(currentMovie.id);
    this.notification.success('Movie Deleted', currentMovie.title);
    void this.router.navigate(['/']);
  }

  confirmClearCache(): void {
    this.confirmationService.confirm({
      message:
        'Remove this movie from server storage? The movie will still be available on Mega.nz for re-download.',
      header: 'Clear Cache',
      icon: 'pi pi-exclamation-triangle',
      accept: () => {
        this.clearCache();
      },
    });
  }

  clearCache(): void {
    const currentMovie = this.movie();
    if (!currentMovie) return;

    this.api
      .clearMovieCache(currentMovie.id)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.movie.set({
            ...currentMovie,
            status: 'PENDING',
            cached: false,
            fileSize: undefined,
          });
          this.moviesStore.updateMovieStatus(currentMovie.id, 'PENDING', false);
          this.notification.success('Cache Cleared', `${currentMovie.title} removed from server`);
        },
        error: (err: Error) => {
          this.notification.error('Failed to Clear Cache', err.message);
        },
      });
  }

  getStatusSeverity(
    status: string
  ): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    switch (status) {
      case 'READY':
        return 'success';
      case 'DOWNLOADING':
        return 'info';
      case 'PENDING':
        return 'warn';
      case 'ERROR':
        return 'danger';
      default:
        return 'secondary';
    }
  }

  getStatusLabel(movie: MovieResponse): string {
    if (movie.cached) {
      return 'On Server';
    }
    switch (movie.status) {
      case 'READY':
        return 'Ready';
      case 'DOWNLOADING':
        return 'Downloading';
      case 'PENDING':
        return 'On Mega';
      case 'ERROR':
        return 'Error';
      default:
        return movie.status;
    }
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
    return (bytes / (1024 * 1024 * 1024)).toFixed(2) + ' GB';
  }
}
