import { Injectable } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import * as SockJS from 'sockjs-client';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { AuthService } from './auth.service';
import { NotificationMessage } from "../models/notification.model";
import { environment } from "../../environments/environment";

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private readonly apiUrl = environment.apiUrl;
  private readonly wsUrl = environment.wsUrl;

  private stompClient: Client;

  private notificationsSubject = new BehaviorSubject<NotificationMessage[]>([]);

  constructor(private authService: AuthService, private http: HttpClient) {
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(this.wsUrl),
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('STOMP client connected.');
        this.subscribeToNotifications();
        this.loadNotificationsFromServer();
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
      }
    });
  }

  public connect(): void {
    const userId = this.authService.getCurrentUserId();
    if (!userId) {
      console.error('Cannot connect NotificationService: User ID is not available.');
      return;
    }
    if (!this.stompClient.active) {
      console.log('Activating STOMP client for user:', userId);
      this.stompClient.activate();
    }
  }

  public disconnect(): void {
    if (this.stompClient.active) {
      this.stompClient.deactivate();
      console.log('STOMP client deactivated.');
    }
    this.notificationsSubject.next([]);
  }


  private subscribeToNotifications(): void {
    const currentUserId = this.authService.getCurrentUserId();
    if (!currentUserId) {
      console.error('Subscription failed: User ID not found after connect.');
      return;
    }

    this.stompClient.subscribe(
      `/topic/notifications/${currentUserId}`,
      (message: IMessage) => {
        const notification: NotificationMessage = JSON.parse(message.body);
        const currentNotifications = this.notificationsSubject.getValue();
        this.notificationsSubject.next([notification, ...currentNotifications]);
      }
    );
  }

  public sendNotification(message: string, userId: string): void {
    if (!this.stompClient.connected) {
      console.error('Cannot send notification: STOMP client is not connected.');
      return;
    }

    const payload = { message, userId };
    this.stompClient.publish({
      destination: '/app/sendMessage',
      body: JSON.stringify(payload),
    });
  }

  public getNotifications(): Observable<NotificationMessage[]> {
    return this.notificationsSubject.asObservable();
  }

  public loadNotificationsFromServer(): void {
    const userId = this.authService.getCurrentUserId();
    if (!userId) return;

    this.http.get<NotificationMessage[]>(`${this.apiUrl}/notifications/${userId}`)
      .subscribe(data => {
        this.notificationsSubject.next(data);
      });
  }

  public markAllAsSeen(userId: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/notifications/${userId}/seen`, {}).pipe(
      tap(() => {
        const currentNotifications = this.notificationsSubject.getValue();
        const updatedNotifications = currentNotifications.map(n => ({ ...n, seen: true }));
        this.notificationsSubject.next(updatedNotifications);
      })
    );
  }
}
