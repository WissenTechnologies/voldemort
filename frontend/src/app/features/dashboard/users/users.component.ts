import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { Subscription, interval } from 'rxjs';
import { User } from '../../../core/models/user.model';
import { AuthService } from '../../../core/services/auth';
import { AdminUsersService, CreateDemoUserRequest, DemoUser } from '../../../core/services/admin-users.service';

export interface UserManagement {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  status: 'Active' | 'Inactive' | 'Suspended';
  lastLogin: Date;
  createdAt: Date;
  company?: string;
  password?: string;
}

@Component({
  selector: 'app-users',
  standalone: false,
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit, OnDestroy {
  currentUser: User | null = null;
  isEditing = false;
  editingUser: UserManagement | null = null;
  loading = false;
  error: string | null = null;
  
  users: UserManagement[] = [];

  private subscriptions: Subscription[] = [];

  constructor(
    private authService: AuthService,
    private adminUsersService: AdminUsersService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.subscriptions.push(
      this.authService.currentUser$.subscribe(user => {
        this.currentUser = user;
      })
    );

    this.loadUsers();
    
    // Poll for users data every second
    this.subscriptions.push(
      interval(1000).subscribe(() => {
        this.refreshUsers();
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
  }

  get isAdmin(): boolean {
    return this.currentUser?.role === 'ADMIN';
  }

  loadUsers(): void {
    if (!this.isAdmin) {
      this.users = [];
      return;
    }

    this.loading = true;
    this.error = null;

    this.adminUsersService.getUsers().subscribe({
      next: (users) => {
        this.users = users.map(u => this.mapDemoUser(u));
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error || err?.message || 'Failed to load users';
        this.cdr.detectChanges();
      }
    });
  }

  // Silent refresh without loading state
  refreshUsers(): void {
    if (!this.isAdmin || this.isEditing) return; // Skip if editing

    this.subscriptions.push(
      this.adminUsersService.getUsers().subscribe({
        next: (users) => {
          this.users = users.map(u => this.mapDemoUser(u));
          this.cdr.detectChanges();
        },
        error: () => {
          // Silent fail on auto-refresh
        }
      })
    );
  }

  private mapDemoUser(user: DemoUser): UserManagement {
    const createdAt = user.createdAt ? new Date(user.createdAt) : new Date();
    return {
      id: user.id,
      username: user.username,
      email: user.email,
      role: user.role,
      status: user.enabled === false ? 'Inactive' : 'Active',
      lastLogin: new Date(),
      createdAt,
      company: ''
    };
  }

  editUser(user: UserManagement): void {
    this.isEditing = true;
    this.editingUser = { ...user };
  }

  addUser(): void {
    if (!this.isAdmin) return;
    this.isEditing = true;
    this.editingUser = {
      id: 0,
      username: '',
      email: '',
      password: '',
      role: 'USER',
      status: 'Active',
      lastLogin: new Date(),
      createdAt: new Date(),
      company: ''
    };
  }

  saveUser(): void {
    if (!this.isAdmin) return;
    if (!this.editingUser) return;

    if (this.editingUser.id !== 0) {
      this.cancelEdit();
      return;
    }

    if (!this.editingUser.email || !this.editingUser.username || !this.editingUser.password) {
      this.error = 'Email, username, and password are required';
      return;
    }

    this.loading = true;
    this.error = null;

    const payload: CreateDemoUserRequest = {
      email: this.editingUser.email,
      username: this.editingUser.username,
      password: this.editingUser.password,
      role: this.editingUser.role
    };

    this.adminUsersService.createUser(payload).subscribe({
      next: () => {
        this.loading = false;
        this.cancelEdit();
        this.loadUsers();
      },
      error: (err) => {
        this.loading = false;
        this.error = err?.error || err?.message || 'Failed to create user';
      }
    });
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.editingUser = null;
  }

  deleteUser(user: UserManagement): void {
    if (!this.isAdmin) return;
    if (confirm(`Are you sure you want to delete ${user.username}? This action cannot be undone.`)) {
      this.loading = true;
      this.error = null;
      this.adminUsersService.deleteUser(user.id).subscribe({
        next: () => {
          this.loading = false;
          this.loadUsers();
        },
        error: (err) => {
          this.loading = false;
          this.error = err?.error || err?.message || 'Failed to delete user';
        }
      });
    }
  }

  suspendUser(user: UserManagement): void {
    if (confirm(`Are you sure you want to suspend ${user.username}?`)) {
      const index = this.users.findIndex(u => u.id === user.id);
      if (index !== -1) {
        this.users[index].status = 'Suspended';
      }
    }
  }

  activateUser(user: UserManagement): void {
    const index = this.users.findIndex(u => u.id === user.id);
    if (index !== -1) {
      this.users[index].status = 'Active';
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'Active': return 'status-active';
      case 'Inactive': return 'status-inactive';
      case 'Suspended': return 'status-suspended';
      default: return 'status-inactive';
    }
  }

  getRoleColor(role: string): string {
    switch (role) {
      case 'ADMIN': return 'role-admin';
      case 'USER': return 'role-user';
      default: return 'role-user';
    }
  }

  formatDate(date: Date): string {
    return new Date(date).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
