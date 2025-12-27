import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  HostListener,
  inject,
  OnInit,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { Skeleton } from 'primeng/skeleton';
import { TagModule } from 'primeng/tag';

import { NotificationService } from '../../services/notification.service';
import { WebSocketService } from '../../services/websocket.service';
import { MoviesStore } from '../../store/movies.store';
import { MovieResponse } from '../../types';

@Component({
  selector: 'app-movie-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    FormsModule,
    InputTextModule,
    TagModule,
    Skeleton,
    IconField,
    InputIcon,
    ButtonModule,
  ],
  template: `
    <div class="netflix-browse">
      <!-- Navigation Bar -->
      <nav class="nav-bar" [class.scrolled]="isScrolled()">
        <div class="nav-left">
          <h1 class="logo">
            <i class="pi pi-video logo-icon"></i>
            MEDIASERVER
          </h1>
        </div>
        <div class="nav-right">
          <p-iconfield class="search-field">
            <p-inputicon styleClass="pi pi-search" />
            <input
              type="text"
              pInputText
              placeholder="Search titles, categories..."
              [ngModel]="store.filter()"
              (ngModelChange)="store.setFilter($event)"
              aria-label="Search movies"
            />
          </p-iconfield>
        </div>
      </nav>

      @if (store.loading()) {
        <!-- Skeleton Loading State -->
        <div class="skeleton-container">
          <div class="skeleton-hero">
            <p-skeleton width="100%" height="80vh" styleClass="skeleton-hero-bg" />
          </div>
          <div class="skeleton-content">
            @for (i of [1, 2, 3]; track i) {
              <div class="skeleton-category">
                <p-skeleton width="200px" height="28px" styleClass="skeleton-title" />
                <div class="skeleton-row">
                  @for (j of [1, 2, 3, 4, 5, 6]; track j) {
                    <div class="skeleton-card">
                      <p-skeleton width="240px" height="135px" styleClass="skeleton-card-img" />
                      <p-skeleton width="160px" height="16px" styleClass="skeleton-card-title" />
                    </div>
                  }
                </div>
              </div>
            }
          </div>
        </div>
      } @else {
        <!-- Hero Section -->
        @if (store.featuredMovie(); as featured) {
          <section
            class="hero"
            [style.background-image]="'url(' + (featured.thumbnailUrl || '') + ')'"
          >
            <div class="hero-gradient"></div>
            <div class="hero-content animate-slide-up">
              <h2 class="hero-title">{{ featured.title }}</h2>
              @if (featured.description) {
                <p class="hero-description">{{ featured.description }}</p>
              }
              <div class="hero-meta">
                @if (featured.year) {
                  <span><i class="pi pi-calendar"></i> {{ featured.year }}</span>
                }
                @if (featured.duration) {
                  <span><i class="pi pi-clock"></i> {{ featured.duration }}</span>
                }
                @if (featured.categoryName) {
                  <span><i class="pi pi-tag"></i> {{ featured.categoryName }}</span>
                }
              </div>
              <div class="hero-actions">
                @if (featured.cached) {
                  <button pButton class="play-btn" [routerLink]="['/play', featured.id]">
                    <i class="pi pi-play-circle"></i> Play
                  </button>
                } @else {
                  <button pButton class="play-btn" [routerLink]="['/movie', featured.id]">
                    <i class="pi pi-info-circle"></i> More Info
                  </button>
                }
                <button
                  pButton
                  class="info-btn"
                  [outlined]="true"
                  [routerLink]="['/movie', featured.id]"
                >
                  <i class="pi pi-info-circle"></i> Details
                </button>
              </div>
            </div>
          </section>
        }

        <!-- Category Rows -->
        <div class="content-rows">
          @for (category of store.moviesByCategory(); track category.name) {
            <section class="category-row animate-fade-in">
              <h3 class="category-title">{{ category.name }}</h3>
              <div class="row-wrapper">
                <button
                  class="scroll-btn scroll-left"
                  (click)="scrollRow($event, -1)"
                  aria-label="Scroll left"
                >
                  <i class="pi pi-chevron-left"></i>
                </button>
                <div class="movies-row">
                  @for (movie of category.movies; track movie.id) {
                    <div
                      class="movie-card"
                      [class.cached]="movie.cached"
                      [routerLink]="['/movie', movie.id]"
                      [attr.aria-label]="
                        movie.title + (movie.cached ? ' - Ready to play' : ' - On Mega')
                      "
                      tabindex="0"
                    >
                      <div class="card-image">
                        @if (movie.thumbnailUrl) {
                          <img [src]="movie.thumbnailUrl" [alt]="movie.title" loading="lazy" />
                        } @else {
                          <div class="card-placeholder">
                            <i class="pi pi-video"></i>
                          </div>
                        }
                        @if (movie.cached) {
                          <div class="cached-indicator">
                            <i class="pi pi-check-circle"></i>
                          </div>
                        }
                        <div class="card-overlay">
                          <div class="overlay-content">
                            <div class="overlay-actions">
                              <button
                                class="play-icon"
                                (click)="playOrView($event, movie)"
                                [attr.aria-label]="movie.cached ? 'Play movie' : 'View details'"
                              >
                                <i
                                  class="pi"
                                  [class.pi-play]="movie.cached"
                                  [class.pi-info-circle]="!movie.cached"
                                ></i>
                              </button>
                              <button
                                class="favorite-icon"
                                [class.favorited]="movie.favorite"
                                (click)="toggleFavorite($event, movie)"
                                [attr.aria-label]="
                                  movie.favorite ? 'Remove from favorites' : 'Add to favorites'
                                "
                              >
                                <i
                                  class="pi"
                                  [class.pi-heart-fill]="movie.favorite"
                                  [class.pi-heart]="!movie.favorite"
                                ></i>
                              </button>
                            </div>
                            <div class="overlay-info">
                              <span class="overlay-title">{{ movie.title }}</span>
                              <div class="overlay-meta">
                                @if (movie.year) {
                                  <span>{{ movie.year }}</span>
                                }
                                <p-tag
                                  [value]="movie.cached ? 'Ready' : 'Mega'"
                                  [severity]="movie.cached ? 'success' : 'warn'"
                                  class="status-badge"
                                />
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      <div class="card-info">
                        <span class="card-title">{{ movie.title }}</span>
                      </div>
                    </div>
                  }
                </div>
                <button
                  class="scroll-btn scroll-right"
                  (click)="scrollRow($event, 1)"
                  aria-label="Scroll right"
                >
                  <i class="pi pi-chevron-right"></i>
                </button>
              </div>
            </section>
          } @empty {
            <div class="empty-state animate-fade-in">
              <div class="empty-icon">
                <i class="pi pi-video"></i>
              </div>
              <h3>No movies found</h3>
              <p>Movies will appear here once discovered from Mega.nz</p>
              @if (store.filter()) {
                <button pButton [text]="true" icon="pi pi-times" (click)="store.setFilter('')">
                  Clear Search
                </button>
              }
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
        background: var(--bg-secondary);
        min-height: 100vh;
      }

      .netflix-browse {
        background: var(--bg-secondary);
        color: var(--text-primary);
      }

      /* Navigation */
      .nav-bar {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        z-index: 100;
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: var(--space-md) var(--space-2xl);
        background: linear-gradient(180deg, var(--overlay-dark-90) 0%, transparent 100%);
        transition:
          background var(--transition-default),
          backdrop-filter var(--transition-default);
      }

      .nav-bar.scrolled {
        background: var(--nav-gradient-scrolled);
        backdrop-filter: blur(20px);
        border-bottom: 1px solid var(--border-subtle);
      }

      .logo {
        font-size: 1.75rem;
        font-weight: 700;
        color: var(--primary);
        letter-spacing: 3px;
        margin: 0;
        display: flex;
        align-items: center;
        gap: var(--space-sm);
      }

      .logo-icon {
        font-size: 1.5rem;
      }

      .search-field {
        :host ::ng-deep input {
          background: var(--input-bg);
          border: 1px solid var(--border-default);
          color: var(--text-primary);
          width: 280px;
          border-radius: var(--radius-md);
          padding: 0.75rem 1rem 0.75rem 2.5rem;
          transition: all var(--transition-fast);

          &::placeholder {
            color: var(--text-tertiary);
          }

          &:hover {
            border-color: var(--text-muted);
          }

          &:focus {
            border-color: var(--text-primary);
            background: var(--input-bg-focus);
            width: 320px;
          }
        }
      }

      /* Skeleton Loading */
      .skeleton-container {
        background: var(--bg-secondary);
        min-height: 100vh;
        padding-top: 70px;
      }

      .skeleton-hero {
        margin-bottom: var(--space-xl);
      }

      :host ::ng-deep .skeleton-hero-bg {
        border-radius: 0;
      }

      .skeleton-content {
        margin-top: -6rem;
        position: relative;
        z-index: 2;
      }

      .skeleton-category {
        padding: 0 var(--space-2xl);
        margin-bottom: var(--space-2xl);
      }

      :host ::ng-deep .skeleton-title {
        margin-bottom: var(--space-md);
        border-radius: var(--radius-sm);
      }

      .skeleton-row {
        display: flex;
        gap: var(--space-sm);
        overflow: hidden;
      }

      .skeleton-card {
        flex-shrink: 0;
      }

      :host ::ng-deep .skeleton-card-img {
        border-radius: var(--radius-md);
      }

      :host ::ng-deep .skeleton-card-title {
        margin-top: var(--space-sm);
        border-radius: var(--radius-sm);
      }

      /* Hero Section */
      .hero {
        position: relative;
        height: 80vh;
        min-height: 500px;
        background-size: cover;
        background-position: center top;
        display: flex;
        align-items: flex-end;
        padding: 0 var(--space-2xl) 8rem;
      }

      .hero-gradient {
        position: absolute;
        inset: 0;
        background: linear-gradient(
          0deg,
          var(--bg-secondary) 0%,
          var(--gradient-fade) 20%,
          var(--gradient-subtle) 40%,
          var(--overlay-light) 60%,
          var(--gradient-subtle) 100%
        );
      }

      .hero-content {
        position: relative;
        z-index: 1;
        max-width: 600px;
      }

      .hero-title {
        font-size: 3.5rem;
        font-weight: 700;
        margin: 0 0 var(--space-md);
        text-shadow: 2px 2px 8px var(--overlay-dark);
        letter-spacing: -0.5px;
        line-height: 1.1;
      }

      .hero-description {
        font-size: 1.1rem;
        line-height: 1.6;
        margin: 0 0 var(--space-md);
        color: var(--text-bright);
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-shadow: 1px 1px 4px var(--overlay-dark);
      }

      .hero-meta {
        display: flex;
        gap: var(--space-lg);
        margin-bottom: var(--space-lg);
        font-size: 0.95rem;
        color: var(--text-secondary);

        span {
          display: flex;
          align-items: center;
          gap: var(--space-xs);

          i {
            color: var(--primary);
            font-size: 0.85rem;
          }
        }
      }

      .hero-actions {
        display: flex;
        gap: var(--space-md);
      }

      .play-btn {
        background: var(--text-primary) !important;
        color: var(--bg-primary) !important;
        border: none !important;
        font-weight: 600;
        padding: 0.875rem 2rem;
        font-size: 1.1rem;
        border-radius: var(--radius-md);
        transition: all var(--transition-fast);

        i {
          margin-right: var(--space-sm);
        }

        &:hover {
          background: var(--white-85) !important;
          transform: scale(1.05);
        }
      }

      .info-btn {
        background: var(--control-bg) !important;
        color: var(--text-primary) !important;
        border: none !important;
        font-weight: 600;
        padding: 0.875rem 2rem;
        font-size: 1.1rem;
        border-radius: var(--radius-md);
        transition: all var(--transition-fast);

        i {
          margin-right: var(--space-sm);
        }

        &:hover {
          background: var(--control-bg-hover) !important;
          transform: scale(1.05);
        }
      }

      /* Category Rows */
      .content-rows {
        margin-top: -6rem;
        position: relative;
        z-index: 2;
        padding-bottom: var(--space-2xl);
      }

      .category-row {
        margin-bottom: var(--space-2xl);
        padding: 0 var(--space-2xl);
      }

      .category-title {
        font-size: 1.4rem;
        font-weight: 600;
        margin: 0 0 0.75rem;
        color: var(--text-secondary);
      }

      .row-wrapper {
        position: relative;
        margin: 0 calc(var(--space-2xl) * -1);
        padding: 0 var(--space-2xl);
      }

      .scroll-btn {
        position: absolute;
        top: 0;
        bottom: 0;
        width: 3rem;
        background: var(--btn-bg-hover);
        border: none;
        color: var(--text-primary);
        cursor: pointer;
        opacity: 0;
        transition: opacity var(--transition-fast);
        z-index: 10;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;

        &:hover {
          background: var(--btn-bg-active);
        }

        &:focus-visible {
          opacity: 1;
        }
      }

      .scroll-left {
        left: 0;
      }

      .scroll-right {
        right: 0;
      }

      .row-wrapper:hover .scroll-btn {
        opacity: 1;
      }

      .movies-row {
        display: flex;
        gap: var(--space-sm);
        overflow-x: auto;
        scroll-behavior: smooth;
        padding: var(--space-md) 0;
        scrollbar-width: none;

        &::-webkit-scrollbar {
          display: none;
        }
      }

      /* Movie Cards */
      .movie-card {
        flex-shrink: 0;
        width: 240px;
        cursor: pointer;
        transition:
          transform var(--transition-default),
          z-index 0s var(--transition-default);
        position: relative;
        border-radius: var(--radius-md);
        overflow: visible;

        &:hover {
          transform: scale(1.35);
          z-index: 20;
          transition:
            transform var(--transition-default),
            z-index 0s;

          .card-overlay {
            opacity: 1;
          }

          .card-info {
            opacity: 0;
            transform: translateY(10px);
          }

          .card-image {
            box-shadow: var(--shadow-lg);

            img {
              transform: scale(1.05);
            }
          }
        }

        &:focus-visible {
          outline: none;

          .card-image {
            box-shadow: 0 0 0 3px var(--primary);
          }
        }
      }

      .card-image {
        position: relative;
        aspect-ratio: 16/9;
        border-radius: var(--radius-md);
        overflow: hidden;
        background: var(--bg-tertiary);
        transition: box-shadow var(--transition-default);

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          transition: transform var(--transition-slow);
        }
      }

      .card-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: var(--text-muted);
        background: linear-gradient(135deg, var(--bg-tertiary), var(--bg-elevated));

        i {
          font-size: 2.5rem;
        }
      }

      .cached-indicator {
        position: absolute;
        top: 8px;
        right: 8px;
        background: var(--primary);
        color: var(--text-primary);
        width: 26px;
        height: 26px;
        border-radius: var(--radius-full);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.75rem;
        box-shadow: var(--shadow-sm);
      }

      .card-overlay {
        position: absolute;
        inset: 0;
        background: linear-gradient(
          0deg,
          var(--card-overlay-bottom) 0%,
          var(--overlay-card-light) 40%,
          var(--overlay-light) 70%,
          transparent 100%
        );
        opacity: 0;
        transition: opacity var(--transition-default);
        display: flex;
        flex-direction: column;
        justify-content: flex-end;
        padding: var(--space-md);
        border-radius: var(--radius-md);
      }

      .overlay-content {
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
      }

      .overlay-actions {
        display: flex;
        gap: var(--space-sm);
      }

      .play-icon,
      .favorite-icon {
        width: 42px;
        height: 42px;
        border-radius: var(--radius-full);
        border: 2px solid var(--text-primary);
        background: var(--btn-bg-active);
        color: var(--text-primary);
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all var(--transition-fast);
        backdrop-filter: blur(10px);

        &:hover {
          background: var(--text-primary);
          color: var(--bg-primary);
          transform: scale(1.15);
          box-shadow: var(--shadow-sm);
        }

        &:active {
          transform: scale(1.05);
        }

        i {
          font-size: 1rem;
        }
      }

      .play-icon {
        background: var(--primary);
        border-color: var(--primary);

        &:hover {
          background: var(--primary-hover);
          border-color: var(--primary-hover);
          color: var(--text-primary);
        }
      }

      .favorite-icon.favorited {
        background: var(--primary);
        border-color: var(--primary);
        color: var(--text-primary);

        &:hover {
          background: var(--primary-dark);
          border-color: var(--primary-dark);
          color: var(--text-primary);
        }
      }

      .overlay-info {
        display: flex;
        flex-direction: column;
        gap: 0.25rem;
      }

      .overlay-title {
        font-weight: 600;
        font-size: 0.9rem;
      }

      .overlay-meta {
        display: flex;
        align-items: center;
        gap: var(--space-sm);
        font-size: 0.8rem;
        color: var(--text-secondary);
      }

      :host ::ng-deep .status-badge {
        font-size: 0.7rem;
        padding: 0.15rem 0.4rem;
      }

      .card-info {
        padding: var(--space-sm) 0;
        transition:
          opacity var(--transition-default),
          transform var(--transition-default);
      }

      .card-title {
        font-size: 0.85rem;
        color: var(--text-secondary);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        display: block;
      }

      .movie-card.cached .card-image {
        box-shadow: 0 0 0 2px var(--primary);
      }

      /* Empty State */
      .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 8rem var(--space-xl);
        text-align: center;

        .empty-icon {
          width: 120px;
          height: 120px;
          border-radius: var(--radius-full);
          background: var(--bg-tertiary);
          display: flex;
          align-items: center;
          justify-content: center;
          margin-bottom: var(--space-xl);

          i {
            font-size: 3rem;
            color: var(--text-tertiary);
          }
        }

        h3 {
          font-size: 1.5rem;
          font-weight: 600;
          margin: 0 0 var(--space-sm);
          color: var(--text-primary);
        }

        p {
          font-size: 1rem;
          color: var(--text-tertiary);
          margin: 0 0 var(--space-lg);
        }
      }

      /* Responsive */
      @media (max-width: 1200px) {
        .movie-card {
          width: 200px;
        }

        .hero-title {
          font-size: 2.5rem;
        }
      }

      @media (max-width: 768px) {
        .nav-bar {
          padding: var(--space-md);
        }

        .search-field :host ::ng-deep input {
          width: 180px;

          &:focus {
            width: 220px;
          }
        }

        .category-row {
          padding: 0 var(--space-md);
        }

        .row-wrapper {
          margin: 0 calc(var(--space-md) * -1);
          padding: 0 var(--space-md);
        }

        .hero {
          padding: 0 var(--space-md) 6rem;
          height: 60vh;
        }

        .hero-title {
          font-size: 2rem;
        }

        .movie-card {
          width: 150px;

          &:hover {
            transform: scale(1.15);
          }
        }

        .scroll-btn {
          display: none;
        }

        .skeleton-category {
          padding: 0 var(--space-md);
        }
      }
    `,
  ],
})
export class MovieListComponent implements OnInit {
  readonly store = inject(MoviesStore);
  private readonly router = inject(Router);
  private readonly ws = inject(WebSocketService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly notification = inject(NotificationService);

  readonly isScrolled = signal(false);

  @HostListener('window:scroll')
  onScroll(): void {
    this.isScrolled.set(window.scrollY > 50);
  }

  ngOnInit(): void {
    this.store.loadMovies();
    this.ws.connect();

    this.ws
      .getDownloadProgress()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((progress) => {
        if (progress.status === 'COMPLETED') {
          this.store.updateMovieStatus(progress.movieId, 'READY', true);
          this.notification.success('Download Complete', 'Movie is ready to play');
        } else if (progress.status === 'FAILED') {
          this.store.updateMovieStatus(progress.movieId, 'ERROR', false);
          this.notification.error('Download Failed', 'Please try again');
        }
      });
  }

  scrollRow(event: MouseEvent, direction: number): void {
    event.stopPropagation();
    const button = event.target as HTMLElement;
    const wrapper = button.closest('.row-wrapper');
    const row = wrapper?.querySelector('.movies-row');
    if (row) {
      const scrollAmount = row.clientWidth * 0.8;
      row.scrollBy({ left: direction * scrollAmount, behavior: 'smooth' });
    }
  }

  playOrView(event: MouseEvent, movie: MovieResponse): void {
    event.stopPropagation();
    event.preventDefault();
    if (movie.cached) {
      void this.router.navigate(['/play', movie.id]);
    } else {
      void this.router.navigate(['/movie', movie.id]);
    }
  }

  toggleFavorite(event: MouseEvent, movie: MovieResponse): void {
    event.stopPropagation();
    event.preventDefault();
    this.store.toggleFavorite(movie.id);

    if (movie.favorite) {
      this.notification.info('Removed from My List', movie.title);
    } else {
      this.notification.success('Added to My List', movie.title);
    }
  }
}
