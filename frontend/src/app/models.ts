export interface User {
  email: string;
  password?: string;
  username: string;
}

export interface LoginRequest {
  email: string;
  password?: string;
}

export interface VerifyOtpRequest {
  email: string;
  otp: string;
}

export interface AuthResponse {
  token: string;
}
