import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-loading-spinner',
  templateUrl: './loading-spinner.component.html',
  standalone:false,
  styleUrls: ['./loading-spinner.component.css']
})
export class LoadingSpinnerComponent {
  @Input() size: 'sm' | 'md' | 'lg' = 'md';
  @Input() fullScreen = false;
  @Input() color: 'primary' | 'white' = 'primary';

  get spinnerClasses(): string {
    const sizes = {
      sm: 'w-5 h-5 border-2',
      md: 'w-8 h-8 border-3',
      lg: 'w-12 h-12 border-4'
    };
    
    const colors = {
      primary: 'border-indigo-600 border-t-transparent',
      white: 'border-white border-t-transparent'
    };

    return `${sizes[this.size]} ${colors[this.color]} rounded-full animate-spin`;
  }
}