import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CompanyPrice, CandlestickData } from '../../../../core/models/company.model';

@Component({
  selector: 'app-stock-chart',
  standalone: false,
  templateUrl: './stock-chart.component.html',
  styleUrls: ['./stock-chart.component.css']
})
export class StockChartComponent implements OnChanges {
  @Input() stockData: CompanyPrice[] = [];
  @Input() candlestickData: CandlestickData[] = [];
  @Input() chartType: 'line' | 'candlestick' = 'line';
  @Input() height = 300;

  chartData: any = null;
  chartOptions: any = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['stockData'] || changes['candlestickData']) {
      this.updateChartData();
    }
  }

  private updateChartData(): void {
    if (this.chartType === 'line' && this.stockData.length > 0) {
      this.setupLineChart();
    } else if (this.chartType === 'candlestick' && this.candlestickData.length > 0) {
      this.setupCandlestickChart();
    }
  }

  private setupLineChart(): void {
    this.chartData = {
      labels: this.stockData.map(price => new Date(price.recordedAt).toLocaleDateString()),
      datasets: [{
        label: 'Stock Price',
        data: this.stockData.map(price => price.value),
        borderColor: 'rgb(75, 192, 192)',
        backgroundColor: 'rgba(75, 192, 192, 0.1)',
        tension: 0.1,
        fill: true
      }]
    };

    this.chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        x: {
          grid: {
            color: 'rgba(255, 255, 255, 0.1)'
          },
          ticks: {
            color: 'rgba(255, 255, 255, 0.7)'
          }
        },
        y: {
          grid: {
            color: 'rgba(255, 255, 255, 0.1)'
          },
          ticks: {
            color: 'rgba(255, 255, 255, 0.7)',
            callback: (value: number) => '₹' + value.toFixed(2)
          }
        }
      }
    };
  }

  private setupCandlestickChart(): void {
    // For candlestick chart, we'll use a bar chart with custom styling
    this.chartData = {
      labels: this.candlestickData.map(candle => new Date(candle.time).toLocaleDateString()),
      datasets: [
        {
          label: 'High',
          data: this.candlestickData.map(candle => candle.high),
          backgroundColor: 'rgba(255, 99, 132, 0.8)',
          borderColor: 'rgb(255, 99, 132)',
          borderWidth: 1
        },
        {
          label: 'Low',
          data: this.candlestickData.map(candle => candle.low),
          backgroundColor: 'rgba(54, 162, 235, 0.8)',
          borderColor: 'rgb(54, 162, 235)',
          borderWidth: 1
        }
      ]
    };

    this.chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          labels: {
            color: 'rgba(255, 255, 255, 0.7)'
          }
        }
      },
      scales: {
        x: {
          grid: {
            color: 'rgba(255, 255, 255, 0.1)'
          },
          ticks: {
            color: 'rgba(255, 255, 255, 0.7)'
          }
        },
        y: {
          grid: {
            color: 'rgba(255, 255, 255, 0.1)'
          },
          ticks: {
            color: 'rgba(255, 255, 255, 0.7)',
            callback: (value: number) => '₹' + value.toFixed(2)
          }
        }
      }
    };
  }
}
