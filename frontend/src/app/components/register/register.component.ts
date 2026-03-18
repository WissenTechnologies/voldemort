import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-container">
      <div class="glass auth-card">
        <h1 class="gradient-text">{{ step === 1 ? 'Create Account' : 'Verify Email' }}</h1>
        <p class="subtitle">{{ step === 1 ? 'Join us today and get started' : 'Enter the OTP sent to your email' }}</p>

        <!-- Step 1: Registration Form -->
        <form *ngIf="step === 1" (ngSubmit)="onRegister()" #regForm="ngForm">
          <div class="form-group">
            <label class="form-label">Username</label>
            <input type="text" name="username" [(ngModel)]="user.username" class="form-input" placeholder="johndoe" required>
          </div>
          <div class="form-group">
            <label class="form-label">Email Address</label>
            <input type="email" name="email" [(ngModel)]="user.email" class="form-input" placeholder="name@example.com" required>
          </div>
          <div class="form-group">
            <label class="form-label">Password</label>
            <input type="password" name="password" [(ngModel)]="user.password" class="form-input" placeholder="••••••••" required>
          </div>

          <button type="submit" class="btn-premium w-full" [disabled]="loading">
            {{ loading ? 'Creating account...' : 'Create Account' }}
          </button>
        </form>

        <!-- Step 2: OTP Verification -->
        <form *ngIf="step === 2" (ngSubmit)="onVerifyOtp()" #otpForm="ngForm">
          <div class="form-group">
            <label class="form-label">6-Digit OTP Code</label>
            <input type="text" name="otp" [(ngModel)]="otp" class="form-input otp-input" placeholder="000000" maxlength="6" required>
          </div>

          <button type="submit" class="btn-premium w-full" [disabled]="loading">
            {{ loading ? 'Verifying...' : 'Verify & Sign In' }}
          </button>
          
          <p class="resend-text">Didn't receive code? <a (click)="onRegister()" class="gradient-text pointer">Resend</a></p>
        </form>

        <p class="error-msg" *ngIf="error">{{ error }}</p>

        <div class="auth-footer">
          <span>Already have an account?</span>
          <a routerLink="/login" class="gradient-text">Sign In</a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; padding: 20px; }
    .auth-card { width: 100%; max-width: 420px; padding: 40px; text-align: center; }
    h1 { margin-bottom: 8px; font-size: 2rem; }
    .subtitle { color: var(--text-muted); margin-bottom: 32px; font-size: 0.95rem; }
    .w-full { width: 100%; margin-top: 8px; }
    .otp-input { text-align: center; font-size: 1.5rem; letter-spacing: 0.5rem; }
    .auth-footer { margin-top: 32px; font-size: 0.9rem; color: var(--text-muted); }
    .auth-footer a { text-decoration: none; font-weight: 600; margin-left: 8px; }
    .error-msg { color: #ef4444; margin-top: 16px; font-size: 0.9rem; }
    .resend-text { margin-top: 24px; font-size: 0.85rem; color: var(--text-muted); }
    .pointer { cursor: pointer; }
  `]
})
export class RegisterComponent {
  user = { username: '', email: '', password: '' };
  otp = '';
  step = 1;
  loading = false;
  error = '';

  constructor(private authService: AuthService, private router: Router) {}

  onRegister() {
    this.loading = true;
    this.error = '';
    this.authService.register(this.user).subscribe({
      next: () => {
        this.loading = false;
        this.step = 2;
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error || 'Registration failed. Email might already exist.';
      }
    });
  }

  onVerifyOtp() {
    this.loading = true;
    this.error = '';
    this.authService.verifyOtp(this.user.email, this.otp).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Invalid or expired OTP. Please try again.';
      }
    });
  }
}
