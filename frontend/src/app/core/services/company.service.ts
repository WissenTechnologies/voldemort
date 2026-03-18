import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of, throwError } from 'rxjs';
import { map, catchError, delay } from 'rxjs/operators';
import { Company, CompanyPrice, CandlestickData, CompanyWithLatestPrice } from '../models/company.model';

@Injectable({
  providedIn: 'root'
})
export class CompanyService {
  private readonly baseUrl = 'http://localhost:5001'; // Company service base URL
  private readonly pricesUrl = 'http://localhost:5002'; // Prices service base URL
  private useMockData = false; // Toggle for mock/real data

  constructor(private http: HttpClient) {}

  // Mock data for demonstration
  private mockCompanies: Company[] = [
    {
      id: 1,
      name: 'Apple Inc.',
      symbol: 'AAPL',
      description: 'Technology company that designs, manufactures, and markets smartphones, personal computers, tablets, wearables, and accessories.',
      sector: 'Technology',
      marketCap: 3000000000000,
      createdAt: '2023-01-15T00:00:00Z',
      updatedAt: '2023-03-18T00:00:00Z'
    },
    {
      id: 2,
      name: 'Microsoft Corporation',
      symbol: 'MSFT',
      description: 'Technology company that develops, manufactures, licenses, supports, and sells computer software, consumer electronics, personal computers, and related services.',
      sector: 'Technology',
      marketCap: 2500000000000,
      createdAt: '2023-01-20T00:00:00Z',
      updatedAt: '2023-03-18T00:00:00Z'
    },
    {
      id: 3,
      name: 'Amazon.com Inc.',
      symbol: 'AMZN',
      description: 'Multinational technology company focusing on e-commerce, cloud computing, digital streaming, and artificial intelligence.',
      sector: 'Consumer Discretionary',
      marketCap: 1500000000000,
      createdAt: '2023-02-01T00:00:00Z',
      updatedAt: '2023-03-18T00:00:00Z'
    },
    {
      id: 4,
      name: 'Tesla Inc.',
      symbol: 'TSLA',
      description: 'Electric vehicle and clean energy company that designs, manufactures, and sells electric cars, battery energy storage, and solar panel products.',
      sector: 'Automotive',
      marketCap: 800000000000,
      createdAt: '2023-02-10T00:00:00Z',
      updatedAt: '2023-03-18T00:00:00Z'
    },
    {
      id: 5,
      name: 'Alphabet Inc.',
      symbol: 'GOOGL',
      description: 'Multinational technology company that specializes in Internet-related services, products, and technologies.',
      sector: 'Technology',
      marketCap: 1800000000000,
      createdAt: '2023-02-15T00:00:00Z',
      updatedAt: '2023-03-18T00:00:00Z'
    },
    {
      id: 6,
      name: 'NVIDIA Corporation',
      symbol: 'NVDA',
      description: 'Technology company that designs graphics processing units (GPUs) for the gaming and professional markets, as well as system on a chip units (SoCs) for the mobile computing and automotive market.',
      sector: 'Technology',
      marketCap: 600000000000,
      createdAt: '2023-02-20T00:00:00Z',
      updatedAt: '2023-03-18T00:00:00Z'
    }
  ];

  private generateMockPrices(companyId: number, days: number): CompanyPrice[] {
    const prices: CompanyPrice[] = [];
    const now = new Date();
    let basePrice = 100 + Math.random() * 400; // Base price between $100-$500
    
    for (let i = days; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(date.getDate() - i);
      
      // Add some random variation
      const variation = (Math.random() - 0.5) * 20;
      basePrice = Math.max(50, basePrice + variation);
      
      prices.push({
        id: i + 1,
        companyId,
        value: parseFloat(basePrice.toFixed(2)),
        recordedAt: date.toISOString()
      });
    }
    
    return prices;
  }

  private generateMockCandlestick(companyId: number, days: number): CandlestickData[] {
    const candlesticks: CandlestickData[] = [];
    const now = new Date();
    let basePrice = 100 + Math.random() * 400;
    
    for (let i = days; i >= 0; i--) {
      const date = new Date(now);
      date.setDate(date.getDate() - i);
      
      const open = basePrice;
      const close = basePrice + (Math.random() - 0.5) * 10;
      const high = Math.max(open, close) + Math.random() * 5;
      const low = Math.min(open, close) - Math.random() * 5;
      
      candlesticks.push({
        time: date.toISOString(),
        open: parseFloat(open.toFixed(2)),
        high: parseFloat(high.toFixed(2)),
        low: parseFloat(low.toFixed(2)),
        close: parseFloat(close.toFixed(2))
      });
      
      basePrice = close;
    }
    
    return candlesticks;
  }

