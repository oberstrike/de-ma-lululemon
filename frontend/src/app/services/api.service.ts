import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@env/environment';

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

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private baseUrl = environment.apiUrl;

  // Movies
  getMovies(params?: {
    search?: string;
    readyOnly?: boolean;
    categoryId?: string;
  }): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.baseUrl}/movies`, {
      params: params as Record<string, string>,
    });
  }

  getMovie(id: string): Observable<Movie> {
    return this.http.get<Movie>(`${this.baseUrl}/movies/${id}`);
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

  // Downloads
  startDownload(movieId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/movies/${movieId}/download`, {});
  }

  getDownloadProgress(movieId: string): Observable<DownloadProgress> {
    return this.http.get<DownloadProgress>(`${this.baseUrl}/downloads/${movieId}`);
  }

  getActiveDownloads(): Observable<DownloadProgress[]> {
    return this.http.get<DownloadProgress[]>(`${this.baseUrl}/downloads`);
  }

  // Streaming
  getStreamInfo(movieId: string): Observable<StreamInfo> {
    return this.http.get<StreamInfo>(`${this.baseUrl}/stream/${movieId}/info`);
  }

  getStreamUrl(movieId: string): string {
    return `${this.baseUrl}/stream/${movieId}`;
  }

  getThumbnailUrl(movieId: string): string {
    return `${this.baseUrl}/thumbnails/${movieId}`;
  }

  // Categories
  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.baseUrl}/categories`);
  }

  // Cache
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

  // Favorites
  getFavoriteMovies(): Observable<Movie[]> {
    return this.http.get<Movie[]>(`${this.baseUrl}/movies/favorites`);
  }

  addFavorite(movieId: string): Observable<Movie> {
    return this.http.post<Movie>(`${this.baseUrl}/movies/${movieId}/favorite`, {});
  }

  removeFavorite(movieId: string): Observable<Movie> {
    return this.http.delete<Movie>(`${this.baseUrl}/movies/${movieId}/favorite`);
  }
}
