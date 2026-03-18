import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="auth-container">
      <div class="glass auth-card">
        <h1 class="gradient-text">{{ step === 1 ? 'Forgot Password' : (step === 2 ? 'Verify OTP' : 'Success') }}</h1>
        <p class="subtitle">
          {{ step === 1 ? 'Enter your email to receive a password reset code' : 
             (step === 2 ? 'Enter the security code we sent to your email' : 'Your password has been reset successfully') }}
        </p>

        <!-- Step 1: Email Form -->
        <form *ngIf="step === 1" (ngSubmit)="onRequestOtp()">
          <div class="form-group">
            <label class="form-label">Email Address</label>
            <input type="email" name="email" [(ngModel)]="email" class="form-input" placeholder="name@example.com" required>
          </div>
          <button type="submit" class="btn-premium w-full" [disabled]="loading">
            {{ loading ? 'Sending code...' : 'Send Reset Code' }}
          </button>
        </form>

        <!-- Step 2: OTP & Reset Form -->
        <form *ngIf="step === 2" (ngSubmit)="onResetPassword()">
          <div class="form-group">
            <label class="form-label">Security Code</label>
            <input type="text" name="otp" [(ngModel)]="otp" class="form-input" placeholder="000000" maxlength="6" required>
          </div>
          <div class="form-group">
            <label class="form-label">New Password</label>
            <input type="password" name="newPassword" [(ngModel)]="newPassword" class="form-input" placeholder="••••••••" required>
          </div>
          <button type="submit" class="btn-premium w-full" [disabled]="loading">
            {{ loading ? 'Resetting...' : 'Reset Password' }}
          </button>
        </form>

        <!-- Step 3: Success Message -->
        <div *ngIf="step === 3" class="success-actions">
          <button routerLink="/login" class="btn-premium w-full">Back to Login</button>
        </div>

        <p class="error-msg" *ngIf="error">{{ error }}</p>

        <div class="auth-footer" *ngIf="step !== 3">
          <a routerLink="/login" class="text-muted no-underline">Back to Login</a>
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
    .auth-footer { margin-top: 32px; font-size: 0.9rem; }
    .no-underline { text-decoration: none; }
    .error-msg { color: #ef4444; margin-top: 16px; font-size: 0.9rem; }
    .success-actions { margin-top: 24px; }
  `]
})
export class ForgotPasswordComponent {
  email = '';
  otp = '';
  newPassword = '';
  step = 1;
  loading = false;
  error = '';

  constructor(private authService: AuthService) {}

  onRequestOtp() {
    this.loading = true;
    this.error = '';
    this.authService.sendOtp(this.email).subscribe({
      next: () => {
        this.loading = false;
        this.step = 2;
      },
      error: (err) => {
        this.loading = false;
        this.error = 'User not found or failed to send email.';
      }
    });
  }

  onResetPassword() {
    this.loading = true;
    this.error = '';
    this.authService.resetPassword({ email: this.email, otp: this.otp, newPassword: this.newPassword }).subscribe({
      next: () => {
        this.loading = false;
        this.step = 3;
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error || 'Password reset failed. Check your OTP.';
      }
    });
  }
}
