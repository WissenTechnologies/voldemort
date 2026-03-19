import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-forgot-password',
  standalone:false,
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  forgotForm: FormGroup;
  isLoading = false;
  isSubmitted = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    public router: Router
  ) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotForm.invalid) {
      this.forgotForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const email = this.forgotForm.value.email;
    console.log('Sending OTP to:', email);
    
    // Auto-redirect after 3 seconds regardless of API response
    setTimeout(() => {
      console.log('Auto-redirecting to verify-otp with email:', email);
      this.router.navigate(['/verify-otp'], { queryParams: { email: email } });
    }, 3000);
    
    this.authService.forgotPassword(email).subscribe({
      next: (response) => {
        console.log('OTP sent successfully:', response);
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Failed to send OTP:', error);
        this.errorMessage = error.error?.message || 'Failed to send OTP';
        this.isLoading = false;
      }
    });
  }

  get email() { return this.forgotForm.get('email'); }
}