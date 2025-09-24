import {Injectable, Injector} from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '../models/auth.model';
import { RefreshTokenRequest, RefreshTokenResponse } from '../models/refresh-token.interface';
import {environment} from "../../environments/environment";
import {NotificationService} from "./notification.service";

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly apiUrl = environment.apiUrl;
  private readonly authUrl = `${this.apiUrl}/auth`;
  private readonly usersUrl = `${this.apiUrl}/users`;
  private loggedIn = new BehaviorSubject<boolean>(!!localStorage.getItem('token'));
  public loggedIn$ = this.loggedIn.asObservable();

  constructor(private http: HttpClient, private injector: Injector) {}

  public signup(data: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.authUrl}/signup`, data);
  }

  public login(data: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.authUrl}/login`, data).pipe(
      tap(res => {
        this.setToken(res.token);
        if (res.refreshToken) this.setRefreshToken(res.refreshToken);
      })
    );
  }

  public setRefreshToken(token: string): void {
    localStorage.setItem('refreshToken', token);
  }

  public setToken(token: string): void {
    localStorage.setItem('token', token);
    this.loggedIn.next(true);
  }

  public isLoggedIn(): boolean {
    return !!localStorage.getItem('token');
  }

  public getToken(): string | null {
    return localStorage.getItem('token');
  }

  private decodeToken(): any | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      return JSON.parse(atob(token.split('.')[1]));
    } catch {
      return null;
    }
  }

  public getUserRole(): string | null {
    const payload = this.decodeToken();
    if (!payload?.role) return null;
    return payload.role.startsWith('ROLE_') ? payload.role.substring(5) : payload.role;
  }

  public getUsername(): string {
    const payload = this.decodeToken();
    return payload?.username || '';
  }

  public logout(): void {
    const refreshToken = localStorage.getItem('refreshToken');
    if (refreshToken) {
      this.http.post(`${this.authUrl}/logout`, { refreshToken }, {
        headers: new HttpHeaders({ 'Content-Type': 'application/json' })
      }).subscribe(() => this.clearAuthData(), () => this.clearAuthData());
    } else {
      this.clearAuthData();
      const notificationService = this.injector.get(NotificationService);
      notificationService.disconnect();
    }
  }

  private clearAuthData(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    this.loggedIn.next(false);
  }

  public isAdmin(): boolean {
    return this.getUserRole() === 'ADMIN';
  }

  public sendResetEmail(email: string): Observable<string> {
    const params = new HttpParams().set('email', email);
    return this.http.get(`${this.authUrl}/sendEmail`, { params, responseType: 'text' });
  }
  public refreshToken(refreshToken: string): Observable<RefreshTokenResponse> {
    return this.http.post<RefreshTokenResponse>(
      `${this.authUrl}/refresh-token`,
      { refreshToken },
      { headers: { 'Content-Type': 'application/json' } }
    );
  }

  public getAllUsernames(): Observable<string[]> {
    return this.http.get<string[]>(`${this.usersUrl}/all`);
  }

  public getCurrentUserId(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload?.id || null;
    } catch {
      return null;
    }
  }

  public getUserIdByUsername(username: string): Observable<string> {
    return this.http.get<string>(`${this.usersUrl}/id/${username}`);
  }

  public resetPassword(token: string, newPassword: string): Observable<string> {
    const params = new HttpParams().set('token', token);
    const body = { password: newPassword };
    return this.http.post(`${this.authUrl}/reset-password`, body, { params, responseType: 'text' });
  }

}
