export enum UserRole {
  ADMIN = 'ADMIN',
  USER = 'USER'
}

export interface UserResponse {
  firstName: string;
  lastName: string;
  email: string;
  username: string;
  role: UserRole;
}
