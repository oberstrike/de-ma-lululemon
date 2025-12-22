import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TestBed, ComponentFixture } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { ActivatedRoute } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ConfirmationService } from 'primeng/api';
import { MovieDetailComponent } from './movie-detail.component';
import { MoviesStore } from '../../store/movies.store';
import { WebSocketService } from '../../services/websocket.service';
import { of } from 'rxjs';

describe('MovieDetailComponent', () => {
  let component: MovieDetailComponent;
  let fixture: ComponentFixture<MovieDetailComponent>;
  let httpMock: HttpTestingController;
  let mockWebSocketService: {
    connect: ReturnType<typeof vi.fn>;
    getDownloadProgress: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    mockWebSocketService = {
      connect: vi.fn(),
      getDownloadProgress: vi.fn().mockReturnValue(of())
    };

    await TestBed.configureTestingModule({
      imports: [
        MovieDetailComponent,
        NoopAnimationsModule
      ],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        MoviesStore,
        ConfirmationService,
        { provide: WebSocketService, useValue: mockWebSocketService },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => 'movie-1'
              }
            }
          }
        }
      ]
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(MovieDetailComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load movie on init', () => {
    fixture.detectChanges();

    const req = httpMock.expectOne('/api/movies/movie-1');
    expect(req.request.method).toBe('GET');
    req.flush({
      id: 'movie-1',
      title: 'Test Movie',
      status: 'READY',
      cached: true
    });

    expect(component.movie).toBeTruthy();
    expect(component.movie?.title).toBe('Test Movie');
  });

  it('should connect to websocket on init', () => {
    fixture.detectChanges();
    httpMock.expectOne('/api/movies/movie-1').flush({ id: 'movie-1', title: 'Test' });

    expect(mockWebSocketService.connect).toHaveBeenCalled();
  });

  it('should format file size correctly', () => {
    expect(component.formatSize(500)).toBe('500 B');
    expect(component.formatSize(1024)).toBe('1.0 KB');
    expect(component.formatSize(1024 * 1024)).toBe('1.0 MB');
    expect(component.formatSize(1024 * 1024 * 1024)).toBe('1.00 GB');
  });

  it('should handle download progress updates', () => {
    const progressSubject = of({
      movieId: 'movie-1',
      progress: 50,
      status: 'IN_PROGRESS' as const,
      bytesDownloaded: 500,
      totalBytes: 1000,
      movieTitle: 'Test'
    });
    mockWebSocketService.getDownloadProgress.mockReturnValue(progressSubject);

    fixture.detectChanges();
    httpMock.expectOne('/api/movies/movie-1').flush({
      id: 'movie-1',
      title: 'Test Movie',
      status: 'DOWNLOADING'
    });

    expect(component.downloadProgress?.progress).toBe(50);
  });

  it('should return correct severity for status', () => {
    expect(component.getStatusSeverity('READY')).toBe('success');
    expect(component.getStatusSeverity('DOWNLOADING')).toBe('info');
    expect(component.getStatusSeverity('PENDING')).toBe('warn');
    expect(component.getStatusSeverity('ERROR')).toBe('danger');
    expect(component.getStatusSeverity('UNKNOWN')).toBe('secondary');
  });
});
