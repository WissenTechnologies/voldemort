import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { User } from '../../../core/models/user.model';
import { AuthService } from '../../../core/services/auth';
import { Wallet } from '../../../core/models/wallet.model';
import { WalletService } from '../../../core/services/wallet.service';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: User | null = null;
  isEditing = false;
  profileForm: any = {};

  wallet: Wallet | null = null;
  walletLoading = false;
  walletError = '';
  private walletInitAttempted = false;
  addAmount: number | null = null;
  addLoading = false;
  addError = '';
  addSuccess = '';

  constructor(
    private authService: AuthService,
    private walletService: WalletService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.authService.currentUser$.subscribe(user => {
      this.user = user;
      if (user) {
        this.profileForm = {
          username: user.username,
          email: user.email,
          role: user.role
        };

        this.loadWallet(user.id);
      }
      this.cdr.detectChanges();
    });
  }

  private loadWallet(userId: number): void {
    this.walletLoading = true;
    this.walletError = '';
    this.wallet = null;

    this.walletService.getWallet(userId).subscribe({
      next: (w) => {
        this.wallet = w;
        this.walletLoading = false;
        this.walletInitAttempted = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        const msg = err?.error?.error || err?.message || 'Failed to load wallet';

        if (!this.walletInitAttempted && typeof msg === 'string' && msg.toLowerCase().includes('wallet not found')) {
          this.walletInitAttempted = true;
          this.walletService.createWallet(userId, 0).subscribe({
            next: () => {
              this.loadWallet(userId);
            },
            error: (createErr) => {
              this.walletLoading = false;
              this.walletError = createErr?.error?.error || createErr?.message || 'Failed to initialize wallet';
              this.cdr.detectChanges();
            }
          });
          return;
        }

        this.walletLoading = false;
        this.walletError = msg;
        this.cdr.detectChanges();
      }
    });
  }

  addMoney(): void {
    this.addError = '';
    this.addSuccess = '';

    if (!this.user?.id) {
      this.addError = 'User not available';
      return;
    }

    const amount = this.addAmount;
    if (amount == null || amount <= 0) {
      this.addError = 'Enter a valid amount';
      return;
    }

    this.addLoading = true;
    this.walletService.addMoney(this.user.id, amount).subscribe({
      next: (res) => {
        this.addLoading = false;
        this.addSuccess = res?.message || 'Money added successfully';
        this.addAmount = null;
        this.loadWallet(this.user!.id);
      },
      error: (err) => {
        this.addLoading = false;
        this.addError = err?.error?.error || err?.message || 'Failed to add money';
        this.cdr.detectChanges();
      }
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
  }

  saveProfile(): void {
    // In a real app, this would call an API to update the profile
    console.log('Saving profile:', this.profileForm);
    this.isEditing = false;
  }

  cancelEdit(): void {
    if (this.user) {
      this.profileForm = {
        username: this.user.username,
        email: this.user.email,
        role: this.user.role
      };
    }
    this.isEditing = false;
  }
}
