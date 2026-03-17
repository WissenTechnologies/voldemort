import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { User } from '../../../core/models/user.model';
import { AuthService } from '../../../core/services/auth';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {

  user: User | null = null;
  isSidebarOpen = false;
  currentDate = new Date();
  greeting = '';

  private resizeListener!: () => void;

  constructor(
    private authService: AuthService,
    public router: Router
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
    });

    this.setGreeting();

    // Initial state
    this.handleResize();

    // Responsive fix
    this.resizeListener = this.handleResize.bind(this);
    window.addEventListener('resize', this.resizeListener);
  }

  ngOnDestroy(): void {
    window.removeEventListener('resize', this.resizeListener);
  }

  handleResize(): void {
    if (window.innerWidth >= 1024) {
      this.isSidebarOpen = true;   // Desktop → always open
    } else {
      this.isSidebarOpen = false;  // Mobile → closed
    }
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  setGreeting(): void {
    const hour = new Date().getHours();
    if (hour < 12) this.greeting = 'Good morning';
    else if (hour < 17) this.greeting = 'Good afternoon';
    else this.greeting = 'Good evening';
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}