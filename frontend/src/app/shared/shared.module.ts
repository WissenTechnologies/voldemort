import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AlertMessageComponent } from './components/alert-message/alert-message';
import { LoadingSpinnerComponent } from './components/loading-spinner/loading-spinner';

@NgModule({
  declarations: [
    LoadingSpinnerComponent,
    AlertMessageComponent
  ],
  imports: [
    CommonModule,
    FormsModule
  ],
  exports: [
    LoadingSpinnerComponent,
    AlertMessageComponent,
    FormsModule
  ]
})
export class SharedModule { }