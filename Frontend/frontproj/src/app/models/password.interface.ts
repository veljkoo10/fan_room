export interface PasswordResetRequest {
  username?: string;
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}
