import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-container">
      <div class="glass auth-card">
        <h1 class="gradient-text">Welcome Back</h1>
        <p class="subtitle">Enter your credentials to access your account</p>

        <form (ngSubmit)="onLogin()" #loginForm="ngForm">
          <div class="form-group">
            <label class="form-label">Email Address</label>
            <input 
              type="email" 
              name="email" 
              [(ngModel)]="credentials.email" 
              class="form-input" 
              placeholder="name@example.com" 
              required
            >
          </div>
          <div class="form-group">
            <label class="form-label">Password</label>
            <input 
              type="password" 
              name="password" 
              [(ngModel)]="credentials.password" 
              class="form-input" 
              placeholder="••••••••" 
              required
            >
          </div>

          <div class="auth-actions">
            <a routerLink="/forgot-password" class="forgot-link">Forgot Password?</a>
          </div>

          <button type="submit" class="btn-premium w-full" [disabled]="loading">
            {{ loading ? 'Signing in...' : 'Sign In' }}
          </button>

          <p class="error-msg" *ngIf="error">{{ error }}</p>
        </form>

        <div class="auth-footer">
          <span>Don't have an account?</span>
          <a routerLink="/register" class="gradient-text">Create Account</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      padding: 20px;
    }
    .auth-card {
      width: 100%;
      max-width: 420px;
      padding: 40px;
      text-align: center;
    }
    h1 { margin-bottom: 8px; font-size: 2rem; }
    .subtitle { color: var(--text-muted); margin-bottom: 32px; font-size: 0.95rem; }
    .auth-actions { text-align: right; margin-bottom: 24px; }
    .forgot-link { color: var(--text-muted); font-size: 0.85rem; text-decoration: none; }
    .forgot-link:hover { color: var(--accent-primary); }
    .w-full { width: 100%; margin-top: 8px; }
    .auth-footer { margin-top: 32px; font-size: 0.9rem; color: var(--text-muted); }
    .auth-footer a { text-decoration: none; font-weight: 600; margin-left: 8px; }
    .error-msg { color: #ef4444; margin-top: 16px; font-size: 0.9rem; }
  `]
})
export class LoginComponent {
  credentials = { email: '', password: '' };
  loading = false;
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  onLogin() {
    this.loading = true;
    this.error = '';
    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Invalid email or password. Please try again.';
      }
    });
  }
}
