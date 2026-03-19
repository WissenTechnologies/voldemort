import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { DashboardComponent } from './dashboard/dashboard.component';
import { ProfileComponent } from './profile/profile.component';
import { CompanyComponent } from './company/company.component';
import { UsersComponent } from './users/users.component';
import { StockChartComponent } from './components/stock-chart/stock-chart.component';
import { PortfolioComponent } from './portfolio/portfolio.component';
import { OrdersComponent } from './orders/orders.component';

import { SharedModule } from '../../shared/shared.module';

// Child routes for dashboard module
const dashboardRoutes: Routes = [
  { 
    path: '', 
    component: DashboardComponent, // Default route for /dashboard
    children: [
      { path: '', redirectTo: 'company', pathMatch: 'full' }, // Redirect to company by default
      { path: 'company', component: CompanyComponent },
      { path: 'portfolio', component: PortfolioComponent },
      { path: 'orders', component: OrdersComponent },
      { path: 'users', component: UsersComponent },
      { path: 'profile', component: ProfileComponent },
      { path: '**', redirectTo: 'company' } // Wildcard within dashboard
    ]
  }
];

@NgModule({
  declarations: [
    DashboardComponent,
    ProfileComponent,
    CompanyComponent,
    PortfolioComponent,
    OrdersComponent,
    UsersComponent,
    StockChartComponent
  ],
  imports: [
    CommonModule,
    RouterModule.forChild(dashboardRoutes), // Register child routes
    FormsModule,
    ReactiveFormsModule,
    SharedModule
  ],
  exports: [
    DashboardComponent,
    ProfileComponent,
    CompanyComponent,
    PortfolioComponent,
    OrdersComponent,
    UsersComponent,
    StockChartComponent
  ]
})
export class DashboardModule { }
