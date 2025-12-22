import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MoviesStore } from '../../store/movies.store';
import { WebSocketService } from '../../services/websocket.service';
import { InputTextModule } from 'primeng/inputtext';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { ProgressSpinner } from 'primeng/progressspinner';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { Message } from 'primeng/message';

@Component({
  selector: 'app-movie-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    InputTextModule,
    CardModule,
    TagModule,
    ProgressSpinner,
    IconField,
    InputIcon,
    Message
  ],
  template: `
    <div class="movie-list-page">
      <header class="header">
        <h1>Media Server</h1>
        <p-iconfield>
          <p-inputicon styleClass="pi pi-search" />
          <input
            type="text"
            pInputText
            placeholder="Search movies..."
            [ngModel]="store.filter()"
            (ngModelChange)="store.setFilter($event)"
          />
        </p-iconfield>
      </header>

      @if (store.loading()) {
        <div class="loading">
          <p-progressspinner strokeWidth="4" />
          <p>Loading movies...</p>
        </div>
      }

      @if (store.error()) {
        <p-message severity="error" [text]="store.error()!" />
      }

      <div class="movies-grid">
        @for (movie of store.filteredMovies(); track movie.id) {
          <p-card styleClass="movie-card" [routerLink]="['/movie', movie.id]">
            <ng-template pTemplate="header">
              <div class="thumbnail">
                @if (movie.thumbnailUrl) {
                  <img [src]="movie.thumbnailUrl" [alt]="movie.title" />
                } @else {
                  <div class="placeholder">
                    <i class="pi pi-video" style="font-size: 3rem"></i>
                  </div>
                }
                <p-tag
                  [value]="movie.status"
                  [severity]="getStatusSeverity(movie.status)"
                  class="status-tag"
                />
              </div>
            </ng-template>
            <div class="info">
              <h3>{{ movie.title }}</h3>
              <div class="meta">
                @if (movie.year) {
                  <span><i class="pi pi-calendar"></i> {{ movie.year }}</span>
                }
                @if (movie.duration) {
                  <span><i class="pi pi-clock"></i> {{ movie.duration }}</span>
                }
              </div>
            </div>
          </p-card>
        } @empty {
          <div class="empty">
            <i class="pi pi-inbox" style="font-size: 3rem"></i>
            <p>No movies found</p>
          </div>
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
        color: var(--p-primary-color);
        font-size: 2rem;
        margin: 0;
      }
    }

    .movies-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 1.5rem;
    }

    :host ::ng-deep .movie-card {
      cursor: pointer;
      transition: transform 0.2s, box-shadow 0.2s;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
      }

      .p-card-body {
        padding: 0;
      }

      .p-card-content {
        padding: 1rem;
      }
    }

    .thumbnail {
      position: relative;
      aspect-ratio: 16/9;
      background: var(--p-surface-800);
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

    .status-tag {
      position: absolute;
      top: 8px;
      right: 8px;
    }

    .info {
      h3 {
        font-size: 1rem;
        margin: 0 0 0.5rem 0;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .meta {
        display: flex;
        gap: 1rem;
        font-size: 0.875rem;
        color: var(--p-text-muted-color);

        span {
          display: flex;
          align-items: center;
          gap: 0.25rem;
        }
      }
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

    .empty {
      grid-column: 1 / -1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 3rem;
      gap: 1rem;
      color: var(--p-text-muted-color);
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

  getStatusSeverity(status: string): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
    switch (status) {
      case 'READY': return 'success';
      case 'DOWNLOADING': return 'info';
      case 'PENDING': return 'warn';
      case 'ERROR': return 'danger';
      default: return 'secondary';
    }
  }
}
