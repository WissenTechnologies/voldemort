import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  standalone: false,
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit, OnDestroy {
  resetForm: FormGroup;
  isLoading = false;
  isSubmitted = false;
  isValidToken = false;
  isVerifyingToken = true;
  errorMessage = '';
  successMessage = '';
  showPassword = false;
  showConfirmPassword = false;
  token: string = '';
  email: string = '';
  
  private subscription: Subscription = new Subscription();

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private route: ActivatedRoute,
    public router: Router
  ) {
    this.resetForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],
      otp: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(6)]]
    }, { validator: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'] || '';
    this.email = this.route.snapshot.queryParams['email'] || '';
    const verified = this.route.snapshot.queryParams['verified'] === 'true';
    
    if (this.token) {
      // Token-based reset flow
      this.isVerifyingToken = false;
      this.isValidToken = true;
      // Remove OTP field for token-based flow
      this.resetForm.removeControl('otp');
    } else if (this.email && verified) {
      // OTP-based reset flow after verification
      this.isVerifyingToken = false;
      this.isValidToken = true;
      // OTP already verified on verify-otp page; don't ask for it again
      this.resetForm.removeControl('otp');
    } else {
      // No valid session, redirect to forgot-password
      this.isVerifyingToken = true;
      this.isValidToken = false;
      this.router.navigate(['/forgot-password']);
    }
  }

  passwordMatchValidator(control: AbstractControl) {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  onSubmit(): void {
    if (this.resetForm.invalid) {
      this.resetForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    if (this.token) {
      // Token-based reset flow (old method)
      this.authService.resetPassword(this.token, this.resetForm.value.password, this.resetForm.value.confirmPassword, '').subscribe({
        next: () => {
          this.isSubmitted = true;
          this.successMessage = 'Password has been reset successfully.';
          this.isLoading = false;
          // Redirect to login after 2 seconds
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Failed to reset password';
          this.isLoading = false;
        }
      });
    } else {
      // OTP-based reset flow (new method)
      const otp = this.resetForm.get('otp')?.value || '';
      this.authService.resetPassword(this.email, otp, this.resetForm.value.password, this.resetForm.value.confirmPassword).subscribe({
        next: () => {
          this.isSubmitted = true;
          this.successMessage = 'Password has been reset successfully.';
          this.isLoading = false;
          // Redirect to login after 2 seconds
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 2000);
        },
        error: (error) => {
          this.errorMessage = error.error?.message || 'Failed to reset password';
          this.isLoading = false;
        }
      });
    }
  }

  ngOnDestroy(): void {
    this.subscription.unsubscribe();
  }

  get password() {
    return this.resetForm.get('password');
  }

  get confirmPassword() {
    return this.resetForm.get('confirmPassword');
  }

  get otp() {
    return this.resetForm.get('otp');
  }
}
