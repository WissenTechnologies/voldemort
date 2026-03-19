import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { User } from '../../../core/models/user.model';
import { AuthService } from '../../../core/services/auth';
import { CompanyService } from '../../../core/services/company.service';
import { PortfolioService } from '../../../core/services/portfolio.service';
import { OrderService } from '../../../core/services/order.service';
import { CompanyWithLatestPrice, CompanyPrice, CandlestickData } from '../../../core/models/company.model';
import { Portfolio } from '../../../core/models/portfolio.model';
import { Subscription, interval } from 'rxjs';
import { OrderMode, OrderStatus, OrderType, OrderRequest } from '../../../core/models/order.model';

@Component({
  selector: 'app-company',
  standalone: false,
  templateUrl: './company.component.html',
  styleUrls: ['./company.component.css']
})
export class CompanyComponent implements OnInit, OnDestroy {
  user: User | null = null;
  companies: CompanyWithLatestPrice[] = [];
  loading = true;
  selectedCompany: CompanyWithLatestPrice | null = null;
  stockChartData: CompanyPrice[] = [];
  candlestickData: CandlestickData[] = [];
  selectedTimeRange = '1M'; // 1M, 6M, 1Y
  currentTime = new Date().toLocaleTimeString();

  companyFormOpen = false;
  companyFormMode: 'create' | 'edit' = 'create';
  companyForm!: FormGroup;
  savingCompany = false;
  companyFormError: string | null = null;
  editingCompanyId: number | null = null;
  
  // Stock data loading state
  stockDataLoading = false;

  portfolios: Portfolio[] = [];
  loadingPortfolios = false;
  tradePortfolioId: number | null = null;
  tradeQuantity = 1;
  tradeOrderMode: OrderMode = 'MARKET';
  tradeTargetPrice: number | null = null;
  trading = false;
  tradeError: string | null = null;
  tradeSuccess: string | null = null;
  
  private subscriptions: Subscription[] = [];
  private selectedCompanyChartTickSub: Subscription | null = null;

  constructor(
    private authService: AuthService,
    private companyService: CompanyService,
    private portfolioService: PortfolioService,
    private orderService: OrderService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.subscriptions.push(
      this.authService.currentUser$.subscribe(user => {
        this.user = user;
      })
    );
    
    // Initial load
    this.loadCompanies();
    
    // Poll for company data every second
    this.subscriptions.push(
      interval(1000).subscribe(() => {
        this.currentTime = new Date().toLocaleTimeString();
        this.refreshCompanyData();
      })
    );

    this.companyForm = this.fb.group({
      id: [null, [Validators.required]],
      name: ['', [Validators.required]],
      symbol: ['', [Validators.required]],
      sector: [''],
      industry: [''],
      ceo: [''],
      foundedYear: [null],
      headquarters: [''],
      volume: [null],
      value: [null],
      marketCap: [null],
      peRatio: [null],
      eps: [null],
      description: ['']
    });
  }

