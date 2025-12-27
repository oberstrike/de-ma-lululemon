import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { ConfirmationService } from 'primeng/api';
import { of, Subject } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { CurrentUserService } from '../../services/current-user.service';
import { WebSocketService } from '../../services/websocket.service';
import { MoviesStore } from '../../store/movies.store';
import { MovieDetailComponent } from './movie-detail.component';

describe('MovieDetailComponent', () => {
  let component: MovieDetailComponent;
  let fixture: ComponentFixture<MovieDetailComponent>;
  let httpMock: HttpTestingController;
  let currentUser: CurrentUserService;
  let mockWebSocketService: {
    connect: ReturnType<typeof vi.fn>;
    getDownloadProgress: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    mockWebSocketService = {
      connect: vi.fn(),
      getDownloadProgress: vi.fn().mockReturnValue(of()),
    };

    await TestBed.configureTestingModule({
      imports: [MovieDetailComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideNoopAnimations(),
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
                get: () => 'movie-1',
              },
            },
          },
        },
      ],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    currentUser = TestBed.inject(CurrentUserService);
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
      cached: true,
    });

    expect(component.movie()).toBeTruthy();
    expect(component.movie()?.title).toBe('Test Movie');
  });

  it('should connect to websocket on init', () => {
    fixture.detectChanges();
    httpMock.expectOne('/api/movies/movie-1').flush({ id: 'movie-1', title: 'Test' });

    expect(mockWebSocketService.connect).toHaveBeenCalled();
  });

  it('should reload movie details when user changes', async () => {
    fixture.detectChanges();

    const firstReq = httpMock.expectOne('/api/movies/movie-1');
    expect(firstReq.request.headers.get('X-Mock-UserId')).toBe('user-1');
    firstReq.flush({
      id: 'movie-1',
      title: 'User One Movie',
      status: 'READY',
      cached: true,
    });

    currentUser.setUserId('user-2');

    await fixture.whenStable();
    fixture.detectChanges();

    const secondReq = httpMock.expectOne('/api/movies/movie-1');
    expect(secondReq.request.headers.get('X-Mock-UserId')).toBe('user-2');
    secondReq.flush({
      id: 'movie-1',
      title: 'User Two Movie',
      status: 'READY',
      cached: true,
    });

    expect(component.movie()?.title).toBe('User Two Movie');
  });

  it('should format file size correctly', () => {
    expect(component.formatSize(500)).toBe('500 B');
    expect(component.formatSize(1024)).toBe('1.0 KB');
    expect(component.formatSize(1024 * 1024)).toBe('1.0 MB');
    expect(component.formatSize(1024 * 1024 * 1024)).toBe('1.00 GB');
  });

  it('should handle download progress updates', () => {
    const progressSubject = new Subject();
    mockWebSocketService.getDownloadProgress.mockReturnValue(progressSubject);

    fixture.detectChanges();
    httpMock.expectOne('/api/movies/movie-1').flush({
      id: 'movie-1',
      title: 'Test Movie',
      status: 'DOWNLOADING',
    });

    progressSubject.next({
      movieId: 'movie-1',
      progress: 50,
      status: 'IN_PROGRESS' as const,
      bytesDownloaded: 500,
      totalBytes: 1000,
      movieTitle: 'Test',
    });

    expect(component.downloadProgress()?.progress).toBe(50);
  });

  it('should return correct severity for status', () => {
    expect(component.getStatusSeverity('READY')).toBe('success');
    expect(component.getStatusSeverity('DOWNLOADING')).toBe('info');
    expect(component.getStatusSeverity('PENDING')).toBe('warn');
    expect(component.getStatusSeverity('ERROR')).toBe('danger');
    expect(component.getStatusSeverity('UNKNOWN')).toBe('secondary');
  });
});
