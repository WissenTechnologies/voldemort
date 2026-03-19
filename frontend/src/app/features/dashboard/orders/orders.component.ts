import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subscription } from 'rxjs';

import { AuthService } from '../../../core/services/auth';
import { OrderService } from '../../../core/services/order.service';
import { PortfolioService } from '../../../core/services/portfolio.service';
import { OrderResponse, OrderStatus, OrderMode } from '../../../core/models/order.model';
import { Portfolio } from '../../../core/models/portfolio.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-orders',
  standalone: false,
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit, OnDestroy {
  user: User | null = null;
  orders: OrderResponse[] = [];
  loading = false;
  error: string | null = null;

  // Filters
  filterPortfolioId: number | null = null;
  filterStatus: OrderStatus | '' = '';
  filterOrderMode: OrderMode | '' = '';
  filterType: 'BUY' | 'SELL' | '' = '';

  portfolios: Portfolio[] = [];
  loadingPortfolios = false;

  orderStatuses: OrderStatus[] = ['PENDING', 'EXECUTED', 'FAILED', 'EXPIRED'];
  orderModes: OrderMode[] = ['MARKET', 'LIMIT', 'STOP_LOSS'];
  orderTypes: ('BUY' | 'SELL')[] = ['BUY', 'SELL'];

  private subscriptions: Subscription[] = [];

  constructor(
    private authService: AuthService,
    private orderService: OrderService,
    private portfolioService: PortfolioService
  ) {}

  ngOnInit(): void {
    this.subscriptions.push(
      this.authService.currentUser$.subscribe(user => {
        this.user = user;
        if (this.getNumericUserId() > 0) {
          this.loadPortfolios();
          this.loadOrders();
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  private getNumericUserId(): number {
    const raw: any = this.user?.id;
    if (typeof raw === 'number') return raw;
    if (typeof raw === 'string' && /^\d+$/.test(raw)) return Number(raw);
    return 0;
  }

  loadPortfolios(): void {
    const userId = this.getNumericUserId();
    if (userId <= 0) return;

    this.loadingPortfolios = true;
    this.subscriptions.push(
      this.portfolioService.getPortfoliosByUserId(userId).subscribe({
        next: (res) => {
          this.portfolios = Array.isArray(res?.data) ? res.data : [];
          this.loadingPortfolios = false;
        },
        error: () => {
          this.loadingPortfolios = false;
        }
      })
    );
  }

  loadOrders(): void {
    const userId = this.getNumericUserId();
    if (userId <= 0) {
      this.error = 'User id is not available. Please re-login.';
      return;
    }

    this.loading = true;
    this.error = null;

    const params: any = { userId };
    if (this.filterPortfolioId) {
      params.portfolioId = this.filterPortfolioId;
    }
    if (this.filterStatus) {
      params.status = this.filterStatus;
    }

    this.subscriptions.push(
      this.orderService.filterOrders(params).subscribe({
        next: (orders) => {
          this.orders = orders;
          this.applyFrontendFilters();
          this.loading = false;
        },
        error: (err) => {
          this.loading = false;
          this.error = err?.error?.message || err?.error || err?.message || 'Failed to load orders';
        }
      })
    );
  }

  applyFilters(): void {
    this.loadOrders();
  }

  applyFrontendFilters(): void {
    // Apply frontend filters for order mode and type
    if (this.filterOrderMode || this.filterType) {
      this.orders = this.orders.filter(order => {
        let matches = true;
        if (this.filterOrderMode && order.orderMode !== this.filterOrderMode) {
          matches = false;
        }
        if (this.filterType && order.type !== this.filterType) {
          matches = false;
        }
        return matches;
      });
    }
  }

  clearFilters(): void {
    this.filterPortfolioId = null;
    this.filterStatus = '';
    this.filterOrderMode = '';
    this.filterType = '';
    this.loadOrders();
  }

  getStatusClass(status: OrderStatus): string {
    switch (status) {
      case 'EXECUTED':
        return 'text-green-400';
      case 'PENDING':
        return 'text-yellow-400';
      case 'FAILED':
        return 'text-red-400';
      case 'EXPIRED':
        return 'text-gray-400';
      default:
        return 'text-gray-400';
    }
  }

  getStatusBgClass(status: OrderStatus): string {
    switch (status) {
      case 'EXECUTED':
        return 'bg-green-500/10 border-green-500/30';
      case 'PENDING':
        return 'bg-yellow-500/10 border-yellow-500/30';
      case 'FAILED':
        return 'bg-red-500/10 border-red-500/30';
      case 'EXPIRED':
        return 'bg-gray-500/10 border-gray-500/30';
      default:
        return 'bg-gray-500/10 border-gray-500/30';
    }
  }

  getOrderModeLabel(mode: OrderMode): string {
    switch (mode) {
      case 'MARKET':
        return 'Market';
      case 'LIMIT':
        return 'Limit';
      case 'STOP_LOSS':
        return 'Stop Loss';
      default:
        return mode;
    }
  }

  getOrderModeClass(mode: OrderMode): string {
    switch (mode) {
      case 'MARKET':
        return 'text-blue-400';
      case 'LIMIT':
        return 'text-purple-400';
      case 'STOP_LOSS':
        return 'text-orange-400';
      default:
        return 'text-gray-400';
    }
  }

  getTypeClass(type: 'BUY' | 'SELL'): string {
    return type === 'BUY' ? 'text-green-400' : 'text-red-400';
  }

  getTypeBgClass(type: 'BUY' | 'SELL'): string {
    return type === 'BUY' ? 'bg-green-500/20' : 'bg-red-500/20';
  }

  formatPrice(price: number | undefined | null): string {
    if (price === null || price === undefined) return '--';
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(price);
  }

  formatDate(dateStr: string | undefined | null): string {
    if (!dateStr) return '--';
    const date = new Date(dateStr);
    return date.toLocaleString('en-IN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  getTotalOrders(): number {
    return this.orders.length;
  }

  getPendingCount(): number {
    return this.orders.filter(o => o.status === 'PENDING').length;
  }

  getExecutedCount(): number {
    return this.orders.filter(o => o.status === 'EXECUTED').length;
  }

  getFailedCount(): number {
    return this.orders.filter(o => o.status === 'FAILED').length;
  }
}
