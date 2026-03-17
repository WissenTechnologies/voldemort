import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { User, UserCredentials, RegisterRequest, AuthResponse } from '../models/user.model';
import { TokenService } from './token';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();
  
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(
    private http: HttpClient,
    private tokenService: TokenService,
    private router: Router
  ) {
    this.loadUser();
  }

  private loadUser(): void {
    const user = this.tokenService.getUser();
    const token = this.tokenService.getToken();
    
    if (user && token && !this.tokenService.isTokenExpired()) {
      this.currentUserSubject.next(user);
    } else {
      this.tokenService.clear();
    }
  }

  login(credentials: UserCredentials): Observable<AuthResponse> {
    const payload = {
      email: credentials.email,
      password: credentials.password
    };
    
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, payload).pipe(
      tap(response => {
        console.log('Login response:', response);
        
        // Handle different response formats
        let token: string;
        let isJwtToken = false;
        
        if (typeof response === 'string') {
          token = response; // Plain text token
          isJwtToken = false;
        } else if (response.token) {
          token = response.token; // JSON response with token field
          isJwtToken = true;
        } else {
          console.error('Invalid response format:', response);
          throw new Error('Invalid response from server');
        }
        
        // Store token
        this.tokenService.setToken(token);
        
        // Get user info from token
        const user = this.getUserFromToken(token, isJwtToken);
        if (user) {
          this.tokenService.setUser(user);
          this.currentUserSubject.next(user);
        }
      }),
      catchError(error => {
        console.error('Login error:', error);
        return throwError(() => error);
      })
    );
  }

  register(userData: RegisterRequest): Observable<string> {
    if (userData.password !== userData.confirmPassword) {
      return throwError(() => new Error('Passwords do not match'));
    }

    const userPayload = {
      username: userData.username,
      email: userData.email,
      password: userData.password
    };
    
    return this.http.post(`${this.apiUrl}/register`, userPayload, { 
      responseType: 'text' 
    }).pipe(
      tap(response => {
        console.log('Registration successful:', response);
        // After successful registration, redirect to login
        // You could also automatically log the user in
      }),
      catchError(error => {
        console.error('Registration error:', error);
        return throwError(() => error);
      })
    );
  }

  private getUserFromToken(token: string, isJwtToken: boolean = true): User | null {
    try {
      if (isJwtToken) {
        // Parse JWT token
        const payload = this.tokenService.getTokenPayload();
        console.log('Token payload:', payload);
        
        if (payload) {
          // Create a user object from token payload
          // Handle different possible field names in JWT payload
          const user: User = {
            id: payload.sub || payload.id || payload.userId || 0,
            username: payload.username || payload.name || payload.email?.split('@')[0] || 'Unknown',
            email: payload.email || payload.emailAddress || '',
            role: payload.role || payload.authorities?.[0]?.replace('ROLE_', '') || 'USER'
          };
          
          console.log('Extracted user:', user);
          return user;
        }
        return null;
      } else {
        // Handle plain text token - create a mock user or extract from backend response
        // For plain text tokens, we might need to make an additional call to get user info
        // For now, create a basic user structure
        const mockUser: User = {
          id: 1,
          username: 'User', // This should come from backend or token
          email: 'user@example.com', // This should come from backend or token
          role: 'USER'
        };
        
        console.log('Created mock user for plain text token:', mockUser);
        return mockUser;
      }
    } catch (error) {
      console.error('Error parsing user from token:', error);
      return null;
    }
  }

  logout(): void {
    this.tokenService.clear();
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  isAuthenticated(): boolean {
    return !!this.currentUserSubject.value && !this.tokenService.isTokenExpired();
  }

  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  // Mock login for development (remove when connecting to real API)
  mockLogin(email: string, password: string, role: string = 'USER'): void {
    const mockUser: User = {
      id: 1,
      username: email.split('@')[0],
      email: email,
      role: role
    };
    
    // Create a mock JWT token with role payload
    const mockPayload = {
      sub: mockUser.id,
      email: mockUser.email,
      role: mockUser.role,
      exp: Math.floor(Date.now() / 1000) + 3600 // 1 hour expiration
    };
    
    const mockHeader = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const mockToken = mockHeader + '.' + btoa(JSON.stringify(mockPayload)) + '.signature';
    
    this.tokenService.setToken(mockToken);
    this.tokenService.setUser(mockUser);
    this.currentUserSubject.next(mockUser);
  }

  // Additional methods that might be needed by components
  forgotPassword(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/forgot-password`, { email })
      .pipe(
        tap(response => {
          console.log('OTP sent successfully:', response);
        }),
        catchError(error => throwError(() => error))
      );
  }

  resetPassword(email: string, otp: string, newPassword: string, confirmPassword: string): Observable<any> {
    if (newPassword !== confirmPassword) {
      return throwError(() => new Error('Passwords do not match'));
    }

    return this.http.post(`${this.apiUrl}/reset-password`, { 
      email,
      otp,
      newPassword,
      confirmPassword 
    }).pipe(
      tap(response => {
        console.log('Password reset successful:', response);
      }),
      catchError(error => throwError(() => error))
    );
  }

  verifyEmail(email: string, code: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/verify-email`, { email, code })
      .pipe(
        tap(response => {
          console.log('Email verification successful:', response);
        }),
        catchError(error => throwError(() => error))
      );
  }

  verifyOtp(email: string, otp: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/verify-otp`, { email, otp })
      .pipe(
        tap(response => {
          console.log('OTP verification successful:', response);
        }),
        catchError(error => throwError(() => error))
      );
  }

  resendVerification(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/resend-verification`, { email })
      .pipe(
        catchError(error => throwError(() => error))
      );
  }

  verifyResetToken(token: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/verify-reset-token`, { token })
      .pipe(
        catchError(error => throwError(() => error))
      );
  }

  refreshToken(): Observable<any> {
    // Spring Boot might not have refresh token endpoint
    return throwError(() => new Error('Refresh token not implemented'));
  }

  hasRole(role: string): boolean {
    const user = this.currentUserSubject.value;
    return user ? user.role === role : false;
  }
}
