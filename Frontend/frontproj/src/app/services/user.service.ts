import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserResponse} from "../models/user.interface";
import {PasswordResetRequest} from "../models/password.interface";
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  public getCurrentUser(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.apiUrl}/me`);
  }

  public resetPassword(request: PasswordResetRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/reset-password`, request);
  }
}
