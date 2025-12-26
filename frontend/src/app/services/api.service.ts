import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';

import { CurrentUserService } from './current-user.service';
export interface Movie {
  id: string;
  title: string;
  description?: string;
  year?: number;
  duration?: string;
  thumbnailUrl?: string;
  cached: boolean;
  favorite: boolean;
  status: 'PENDING' | 'DOWNLOADING' | 'READY' | 'ERROR';
  categoryId?: string;
  categoryName?: string;
  fileSize?: number;
  createdAt: string;
}

export interface MovieCreateRequest {
  title: string;
  description?: string;
  year?: number;
  duration?: string;
  megaUrl: string;
  thumbnailUrl?: string;
  categoryId?: string;
}

export interface DownloadProgress {
  movieId: string;
  movieTitle: string;
  status: 'QUEUED' | 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
  bytesDownloaded: number;
  totalBytes: number;
  progress: number;
  errorMessage?: string;
}

export interface StreamInfo {
  movieId: string;
  title: string;
  fileSize: number;
  contentType: string;
  streamUrl: string;
  supportsRangeRequests: boolean;
}

export interface Category {
  id: string;
  name: string;
  description?: string;
  sortOrder?: number;
  movieCount: number;
}

export interface CacheStats {
  totalSizeBytes: number;
  maxSizeBytes: number;
  usagePercent: number;
  movieCount: number;
}

export interface MovieGroup {
  name: string;
  categoryId?: string;
  special: boolean;
  sortOrder: number;
  movies: Movie[];
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly currentUser = inject(CurrentUserService);

  private userHeaders(): { headers: Record<string, string> } {
    return { headers: { 'X-User-Id': this.currentUser.userId() } };
  }

  getMovies(params?: {
    search?: string;
    readyOnly?: boolean;
    categoryId?: string;
  }): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.baseUrl}/movies`, {
      params: params as Record<string, string>,
      ...this.userHeaders(),
    });
  }

  getMoviesGrouped(search?: string): Observable<MovieGroup[]> {
    if (search) {
      return this.http.get<MovieGroup[]>(`${this.baseUrl}/movies/grouped`, {
        params: { search },
        ...this.userHeaders(),
      });
    }
    return this.http.get<MovieGroup[]>(`${this.baseUrl}/movies/grouped`, this.userHeaders());
  }

  getMovie(id: string): Observable<Movie> {
    return this.http.get<Movie>(`${this.baseUrl}/movies/${id}`, this.userHeaders());
  }

  createMovie(request: MovieCreateRequest): Observable<Movie> {
    return this.http.post<Movie>(`${this.baseUrl}/movies`, request);
  }

  updateMovie(id: string, request: MovieCreateRequest): Observable<Movie> {
    return this.http.put<Movie>(`${this.baseUrl}/movies/${id}`, request);
  }

  deleteMovie(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/movies/${id}`);
  }

  startDownload(movieId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/movies/${movieId}/download`, {});
  }

  getDownloadProgress(movieId: string): Observable<DownloadProgress> {
    return this.http.get<DownloadProgress>(`${this.baseUrl}/downloads/${movieId}`);
  }

  getActiveDownloads(): Observable<DownloadProgress[]> {
    return this.http.get<DownloadProgress[]>(`${this.baseUrl}/downloads`);
  }

  getStreamInfo(movieId: string): Observable<StreamInfo> {
    return this.http.get<StreamInfo>(`${this.baseUrl}/stream/${movieId}/info`);
  }

  getStreamUrl(movieId: string): string {
    return `${this.baseUrl}/stream/${movieId}`;
  }

  getThumbnailUrl(movieId: string): string {
    return `${this.baseUrl}/thumbnails/${movieId}`;
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`);
  }

  getCacheStats(): Observable<CacheStats> {
    return this.http.get<CacheStats>(`${this.baseUrl}/movies/cache/stats`);
  }

  getCachedMovies(): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.baseUrl}/movies/cached`);
  }

  clearMovieCache(movieId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/movies/${movieId}/cache`);
  }

  clearAllCache(): Observable<number> {
    return this.http.delete<number>(`${this.baseUrl}/movies/cache`);
  }

  getFavoriteMovies(): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.baseUrl}/movies/favorites`, this.userHeaders());
  }

  addFavorite(movieId: string): Observable<Movie> {
    return this.http.post<Movie>(
      `${this.baseUrl}/movies/${movieId}/favorite`,
      {},
      this.userHeaders()
    );
  }

  removeFavorite(movieId: string): Observable<Movie> {
    return this.http.delete<Movie>(
      `${this.baseUrl}/movies/${movieId}/favorite`,
      this.userHeaders()
    );
  }
}
