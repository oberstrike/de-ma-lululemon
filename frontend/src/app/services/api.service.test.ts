import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { ApiService, Movie, MovieCreateRequest } from './api.service';
import { CurrentUserService } from './current-user.service';

describe('ApiService', (): void => {
  let service: ApiService;
  let httpMock: HttpTestingController;
  let currentUser: CurrentUserService;

  beforeEach((): void => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ApiService],
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
    currentUser = TestBed.inject(CurrentUserService);
  });

  afterEach((): void => {
    httpMock.verify();
  });

  it('should be created', (): void => {
    expect(service).toBeTruthy();
  });

  describe('getMovies', (): void => {
    it('should return movies', async (): Promise<void> => {
      const mockMovies: Movie[] = [
        {
          id: '1',
          title: 'Test Movie',
          cached: false,
          status: 'PENDING',
          createdAt: '2024-01-01',
        },
      ];

      const moviesPromise = firstValueFrom(service.getMovies());

      const req = httpMock.expectOne('/api/movies');
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('X-Mock-UserId')).toBe('user-1');
      req.flush(mockMovies);

      const movies = await moviesPromise;
      expect(movies.length).toBe(1);
      expect(movies[0].title).toBe('Test Movie');
    });

    it('should handle search parameter', async (): Promise<void> => {
      const moviesPromise = firstValueFrom(service.getMovies({ search: 'test' }));

      const req = httpMock.expectOne('/api/movies?search=test');
      expect(req.request.method).toBe('GET');
      req.flush([]);

      await moviesPromise;
    });

    it('should handle readyOnly parameter', async (): Promise<void> => {
      const moviesPromise = firstValueFrom(service.getMovies({ readyOnly: true }));

      const req = httpMock.expectOne('/api/movies?readyOnly=true');
      expect(req.request.method).toBe('GET');
      req.flush([]);

      await moviesPromise;
    });
  });

  describe('getFavoriteMovies', (): void => {
    it('should use current user headers', async (): Promise<void> => {
      const favoritesPromise = firstValueFrom(service.getFavoriteMovies());

      const req = httpMock.expectOne('/api/movies/favorites');
      expect(req.request.method).toBe('GET');
      expect(req.request.headers.get('X-Mock-UserId')).toBe('user-1');
      req.flush([]);

      await favoritesPromise;

      currentUser.setUserId('user-2');

      const updatedPromise = firstValueFrom(service.getFavoriteMovies());

      const updatedReq = httpMock.expectOne('/api/movies/favorites');
      expect(updatedReq.request.method).toBe('GET');
      expect(updatedReq.request.headers.get('X-Mock-UserId')).toBe('user-2');
      updatedReq.flush([]);

      await updatedPromise;
    });
  });

  describe('getMovie', (): void => {
    it('should return a single movie', async (): Promise<void> => {
      const mockMovie: Movie = {
        id: '1',
        title: 'Test Movie',
        description: 'A test',
        cached: true,
        status: 'READY',
        createdAt: '2024-01-01',
      };

      const moviePromise = firstValueFrom(service.getMovie('1'));

      const req = httpMock.expectOne('/api/movies/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockMovie);

      const movie = await moviePromise;
      expect(movie.id).toBe('1');
      expect(movie.title).toBe('Test Movie');
    });
  });

  describe('createMovie', (): void => {
    it('should create a movie', async (): Promise<void> => {
      const request: MovieCreateRequest = {
        title: 'New Movie',
        megaUrl: 'https://mega.nz/file/test',
      };

      const mockResponse: Movie = {
        id: '2',
        title: 'New Movie',
        cached: false,
        status: 'PENDING',
        createdAt: '2024-01-01',
      };

      const moviePromise = firstValueFrom(service.createMovie(request));

      const req = httpMock.expectOne('/api/movies');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);

      const movie = await moviePromise;
      expect(movie.id).toBe('2');
      expect(movie.title).toBe('New Movie');
    });
  });

  describe('updateMovie', (): void => {
    it('should update a movie', async (): Promise<void> => {
      const request: MovieCreateRequest = {
        title: 'Updated Movie',
        megaUrl: 'https://mega.nz/file/test',
      };

      const mockResponse: Movie = {
        id: '1',
        title: 'Updated Movie',
        cached: false,
        status: 'PENDING',
        createdAt: '2024-01-01',
      };

      const moviePromise = firstValueFrom(service.updateMovie('1', request));

      const req = httpMock.expectOne('/api/movies/1');
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);

      const movie = await moviePromise;
      expect(movie.title).toBe('Updated Movie');
    });
  });

  describe('deleteMovie', (): void => {
    it('should delete a movie', async (): Promise<void> => {
      const deletePromise = firstValueFrom(service.deleteMovie('1'));

      const req = httpMock.expectOne('/api/movies/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);

      await deletePromise;
    });
  });

  describe('startDownload', (): void => {
    it('should start a download', async (): Promise<void> => {
      const downloadPromise = firstValueFrom(service.startDownload('1'));

      const req = httpMock.expectOne('/api/movies/1/download');
      expect(req.request.method).toBe('POST');
      req.flush(null);

      await downloadPromise;
    });
  });

  describe('getStreamUrl', (): void => {
    it('should return the stream URL', (): void => {
      const url = service.getStreamUrl('1');
      expect(url).toBe('/api/stream/1');
    });
  });

  describe('getStreamInfo', (): void => {
    it('should return stream info', async (): Promise<void> => {
      const mockInfo = {
        movieId: '1',
        title: 'Test',
        fileSize: 1000,
        contentType: 'video/mp4',
        streamUrl: '/api/stream/1',
        supportsRangeRequests: true,
      };

      const infoPromise = firstValueFrom(service.getStreamInfo('1'));

      const req = httpMock.expectOne('/api/stream/1/info');
      expect(req.request.method).toBe('GET');
      req.flush(mockInfo);

      const info = await infoPromise;
      expect(info.movieId).toBe('1');
      expect(info.supportsRangeRequests).toBe(true);
    });
  });

  describe('getCategories', (): void => {
    it('should return categories', async (): Promise<void> => {
      const categoriesPromise = firstValueFrom(service.getCategories());

      const req = httpMock.expectOne('/api/categories');
      expect(req.request.method).toBe('GET');
      req.flush([{ id: '1', name: 'Action', movieCount: 5 }]);

      const categories = await categoriesPromise;
      expect(categories.length).toBe(1);
    });
  });

  describe('getActiveDownloads', (): void => {
    it('should return active downloads', async (): Promise<void> => {
      const downloadsPromise = firstValueFrom(service.getActiveDownloads());

      const req = httpMock.expectOne('/api/downloads');
      expect(req.request.method).toBe('GET');
      req.flush([{ movieId: '1', progress: 50, status: 'IN_PROGRESS' }]);

      const downloads = await downloadsPromise;
      expect(downloads.length).toBe(1);
    });
  });

  describe('getCacheStats', (): void => {
    it('should return cache stats', async (): Promise<void> => {
      const statsPromise = firstValueFrom(service.getCacheStats());

      const req = httpMock.expectOne('/api/movies/cache/stats');
      expect(req.request.method).toBe('GET');
      req.flush({ totalSizeBytes: 1000, maxSizeBytes: 10000, usagePercent: 10, movieCount: 5 });

      const stats = await statsPromise;
      expect(stats.movieCount).toBe(5);
    });
  });
});
