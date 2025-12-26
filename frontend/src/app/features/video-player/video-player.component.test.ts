import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideZonelessChangeDetection } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';

import { PlayerStore } from '../../store/player.store';
import { VideoPlayerComponent } from './video-player.component';

describe('VideoPlayerComponent', () => {
  let component: VideoPlayerComponent;
  let fixture: ComponentFixture<VideoPlayerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VideoPlayerComponent],
      providers: [
        provideZonelessChangeDetection(),
        provideNoopAnimations(),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        PlayerStore,
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: {
                get: () => 'test-movie-id',
              },
            },
          },
        },
      ],
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

  it('should set duration', () => {
    component.player.setDuration(120);
    expect(component.player.duration()).toBe(120);
  });

  it('should set current time', () => {
    component.player.setCurrentTime(60);
    expect(component.player.currentTime()).toBe(60);
  });

  it('should calculate progress correctly', () => {
    component.player.setDuration(100);
    component.player.setCurrentTime(50);
    expect(component.player.progress()).toBe(50);
  });

  it('should format time correctly', () => {
    component.player.setCurrentTime(65);
    expect(component.player.formattedTime()).toBe('1:05');
  });

  it('should format duration with hours', () => {
    component.player.setDuration(3665);
    expect(component.player.formattedDuration()).toBe('1:01:05');
  });

  it('should toggle mute', () => {
    const initialMuted = component.player.muted();
    component.player.toggleMute();
    expect(component.player.muted()).toBe(!initialMuted);
  });

  it('should set volume', () => {
    component.player.setVolume(0.5);
    expect(component.player.volume()).toBe(0.5);
  });

  it('should clamp volume between 0 and 1', () => {
    component.player.setVolume(1.5);
    expect(component.player.volume()).toBe(1);

    component.player.setVolume(-0.5);
    expect(component.player.volume()).toBe(0);
  });

  it('should toggle fullscreen', () => {
    const initialFullscreen = component.player.isFullscreen();
    component.player.toggleFullscreen();
    expect(component.player.isFullscreen()).toBe(!initialFullscreen);
  });

  it('should show and hide controls', () => {
    component.player.showControls();
    expect(component.player.controlsVisible()).toBe(true);

    component.player.hideControls();
    expect(component.player.controlsVisible()).toBe(false);
  });

  it('should reset player state', () => {
    component.player.setDuration(100);
    component.player.setCurrentTime(50);
    component.player.play();

    component.player.reset();

    expect(component.player.duration()).toBe(0);
    expect(component.player.currentTime()).toBe(0);
    expect(component.player.isPlaying()).toBe(false);
  });

  it('should initialize with isBuffering signal as false', () => {
    expect(component.isBuffering()).toBe(false);
  });

  it('should initialize volume value', () => {
    expect(component.volumeValue).toBe(100);
  });
});
