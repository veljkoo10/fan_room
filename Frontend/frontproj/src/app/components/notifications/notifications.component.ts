import { Component, NgZone, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

import {NotificationMessage} from "../../models/notification.model";
import {NotificationService} from "../../services/notification.service";
import {AuthService} from "../../services/auth.service";
import {NotificationStateService} from "../../services/notification-state.service";

@Component({
  selector: 'app-notifications',
  templateUrl: './notifications.component.html',
  styleUrls: ['./notifications.component.css']
})
export class NotificationsComponent implements OnInit, OnDestroy {
  notifications: NotificationMessage[] = [];
  private subscription!: Subscription;

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService,
    private notificationState: NotificationStateService,
    private ngZone: NgZone
  ) {}

  ngOnInit(): void {
    const userId = this.authService.getCurrentUserId();
    if (!userId) return;

    this.subscription = this.notificationService.getNotifications().subscribe(notifs => {
      this.ngZone.run(() => this.notifications = notifs);
    });

    this.notificationService.markAllAsSeen(userId).subscribe(() => {
      this.notificationState.setUnread(false);
    });
  }

  ngOnDestroy(): void {
    if (this.subscription) this.subscription.unsubscribe();
  }
}
