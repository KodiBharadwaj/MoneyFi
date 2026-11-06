
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
  userId: number;
  agentId: number | null;
  status: 'WAITING' | 'ACTIVE' | 'CLOSED' | 'CANCELLED' | 'IN_PROGRESS';
  message: string;
}

interface SupportMessage {
  messageId?: string;
  conversationId: string;
  senderId: number;
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
  isLoadingHistory = false;

  conversationId: string | null = null;

  conversationStatus:
    'WAITING' | 'ACTIVE' | 'CLOSED' | 'CANCELLED' | 'IN_PROGRESS' | null = null;

  messageText = '';
  messages: SupportMessage[] = [];

  private subscriptions = new Subscription();
  private conversationSubscription?: any;

  constructor(
    private http: HttpClient,
    private supportWebSocket: SupportWebSocketService
  ) {}

  get isChatActive(): boolean {
    return this.conversationStatus === 'ACTIVE' ||
      this.conversationStatus === 'IN_PROGRESS';
  }

  ngOnInit(): void {

    this.subscriptions.add(
      this.supportWebSocket.connected$.subscribe(connected => {

        this.isConnected = connected;

        if (
          connected &&
          this.conversationId &&
          this.isChatActive &&
          !this.conversationSubscription
        ) {
          this.subscribeToMessages();
        }

        if (!connected) {
          this.conversationSubscription?.unsubscribe();
          this.conversationSubscription = undefined;
        }
      })
    );

    this.subscriptions.add(
      this.supportWebSocket.messages$.subscribe(
        (message: SupportMessage) => {

          // Ignore messages belonging to other conversations.
          if (message.conversationId !== this.conversationId) {
            return;
          }

          this.addMessageIfNotExists(message);
          this.scrollToBottom();
        }
      )
    );
  }

  openChat(): void {

    this.isChatOpen = true;

    // Existing conversation: do not create another one.
    if (this.conversationId) {
      return;
    }

    this.startConversation();
  }

  closeChat(): void {
    this.isChatOpen = false;

    // Do not close the actual support conversation.
    // Keep the WebSocket connection and conversation alive.
  }

  startConversation(): void {

    this.isStartingConversation = true;

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

        // Connect to WebSocket.
        this.supportWebSocket.connect(token);

        // Load previous messages from MongoDB.
        this.loadConversationHistory();

        this.isStartingConversation = false;

        if (this.isChatActive) {
          this.subscribeWhenConnected();
        }
      },

      error: (error) => {
        console.error('Unable to start support conversation', error);
        this.isStartingConversation = false;
      }
    });
  }

  /**
   * Fetch previous messages from MongoDB.
   */
  private loadConversationHistory(): void {

    if (!this.conversationId) {
      return;
    }

    this.isLoadingHistory = true;

    this.http.get<SupportMessage[]>(
      `${environment.BASE_URL}/api/v1/user-service/common/conversations/${this.conversationId}/messages`
    ).subscribe({

      next: (response) => {

        const previousMessages = response ?? [];

        // Merge history with any real-time messages received
        // while the HTTP request was in progress.
        previousMessages.forEach(message => {
          this.addMessageIfNotExists(message);
        });

        // Display messages in chronological order.
        this.messages.sort(
          (a, b) =>
            new Date(a.sentAt).getTime() -
            new Date(b.sentAt).getTime()
        );

        this.isLoadingHistory = false;
        this.scrollToBottom();
      },

      error: (error) => {
        console.error('Unable to load conversation history', error);
        this.isLoadingHistory = false;
      }
    });
  }

  /**
   * Prevent duplicate messages when history and WebSocket
   * deliver the same message.
   */
  private addMessageIfNotExists(message: SupportMessage): void {

    const alreadyExists = this.messages.some(existing => {

      if (existing.messageId && message.messageId) {
        return existing.messageId === message.messageId;
      }

      return existing.conversationId === message.conversationId &&
        existing.senderId === message.senderId &&
        existing.content === message.content &&
        existing.sentAt === message.sentAt;
    });

    if (!alreadyExists) {
      this.messages.push(message);

      this.messages.sort(
        (a, b) =>
          new Date(a.sentAt).getTime() -
          new Date(b.sentAt).getTime()
      );
    }
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

    if (
      !content ||
      !this.conversationId ||
      !this.isConnected ||
      !this.isChatActive
    ) {
      return;
    }

    this.supportWebSocket.sendMessage(
      this.conversationId,
      content
    );

    this.messageText = '';
  }

  private getAuthToken(): string | null {

    const auth = sessionStorage.getItem('moneyfi.auth');

    if (!auth) {
      return null;
    }

    try {
      const parsed = JSON.parse(auth);

      if (typeof parsed === 'string') {
        return parsed;
      }

      return parsed.token ?? parsed.accessToken ?? parsed.jwt ?? null;

    } catch {
      return auth;
    }
  }

  private scrollToBottom(): void {

    setTimeout(() => {

      if (!this.isChatOpen) {
        return;
      }

      const container = document.querySelector('.chat-messages');

      if (container) {
        container.scrollTop = container.scrollHeight;
      }

    });
  }

  ngOnDestroy(): void {

    this.conversationSubscription?.unsubscribe();
    this.subscriptions.unsubscribe();

    // Do not disconnect the shared WebSocket here if this
    // service is also being used by another active component.
  }
}
