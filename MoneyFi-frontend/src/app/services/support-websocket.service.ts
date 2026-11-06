
import { Injectable, OnDestroy } from '@angular/core';
import {
  Client,
  IMessage,
  StompSubscription
} from '@stomp/stompjs';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class SupportWebSocketService implements OnDestroy {

  private stompClient?: Client;

  private messageSubject = new Subject<any>();
  private connectedSubject = new BehaviorSubject<boolean>(false);

  messages$: Observable<any> = this.messageSubject.asObservable();
  connected$: Observable<boolean> = this.connectedSubject.asObservable();

  baseUrl = environment.BASE_URL;

  connect(token: string): void {
    if (this.stompClient?.active) {
      return;
    }

    const brokerUrl = this.baseUrl.replace('http://', 'ws://').replace('https://', 'wss://')
    + '/api/v1/user-service/ws/support';

    this.stompClient = new Client({
      
      brokerURL: brokerUrl,

      connectHeaders: {
        Authorization: `Bearer ${token}`
      },

      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,

      onConnect: () => {
        console.log('MoneyFi support WebSocket connected');
        this.connectedSubject.next(true);
      },

      onDisconnect: () => {
        this.connectedSubject.next(false);
      },

      onWebSocketClose: () => {
        this.connectedSubject.next(false);
      },

      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message']);
      },

      onWebSocketError: (error) => {
        console.error('WebSocket error:', error);
      }
    });

    this.stompClient.activate();
  }

  subscribeToConversation(
    conversationId: string
  ): StompSubscription {

    if (!this.stompClient?.connected) {
      throw new Error('WebSocket is not connected');
    }

    return this.stompClient.subscribe(
      `/api/v1/user-service/topic/support/${conversationId}`,
      (message: IMessage) => {
        this.messageSubject.next(JSON.parse(message.body));
      }
    );
  }

  sendMessage(conversationId: string, content: string): void {

    if (!this.stompClient?.connected) {
      throw new Error('WebSocket is not connected');
    }

    this.stompClient.publish({
      destination: `/api/v1/user-service/app/support/${conversationId}/send`,
      body: JSON.stringify({ content })
    });
  }

  disconnect(): void {
    this.connectedSubject.next(false);
    void this.stompClient?.deactivate();
    this.stompClient = undefined;
  }

  ngOnDestroy(): void {
    this.disconnect();
    this.messageSubject.complete();
    this.connectedSubject.complete();
  }
}
