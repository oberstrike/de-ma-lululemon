import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService, Movie, MovieCreateRequest } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
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
          createdAt: '2024-01-01'
        }
      ];

      service.getMovies().subscribe(movies => {
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
  });

  describe('getMovie', () => {
    it('should return a single movie', () => {
      const mockMovie: Movie = {
        id: '1',
        title: 'Test Movie',
        description: 'A test',
        cached: true,
        status: 'READY',
        createdAt: '2024-01-01'
      };

      service.getMovie('1').subscribe(movie => {
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
        megaUrl: 'https://mega.nz/file/test'
      };

      const mockResponse: Movie = {
        id: '2',
        title: 'New Movie',
        cached: false,
        status: 'PENDING',
        createdAt: '2024-01-01'
      };

      service.createMovie(request).subscribe(movie => {
        expect(movie.id).toBe('2');
        expect(movie.title).toBe('New Movie');
      });

      const req = httpMock.expectOne('/api/movies');
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(request);
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

  describe('getCategories', () => {
    it('should return categories', () => {
      service.getCategories().subscribe(categories => {
        expect(categories.length).toBe(1);
      });

      const req = httpMock.expectOne('/api/categories');
      expect(req.request.method).toBe('GET');
      req.flush([{ id: '1', name: 'Action', movieCount: 5 }]);
    });
  });
});
