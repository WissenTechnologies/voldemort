import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { Wallet } from '../models/wallet.model';

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private readonly baseUrl = 'http://localhost:5003/api/wallet';

  constructor(private http: HttpClient) {}

  getWallet(userId: number): Observable<Wallet> {
    return this.http.get<Wallet>(`${this.baseUrl}/${userId}`, {
      headers: {
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => throwError(() => error))
    );
  }

  createWallet(userId: number, balance: number): Observable<Wallet> {
    return this.http.post<Wallet>(`${this.baseUrl}/create`, null, {
      params: {
        userId: String(userId),
        balance: String(balance)
      },
      headers: {
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => throwError(() => error))
    );
  }

  addMoney(userId: number, amount: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/add`, null, {
      params: {
        userId: String(userId),
        amount: String(amount)
      },
      headers: {
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => throwError(() => error))
    );
  }

  withdraw(userId: number, amount: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/withdraw`, null, {
      params: {
        userId: String(userId),
        amount: String(amount)
      },
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
