import { Component, Input, Output, EventEmitter } from '@angular/core';

export type AlertType = 'success' | 'error' | 'warning' | 'info';

@Component({
  selector: 'app-alert-message',
  templateUrl: './alert-message.component.html',
  standalone:false,
  styleUrls: ['./alert-message.component.css']
})
export class AlertMessageComponent {
  @Input() type: AlertType = 'info';
  @Input() message: string = '';
  @Input() dismissible: boolean = true;
  @Output() dismissed = new EventEmitter<void>();

  get alertClasses(): string {
    const baseClasses = 'border-l-4 p-4 rounded-lg animate-fade-in';
    const typeClasses = {
      success: 'bg-green-50 border-green-500 text-green-700',
      error: 'bg-red-50 border-red-500 text-red-700',
      warning: 'bg-yellow-50 border-yellow-500 text-yellow-700',
      info: 'bg-blue-50 border-blue-500 text-blue-700'
    };
    return `${baseClasses} ${typeClasses[this.type]}`;
  }

  dismiss(): void {
    this.dismissed.emit();
  }
}