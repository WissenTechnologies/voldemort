export enum UserRole {
  ADMIN = 'ADMIN',
  USER = 'USER'
}

export interface User {
  id: number;
  username: string;
  email: string;
  password?: string;
  role?: string; // Optional role field for frontend use
}

export interface UserCredentials {
  email: string;
  password: string;
  rememberMe?: boolean;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  role?: string; // Optional role for registration
}

export interface AuthResponse {
  token?: string;
  // For handling plain text responses from Spring Boot
  [key: string]: any;
}