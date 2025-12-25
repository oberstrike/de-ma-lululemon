import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';

import { ApiService, Movie, MovieCreateRequest } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ApiService],
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('getMovies', () => {
    it('should return movies', () => {
      const mockMovies: Movie[] = [
        {
          id: '1',
          title: 'Test Movie',
          cached: false,
          status: 'PENDING',
          createdAt: '2024-01-01',
        },
      ];

      service.getMovies().subscribe((movies) => {
        expect(movies.length).toBe(1);
        expect(movies[0].title).toBe('Test Movie');
      });

      const req = httpMock.expectOne('/api/movies');
      expect(req.request.method).toBe('GET');
      req.flush(mockMovies);
    });

    it('should handle search parameter', () => {
      service.getMovies({ search: 'test' }).subscribe();

      const req = httpMock.expectOne('/api/movies?search=test');
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });

    it('should handle readyOnly parameter', () => {
      service.getMovies({ readyOnly: true }).subscribe();

      const req = httpMock.expectOne('/api/movies?readyOnly=true');
      expect(req.request.method).toBe('GET');
      req.flush([]);
    });
  });

  describe('getMovie', () => {
    it('should return a single movie', () => {
      const mockMovie: Movie = {
        id: '1',
        title: 'Test Movie',
        description: 'A test',
        cached: true,
        status: 'READY',
        createdAt: '2024-01-01',
      };

      service.getMovie('1').subscribe((movie) => {
        expect(movie.id).toBe('1');
        expect(movie.title).toBe('Test Movie');
      });

      const req = httpMock.expectOne('/api/movies/1');
      expect(req.request.method).toBe('GET');
      req.flush(mockMovie);
    });
  });

  describe('createMovie', () => {
    it('should create a movie', () => {
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

      service.createMovie(request).subscribe((movie) => {
        expect(movie.id).toBe('2');
        expect(movie.title).toBe('New Movie');
      });

      const req = httpMock.expectOne('/api/movies');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
      req.flush(mockResponse);
    });
  });

  describe('updateMovie', () => {
    it('should update a movie', () => {
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

      service.updateMovie('1', request).subscribe((movie) => {
        expect(movie.title).toBe('Updated Movie');
      });

      const req = httpMock.expectOne('/api/movies/1');
      expect(req.request.method).toBe('PUT');
      req.flush(mockResponse);
    });
  });

  describe('deleteMovie', () => {
    it('should delete a movie', () => {
      service.deleteMovie('1').subscribe();

      const req = httpMock.expectOne('/api/movies/1');
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });
  });

  describe('startDownload', () => {
    it('should start a download', () => {
      service.startDownload('1').subscribe();

      const req = httpMock.expectOne('/api/movies/1/download');
      expect(req.request.method).toBe('POST');
      req.flush(null);
    });
  });

  describe('getStreamUrl', () => {
    it('should return the stream URL', () => {
      const url = service.getStreamUrl('1');
      expect(url).toBe('/api/stream/1');
    });
  });

  describe('getStreamInfo', () => {
    it('should return stream info', () => {
      const mockInfo = {
        movieId: '1',
        title: 'Test',
        fileSize: 1000,
        contentType: 'video/mp4',
        streamUrl: '/api/stream/1',
        supportsRangeRequests: true,
      };

      service.getStreamInfo('1').subscribe((info) => {
        expect(info.movieId).toBe('1');
        expect(info.supportsRangeRequests).toBe(true);
      });

      const req = httpMock.expectOne('/api/stream/1/info');
      expect(req.request.method).toBe('GET');
      req.flush(mockInfo);
    });
  });

  describe('getCategories', () => {
    it('should return categories', () => {
      service.getCategories().subscribe((categories) => {
        expect(categories.length).toBe(1);
      });

      const req = httpMock.expectOne('/api/categories');
      expect(req.request.method).toBe('GET');
      req.flush([{ id: '1', name: 'Action', movieCount: 5 }]);
    });
  });

  describe('getActiveDownloads', () => {
    it('should return active downloads', () => {
      service.getActiveDownloads().subscribe((downloads) => {
        expect(downloads.length).toBe(1);
      });

      const req = httpMock.expectOne('/api/downloads');
      expect(req.request.method).toBe('GET');
      req.flush([{ movieId: '1', progress: 50, status: 'IN_PROGRESS' }]);
    });
  });

  describe('getCacheStats', () => {
    it('should return cache stats', () => {
      service.getCacheStats().subscribe((stats) => {
        expect(stats.movieCount).toBe(5);
      });

      const req = httpMock.expectOne('/api/movies/cache/stats');
      expect(req.request.method).toBe('GET');
      req.flush({ totalSizeBytes: 1000, maxSizeBytes: 10000, usagePercent: 10, movieCount: 5 });
    });
  });
});