  private loadRealtimeStockData(companyId: number): void {
    this.stockDataLoading = true;

    let initial$;
    switch (this.selectedTimeRange) {
      case '6M':
        initial$ = this.companyService.getSixMonthsData(companyId);
        break;
      case '1Y':
        initial$ = this.companyService.getOneYearData(companyId);
        break;
      case '1M':
      default:
        initial$ = this.companyService.getOneMonthData(companyId);
        break;
    }

    this.subscriptions.push(
      initial$.subscribe({
        next: (data) => {
          this.stockChartData = Array.isArray(data) ? data : [];
          this.candlestickData = this.generateCandlestickFromPrices(this.stockChartData);
          this.stockDataLoading = false;
          this.cdr.detectChanges();

          this.selectedCompanyChartTickSub?.unsubscribe();
          this.selectedCompanyChartTickSub = interval(1000).subscribe(() => {
            if (!this.selectedCompany || this.selectedCompany.id !== companyId) return;

            this.companyService.getLatestPrice(companyId).subscribe(price => {
              if (price === null) return;
              const point: CompanyPrice = {
                id: 0,
                companyId,
                value: price,
                recordedAt: new Date().toISOString()
              };

              const last = this.stockChartData[this.stockChartData.length - 1];
              if (last && Math.abs(new Date(last.recordedAt).getTime() - new Date(point.recordedAt).getTime()) < 900) {
                this.stockChartData[this.stockChartData.length - 1] = point;
              } else {
                this.stockChartData = [...this.stockChartData, point];
              }

              const maxPoints = this.selectedTimeRange === '1M' ? 4000 : this.selectedTimeRange === '6M' ? 6000 : 8000;
              if (this.stockChartData.length > maxPoints) {
                this.stockChartData = this.stockChartData.slice(this.stockChartData.length - maxPoints);
              }

              this.candlestickData = this.generateCandlestickFromPrices(this.stockChartData);
              this.cdr.detectChanges();
            });
          });
        },
        error: () => {
          this.subscriptions.push(
            this.companyService.getRecentPrices(companyId, 300).subscribe({
              next: (fallback) => {
                this.stockChartData = Array.isArray(fallback) ? fallback : [];
                this.candlestickData = this.generateCandlestickFromPrices(this.stockChartData);
                this.stockDataLoading = false;
                this.cdr.detectChanges();
              },
              error: () => {
                this.stockDataLoading = false;
                this.cdr.detectChanges();
              }
            })
          );
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
    this.selectedCompanyChartTickSub?.unsubscribe();
  }

  get isAdmin(): boolean {
    return this.user?.role === 'ADMIN';
  }

  openCreateCompany(): void {
    if (!this.isAdmin) return;
    this.companyFormMode = 'create';
    this.editingCompanyId = null;
    this.companyFormError = null;
    this.companyForm.reset();
    this.companyForm.get('id')?.enable();
    this.companyFormOpen = true;
  }

  openEditCompany(company: CompanyWithLatestPrice, event?: Event): void {
    event?.stopPropagation();
    if (!this.isAdmin) return;
    this.companyFormMode = 'edit';
    this.editingCompanyId = company.id;
    this.companyFormError = null;

    this.companyForm.reset({
      id: company.id,
      name: company.name,
      symbol: company.symbol,
      sector: company.sector || '',
      marketCap: company.marketCap ?? null,
      description: company.description || ''
    });
    this.companyForm.get('id')?.disable();
    this.companyFormOpen = true;
  }

  closeCompanyForm(): void {
    this.companyFormOpen = false;
    this.companyFormError = null;
    this.savingCompany = false;
  }

  saveCompany(): void {
    if (!this.isAdmin) return;
    this.companyFormError = null;

    if (this.companyForm.invalid) {
      this.companyForm.markAllAsTouched();
      return;
    }

    this.savingCompany = true;

    const raw = this.companyForm.getRawValue();
    const payload = {
      id: raw.id,
      companyName: raw.name,
      symbol: raw.symbol,
      sector: raw.sector,
      industry: raw.industry,
      ceo: raw.ceo,
      foundedYear: raw.foundedYear,
      headquarters: raw.headquarters,
      volume: raw.volume,
      value: raw.value,
      marketCap: raw.marketCap,
      peRatio: raw.peRatio,
      eps: raw.eps,
      description: raw.description
    };

    const request$ = this.companyFormMode === 'create'
      ? this.companyService.createCompany(payload)
      : this.companyService.updateCompany(this.editingCompanyId as number, payload);

    this.subscriptions.push(
      request$.subscribe({
        next: () => {
          this.savingCompany = false;
          this.closeCompanyForm();
          this.loadCompanies();
        },
        error: (err) => {
          this.savingCompany = false;
          this.companyFormError = err?.error || err?.message || 'Failed to save company';
        }
      })
    );
  }

  deleteCompany(company: CompanyWithLatestPrice, event?: Event): void {
    event?.stopPropagation();
    if (!this.isAdmin) return;

    const ok = confirm(`Delete company "${company.name}"?`);
    if (!ok) return;

    this.subscriptions.push(
      this.companyService.deleteCompany(company.id).subscribe({
        next: () => {
          if (this.selectedCompany?.id === company.id) {
            this.closeDetail();
          }
          this.loadCompanies();
        },
        error: (err) => {
          alert(err?.error || err?.message || 'Failed to delete company');
        }
      })
    );
  }

  loadCompanies(): void {
    this.loading = true;
    this.subscriptions.push(
      this.companyService.getCompanies().subscribe(companies => {
        this.companies = companies;
        this.loadLatestPrices();
        this.cdr.detectChanges();
      })
    );
  }

  // Called every second for automatic refresh (no loading spinner)
  refreshCompanyData(): void {
    this.subscriptions.push(
      this.companyService.getCompanies().subscribe(companies => {
        // Merge to preserve any existing price data and update
        companies.forEach(updated => {
          const existing = this.companies.find(c => c.id === updated.id);
          if (existing) {
            Object.assign(existing, updated);
          } else {
            this.companies.push(updated);
          }
        });
        this.refreshLatestPrices();
      })
    );
  }

  // Refresh prices without full loading state
  refreshLatestPrices(): void {
    const priceSubscriptions = this.companies.map(company => 
      this.companyService.getLatestPrice(company.id).subscribe(price => {
        if (price !== null) {
          const previousPrice = company.latestPrice || price;
          company.latestPrice = price;
          // Calculate actual price change
          company.priceChange = price - previousPrice;
          company.priceChangePercent = previousPrice !== 0 
            ? ((price - previousPrice) / previousPrice) * 100 
            : 0;
        }
      })
    );
    
    this.subscriptions.push(...priceSubscriptions);
    this.cdr.detectChanges();
  }

  loadLatestPrices(): void {
    const priceSubscriptions = this.companies.map(company => 
      this.companyService.getLatestPrice(company.id).subscribe(price => {
        if (price !== null) {
          const previousPrice = company.latestPrice || price;
          company.latestPrice = price;
          // Calculate actual price change
          company.priceChange = price - previousPrice;
          company.priceChangePercent = previousPrice !== 0 
            ? ((price - previousPrice) / previousPrice) * 100 
            : 0;
        }
      })
    );
    
    this.subscriptions.push(...priceSubscriptions);
    this.loading = false;
    this.cdr.detectChanges();
  }

  selectCompany(company: CompanyWithLatestPrice): void {
    this.selectedCompany = company;
    this.stockDataLoading = true;
    this.stockChartData = [];
    this.candlestickData = [];
    this.selectedCompanyChartTickSub?.unsubscribe();
    this.selectedCompanyChartTickSub = null;
    this.cdr.detectChanges();
    this.loadRealtimeStockData(company.id);

    this.tradeError = null;
    this.tradeSuccess = null;
    this.tradeQuantity = 1;
    this.tradeOrderMode = 'MARKET';
    this.tradeTargetPrice = null;
    this.tradePortfolioId = null;
    this.loadUserPortfolios();
  }

  private getNumericUserId(): number {
    const raw: any = this.user?.id;
    if (typeof raw === 'number') return raw;
    if (typeof raw === 'string' && /^\d+$/.test(raw)) return Number(raw);
    return 0;
  }

  loadUserPortfolios(): void {
    const userId = this.getNumericUserId();
    if (userId <= 0) {
      this.tradeError = 'User id is not available. Please re-login.';
      this.portfolios = [];
      this.tradePortfolioId = null;
      return;
    }

    this.loadingPortfolios = true;
    this.subscriptions.push(
      this.portfolioService.getPortfoliosByUserId(userId).subscribe({
        next: (res) => {
          this.portfolios = Array.isArray(res?.data) ? res.data : [];
          this.tradePortfolioId = this.portfolios.length > 0 ? this.portfolios[0].id : null;
          this.loadingPortfolios = false;
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.loadingPortfolios = false;
          this.portfolios = [];
          this.tradePortfolioId = null;
          this.tradeError = err?.error?.message || err?.message || 'Failed to load portfolios';
          this.cdr.detectChanges();
        }
      })
    );
  }

  buyStock(): void {
    const userId = this.getNumericUserId();
    const companyId = this.selectedCompany?.id ?? 0;
    const portfolioId = this.tradePortfolioId ?? 0;

    this.tradeError = null;
    this.tradeSuccess = null;

    if (userId <= 0) {
      this.tradeError = 'User id is not available. Please re-login.';
      return;
    }
    if (companyId <= 0) {
      this.tradeError = 'Company id is missing.';
      return;
    }
    if (portfolioId <= 0) {
      this.tradeError = 'Please select a portfolio.';
      return;
    }
    if (!Number.isFinite(this.tradeQuantity) || this.tradeQuantity <= 0) {
      this.tradeError = 'Quantity must be greater than 0.';
      return;
    }
    if (this.tradeOrderMode !== 'MARKET' && (!this.tradeTargetPrice || this.tradeTargetPrice <= 0)) {
      this.tradeError = 'Target price is required for LIMIT and STOP LOSS orders.';
      return;
    }

    this.trading = true;
    const orderRequest: OrderRequest = {
      userId,
      portfolioId,
      companyId,
      quantity: this.tradeQuantity,
      orderMode: this.tradeOrderMode,
      targetPrice: this.tradeOrderMode !== 'MARKET' ? this.tradeTargetPrice || undefined : undefined
    };
    this.subscriptions.push(
      this.orderService.buyStock(orderRequest).subscribe({
        next: () => {
          this.trading = false;
          this.tradeSuccess = 'Bought successfully';
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.trading = false;
          const backendBody = err?.error;
          this.tradeError =
            (typeof backendBody === 'string' ? backendBody : null) ||
            backendBody?.message ||
            err?.message ||
            'Failed to buy stock';
          this.cdr.detectChanges();
        }
      })
    );
  }

  loadStockData(companyId: number): void {
    this.stockDataLoading = true;
    let dataObservable;
    
    switch (this.selectedTimeRange) {
      case '1M':
        dataObservable = this.companyService.getOneMonthData(companyId);
        break;
      case '6M':
        dataObservable = this.companyService.getSixMonthsData(companyId);
        break;
      case '1Y':
        dataObservable = this.companyService.getOneYearData(companyId);
        break;
      default:
        dataObservable = this.companyService.getOneMonthData(companyId);
    }
    
    this.subscriptions.push(
      dataObservable.subscribe({
        next: (data) => {
          this.stockChartData = data;
          this.candlestickData = this.generateCandlestickFromPrices(data);
          this.stockDataLoading = false;
          this.cdr.detectChanges();
        },
        error: () => {
          this.stockDataLoading = false;
          this.cdr.detectChanges();
        }
      })
    );
  }

  loadCandlestickData(companyId: number): void {
    // No-op: candlestick data is derived from the price history we already load.
    // Kept for backwards compatibility with older template calls.
    this.stockDataLoading = false;
    this.cdr.detectChanges();
  }

  private generateCandlestickFromPrices(prices: CompanyPrice[]): CandlestickData[] {
    if (!prices || prices.length === 0) return [];

    // Group by day (YYYY-MM-DD)
    const grouped = new Map<string, CompanyPrice[]>();
    for (const p of prices) {
      const key = new Date(p.recordedAt).toISOString().slice(0, 10);
      const arr = grouped.get(key);
      if (arr) {
        arr.push(p);
      } else {
        grouped.set(key, [p]);
      }
    }

    const out: CandlestickData[] = [];
    grouped.forEach((dayPrices, day) => {
      // ensure sorted
      dayPrices.sort((a, b) => new Date(a.recordedAt).getTime() - new Date(b.recordedAt).getTime());
      const open = dayPrices[0].value;
      const close = dayPrices[dayPrices.length - 1].value;
      let high = open;
      let low = open;
      for (const dp of dayPrices) {
        if (dp.value > high) high = dp.value;
        if (dp.value < low) low = dp.value;
      }
      out.push({ time: day, open, high, low, close });
    });

    out.sort((a, b) => new Date(a.time).getTime() - new Date(b.time).getTime());
    return out;
  }

  setTimeRange(range: string): void {
    this.selectedTimeRange = range;
    if (this.selectedCompany) {
      this.loadRealtimeStockData(this.selectedCompany.id);
    }
  }

  closeDetail(): void {
    this.selectedCompany = null;
    this.stockChartData = [];
    this.candlestickData = [];
    this.selectedCompanyChartTickSub?.unsubscribe();
    this.selectedCompanyChartTickSub = null;
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(price);
  }

  formatPercent(percent?: number): string {
    if (!percent) return '--';
    const sign = percent >= 0 ? '+' : '';
    return `${sign}${percent.toFixed(2)}%`;
  }

  getPriceChangeClass(change?: number): string {
    if (!change) return 'text-gray-400';
    return change >= 0 ? 'text-green-400' : 'text-red-400';
  }
}
