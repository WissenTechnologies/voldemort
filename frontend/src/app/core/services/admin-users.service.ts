import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface DemoUser {
  id: number;
  email: string;
  username: string;
  role: 'USER' | 'ADMIN';
  createdAt?: string;
  enabled?: boolean;
}

export interface CreateDemoUserRequest {
  email: string;
  username: string;
  password: string;
  role?: 'USER' | 'ADMIN';
}

@Injectable({
  providedIn: 'root'
})
export class AdminUsersService {
  private readonly baseUrl = 'http://localhost:5000';

  constructor(private http: HttpClient) {}

  getUsers(): Observable<DemoUser[]> {
    return this.http.get<DemoUser[]>(`${this.baseUrl}/auth/users`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        console.error('Error fetching users:', error);
        return throwError(() => error);
      })
    );
  }

  createUser(payload: CreateDemoUserRequest): Observable<DemoUser> {
    return this.http.post<DemoUser>(`${this.baseUrl}/auth/users`, payload, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        console.error('Error creating user:', error);
        return throwError(() => error);
      })
    );
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/auth/users/${id}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        console.error('Error deleting user:', error);
        return throwError(() => error);
      })
    );
  }

  private getAuthToken(): string {
    return localStorage.getItem('access_token') || '';
  }
}
