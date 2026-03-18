import { Component, OnInit } from '@angular/core';
import { User } from '../../../core/models/user.model';
import { AuthService } from '../../../core/services/auth';

export interface UserManagement {
  id: number;
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  status: 'Active' | 'Inactive' | 'Suspended';
  lastLogin: Date;
  createdAt: Date;
  company?: string;
}

@Component({
  selector: 'app-users',
  standalone: false,
  templateUrl: './users.component.html',
  styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {
  currentUser: User | null = null;
  isEditing = false;
  editingUser: UserManagement | null = null;
  
  users: UserManagement[] = [
    {
      id: 1,
      username: 'john_doe',
      email: 'john.doe@techsolutions.com',
      role: 'USER',
      status: 'Active',
      lastLogin: new Date('2024-01-15T10:30:00'),
      createdAt: new Date('2023-06-01'),
      company: 'Tech Solutions Inc.'
    },
    {
      id: 2,
      username: 'jane_smith',
      email: 'jane.smith@globalmfg.com',
      role: 'USER',
      status: 'Active',
      lastLogin: new Date('2024-01-14T14:20:00'),
      createdAt: new Date('2023-08-15'),
      company: 'Global Manufacturing Co.'
    },
    {
      id: 3,
      username: 'admin_user',
      email: 'admin@company.com',
      role: 'ADMIN',
      status: 'Active',
      lastLogin: new Date('2024-01-15T09:15:00'),
      createdAt: new Date('2023-01-01'),
      company: 'System Administration'
    },
    {
      id: 4,
      username: 'bob_wilson',
      email: 'bob.wilson@healthcareplus.com',
      role: 'USER',
      status: 'Suspended',
      lastLogin: new Date('2023-12-20T16:45:00'),
      createdAt: new Date('2023-09-10'),
      company: 'Healthcare Plus'
    },
    {
      id: 5,
      username: 'alice_brown',
      email: 'alice.brown@financecorp.com',
      role: 'USER',
      status: 'Inactive',
      lastLogin: new Date('2023-11-05T11:30:00'),
      createdAt: new Date('2023-07-20'),
      company: 'Finance Corp International'
    }
  ];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });
  }

  editUser(user: UserManagement): void {
    this.isEditing = true;
    this.editingUser = { ...user };
  }

  addUser(): void {
    this.isEditing = true;
    this.editingUser = {
      id: 0,
      username: '',
      email: '',
      role: 'USER',
      status: 'Active',
      lastLogin: new Date(),
      createdAt: new Date(),
      company: ''
    };
  }

  saveUser(): void {
    if (this.editingUser) {
      if (this.editingUser.id === 0) {
        // Add new user
        const newUser = {
          ...this.editingUser,
          id: Math.max(...this.users.map(u => u.id)) + 1
        };
        this.users.push(newUser);
      } else {
        // Update existing user
        const index = this.users.findIndex(u => u.id === this.editingUser!.id);
        if (index !== -1) {
          this.users[index] = this.editingUser;
        }
      }
    }
    this.cancelEdit();
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.editingUser = null;
  }

  deleteUser(user: UserManagement): void {
    if (confirm(`Are you sure you want to delete ${user.username}? This action cannot be undone.`)) {
      this.users = this.users.filter(u => u.id !== user.id);
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
