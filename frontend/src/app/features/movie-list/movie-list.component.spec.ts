import { TestBed, ComponentFixture } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MovieListComponent } from './movie-list.component';
import { MoviesStore } from '../../store/movies.store';
import { WebSocketService } from '../../services/websocket.service';
import { of } from 'rxjs';

describe('MovieListComponent', () => {
  let component: MovieListComponent;
  let fixture: ComponentFixture<MovieListComponent>;
  let mockWebSocketService: jasmine.SpyObj<WebSocketService>;

  beforeEach(async () => {
    mockWebSocketService = jasmine.createSpyObj('WebSocketService', ['connect', 'getDownloadProgress']);
    mockWebSocketService.getDownloadProgress.and.returnValue(of());

    await TestBed.configureTestingModule({
      imports: [
        MovieListComponent,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        MoviesStore,
        { provide: WebSocketService, useValue: mockWebSocketService }
      ]
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
    expect(compiled.querySelector('h1')?.textContent).toContain('Media Server');
  });

  it('should render the search input', () => {
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    const searchInput = compiled.querySelector('input[type="text"]');
    expect(searchInput).toBeTruthy();
  });
});
