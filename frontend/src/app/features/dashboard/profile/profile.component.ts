import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { User } from '../../../core/models/user.model';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  isEditing = false;
  profileForm: any = {};

  constructor(private authService: AuthService, private cdr: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
      if (user) {
        this.profileForm = {
          username: user.username,
          email: user.email,
          role: user.role
        };
      }
      this.cdr.detectChanges();
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
  }

  saveProfile(): void {
    // In a real app, this would call an API to update the profile
    console.log('Saving profile:', this.profileForm);
    this.isEditing = false;
  }

  cancelEdit(): void {
    if (this.user) {
      this.profileForm = {
        username: this.user.username,
        email: this.user.email,
        role: this.user.role
      };
    }
    this.isEditing = false;
  }
}
