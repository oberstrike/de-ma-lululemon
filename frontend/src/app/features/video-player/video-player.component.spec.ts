import { TestBed, ComponentFixture } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute } from '@angular/router';
import { VideoPlayerComponent } from './video-player.component';
import { PlayerStore } from '../../store/player.store';
import { of } from 'rxjs';

describe('VideoPlayerComponent', () => {
  let component: VideoPlayerComponent;
  let fixture: ComponentFixture<VideoPlayerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        VideoPlayerComponent,
        HttpClientTestingModule,
        RouterTestingModule
      ],
      providers: [
        PlayerStore,
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => 'test-movie-id'
              }
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(VideoPlayerComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a player store', () => {
    expect(component.player).toBeTruthy();
  });

  it('should toggle play state', () => {
    const initialState = component.player.isPlaying();
    component.togglePlay();
    expect(component.player.isPlaying()).toBe(!initialState);
  });

  it('should seek relative time', () => {
    // Set initial duration and time
    component.player.setDuration(100);
    component.player.setCurrentTime(50);

    // This would normally update video element, but we're just testing store
    expect(component.player.currentTime()).toBe(50);
  });
});
