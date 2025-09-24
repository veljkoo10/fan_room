import { Component, DestroyRef } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { LoginRequest } from "../../models/auth.model";
import { finalize } from 'rxjs/operators';
import { takeUntilDestroyed } from "@angular/core/rxjs-interop";
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  public imagePath = 'assets/billiards.jpg';
  public submitting = false;
  public popupMessage: string | null = null;
  showForgotPasswordModal = false;
  public popupType: 'success' | 'error' = 'error';
  public popupTitle: string = 'Login Failed';

  public formFields = [
    {
      name: 'email',
      label: 'Email address',
      type: 'email',
      placeholder: 'Enter your email',
      autocomplete: 'username',
      validators: [Validators.required, Validators.email]
    },
    {
      name: 'password',
      label: 'Password',
      type: 'password',
      placeholder: 'Enter your password',
      autocomplete: 'current-password',
      validators: [Validators.required, Validators.minLength(6)]
    }
  ];

  public loginForm = new FormGroup(
    this.formFields.reduce((controls, field) => {
      controls[field.name] = new FormControl('', field.validators);
      return controls;
    }, {} as { [key: string]: FormControl })
  );

  constructor(private authService: AuthService, private router: Router, private destroyRef: DestroyRef) {}

  public onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    const data: LoginRequest = this.loginForm.value as LoginRequest;

    this.authService.login(data)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.submitting = false)
      )
      .subscribe({
        next: () => {
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          console.error('Login error:', err);
          this.showPopup(err.error?.message || 'Login failed');
        }
      });
  }

  public showPopup(message: string, type: 'success' | 'error' = 'error', title?: string): void {
    this.popupMessage = message;
    this.popupType = type;
    this.popupTitle = title || (type === 'error' ? 'Error' : 'Success');
  }

  public closePopup(): void {
    this.popupMessage = null;
  }

  public goToRegister(): void {
    this.router.navigate(['/register']);
  }

  forgotPasswordForm = new FormGroup({
    email: new FormControl('', [Validators.required, Validators.email])
  });

  sendResetLink() {
    if (this.forgotPasswordForm.valid) {
      const email = this.forgotPasswordForm.value.email;
      if (email) {
        this.authService.sendResetEmail(email)
          .pipe(takeUntilDestroyed(this.destroyRef))
          .subscribe({
            next: (res) => {
              console.log('Response from backend:', res);
              this.showPopup(res, 'success', 'Email Sent');
              this.forgotPasswordForm.reset();
              this.showForgotPasswordModal = false;
            },
            error: (err) => {
              console.error('Error from backend:', err);
              this.showPopup(err.error?.message || 'Email does not exist', 'error');
            }
          });
      } else {
        this.showPopup('Email is required', 'error');
      }
    }
  }

  closeForgotPasswordModal() {
    this.showForgotPasswordModal = false;
    this.forgotPasswordForm.reset();
  }

}
