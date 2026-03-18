import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing-module';
import { FormsModule } from '@angular/forms';

import { AuthModule } from './features/auth/auth.module';
import { SharedModule } from './shared/shared.module';
import { CoreModule } from './core/core.module';
import { AppComponent } from './app.component';

@NgModule({
  declarations: [AppComponent],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    FormsModule,
    CoreModule, // ✅ interceptor module
    AuthModule, // ✅ auth features
    SharedModule, // ✅ shared components
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
