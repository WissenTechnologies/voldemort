import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { DashboardComponent } from './dashboard/dashboard.component';
import { ProfileComponent } from './profile/profile.component';
import { CompanyComponent } from './company/company.component';
import { UsersComponent } from './users/users.component';

import { SharedModule } from '../../shared/shared.module';

// Child routes for dashboard module
const dashboardRoutes: Routes = [
  { 
    path: '', 
    component: DashboardComponent, // Default route for /dashboard
    children: [
      { path: '', redirectTo: 'company', pathMatch: 'full' }, // Redirect to company by default
      { path: 'company', component: CompanyComponent },
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
    UsersComponent
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
    UsersComponent
  ]
})
export class DashboardModule { }
