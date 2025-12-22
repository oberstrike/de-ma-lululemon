import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MoviesStore } from '../../store/movies.store';
import { WebSocketService } from '../../services/websocket.service';

@Component({
  selector: 'app-movie-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <div class="movie-list-page">
      <header class="header">
        <h1>Media Server</h1>
        <div class="search-box">
          <input
            type="text"
            placeholder="Search movies..."
            [ngModel]="store.filter()"
            (ngModelChange)="store.setFilter($event)"
          />
        </div>
      </header>

      @if (store.loading()) {
        <div class="loading">Loading movies...</div>
      }

      @if (store.error()) {
        <div class="error">{{ store.error() }}</div>
      }

      <div class="movies-grid">
        @for (movie of store.filteredMovies(); track movie.id) {
          <div class="movie-card" [routerLink]="['/movie', movie.id]">
            <div class="thumbnail">
              @if (movie.thumbnailUrl) {
                <img [src]="movie.thumbnailUrl" [alt]="movie.title" />
              } @else {
                <div class="placeholder">{{ movie.title.charAt(0) }}</div>
              }
              <div class="status-badge" [class]="movie.status.toLowerCase()">
                {{ movie.status }}
              </div>
            </div>
            <div class="info">
              <h3>{{ movie.title }}</h3>
              @if (movie.year) {
                <span class="year">{{ movie.year }}</span>
              }
              @if (movie.duration) {
                <span class="duration">{{ movie.duration }}</span>
              }
            </div>
          </div>
        } @empty {
          <div class="empty">No movies found</div>
        }
      </div>
    </div>
  `,
  styles: [`
    .movie-list-page {
      padding: 2rem;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;

      h1 {
        color: var(--primary);
        font-size: 2rem;
      }
    }

    .search-box input {
      padding: 0.75rem 1rem;
      border-radius: 4px;
      border: 1px solid #333;
      background: var(--bg-card);
      color: #fff;
      width: 300px;
      font-size: 1rem;

      &:focus {
        outline: none;
        border-color: var(--primary);
      }
    }

    .movies-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
      gap: 1.5rem;
    }

    .movie-card {
      background: var(--bg-card);
      border-radius: 8px;
      overflow: hidden;
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
      }
    }

    .thumbnail {
      position: relative;
      aspect-ratio: 16/9;
      background: #222;

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
        font-size: 3rem;
        color: #555;
      }
    }

    .status-badge {
      position: absolute;
      top: 8px;
      right: 8px;
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 0.75rem;
      font-weight: 600;
      text-transform: uppercase;

      &.ready { background: #22c55e; }
      &.downloading { background: #3b82f6; }
      &.pending { background: #f59e0b; }
      &.error { background: #ef4444; }
    }

    .info {
      padding: 1rem;

      h3 {
        font-size: 1rem;
        margin-bottom: 0.5rem;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .year, .duration {
        font-size: 0.875rem;
        color: var(--text-secondary);
        margin-right: 0.5rem;
      }
    }

    .loading, .error, .empty {
      text-align: center;
      padding: 3rem;
      color: var(--text-secondary);
    }

    .error {
      color: #ef4444;
    }
  `]
})
export class MovieListComponent implements OnInit {
  readonly store = inject(MoviesStore);
  private ws = inject(WebSocketService);

  ngOnInit() {
    this.store.loadMovies();
    this.ws.connect();

    this.ws.getDownloadProgress().subscribe(progress => {
      if (progress.status === 'COMPLETED') {
        this.store.updateMovieStatus(progress.movieId, 'READY', true);
      } else if (progress.status === 'FAILED') {
        this.store.updateMovieStatus(progress.movieId, 'ERROR', false);
      }
    });
  }
}
