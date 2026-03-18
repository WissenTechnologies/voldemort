import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { UserRole } from '../../../core/models/user.model';
import { AuthService } from '../../../core/services/auth';

import { finalize } from 'rxjs/operators';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  standalone:false,
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  UserRole = UserRole;
  loginForm: FormGroup;
  isLoading = false;
  errorMessage = '';
  showPassword = false;
  returnUrl: string = '/dashboard';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    public router: Router,
    private route: ActivatedRoute,
    private cdf: ChangeDetectorRef
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  ngOnInit(): void {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        console.log('Login successful:', response);
        this.router.navigateByUrl(this.returnUrl);
      },
      error: (error) => {
        console.log('Login error details:', error); // Debug log
        this.isLoading = false;
        
        // Handle different types of errors
        if (error.status === 401) {
          const errorMessage = error.error?.message || 'Invalid email or password. Please try again.';
          
          // Check if error is related to email verification
          if (errorMessage.toLowerCase().includes('email not verified') || 
              errorMessage.toLowerCase().includes('verify your email') ||
              errorMessage.toLowerCase().includes('email verification')) {
            this.errorMessage = 'Please verify your email before logging in.';
            // Redirect to verification page with email
            setTimeout(() => {
              this.router.navigate(['/verify-email'], { 
                queryParams: { email: this.loginForm.value.email } 
              });
            }, 2000);
          } else {
            this.errorMessage = errorMessage;
          }
        } else if (error.status === 403) {
          const errorMessage = error.error?.message || 'Access forbidden.';
          
          // Check if error is related to email verification
          if (errorMessage.toLowerCase().includes('email not verified') || 
              errorMessage.toLowerCase().includes('verify your email') ||
              errorMessage.toLowerCase().includes('email verification') ||
              errorMessage.toLowerCase().includes('otp')) {
            this.errorMessage = 'Please verify your email before logging in.';
            // Redirect to verification page with email
            setTimeout(() => {
              this.router.navigate(['/verify-email'], { 
                queryParams: { email: this.loginForm.value.email } 
              });
            }, 2000);
          } else {
            this.errorMessage = 'Your account has been locked. Please contact support.';
          }
        } else if (error.status === 404) {
          this.errorMessage = 'Account not found. Please check your email or register.';
        } else if (error.status === 0) {
          this.errorMessage = 'Network error. Please check your connection and try again.';
        } else if (error.error?.message) {
          this.errorMessage = error.error.message;
        } else if (error.message) {
          this.errorMessage = error.message;
        } else {
          this.errorMessage = 'An unexpected error occurred. Please try again later.';
        }
        
        // Force change detection
        this.cdf.detectChanges();
      }
    });
  }

  quickLogin(role: string): void {
    const email = `${role.toLowerCase()}@example.com`;
    this.authService.mockLogin(email, 'password', role);
    this.router.navigateByUrl(this.returnUrl);
  }

  get email() { return this.loginForm.get('email'); }
  get password() { return this.loginForm.get('password'); }
}