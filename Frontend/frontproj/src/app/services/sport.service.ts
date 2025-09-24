import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {Sport} from "../models/sport.interface";
import {environment} from "../../environments/environment";
import {SportStatistics} from "../models/sport-statistics.model";

@Injectable({
  providedIn: 'root'
})
export class SportService {

  private apiUrl = `${environment.apiUrl}/sports`;

  constructor(private http: HttpClient) { }

  getAllSports(): Observable<Sport[]> {
    return this.http.get<Sport[]>(`${this.apiUrl}`);
  }

  createSport(sport: {name: string, description: string, playerCount: number}): Observable<Sport> {
    return this.http.post<Sport>(`${this.apiUrl}`, sport);
  }

  deleteSport(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  updateSport(id: string, sportData: { name: string; description: string; playerCount: number }): Observable<Sport> {
    return this.http.put<Sport>(`${this.apiUrl}/${id}`, sportData);
  }

  getSportStatistics(): Observable<SportStatistics[]> {
    return this.http.get<SportStatistics[]>(`${this.apiUrl}/statistics`);
  }

}
