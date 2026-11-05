
import {
  Component,
  OnDestroy,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';

import { SupportWebSocketService } from '../../services/support-websocket.service';
import { environment } from '../../../environments/environment';

interface SupportConversationResponse {
  conversationId: string;
  userId: string;
  agentId: string | null;
  status: 'WAITING' | 'ACTIVE' | 'CLOSED' | 'CANCELLED';
  message: string;
}

interface SupportMessage {
  messageId?: string;
  conversationId: string;
  senderId: string;
  senderType: 'USER' | 'AGENT';
  content: string;
  sentAt: string;
}

@Component({
  selector: 'app-support-chat',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './support-chat.component.html',
  styleUrl: './support-chat.component.css'
})
export class SupportChatComponent implements OnInit, OnDestroy {

  isChatOpen = false;
  isStartingConversation = false;
  isConnected = false;

  conversationId: string | null = null;
  conversationStatus: 'WAITING' | 'ACTIVE' | 'CLOSED' | 'CANCELLED' | null = null;

  messageText = '';
  messages: SupportMessage[] = [];

  private subscriptions = new Subscription();
  private conversationSubscription?: any;

  constructor(
    private http: HttpClient,
    private supportWebSocket: SupportWebSocketService
  ) {}

  ngOnInit(): void {

    this.subscriptions.add(
      this.supportWebSocket.connected$.subscribe(connected => {
        this.isConnected = connected;

        if (connected && this.conversationId &&
            this.conversationStatus === 'ACTIVE' &&
            !this.conversationSubscription) {

          this.subscribeToMessages();
        }

        if (!connected) {
          this.conversationSubscription?.unsubscribe();
          this.conversationSubscription = undefined;
        }
      })
    );

    this.subscriptions.add(
      this.supportWebSocket.messages$.subscribe(message => {
        this.messages.push(message);
        this.scrollToBottom();
      })
    );
  }

  openChat(): void {

    this.isChatOpen = true;

    if (this.conversationId) {
      return;
    }

    this.startConversation();
  }

  closeChat(): void {
    this.isChatOpen = false;

    // Closing the popup does not close the actual conversation.
    // The user can reopen it later.
  }

  startConversation(): void {

    this.isStartingConversation = true;

    // Replace this with your actual JWT retrieval logic
    const token = this.getAuthToken();

    if (!token) {
      this.isStartingConversation = false;
      console.error('Authentication token not found');
      return;
    }

    this.http.post<SupportConversationResponse>(
      `${environment.BASE_URL}/api/v1/user-service/support/conversations`,
      {}
    ).subscribe({
      next: (response) => {

        this.conversationId = response.conversationId;
        this.conversationStatus = response.status;

        this.supportWebSocket.connect(token);

        this.isStartingConversation = false;

        if (response.status === 'ACTIVE') {
          this.subscribeWhenConnected();
        }
      },
      error: (error) => {
        console.error('Unable to start support conversation', error);
        this.isStartingConversation = false;
      }
    });
  }

  private subscribeWhenConnected(): void {

    if (this.isConnected) {
      this.subscribeToMessages();
    }
  }

  private subscribeToMessages(): void {

    if (!this.conversationId || this.conversationSubscription) {
      return;
    }

    this.conversationSubscription =
      this.supportWebSocket.subscribeToConversation(this.conversationId);
  }

  sendMessage(): void {

    const content = this.messageText.trim();

    if (!content || !this.conversationId || !this.isConnected) {
      return;
    }

    this.supportWebSocket.sendMessage(
      this.conversationId,
      content
    );

    this.messageText = '';
  }

  private getAuthToken(): string | null {

    // Adapt this to the actual structure stored by your login flow.
    const auth = sessionStorage.getItem('moneyfi.auth');

    if (!auth) {
      return null;
    }

    try {
      const parsed = JSON.parse(auth);
      return parsed.token ?? parsed.accessToken ?? auth;
    } catch {
      return auth;
    }
  }

  private scrollToBottom(): void {

    setTimeout(() => {
      const container = document.querySelector('.chat-messages');

      if (container) {
        container.scrollTop = container.scrollHeight;
      }
    });
  }

  ngOnDestroy(): void {
    this.conversationSubscription?.unsubscribe();
    this.subscriptions.unsubscribe();
  }
}
