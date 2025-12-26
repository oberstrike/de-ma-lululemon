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
import { ProgressSpinner } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';
import { combineLatest, filter, switchMap } from 'rxjs';

import { ApiService, DownloadProgress, Movie } from '../../services/api.service';
import { CurrentUserService } from '../../services/current-user.service';
import { WebSocketService } from '../../services/websocket.service';
import { MoviesStore } from '../../store/movies.store';

@Component({
  selector: 'app-movie-detail',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, ButtonModule, TagModule, ProgressBar, ProgressSpinner, ConfirmDialog, Chip],
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
        <div class="content">
          <div class="poster">
            @if (m.thumbnailUrl) {
              <img [src]="m.thumbnailUrl" [alt]="m.title" />
            } @else {
              <div class="placeholder">
                <i class="pi pi-video" style="font-size: 5rem"></i>
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
        <div class="loading">
          <p-progressspinner strokeWidth="4" />
          <p>Loading movie details...</p>
        </div>
      }

      <p-confirmdialog />
    </div>
  `,
  styles: [
    `
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
        flex-wrap: wrap;
        gap: 1rem;
        margin-bottom: 1.5rem;

        .file-size {
          color: var(--p-text-muted-color);
          display: flex;
          align-items: center;
          gap: 0.5rem;
        }
      }

      :host ::ng-deep .cached-chip {
        background: var(--p-primary-color);
        color: white;
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

  readonly movie = signal<Movie | null>(null);
  readonly downloadProgress = signal<DownloadProgress | null>(null);
  private readonly movieId = signal<string | null>(null);

  // Create observables in field initializers (injection context)
  private readonly userId$ = toObservable(this.currentUser.userId);
  private readonly movieId$ = toObservable(this.movieId);

  readonly isDownloading = computed(() => this.movie()?.status === 'DOWNLOADING');

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.router.navigate(['/']);
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
        error: () => this.router.navigate(['/']),
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
          } else if (progress.status === 'FAILED') {
            this.movie.set({ ...currentMovie, status: 'ERROR' });
          }
        }
      });
  }

  startDownload(): void {
    const currentMovie = this.movie();
    if (!currentMovie) return;
    this.movie.set({ ...currentMovie, status: 'DOWNLOADING' });
    this.moviesStore.startDownload(currentMovie.id);
  }

  toggleFavorite(): void {
    const currentMovie = this.movie();
    if (!currentMovie) return;

    const newFavorite = !currentMovie.favorite;
    this.movie.set({ ...currentMovie, favorite: newFavorite });

    if (newFavorite) {
      this.moviesStore.addFavorite(currentMovie.id);
    } else {
      this.moviesStore.removeFavorite(currentMovie.id);
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
    this.router.navigate(['/']);
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
        },
        error: (err) => {
          console.error('Failed to clear cache:', err);
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

  getStatusLabel(movie: Movie): string {
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
