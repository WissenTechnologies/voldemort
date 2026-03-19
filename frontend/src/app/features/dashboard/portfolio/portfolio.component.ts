import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subscription, interval } from 'rxjs';
import { startWith, switchMap } from 'rxjs/operators';

import { OrderMode, OrderRequest } from '../../../core/models/order.model';

import { AuthService } from '../../../core/services/auth';
import { OrderService } from '../../../core/services/order.service';
import { PortfolioService } from '../../../core/services/portfolio.service';
import { User } from '../../../core/models/user.model';
import { Holding, Portfolio, PortfolioSummary } from '../../../core/models/portfolio.model';

@Component({
  selector: 'app-portfolio',
  standalone: false,
  templateUrl: './portfolio.component.html',
  styleUrls: ['./portfolio.component.css']
})
export class PortfolioComponent implements OnInit, OnDestroy {
  user: User | null = null;

  createForm!: FormGroup;
  creating = false;
  createError: string | null = null;

  portfolios: Portfolio[] = [];
  loadingPortfolios = false;
  portfoliosError: string | null = null;

  selectedPortfolio: Portfolio | null = null;
  selectedSummary: PortfolioSummary | null = null;
  holdings: Holding[] = [];
  loadingHoldings = false;
  holdingsError: string | null = null;

  sellingHolding: Holding | null = null;
  sellQuantity = 1;
  sellOrderMode: OrderMode = 'MARKET';
  sellTargetPrice: number | null = null;
  selling = false;
  sellError: string | null = null;
  sellSuccess: string | null = null;

  private subscriptions: Subscription[] = [];
  private pricePollingSub: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private portfolioService: PortfolioService,
    private orderService: OrderService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.createForm = this.fb.group({
      name: ['', [Validators.required]]
    });

