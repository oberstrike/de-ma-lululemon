import { inject, Injectable, NgZone } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Observable, Subject } from 'rxjs';
import SockJS from 'sockjs-client';

import { DownloadProgressResponse } from '../types';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private client: Client | undefined;
  private readonly ngZone = inject(NgZone);
  private readonly downloadProgress$ = new Subject<DownloadProgressResponse>();
  private readonly connected$ = new Subject<boolean>();
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
      this.ngZone.run(() => {
        this.connected$.next(true);
      });

      this.client?.subscribe('/topic/downloads', (message: IMessage) => {
        this.ngZone.run(() => {
          const progress = JSON.parse(message.body) as DownloadProgressResponse;
          this.downloadProgress$.next(progress);
        });
      });
    };

    this.client.onDisconnect = () => {
      this.ngZone.run(() => {
        this.connected$.next(false);
      });
    };

    this.client.activate();
  }

  disconnect(): void {
    if (this.client) {
      this.client.deactivate();
      this.initialized = false;
    }
  }

  getDownloadProgress(): Observable<DownloadProgressResponse> {
    return this.downloadProgress$.asObservable();
  }

  isConnected(): Observable<boolean> {
    return this.connected$.asObservable();
  }
}
