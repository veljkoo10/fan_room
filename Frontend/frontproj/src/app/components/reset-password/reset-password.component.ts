import { Component, OnInit, DestroyRef } from '@angular/core';
import { FormGroup, FormControl, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import { finalize } from 'rxjs/operators';
import {AuthService} from "../../services/auth.service";
import {NotificationService} from "../../services/notification.service";

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {

  resetPasswordForm = new FormGroup({
    password: new FormControl('', [
      Validators.required,
      (control: AbstractControl): ValidationErrors | null => {
        const value = control.value || '';
        return /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>]).{8,}$/.test(value)
          ? null
          : { weakPassword: true };
      }
    ]),
    confirmPassword: new FormControl('', [Validators.required])
  }, {
    validators: (group: AbstractControl): ValidationErrors | null => {
      const password = group.get('password')?.value;
      const confirm = group.get('confirmPassword')?.value;
      return password && confirm && password !== confirm ? { passwordMismatch: true } : null;
    }
  });

  token: string | null = null;
  submitting = false;
  popupMessage: string | null = null;
  popupType: 'success' | 'error' = 'error';
  popupTitle: string = 'Error';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router,
    private destroyRef: DestroyRef
  ) {}

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParamMap.get('token');
  }

  onSubmit(): void {
    if (this.resetPasswordForm.invalid || !this.token) return;

    this.submitting = true;
    const newPassword = this.resetPasswordForm.value.password;

    if (newPassword) {
      this.authService.resetPassword(this.token, newPassword)
        .pipe(
          takeUntilDestroyed(this.destroyRef),
          finalize(() => this.submitting = false)
        )
        .subscribe({
          next: (response: any) => {
            const data = typeof response === 'string' ? JSON.parse(response) : response;
            const userId = data.userId;

            if (userId) {
              const message = 'Your password has been successfully updated.';
              this.notificationService.sendNotification(message, userId);
            }

            this.showPopup('Password has been reset successfully. You can now log in.', 'success', 'Success');
            setTimeout(() => this.router.navigate(['/login']), 3000);
          },
          error: (err) => {
            this.showPopup(err.error?.message || 'Failed to reset password.', 'error');
          }
        });
    }
  }

  showPopup(message: string, type: 'success' | 'error' = 'error', title?: string): void {
    this.popupMessage = message;
    this.popupType = type;
    this.popupTitle = title || (type === 'error' ? 'Error' : 'Success');
  }

  closePopup(): void {
    this.popupMessage = null;
  }
}
