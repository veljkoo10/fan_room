import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Reservation} from "../models/reservation.interface";
import {environment} from "../../environments/environment";


@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private apiUrl = `${environment.apiUrl}/reservations`;
  private reservationUrl = `${environment.apiUrl}/reservation`;

  constructor(private http: HttpClient) {}

  getAllReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(this.apiUrl);
  }

  createReservation(reservation: any): Observable<Reservation> {
    return this.http.post<Reservation>(this.apiUrl, reservation);
  }

  cancelReservation(reservationId: string): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${reservationId}/cancel`, {});
  }

  createBlockedReservation(reservation: any): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.apiUrl}/admin/blocked`, reservation);
  }

  removeFromReservation(reservationId: string): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${reservationId}/remove`, {});
  }

  joinReservation(reservationId: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/${reservationId}/join`, {});
  }

  rateReservation(reservationId: string, rating: any): Observable<any> {
    return this.http.post<any>(`${this.reservationUrl}/${reservationId}/ratings`, rating);
  }

  hasUserRated(reservationId: string): Observable<boolean> {
    return this.http.get<boolean>(`${this.reservationUrl}/${reservationId}/has-rated`);
  }

  getRatings(reservationId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.reservationUrl}/${reservationId}/ratings`);
  }

  deleteRating(reservationId: string): Observable<void> {
    return this.http.delete<void>(`${this.reservationUrl}/${reservationId}/ratings`);
  }

}
