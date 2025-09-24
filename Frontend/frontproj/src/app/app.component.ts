import { Component, HostListener, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './services/auth.service';
import { NotificationStateService } from './services/notification-state.service';
import { NotificationService } from './services/notification.service';
import { NotificationMessage } from "./models/notification.model";
import { Subscription } from 'rxjs'; // Importajte Subscription

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent implements OnInit, OnDestroy {
  public title = 'frontproj';
  public imagePath = 'assets/favicon.ico';
  public profilePath = 'assets/profilee.png';
  public showProfileMenu = false;
  public isLoggedIn = false;
  public isAdmin = false;
  public hasUnreadNotifications = false;

  private authSubscription: Subscription | undefined;
  private notificationSubscription: Subscription | undefined;

  constructor(
    private router: Router,
    private authService: AuthService,
    private notificationService: NotificationService,
    private notificationState: NotificationStateService
  ) {}

  ngOnInit(): void {
    this.authSubscription = this.authService.loggedIn$.subscribe(isLoggedIn => {
      this.isLoggedIn = isLoggedIn;
      this.isAdmin = this.authService.isAdmin();

      if (isLoggedIn) {
        this.notificationService.connect();

        this.notificationSubscription = this.notificationService.getNotifications().subscribe((notifs: NotificationMessage[]) => {
          const unread = notifs.some(n => !n.seen);
          this.hasUnreadNotifications = unread;
          this.notificationState.setUnread(unread);
        });

      } else {
        this.notificationService.disconnect();

        this.hasUnreadNotifications = false;
        if (this.notificationSubscription) {
          this.notificationSubscription.unsubscribe();
        }
      }
    });
  }

  ngOnDestroy(): void {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
    if (this.notificationSubscription) {
      this.notificationSubscription.unsubscribe();
    }
  }

  public toggleProfileMenu(): void {
    this.showProfileMenu = !this.showProfileMenu;
  }

  public logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
    this.showProfileMenu = false;
  }

  public goToProfile(): void {
    this.router.navigate(['/profile']);
    this.showProfileMenu = false;
  }

  public goToChart(): void {
    this.router.navigate(['/chart']);
    this.showProfileMenu = false;
  }

  public goToNotification(): void {
    this.router.navigate(['/notifications']);
    this.showProfileMenu = false;
    this.notificationState.setUnread(false);
  }

  public goToDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  public goToSport(): void {
    this.router.navigate(['/sports']);
  }

  public goToReservations(): void {
    this.router.navigate(['/dashboard']);
  }

  @HostListener('document:click', ['$event'])
  public onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    const clickedInsideMenu = target.closest('.profile-menu');
    const clickedOnIcon = target.closest('.profile-icon');
    if (!clickedInsideMenu && !clickedOnIcon) {
      this.showProfileMenu = false;
    }
  }
}