    this.subscriptions.push(
      this.authService.currentUser$.subscribe(user => {
        this.user = user;
        if (this.getNumericUserId() > 0) {
          this.loadPortfolios();
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.stopPricePolling();
    this.subscriptions.forEach(s => s.unsubscribe());
  }

  loadPortfolios(): void {
    const userId = this.getNumericUserId();
    if (userId <= 0) {
      this.portfoliosError = 'User id is not available. Please re-login so a numeric userId is present in the token.';
      return;
    }

    this.loadingPortfolios = true;
    this.portfoliosError = null;

    this.subscriptions.push(
      this.portfolioService.getPortfoliosByUserId(userId).subscribe({
        next: (res) => {
          this.portfolios = Array.isArray(res?.data) ? [...res.data] : [];
          this.loadingPortfolios = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.loadingPortfolios = false;
          this.portfoliosError = err?.error?.message || err?.message || 'Failed to load portfolios';
          this.cdr.detectChanges();
        }
      })
    );
  }

  createPortfolio(): void {
    const userId = this.getNumericUserId();
    if (userId <= 0) {
      this.createError = 'User id is not available. Please re-login so a numeric userId is present in the token.';
      return;
    }

    this.createError = null;
    if (this.createForm.invalid) {
      this.createForm.markAllAsTouched();
      return;
    }

    const name = String(this.createForm.value.name || '').trim();
    if (!name) {
      this.createError = 'Portfolio name is required';
      return;
    }

    this.creating = true;

    this.subscriptions.push(
      this.portfolioService.createPortfolio(userId, name).subscribe({
        next: () => {
          this.creating = false;
          this.createForm.reset();
          this.loadPortfolios();
        },
        error: (err) => {
          this.creating = false;
          this.createError = err?.error?.message || err?.error || err?.message || 'Failed to create portfolio';
        }
      })
    );
  }

  private getNumericUserId(): number {
    const raw: any = this.user?.id;
    if (typeof raw === 'number') return raw;
    if (typeof raw === 'string' && /^\d+$/.test(raw)) return Number(raw);
    return 0;
  }

  selectPortfolio(p: Portfolio): void {
    this.stopPricePolling();
    this.selectedPortfolio = p;
    this.selectedSummary = null;
    this.holdings = [];
    this.holdingsError = null;

    this.cdr.detectChanges();

    this.fetchHoldings(p.id);
    this.startPricePolling(p.id);
  }

  closeSelected(): void {
    this.stopPricePolling();
    this.selectedPortfolio = null;
    this.selectedSummary = null;
    this.holdings = [];
    this.holdingsError = null;
    this.closeSellModal();

    this.cdr.detectChanges();
  }

  openSellModal(h: Holding): void {
    if (!this.selectedPortfolio) return;
    this.sellingHolding = h;
    this.sellQuantity = 1;
    this.sellOrderMode = 'MARKET';
    this.sellTargetPrice = null;
    this.sellError = null;
    this.sellSuccess = null;
  }

  closeSellModal(): void {
    this.sellingHolding = null;
    this.sellQuantity = 1;
    this.sellOrderMode = 'MARKET';
    this.sellTargetPrice = null;
    this.selling = false;
    this.sellError = null;
  }

  sellFromHolding(): void {
    const userId = this.getNumericUserId();
    const portfolioId = this.selectedPortfolio?.id ?? 0;
    const holding = this.sellingHolding;

    this.sellError = null;
    this.sellSuccess = null;

    if (userId <= 0) {
      this.sellError = 'User id is not available. Please re-login.';
      return;
    }
    if (portfolioId <= 0) {
      this.sellError = 'Portfolio id is missing.';
      return;
    }
    if (!holding) {
      this.sellError = 'No holding selected.';
      return;
    }
    if (!Number.isFinite(this.sellQuantity) || this.sellQuantity <= 0) {
      this.sellError = 'Quantity must be greater than 0.';
      return;
    }
    if (this.sellQuantity > (holding.quantity ?? 0)) {
      this.sellError = 'Cannot sell more than available quantity.';
      return;
    }
    if (!Number.isFinite(holding.companyId) || holding.companyId <= 0) {
      this.sellError = 'Company id is missing in holding. Please refresh.';
      return;
    }

    if (this.sellOrderMode !== 'MARKET' && (!this.sellTargetPrice || this.sellTargetPrice <= 0)) {
      this.sellError = 'Target price is required for LIMIT and STOP LOSS orders.';
      return;
    }

    this.selling = true;
    const orderRequest: OrderRequest = {
      userId,
      portfolioId,
      companyId: holding.companyId,
      quantity: this.sellQuantity,
      orderMode: this.sellOrderMode,
      targetPrice: this.sellOrderMode !== 'MARKET' ? this.sellTargetPrice || undefined : undefined
    };
    this.subscriptions.push(
      this.orderService.sellStock(orderRequest).subscribe({
        next: () => {
          this.selling = false;
          this.sellSuccess = 'Sold successfully';
          this.fetchHoldings(portfolioId);
          this.closeSellModal();
        },
        error: (err) => {
          this.selling = false;
          this.sellError = err?.error?.message || err?.error || err?.message || 'Failed to sell stock';
        }
      })
    );
  }

  private fetchHoldings(portfolioId: number): void {
    this.loadingHoldings = true;
    this.holdingsError = null;

    this.cdr.detectChanges();

    this.subscriptions.push(
      this.portfolioService.getPortfolioByPortfolioId(portfolioId).subscribe({
        next: (res) => {
          this.selectedSummary = res?.data || null;
          this.holdings = Array.isArray(this.selectedSummary?.holdings) ? this.selectedSummary!.holdings : [];
          this.loadingHoldings = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.loadingHoldings = false;
          this.holdingsError = err?.error?.message || err?.message || 'Failed to load holdings';
          this.cdr.detectChanges();
        }
      })
    );
  }

  private startPricePolling(portfolioId: number): void {
    this.stopPricePolling();

    this.pricePollingSub = interval(5000)
      .pipe(
        startWith(0),
        switchMap(() => this.portfolioService.getPortfolioByPortfolioId(portfolioId))
      )
      .subscribe({
        next: (res) => {
          this.selectedSummary = res?.data || null;
          this.holdings = Array.isArray(this.selectedSummary?.holdings) ? [...this.selectedSummary!.holdings] : [];
          this.cdr.detectChanges();
        },
        error: () => {
          // Ignore transient polling errors; user can still manually refresh by re-opening or using Refresh
        }
      });
  }

  private stopPricePolling(): void {
    if (this.pricePollingSub) {
      this.pricePollingSub.unsubscribe();
      this.pricePollingSub = null;
    }
  }

  formatCurrency(value?: number): string {
    if (value === null || value === undefined) return '--';
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);
  }

  formatNumber(value?: number): string {
    if (value === null || value === undefined) return '--';
    return new Intl.NumberFormat('en-IN').format(value);
  }

  getPlClass(value?: number): string {
    if (value === null || value === undefined) return 'text-gray-400';
    return value >= 0 ? 'text-green-400' : 'text-red-400';
  }
}
