
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { SupportWebSocketService } from '../../services/support-websocket.service';

interface SupportConversation {
  conversationId: string;
  userId: number;
  agentId: number;
  customerName?: string;
  status: 'WAITING' | 'ACTIVE' | 'CLOSED' | 'CANCELLED';
  lastMessage?: string;
  lastMessageAt?: string;
  messageCount?: number;
}

interface SupportMessage {
  messageId: string;
  conversationId: string;
  senderId: number;
  senderType: 'USER' | 'AGENT';
  content: string;
  sentAt: string;
}

@Component({
  selector: 'app-customer-care-conversations',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './customer-care-conversations.component.html',
  styleUrl: './customer-care-conversations.component.css'
})
export class CustomerCareConversationsComponent implements OnInit, OnDestroy {

  private readonly apiUrl = environment.BASE_URL;

  conversations: SupportConversation[] = [];
  selectedConversation?: SupportConversation;

  messages: SupportMessage[] = [];
  messageContent = '';

  loadingConversations = false;
  loadingMessages = false;
  chatOpen = false;
  sendingMessage = false;
  connected = false;

  private subscriptions = new Subscription();
  private conversationSubscription?: { unsubscribe: () => void };

  constructor(
    private readonly http: HttpClient,
    private readonly supportWebSocketService: SupportWebSocketService
  ) {}

  ngOnInit(): void {
    this.loadAssignedConversations();
    this.initializeWebSocket();
  }

  loadAssignedConversations(): void {
    this.loadingConversations = true;

    this.http.get<SupportConversation[]>(
      `${this.apiUrl}/api/v1/user-service/support/conversations/assigned`
    ).subscribe({
      next: (response) => {
        this.conversations = response ?? [];
        this.loadingConversations = false;
      },
      error: (error) => {
        console.error('Unable to load assigned conversations', error);
        this.loadingConversations = false;
      }
    });
  }

  private initializeWebSocket(): void {
    const token = this.getToken();

    if (!token) {
      console.error('Authentication token not found');
      return;
    }

    this.supportWebSocketService.connect(token);

    this.subscriptions.add(
      this.supportWebSocketService.connected$.subscribe(connected => {
        this.connected = connected;

        if (connected && this.selectedConversation && this.chatOpen) {
          this.subscribeToSelectedConversation();
        } else if (!connected) {
          this.conversationSubscription?.unsubscribe();
          this.conversationSubscription = undefined;
        }
      })
    );

    this.subscriptions.add(
      this.supportWebSocketService.messages$.subscribe(
        (message: SupportMessage) => {
          if (
            this.selectedConversation &&
            message.conversationId === this.selectedConversation.conversationId
          ) {
            // Avoid duplicate messages if the server sends an existing message.
            const alreadyExists = this.messages.some(
              existing => existing.messageId === message.messageId
            );

            if (!alreadyExists) {
              this.messages.push(message);
              this.scrollToBottom();
            }
          }

          // Update the preview in the conversation list.
          const conversation = this.conversations.find(
            item => item.conversationId === message.conversationId
          );

          if (conversation) {
            conversation.lastMessage = message.content;
            conversation.lastMessageAt = message.sentAt;
          }
        }
      )
    );
  }

  openConversation(conversation: SupportConversation): void {
    this.selectedConversation = conversation;
    this.chatOpen = true;
    this.messages = [];

    this.loadConversationHistory(conversation.conversationId);

    if (this.connected) {
      this.subscribeToSelectedConversation();
    }
  }

  private loadConversationHistory(conversationId: string): void {
    this.loadingMessages = true;

    this.http.get<SupportMessage[]>(
      `${this.apiUrl}/api/v1/user-service/common/conversations/${conversationId}/messages`
    ).subscribe({
      next: (response) => {
        // Load previous messages from MongoDB.
        this.messages = response ?? [];
        this.loadingMessages = false;
        this.scrollToBottom();
      },
      error: (error) => {
        console.error('Unable to load conversation history', error);
        this.loadingMessages = false;
      }
    });
  }

  private subscribeToSelectedConversation(): void {
    if (!this.selectedConversation || !this.connected) {
      return;
    }

    this.conversationSubscription?.unsubscribe();

    this.conversationSubscription =
      this.supportWebSocketService.subscribeToConversation(
        this.selectedConversation.conversationId
      );
  }

  sendMessage(): void {
    const content = this.messageContent.trim();

    if (
      !content ||
      !this.selectedConversation ||
      !this.connected ||
      this.selectedConversation.status !== 'ACTIVE'
    ) {
      return;
    }

    this.sendingMessage = true;

    try {
      this.supportWebSocketService.sendMessage(
        this.selectedConversation.conversationId,
        content
      );

      this.messageContent = '';
    } catch (error) {
      console.error('Unable to send message', error);
    } finally {
      this.sendingMessage = false;
    }
  }

  closeChat(): void {
    this.chatOpen = false;
    this.conversationSubscription?.unsubscribe();
    this.conversationSubscription = undefined;
    this.selectedConversation = undefined;
    this.messages = [];

    // Do not close the server-side conversation here.
    // This only closes the popup.
  }

  private getToken(): string | null {
    const raw = sessionStorage.getItem('moneyfi.auth');

    if (!raw) {
      return null;
    }

    try {
      const parsed = JSON.parse(raw);

      if (typeof parsed === 'string') {
        return parsed;
      }

      return parsed.accessToken ?? parsed.token ?? parsed.jwt ?? null;
    } catch {
      return raw;
    }
  }

  private scrollToBottom(): void {
    setTimeout(() => {
      const element = document.getElementById('customer-care-messages');

      if (element) {
        element.scrollTop = element.scrollHeight;
      }
    });
  }

  getInitials(name?: string): string {
    if (!name) {
      return 'CU';
    }

    return name
      .split(' ')
      .map(part => part.charAt(0))
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  ngOnDestroy(): void {
    this.conversationSubscription?.unsubscribe();
    this.subscriptions.unsubscribe();

    // Disconnect only when leaving the dashboard.
    this.supportWebSocketService.disconnect();
  }

  get activeCount(): number {
    return this.conversations.filter(c => c.status === 'ACTIVE').length;
  }

  get waitingCount(): number {
    return this.conversations.filter(c => c.status === 'WAITING').length;
  }
}
