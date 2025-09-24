import { Component } from '@angular/core';
import { FormControl, FormGroup, Validators, ValidatorFn, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { RegisterRequest } from "../../models/auth.model";
import {AuthService} from "../../services/auth.service";

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent {
  public imagePath: string = 'assets/darts.jpg';
  public popupMessage: string | null = null;
  public isServerMessage: boolean = false;
  public submitting: boolean = false;

  public registerForm = new FormGroup({
    username: new FormControl('', [Validators.required, Validators.minLength(3)]),
    firstName: new FormControl('', [Validators.required]),
    lastName: new FormControl('', [Validators.required]),
    email: new FormControl('', [Validators.required, Validators.email]),
    password: new FormControl('', [Validators.required, Validators.minLength(6)]),
  });

  public formFields = [
    { name: 'username', label: 'Username', type: 'text', placeholder: 'Enter your username', autocomplete: 'username' },
    { name: 'firstName', label: 'First Name', type: 'text', placeholder: 'Enter your first name' },
    { name: 'lastName', label: 'Last Name', type: 'text', placeholder: 'Enter your last name' },
    { name: 'email', label: 'Email', type: 'email', placeholder: 'Enter your email', autocomplete: 'email' },
    { name: 'password', label: 'Password', type: 'password', placeholder: 'Enter your password', autocomplete: 'new-password' }
  ];

  constructor(private authService: AuthService, private router: Router) {}

  public onSubmit(): void {
    if (!this.registerForm.valid) {
      this.registerForm.markAllAsTouched();
      this.showPopup("Form is not valid. Please check fields.", false);
      return;
    }

    this.submitting = true;
    const data: RegisterRequest = this.registerForm.value as RegisterRequest;

    this.authService.signup(data).subscribe({
      next: () => {
        this.submitting = false;
        this.showPopup("Registration successful! You can now log in.", false);
      },
      error: err => {
        this.submitting = false;
        console.error('Registration error:', err);
        this.showPopup(err.error?.message || 'An error occurred during registration.', true);
      }
    });
  }

  public showPopup(message: string, fromServer: boolean): void {
    this.popupMessage = message;
    this.isServerMessage = fromServer;
  }

  public closePopup(): void {
    if (!this.isServerMessage) {
      this.router.navigate(['/login']);
    }
    this.popupMessage = null;
    this.isServerMessage = false;
  }

  public goToLogin(): void {
    this.router.navigate(['/login']);
  }

  public fieldErrors(fieldName: string): string[] {
    const control = this.registerForm.get(fieldName);
    if (!control || !control.errors) return [];
    const errors: string[] = [];
    if (control.errors['required']) errors.push(`${this.getLabel(fieldName)} is required.`);
    if (control.errors['minlength']) errors.push(`At least ${control.errors['minlength'].requiredLength} characters.`);
    if (control.errors['email']) errors.push('Invalid email format.');
    return errors;
  }

  public getLabel(fieldName: string): string {
    const field = this.formFields.find(f => f.name === fieldName);
    return field ? field.label : fieldName;
  }
}
