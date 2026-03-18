import { Component, OnInit } from '@angular/core';
import { User } from '../../../core/models/user.model';
import { AuthService } from '../../../core/services/auth';

export interface Company {
  id: number;
  name: string;
  email: string;
  phone: string;
  address: string;
  industry: string;
  employees: number;
  status: 'Active' | 'Inactive' | 'Pending';
  createdAt: Date;
}

@Component({
  selector: 'app-company',
  standalone: false,
  templateUrl: './company.component.html',
  styleUrls: ['./company.component.css']
})
export class CompanyComponent implements OnInit {
  user: User | null = null;
  isEditing = false;
  editingCompany: Company | null = null;
  
  companies: Company[] = [
    {
      id: 1,
      name: 'Tech Solutions Inc.',
      email: 'contact@techsolutions.com',
      phone: '+1 (555) 123-4567',
      address: '123 Tech Street, San Francisco, CA 94105',
      industry: 'Technology',
      employees: 250,
      status: 'Active',
      createdAt: new Date('2023-01-15')
    },
    {
      id: 2,
      name: 'Global Manufacturing Co.',
      email: 'info@globalmfg.com',
      phone: '+1 (555) 987-6543',
      address: '456 Industrial Ave, Detroit, MI 48201',
      industry: 'Manufacturing',
      employees: 1500,
      status: 'Active',
      createdAt: new Date('2023-03-22')
    },
    {
      id: 3,
      name: 'Healthcare Plus',
      email: 'admin@healthcareplus.com',
      phone: '+1 (555) 246-8135',
      address: '789 Medical Blvd, Boston, MA 02108',
      industry: 'Healthcare',
      employees: 500,
      status: 'Pending',
      createdAt: new Date('2023-06-10')
    },
    {
      id: 4,
      name: 'Finance Corp International',
      email: 'service@financecorp.com',
      phone: '+1 (555) 369-2580',
      address: '321 Wall Street, New York, NY 10005',
      industry: 'Finance',
      employees: 800,
      status: 'Inactive',
      createdAt: new Date('2023-02-14')
    }
  ];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
    });
  }

  get isAdmin(): boolean {
    return this.user?.role === 'ADMIN';
  }

  editCompany(company: Company): void {
    this.isEditing = true;
    this.editingCompany = { ...company };
  }

  addCompany(): void {
    this.isEditing = true;
    this.editingCompany = {
      id: 0,
      name: '',
      email: '',
      phone: '',
      address: '',
      industry: '',
      employees: 0,
      status: 'Active',
      createdAt: new Date()
    };
  }

  saveCompany(): void {
    if (this.editingCompany) {
      if (this.editingCompany.id === 0) {
        // Add new company
        const newCompany = {
          ...this.editingCompany,
          id: Math.max(...this.companies.map(c => c.id)) + 1
        };
        this.companies.push(newCompany);
      } else {
        // Update existing company
        const index = this.companies.findIndex(c => c.id === this.editingCompany!.id);
        if (index !== -1) {
          this.companies[index] = this.editingCompany;
        }
      }
    }
    this.cancelEdit();
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.editingCompany = null;
  }

  deleteCompany(company: Company): void {
    if (confirm(`Are you sure you want to delete ${company.name}?`)) {
      this.companies = this.companies.filter(c => c.id !== company.id);
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'Active': return 'bg-emerald-100 text-emerald-700';
      case 'Inactive': return 'bg-red-100 text-red-700';
      case 'Pending': return 'bg-yellow-100 text-yellow-700';
      default: return 'bg-gray-100 text-gray-700';
    }
  }
}
