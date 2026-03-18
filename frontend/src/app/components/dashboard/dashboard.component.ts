import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="dashboard-wrapper">
      <nav class="glass nav-bar">
        <div class="logo gradient-text">VOLDEMORT</div>
        <div class="nav-actions">
          <button (click)="onLogout()" class="btn-logout">Logout</button>
        </div>
      </nav>

      <main class="dashboard-content">
        <header>
          <h1 class="gradient-text">Dashboard</h1>
          <p class="text-muted">Welcome back! Here's what's happening today.</p>
        </header>

        <div class="grid-container">
          <div class="glass card">
            <h3>Total Users</h3>
            <div class="value">1,234</div>
            <div class="trend positive">+12% from last week</div>
          </div>
          <div class="glass card">
            <h3>Active Sessions</h3>
            <div class="value">56</div>
            <div class="trend">Current online users</div>
          </div>
          <div class="glass card">
            <h3>System Status</h3>
            <div class="value status-ok">Operational</div>
            <div class="trend">All systems normal</div>
          </div>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .dashboard-wrapper { padding: 0; min-height: 100vh; }
    .nav-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 40px;
      margin: 20px;
      border-radius: 20px;
    }
    .logo { font-weight: 800; font-size: 1.5rem; letter-spacing: 2px; }
    .btn-logout {
      background: rgba(239, 68, 68, 0.1);
      color: #ef4444;
      border: 1px solid rgba(239, 68, 68, 0.2);
      padding: 8px 20px;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.2s;
    }
    .btn-logout:hover { background: rgba(239, 68, 68, 0.2); }
    .dashboard-content { padding: 40px; max-width: 1200px; margin: 0 auto; }
    header { margin-bottom: 40px; }
    h1 { font-size: 2.5rem; margin-bottom: 8px; }
    .grid-container {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 24px;
    }
    .card { padding: 32px; }
    .card h3 { color: var(--text-muted); font-size: 0.9rem; text-transform: uppercase; letter-spacing: 1px; margin-bottom: 16px; }
    .value { font-size: 2.5rem; font-weight: 700; margin-bottom: 8px; }
    .trend { font-size: 0.85rem; color: var(--text-muted); }
    .positive { color: #10b981; }
    .status-ok { color: #6366f1; }
  `]
})
export class DashboardComponent {
  constructor(private authService: AuthService, private router: Router) {}

  onLogout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
