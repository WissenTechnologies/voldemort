import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { OrderRequest, OrderResponse, OrderFilterParams } from '../models/order.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private readonly baseUrl = 'http://localhost:5008/api/orders';

  constructor(private http: HttpClient) {}

  buyStock(req: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.baseUrl}/buy`, req, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => throwError(() => error))
    );
  }

  sellStock(req: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.baseUrl}/sell`, req, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => throwError(() => error))
    );
  }

  getOrdersByPortfolio(portfolioId: number): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.baseUrl}/portfolio/${portfolioId}`, {
      headers: {
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => throwError(() => error))
    );
  }

  filterOrders(params: OrderFilterParams): Observable<OrderResponse[]> {
    let httpParams = new HttpParams();
    if (params.userId !== undefined) {
      httpParams = httpParams.set('userId', params.userId.toString());
    }
    if (params.portfolioId !== undefined) {
      httpParams = httpParams.set('portfolioId', params.portfolioId.toString());
    }
    if (params.status) {
      httpParams = httpParams.set('status', params.status);
    }

    return this.http.get<OrderResponse[]>(`${this.baseUrl}/filter`, {
      params: httpParams,
      headers: {
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => throwError(() => error))
    );
  }

  getPendingOrders(): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.baseUrl}/pending`, {
      headers: {
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => throwError(() => error))
    );
  }

  private getAuthToken(): string {
    return localStorage.getItem('access_token') || '';
  }
}
