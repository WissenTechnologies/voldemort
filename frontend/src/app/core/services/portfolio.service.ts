import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { ApiResponse, Portfolio, PortfolioSummary } from '../models/portfolio.model';

@Injectable({
  providedIn: 'root'
})
export class PortfolioService {
  private readonly baseUrl = 'http://localhost:5007/api/portfolio';

  constructor(private http: HttpClient) {}

  createPortfolio(userId: number, name: string): Observable<ApiResponse<Portfolio>> {
    return this.http.post<ApiResponse<Portfolio>>(`${this.baseUrl}`, null, {
      params: {
        userId: String(userId),
        name
      },
      headers: {
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  getPortfoliosByUserId(userId: number): Observable<ApiResponse<Portfolio[]>> {
    return this.http.get<ApiResponse<Portfolio[]>>(`${this.baseUrl}/user/${userId}/portfolios`, {
      headers: {
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  getPortfolioByPortfolioId(portfolioId: number): Observable<ApiResponse<PortfolioSummary>> {
    return this.http.get<ApiResponse<PortfolioSummary>>(`${this.baseUrl}/portfolio/${portfolioId}`, {
      headers: {
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        return throwError(() => error);
      })
    );
  }

  private getAuthToken(): string {
    return localStorage.getItem('access_token') || '';
  }
}
