import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { User } from '../../../core/models/user.model';
import { AuthService } from '../../../core/services/auth';
import { CompanyService } from '../../../core/services/company.service';
import { CompanyWithLatestPrice, CompanyPrice, CandlestickData } from '../../../core/models/company.model';
import { Subscription, interval } from 'rxjs';

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
  
  private subscriptions: Subscription[] = [];

  constructor(
    private authService: AuthService,
    private companyService: CompanyService,
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
      marketCap: [null],
      peRatio: [null],
      eps: [null],
      description: ['']
    });
  }

  ngOnDestroy(): void {
    this.subscriptions.forEach(sub => sub.unsubscribe());
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
    this.cdr.detectChanges();
    this.loadStockData(company.id);
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
          this.loadCandlestickData(companyId);
        },
        error: () => {
          this.stockDataLoading = false;
          this.cdr.detectChanges();
        }
      })
    );
  }

  loadCandlestickData(companyId: number): void {
    this.subscriptions.push(
      this.companyService.getCandlestickData(companyId).subscribe(data => {
        this.candlestickData = data;
        this.stockDataLoading = false;
        this.cdr.detectChanges();
      })
    );
  }

  setTimeRange(range: string): void {
    this.selectedTimeRange = range;
    if (this.selectedCompany) {
      this.loadStockData(this.selectedCompany.id);
    }
  }

  closeDetail(): void {
    this.selectedCompany = null;
    this.stockChartData = [];
    this.candlestickData = [];
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
