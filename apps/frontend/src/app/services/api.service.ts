import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '@env/environment';
import { Observable } from 'rxjs';

import {
  CacheStatsResponse,
  CategoryResponse,
  DownloadProgressResponse,
  MovieCreateRequest,
  MovieGroupResponse,
  MovieResponse,
  MovieUpdateRequest,
  StreamInfoResponse,
} from '../types';
import { CurrentUserService } from './current-user.service';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiUrl;
  private readonly currentUser = inject(CurrentUserService);

  private userHeaders(): { headers: Record<string, string> } {
    return { headers: { 'X-Mock-UserId': this.currentUser.userId() } };
  }

  getMovies(params?: {
    search?: string;
    readyOnly?: boolean;
    categoryId?: string;
  }): Observable<MovieResponse[]> {
    return this.http.get<MovieResponse[]>(`${this.baseUrl}/movies`, {
      params: params as Record<string, string>,
      ...this.userHeaders(),
    });
  }

  getMoviesGrouped(search?: string): Observable<MovieGroupResponse[]> {
    if (search) {
      return this.http.get<MovieGroupResponse[]>(`${this.baseUrl}/movies/grouped`, {
        params: { search },
        ...this.userHeaders(),
      });
    }
    return this.http.get<MovieGroupResponse[]>(
      `${this.baseUrl}/movies/grouped`,
      this.userHeaders()
    );
  }

  getMovie(id: string): Observable<MovieResponse> {
    return this.http.get<MovieResponse>(`${this.baseUrl}/movies/${id}`, this.userHeaders());
  }

  createMovie(request: MovieCreateRequest): Observable<MovieResponse> {
    return this.http.post<MovieResponse>(`${this.baseUrl}/movies`, request);
  }

  updateMovie(id: string, request: MovieUpdateRequest): Observable<MovieResponse> {
    return this.http.put<MovieResponse>(`${this.baseUrl}/movies/${id}`, request);
  }

  deleteMovie(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/movies/${id}`);
  }

  startDownload(movieId: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/movies/${movieId}/download`, {});
  }

  getDownloadProgress(movieId: string): Observable<DownloadProgressResponse> {
    return this.http.get<DownloadProgressResponse>(`${this.baseUrl}/downloads/${movieId}`);
  }

  getActiveDownloads(): Observable<DownloadProgressResponse[]> {
    return this.http.get<DownloadProgressResponse[]>(`${this.baseUrl}/downloads`);
  }

  getStreamInfo(movieId: string): Observable<StreamInfoResponse> {
    return this.http.get<StreamInfoResponse>(`${this.baseUrl}/stream/${movieId}/info`);
  }

  getStreamUrl(movieId: string): string {
    return `${this.baseUrl}/stream/${movieId}`;
  }

  getThumbnailUrl(movieId: string): string {
    return `${this.baseUrl}/thumbnails/${movieId}`;
  }

  getCategories(): Observable<CategoryResponse[]> {
    return this.http.get<CategoryResponse[]>(`${this.baseUrl}/categories`);
  }

  getCacheStats(): Observable<CacheStatsResponse> {
    return this.http.get<CacheStatsResponse>(`${this.baseUrl}/movies/cache/stats`);
  }

  getCachedMovies(): Observable<MovieResponse[]> {
    return this.http.get<MovieResponse[]>(`${this.baseUrl}/movies/cached`);
  }

  clearMovieCache(movieId: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/movies/${movieId}/cache`);
  }

  clearAllCache(): Observable<number> {
    return this.http.delete<number>(`${this.baseUrl}/movies/cache`);
  }

  getFavoriteMovies(): Observable<MovieResponse[]> {
    return this.http.get<MovieResponse[]>(`${this.baseUrl}/movies/favorites`, this.userHeaders());
  }

  addFavorite(movieId: string): Observable<MovieResponse> {
    return this.http.post<MovieResponse>(
      `${this.baseUrl}/movies/${movieId}/favorite`,
      {},
      this.userHeaders()
    );
  }

  removeFavorite(movieId: string): Observable<MovieResponse> {
    return this.http.delete<MovieResponse>(
      `${this.baseUrl}/movies/${movieId}/favorite`,
      this.userHeaders()
    );
  }
}
