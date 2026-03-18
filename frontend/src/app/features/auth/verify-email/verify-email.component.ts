import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { takeWhile } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-verify-email',
  templateUrl: './verify-email.component.html',
  standalone:false,
  styleUrls: ['./verify-email.component.css']
})
export class VerifyEmailComponent implements OnInit, OnDestroy {
  verificationForm: FormGroup;
  email: string = '';
  isLoading = false;
  isVerified = false;
  errorMessage = '';
  successMessage = '';
  
  resendCooldown = 60;
  canResend = true;
  private cooldownSubscription?: Subscription;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    public router: Router
  ) {
    this.verificationForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]]
    });
  }

  ngOnInit(): void {
    this.email = this.route.snapshot.queryParams['email'] || '';
    if (!this.email) {
      this.router.navigate(['/login']);
    }
  }

  ngOnDestroy(): void {
    this.cooldownSubscription?.unsubscribe();
  }

  onSubmit(): void {
    if (this.verificationForm.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';
    console.log('Submitting verification for email:', this.email, 'with code:', this.verificationForm.value.code);

    this.authService.verifyEmail(this.email, this.verificationForm.value.code)
      .subscribe({
        next: (response) => {
          console.log('Verification response received:', response);
          this.isVerified = true;
          this.successMessage = 'Email verified successfully! Redirecting to login...';
          this.isLoading = false;
          setTimeout(() => this.router.navigate(['/login']), 2000);
        },
        error: (error) => {
          console.error('Verification error in component:', error);
          this.errorMessage = error.error?.message || 'Verification failed';
          this.isLoading = false;
        }
      });
  }

  resendCode(): void {
    if (!this.canResend) return;

    console.log('Resending verification code for email:', this.email);

    this.authService.resendVerification(this.email).subscribe({
      next: () => {
        this.successMessage = 'Verification code resent successfully!';
        this.startResendCooldown();
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to resend code';
      }
    });
  }

  private startResendCooldown(): void {
    this.canResend = false;
    this.resendCooldown = 60;
    
    this.cooldownSubscription = interval(1000)
      .pipe(takeWhile(() => this.resendCooldown > 0))
      .subscribe(() => {
        this.resendCooldown--;
        if (this.resendCooldown === 0) {
          this.canResend = true;
        }
      });
  }
}