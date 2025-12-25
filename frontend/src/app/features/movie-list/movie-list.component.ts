import { ChangeDetectionStrategy, Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ButtonModule } from 'primeng/button';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputTextModule } from 'primeng/inputtext';
import { ProgressSpinner } from 'primeng/progressspinner';
import { TagModule } from 'primeng/tag';

import { Movie } from '../../services/api.service';
import { WebSocketService } from '../../services/websocket.service';
import { MoviesStore } from '../../store/movies.store';

@Component({
  selector: 'app-movie-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    RouterLink,
    FormsModule,
    InputTextModule,
    TagModule,
    ProgressSpinner,
    IconField,
    InputIcon,
    ButtonModule,
  ],
  template: `
    <div class="netflix-browse">
      <!-- Navigation Bar -->
      <nav class="nav-bar">
        <div class="nav-left">
          <h1 class="logo">MEDIASERVER</h1>
        </div>
        <div class="nav-right">
          <p-iconfield class="search-field">
            <p-inputicon styleClass="pi pi-search" />
            <input
              type="text"
              pInputText
              placeholder="Titles, categories..."
              [ngModel]="store.filter()"
              (ngModelChange)="store.setFilter($event)"
            />
          </p-iconfield>
        </div>
      </nav>

      @if (store.loading()) {
        <div class="loading-screen">
          <p-progressspinner strokeWidth="4" />
        </div>
      } @else {
        <!-- Hero Section -->
        @if (store.featuredMovie(); as featured) {
          <section
            class="hero"
            [style.background-image]="'url(' + (featured.thumbnailUrl || '') + ')'"
          >
            <div class="hero-gradient"></div>
            <div class="hero-content">
              <h2 class="hero-title">{{ featured.title }}</h2>
              @if (featured.description) {
                <p class="hero-description">{{ featured.description }}</p>
              }
              <div class="hero-meta">
                @if (featured.year) {
                  <span>{{ featured.year }}</span>
                }
                @if (featured.duration) {
                  <span>{{ featured.duration }}</span>
                }
                @if (featured.categoryName) {
                  <span>{{ featured.categoryName }}</span>
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
            <section class="category-row">
              <h3 class="category-title">{{ category.name }}</h3>
              <div class="row-wrapper">
                <button class="scroll-btn scroll-left" (click)="scrollRow($event, -1)">
                  <i class="pi pi-chevron-left"></i>
                </button>
                <div class="movies-row">
                  @for (movie of category.movies; track movie.id) {
                    <div
                      class="movie-card"
                      [class.cached]="movie.cached"
                      [routerLink]="['/movie', movie.id]"
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
                              <button class="play-icon" (click)="playOrView($event, movie)">
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
                                [title]="
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
                <button class="scroll-btn scroll-right" (click)="scrollRow($event, 1)">
                  <i class="pi pi-chevron-right"></i>
                </button>
              </div>
            </section>
          } @empty {
            <div class="empty-state">
              <i class="pi pi-video"></i>
              <p>No movies found</p>
              <span>Movies will appear here once discovered from Mega.nz</span>
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
        background: #141414;
        min-height: 100vh;
      }

      .netflix-browse {
        background: #141414;
        color: #fff;
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
        padding: 1rem 3rem;
        background: linear-gradient(180deg, rgba(0, 0, 0, 0.8) 0%, transparent 100%);
        transition: background 0.3s;
      }

      .logo {
        font-size: 1.75rem;
        font-weight: 700;
        color: var(--p-primary-color);
        letter-spacing: 2px;
        margin: 0;
      }

      .search-field {
        :host ::ng-deep input {
          background: rgba(0, 0, 0, 0.6);
          border: 1px solid rgba(255, 255, 255, 0.2);
          color: #fff;
          width: 250px;

          &::placeholder {
            color: rgba(255, 255, 255, 0.5);
          }

          &:focus {
            border-color: #fff;
          }
        }
      }

      /* Loading */
      .loading-screen {
        display: flex;
        align-items: center;
        justify-content: center;
        height: 100vh;
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
        padding: 0 3rem 8rem;
      }

      .hero-gradient {
        position: absolute;
        inset: 0;
        background: linear-gradient(
          0deg,
          #141414 0%,
          rgba(20, 20, 20, 0.8) 20%,
          rgba(20, 20, 20, 0.4) 40%,
          rgba(20, 20, 20, 0.2) 60%,
          rgba(20, 20, 20, 0.4) 100%
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
        margin: 0 0 1rem;
        text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.8);
      }

      .hero-description {
        font-size: 1.1rem;
        line-height: 1.5;
        margin: 0 0 1rem;
        color: rgba(255, 255, 255, 0.9);
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
        text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.8);
      }

      .hero-meta {
        display: flex;
        gap: 1.5rem;
        margin-bottom: 1.5rem;
        font-size: 0.95rem;
        color: rgba(255, 255, 255, 0.7);

        span {
          display: flex;
          align-items: center;
          gap: 0.5rem;
        }
      }

      .hero-actions {
        display: flex;
        gap: 1rem;
      }

      .play-btn {
        background: #fff !important;
        color: #000 !important;
        border: none !important;
        font-weight: 600;
        padding: 0.75rem 2rem;
        font-size: 1.1rem;
        border-radius: 4px;

        i {
          margin-right: 0.5rem;
        }

        &:hover {
          background: rgba(255, 255, 255, 0.85) !important;
        }
      }

      .info-btn {
        background: rgba(109, 109, 110, 0.7) !important;
        color: #fff !important;
        border: none !important;
        font-weight: 600;
        padding: 0.75rem 2rem;
        font-size: 1.1rem;
        border-radius: 4px;

        i {
          margin-right: 0.5rem;
        }

        &:hover {
          background: rgba(109, 109, 110, 0.5) !important;
        }
      }

      /* Category Rows */
      .content-rows {
        margin-top: -6rem;
        position: relative;
        z-index: 2;
        padding-bottom: 3rem;
      }

      .category-row {
        margin-bottom: 2.5rem;
        padding: 0 3rem;
      }

      .category-title {
        font-size: 1.4rem;
        font-weight: 600;
        margin: 0 0 0.75rem;
        color: #e5e5e5;
      }

      .row-wrapper {
        position: relative;
        margin: 0 -3rem;
        padding: 0 3rem;
      }

      .scroll-btn {
        position: absolute;
        top: 0;
        bottom: 0;
        width: 3rem;
        background: rgba(20, 20, 20, 0.7);
        border: none;
        color: #fff;
        cursor: pointer;
        opacity: 0;
        transition: opacity 0.2s;
        z-index: 10;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.5rem;

        &:hover {
          background: rgba(20, 20, 20, 0.9);
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
        gap: 0.5rem;
        overflow-x: auto;
        scroll-behavior: smooth;
        padding: 1rem 0;
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
          transform 0.3s ease,
          z-index 0s 0.3s;
        position: relative;

        &:hover {
          transform: scale(1.3);
          z-index: 20;
          transition:
            transform 0.3s ease,
            z-index 0s;

          .card-overlay {
            opacity: 1;
          }

          .card-info {
            opacity: 0;
          }
        }
      }

      .card-image {
        position: relative;
        aspect-ratio: 16/9;
        border-radius: 4px;
        overflow: hidden;
        background: #2a2a2a;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }

      .card-placeholder {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #555;

        i {
          font-size: 2.5rem;
        }
      }

      .cached-indicator {
        position: absolute;
        top: 8px;
        right: 8px;
        background: var(--p-primary-color);
        color: #fff;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 0.75rem;
      }

      .card-overlay {
        position: absolute;
        inset: 0;
        background: linear-gradient(
          0deg,
          rgba(20, 20, 20, 0.95) 0%,
          rgba(20, 20, 20, 0.7) 50%,
          transparent 100%
        );
        opacity: 0;
        transition: opacity 0.3s;
        display: flex;
        flex-direction: column;
        justify-content: flex-end;
        padding: 1rem;
        border-radius: 4px;
      }

      .overlay-content {
        display: flex;
        flex-direction: column;
        gap: 0.75rem;
      }

      .overlay-actions {
        display: flex;
        gap: 0.5rem;
      }

      .play-icon,
      .favorite-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        border: 2px solid #fff;
        background: rgba(30, 30, 30, 0.8);
        color: #fff;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: center;
        transition: all 0.2s;

        &:hover {
          background: #fff;
          color: #000;
          transform: scale(1.1);
        }

        i {
          font-size: 1rem;
        }
      }

      .favorite-icon.favorited {
        background: #e50914;
        border-color: #e50914;
        color: #fff;

        &:hover {
          background: #b20710;
          border-color: #b20710;
          color: #fff;
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
        gap: 0.5rem;
        font-size: 0.8rem;
        color: rgba(255, 255, 255, 0.7);
      }

      :host ::ng-deep .status-badge {
        font-size: 0.7rem;
        padding: 0.15rem 0.4rem;
      }

      .card-info {
        padding: 0.5rem 0;
        transition: opacity 0.3s;
      }

      .card-title {
        font-size: 0.85rem;
        color: #b3b3b3;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        display: block;
      }

      .movie-card.cached .card-image {
        box-shadow: 0 0 0 2px var(--p-primary-color);
      }

      /* Empty State */
      .empty-state {
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 6rem 2rem;
        text-align: center;
        color: #808080;

        i {
          font-size: 4rem;
          margin-bottom: 1rem;
        }

        p {
          font-size: 1.5rem;
          margin: 0 0 0.5rem;
          color: #e5e5e5;
        }

        span {
          font-size: 1rem;
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
          padding: 1rem;
        }

        .category-row {
          padding: 0 1rem;
        }

        .row-wrapper {
          margin: 0 -1rem;
          padding: 0 1rem;
        }

        .hero {
          padding: 0 1rem 6rem;
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
      }
    `,
  ],
})
export class MovieListComponent implements OnInit {
  readonly store = inject(MoviesStore);
  private readonly router = inject(Router);
  private readonly ws = inject(WebSocketService);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit() {
    this.store.loadMovies();
    this.ws.connect();

    this.ws
      .getDownloadProgress()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((progress) => {
        if (progress.status === 'COMPLETED') {
          this.store.updateMovieStatus(progress.movieId, 'READY', true);
        } else if (progress.status === 'FAILED') {
          this.store.updateMovieStatus(progress.movieId, 'ERROR', false);
        }
      });
  }

  scrollRow(event: MouseEvent, direction: number) {
    event.stopPropagation();
    const button = event.target as HTMLElement;
    const wrapper = button.closest('.row-wrapper');
    const row = wrapper?.querySelector('.movies-row');
    if (row !== null && row !== undefined) {
      const scrollAmount = row.clientWidth * 0.8;
      row.scrollBy({ left: direction * scrollAmount, behavior: 'smooth' });
    }
  }

  playOrView(event: MouseEvent, movie: Movie) {
    event.stopPropagation();
    event.preventDefault();
    if (movie.cached) {
      this.router.navigate(['/play', movie.id]);
    } else {
      this.router.navigate(['/movie', movie.id]);
    }
  }

  toggleFavorite(event: MouseEvent, movie: Movie) {
    event.stopPropagation();
    event.preventDefault();
    this.store.toggleFavorite(movie.id);
  }
}
