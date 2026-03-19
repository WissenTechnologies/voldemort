import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-verify-otp',
  standalone: false,
  templateUrl: './verify-otp.component.html',
  styleUrls: ['./verify-otp.component.css']
})
export class VerifyOtpComponent implements OnInit {
  verifyForm: FormGroup;
  isLoading = false;
  isSubmitted = false;
  errorMessage = '';
  successMessage = '';
  email: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.verifyForm = this.fb.group({
      otp: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6), Validators.pattern('^[0-9]*$')]]
    });
  }

  ngOnInit(): void {
    // Get email from query params
    this.email = this.route.snapshot.queryParams['email'] || '';
    if (!this.email) {
      // If no email, redirect back to forgot-password
      this.router.navigate(['/forgot-password']);
    }
  }

  onSubmit(): void {
    if (this.verifyForm.invalid) {
      this.verifyForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const otp = this.verifyForm.value.otp;
    
    this.authService.verifyOtp(this.email, otp).subscribe({
      next: () => {
        this.isSubmitted = true;
        this.successMessage = 'OTP verified successfully!';
        this.isLoading = false;
        
        // Redirect to reset-password page after 1.5 seconds
        setTimeout(() => {
          this.router.navigate(['/reset-password'], { 
            queryParams: { email: this.email, verified: 'true' } 
          });
        }, 1500);
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Invalid OTP. Please try again.';
        this.isLoading = false;
      }
    });
  }

  resendOtp(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.forgotPassword(this.email).subscribe({
      next: () => {
        this.successMessage = 'New OTP has been sent to your email.';
        this.isLoading = false;
      },
      error: (error) => {
        this.errorMessage = error.error?.message || 'Failed to resend OTP';
        this.isLoading = false;
      }
    });
  }

  get otp() { return this.verifyForm.get('otp'); }
}
