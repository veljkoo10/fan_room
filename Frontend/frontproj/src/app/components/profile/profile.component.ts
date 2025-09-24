import { Component, DestroyRef, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserResponse } from "../../models/user.interface";
import { PasswordResetRequest } from "../../models/password.interface";
import { finalize } from 'rxjs/operators';
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import {UserService} from "../../services/user.service";
import {AuthService} from "../../services/auth.service";
import {NotificationService} from "../../services/notification.service";

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  public profileForm!: FormGroup;
  public passwordForm!: FormGroup;
  public loadingProfile = true;

  public showModal = false;
  public modalTitle = '';
  public modalMessage = '';
  public modalType: 'success' | 'error' = 'success';

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private notificationService: NotificationService,
    private destroyRef: DestroyRef
  ) {}

  public ngOnInit(): void {
    this.initForms();
    this.loadUserProfile();
  }

  private initForms(): void {
    this.profileForm = this.fb.group({
      firstName: [{ value: '', disabled: true }],
      lastName: [{ value: '', disabled: true }],
      email: [{ value: '', disabled: true }],
      username: [{ value: '', disabled: true }],
      role: [{ value: '', disabled: true }]
    });

    this.passwordForm = this.fb.group({
      username: [''],
      oldPassword: ['', Validators.required],
      newPassword: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.pattern('^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{}|;:\'",.<>/?]).+$')
      ]],
      confirmPassword: ['', Validators.required]
    }, { validator: this.passwordsMatch });
  }

  private loadUserProfile(): void {
    this.userService.getCurrentUser()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.loadingProfile = false)
      )
      .subscribe({
        next: (user: UserResponse) => {
          this.profileForm.patchValue(user);
          this.passwordForm.patchValue({ username: user.username });
        },
        error: () => {
          this.showErrorModal('Error loading profile. Please refresh the page.');
        }
      });
  }

  private passwordsMatch(group: FormGroup) {
    const newPassword = group.get('newPassword')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { notMatching: true };
  }

  public onSubmitPassword(): void {
    if (this.passwordForm.invalid) return;

    const payload: PasswordResetRequest = this.passwordForm.value;

    this.userService.resetPassword(payload)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.passwordForm.reset())
      )
      .subscribe({
        next: () => {
          this.showSuccessModal('Your password has been successfully changed.');

          const username = payload.username;
          if (username) {
            this.authService.getUserIdByUsername(username).subscribe({
              next: userId => {
                if (userId) {
                  const message = 'Your password has been successfully updated.';
                  this.notificationService.sendNotification(message, userId);
                }
              }
            });
          }
        },
        error: err => {
          this.showErrorModal(err.error?.message || 'An error occurred. Please try again.');
        }
      });
  }

  public showSuccessModal(message: string): void {
    this.modalTitle = 'Success!';
    this.modalMessage = message;
    this.modalType = 'success';
    this.showModal = true;
  }

  public showErrorModal(message: string): void {
    this.modalTitle = 'Error!';
    this.modalMessage = message;
    this.modalType = 'error';
    this.showModal = true;
  }

  public closeModal(): void {
    this.showModal = false;
  }
}