  // Company CRUD operations
  getCompanies(): Observable<Company[]> {
    if (this.useMockData) {
      return of(this.mockCompanies).pipe(delay(500)); // Simulate network delay
    }
    
    return this.http.get<Company[]>(`${this.baseUrl}/api/companies`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      map(response => response.map(company => this.mapCompanyFields(company))),
      catchError(error => {
        console.error('Error fetching companies:', error);
        // Fallback to mock data if backend is unavailable
        return of(this.mockCompanies);
      })
    );
  }

  getRecentPrices(companyId: number, seconds: number = 300): Observable<CompanyPrice[]> {
    if (this.useMockData) {
      return of(this.generateMockPrices(companyId, 1)).pipe(delay(200));
    }

    return this.http.get<CompanyPrice[]>(`${this.pricesUrl}/api/prices/recent/${companyId}`, {
      params: { seconds: String(seconds) },
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        console.error('Error fetching recent prices:', error);
        return of(this.generateMockPrices(companyId, 1));
      })
    );
  }

  createCompany(company: any): Observable<Company> {
    if (this.useMockData) {
      const nextId = Math.max(...this.mockCompanies.map(c => c.id), 0) + 1;
      const created: Company = {
        id: nextId,
        name: company.name,
        symbol: company.symbol,
        description: company.description,
        sector: company.sector,
        marketCap: company.marketCap,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString()
      };
      this.mockCompanies = [created, ...this.mockCompanies];
      return of(created).pipe(delay(300));
    }

    return this.http.post<Company>(`${this.baseUrl}/api/companies`, company, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      map(created => this.mapCompanyFields(created)),
      catchError(error => {
        console.error('Error creating company:', error);
        return throwError(() => error);
      })
    );
  }

  updateCompany(id: number, company: any): Observable<Company> {
    if (this.useMockData) {
      const idx = this.mockCompanies.findIndex(c => c.id === id);
      if (idx === -1) {
        return throwError(() => new Error('Company not found'));
      }
      const updated: Company = {
        ...this.mockCompanies[idx],
        name: company.name,
        symbol: company.symbol,
        description: company.description,
        sector: company.sector,
        marketCap: company.marketCap,
        updatedAt: new Date().toISOString()
      };
      this.mockCompanies = this.mockCompanies.map(c => (c.id === id ? updated : c));
      return of(updated).pipe(delay(300));
    }

    return this.http.put<Company>(`${this.baseUrl}/api/companies/${id}`, company, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      map(updated => this.mapCompanyFields(updated)),
      catchError(error => {
        console.error('Error updating company:', error);
        return throwError(() => error);
      })
    );
  }

  deleteCompany(id: number): Observable<void> {
    if (this.useMockData) {
      this.mockCompanies = this.mockCompanies.filter(c => c.id !== id);
      return of(void 0).pipe(delay(250));
    }

    return this.http.delete<void>(`${this.baseUrl}/api/companies/${id}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        console.error('Error deleting company:', error);
        return throwError(() => error);
      })
    );
  }

  getCompanyById(id: number): Observable<Company | null> {
    if (this.useMockData) {
      const company = this.mockCompanies.find(c => c.id === id);
      return of(company || null).pipe(delay(300));
    }
    
    return this.http.get<Company>(`${this.baseUrl}/api/companies/${id}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      map(company => this.mapCompanyFields(company)),
      catchError(error => {
        console.error('Error fetching company:', error);
        const company = this.mockCompanies.find(c => c.id === id);
        return of(company || null);
      })
    );
  }

  // Helper method to map backend response to frontend model
  private mapCompanyFields(backendCompany: any): Company {
    return {
      id: backendCompany.id,
      name: backendCompany.companyName || backendCompany.name,
      symbol: backendCompany.symbol,
      description: backendCompany.description,
      sector: backendCompany.sector || backendCompany.industry,
      volume: backendCompany.volume,
      value: backendCompany.value,
      marketCap: backendCompany.marketCap,
      createdAt: backendCompany.createdAt || new Date().toISOString(),
      updatedAt: backendCompany.updatedAt || new Date().toISOString()
    };
  }

  // Helper method to get auth token (you may need to implement proper token storage)
  private getAuthToken(): string {
    // For now, return empty or implement proper token retrieval
    return localStorage.getItem('access_token') || '';
  }

  // Stock price operations
  getLatestPrice(companyId: number): Observable<number | null> {
    if (this.useMockData) {
      const prices = this.generateMockPrices(companyId, 1);
      const latestPrice = prices[prices.length - 1]?.value || 0;
      return of(latestPrice).pipe(delay(200));
    }
    
    return this.http.get<number>(`${this.pricesUrl}/api/prices/latest/${companyId}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        console.error('Error fetching latest price:', error);
        // Fallback to mock data
        const prices = this.generateMockPrices(companyId, 1);
        const latestPrice = prices[prices.length - 1]?.value || 0;
        return of(latestPrice);
      })
    );
  }

  getPriceRange(companyId: number, start: string, end: string): Observable<CompanyPrice[]> {
    if (this.useMockData) {
      return of(this.generateMockPrices(companyId, 30)).pipe(delay(300));
    }
    
    return this.http.get<CompanyPrice[]>(`${this.pricesUrl}/api/prices/range/${companyId}`, {
      params: { start, end },
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        console.error('Error fetching price range:', error);
        return of(this.generateMockPrices(companyId, 30));
      })
    );
  }

  getOneMonthData(companyId: number): Observable<CompanyPrice[]> {
    if (this.useMockData) {
      return of(this.generateMockPrices(companyId, 30)).pipe(delay(300));
    }
    
    return this.http.get<CompanyPrice[]>(`${this.pricesUrl}/api/prices/1month/${companyId}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        console.error('Error fetching 1 month data:', error);
        return of(this.generateMockPrices(companyId, 30));
      })
    );
  }

  getSixMonthsData(companyId: number): Observable<CompanyPrice[]> {
    if (this.useMockData) {
      return of(this.generateMockPrices(companyId, 180)).pipe(delay(400));
    }
    
    return this.http.get<CompanyPrice[]>(`${this.pricesUrl}/api/prices/6months/${companyId}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        console.error('Error fetching 6 months data:', error);
        return of(this.generateMockPrices(companyId, 180));
      })
    );
  }

  getOneYearData(companyId: number): Observable<CompanyPrice[]> {
    if (this.useMockData) {
      return of(this.generateMockPrices(companyId, 365)).pipe(delay(500));
    }
    
    return this.http.get<CompanyPrice[]>(`${this.pricesUrl}/api/prices/1year/${companyId}`, {
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        console.error('Error fetching 1 year data:', error);
        return of(this.generateMockPrices(companyId, 365));
      })
    );
  }

  getCandlestickData(companyId: number, start?: string, end?: string): Observable<CandlestickData[]> {
    if (this.useMockData) {
      return of(this.generateMockCandlestick(companyId, 30)).pipe(delay(300));
    }
    
    const url = `${this.pricesUrl}/api/prices/candlestick/${companyId}`;
    const params: any = {};
    
    if (start) params.start = start;
    if (end) params.end = end;

    return this.http.get<CandlestickData[]>(url, { 
      params,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${this.getAuthToken()}`
      }
    }).pipe(
      catchError(error => {
        console.error('Error fetching candlestick data:', error);
        return of(this.generateMockCandlestick(companyId, 30));
      })
    );
  }

  // Combined operations
  getCompaniesWithLatestPrices(): Observable<CompanyWithLatestPrice[]> {
    return this.getCompanies().pipe(
      map(companies => companies.map(company => ({
        ...company,
        latestPrice: undefined,
        priceChange: undefined,
        priceChangePercent: undefined
      })))
    );
  }

  getCompanyWithPriceData(companyId: number): Observable<CompanyWithLatestPrice | null> {
    return this.getCompanyById(companyId).pipe(
      map(company => {
        if (!company) return null;
        return {
          ...company,
          latestPrice: undefined,
          priceChange: undefined,
          priceChangePercent: undefined
        };
      })
    );
  }
}
