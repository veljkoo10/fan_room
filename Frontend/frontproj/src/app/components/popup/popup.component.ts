import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-popup',
  templateUrl: './popup.component.html',
  styleUrls: ['./popup.component.css']
})
export class PopupComponent {
  @Input() message: string | null = null;
  @Input() type: 'success' | 'error' | 'confirm' = 'success';
  @Input() title: string = '';

  @Output() closed = new EventEmitter<void>();
  @Output() confirmed = new EventEmitter<void>();
  public close(): void {
    this.closed.emit();
  }

  public confirm(): void {
    this.confirmed.emit();
    this.close();
  }
}
