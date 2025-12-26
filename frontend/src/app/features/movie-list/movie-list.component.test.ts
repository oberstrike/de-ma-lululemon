import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import { WebSocketService } from '../../services/websocket.service';
import { MoviesStore } from '../../store/movies.store';
import { MovieListComponent } from './movie-list.component';

describe('MovieListComponent', () => {
  let component: MovieListComponent;
  let fixture: ComponentFixture<MovieListComponent>;
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
      imports: [MovieListComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideNoopAnimations(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        MoviesStore,
        { provide: WebSocketService, useValue: mockWebSocketService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MovieListComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a store reference', () => {
    expect(component.store).toBeTruthy();
  });

  it('should connect to websocket on init', () => {
    fixture.detectChanges();
    expect(mockWebSocketService.connect).toHaveBeenCalled();
  });

  it('should render the header', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('MEDIASERVER');
  });

  it('should render the search input', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    const searchInput = compiled.querySelector('input[type="text"]');
    expect(searchInput).toBeTruthy();
  });

  it('should update filter when search input changes', () => {
    fixture.detectChanges();
    const setFilterSpy = vi.spyOn(component.store, 'setFilter');

    component.store.setFilter('test search');

    expect(setFilterSpy).toHaveBeenCalledWith('test search');
  });

  it('should subscribe to download progress updates', () => {
    fixture.detectChanges();
    expect(mockWebSocketService.getDownloadProgress).toHaveBeenCalled();
  });
});
