import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class NotificationStateService {
  private hasUnreadSubject = new BehaviorSubject<boolean>(false);

  setUnread(value: boolean) {
    this.hasUnreadSubject.next(value);
  }

  getUnread(): Observable<boolean> {
    return this.hasUnreadSubject.asObservable();
  }
}
