import { Injectable, OnDestroy, inject } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { Subject } from 'rxjs';
import { environment } from '../environments/environment';
import { AuthService } from './auth.service';
import { Board } from '../models/board.model';

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {
  private auth = inject(AuthService);

  private client: Client | null = null;
  private boardUpdate$ = new Subject<Board>();

  boardUpdates$ = this.boardUpdate$.asObservable();

  connect(boardId: number) {
    this.client = new Client({
      brokerURL: environment.wsUrl,
      connectHeaders: {
        Authorization: `Bearer ${this.auth.getToken()}`
      },
      onConnect: () => {
        this.client!.subscribe(`/topic/board/${boardId}`, (msg: IMessage) => {
          this.boardUpdate$.next(JSON.parse(msg.body));
        });
      },
      reconnectDelay: 5000
    });

    this.client.activate();
  }

  disconnect() {
    this.client?.deactivate();
    this.client = null;
  }

  ngOnDestroy() {
    this.disconnect();
  }
}
