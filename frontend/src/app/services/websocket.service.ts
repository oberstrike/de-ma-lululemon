import { Injectable, inject, NgZone } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { DownloadProgress } from './api.service';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client!: Client;
  private ngZone = inject(NgZone);
  private downloadProgress$ = new Subject<DownloadProgress>();
  private connected$ = new Subject<boolean>();
  private initialized = false;

  connect(): void {
    if (this.initialized) return;
    this.initialized = true;

    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = () => {
      this.ngZone.run(() => this.connected$.next(true));

      this.client.subscribe('/topic/downloads', (message: IMessage) => {
        this.ngZone.run(() => {
          const progress: DownloadProgress = JSON.parse(message.body);
          this.downloadProgress$.next(progress);
        });
      });
    };

    this.client.onDisconnect = () => {
      this.ngZone.run(() => this.connected$.next(false));
    };

    this.client.activate();
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.initialized = false;
    }
  }

  getDownloadProgress(): Observable<DownloadProgress> {
    return this.downloadProgress$.asObservable();
  }

  isConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }
}
