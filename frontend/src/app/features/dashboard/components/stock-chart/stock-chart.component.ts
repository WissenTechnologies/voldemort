import { Component, Input, OnChanges, SimpleChanges, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CompanyPrice, CandlestickData } from '../../../../core/models/company.model';

interface HoverData {
  date: string;
  price: number;
  change?: number;
  changePercent?: number;
}

interface ChartStats {
  high: number;
  low: number;
  avg: number;
  volume: number;
}

@Component({
  selector: 'app-stock-chart',
  standalone: false,
  templateUrl: './stock-chart.component.html',
  styleUrls: ['./stock-chart.component.css']
})
export class StockChartComponent implements OnChanges, OnDestroy, AfterViewInit {
  @Input() stockData: CompanyPrice[] = [];
  @Input() candlestickData: CandlestickData[] = [];
  @Input() chartType: 'line' | 'candlestick' = 'line';
  @Input() height = 300;

  @ViewChild('chartCanvas') canvasRef!: ElementRef<HTMLCanvasElement>;

  chart: any = null;
  hoverData: HoverData | null = null;
  stats: ChartStats = { high: 0, low: 0, avg: 0, volume: 0 };

  private canvas!: HTMLCanvasElement;
  private ctx!: CanvasRenderingContext2D;
  private animationFrameId: number = 0;
  private isHovering = false;

  ngAfterViewInit(): void {
    if (this.canvasRef) {
      this.canvas = this.canvasRef.nativeElement;
      this.ctx = this.canvas.getContext('2d')!;
      this.setupCanvas();
      this.setupEventListeners();
      if (this.stockData.length > 0) {
        this.drawChart();
        this.chart = true;
      }
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['stockData'] || changes['candlestickData']) && this.canvas) {
      this.updateChartData();
    }
  }

  ngOnDestroy(): void {
    if (this.animationFrameId) {
      cancelAnimationFrame(this.animationFrameId);
    }
  }

  private setupCanvas(): void {
    const container = this.canvas.parentElement;
    if (container) {
      this.canvas.width = container.clientWidth;
      this.canvas.height = container.clientHeight;
    }
  }

  private setupEventListeners(): void {
    this.canvas.addEventListener('mousemove', this.handleMouseMove.bind(this));
    this.canvas.addEventListener('mouseleave', this.handleMouseLeave.bind(this));
    window.addEventListener('resize', this.handleResize.bind(this));
  }

  private handleResize(): void {
    this.setupCanvas();
    this.drawChart();
  }

  private handleMouseMove(event: MouseEvent): void {
    if (!this.stockData.length) return;

    const rect = this.canvas.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;

    const padding = { top: 20, right: 60, bottom: 40, left: 60 };
    const chartWidth = this.canvas.width - padding.left - padding.right;
    const chartHeight = this.canvas.height - padding.top - padding.bottom;

    if (x >= padding.left && x <= this.canvas.width - padding.right &&
        y >= padding.top && y <= this.canvas.height - padding.bottom) {
      
      const dataIndex = Math.round(((x - padding.left) / chartWidth) * (this.stockData.length - 1));
      
      if (dataIndex >= 0 && dataIndex < this.stockData.length) {
        const dataPoint = this.stockData[dataIndex];
        const prevPoint = dataIndex > 0 ? this.stockData[dataIndex - 1] : null;
        
        const change = prevPoint ? dataPoint.value - prevPoint.value : 0;
        const changePercent = prevPoint ? (change / prevPoint.value) * 100 : 0;

        this.hoverData = {
          date: new Date(dataPoint.recordedAt).toLocaleDateString('en-IN', {
            day: 'numeric',
            month: 'short',
            hour: '2-digit',
            minute: '2-digit'
          }),
          price: dataPoint.value,
          change,
          changePercent
        };
        this.isHovering = true;
        this.drawChart(x);
      }
    } else {
      this.handleMouseLeave();
    }
  }

  private handleMouseLeave(): void {
    this.hoverData = null;
    this.isHovering = false;
    this.drawChart();
  }

  private updateChartData(): void {
    if (this.stockData.length > 0) {
      this.calculateStats();
      this.drawChart();
      this.chart = true;
    }
  }

  private calculateStats(): void {
    if (!this.stockData.length) return;

    const values = this.stockData.map(d => d.value);
    this.stats = {
      high: Math.max(...values),
      low: Math.min(...values),
      avg: values.reduce((a, b) => a + b, 0) / values.length,
      volume: Math.floor(Math.random() * 1000000) + 500000 // Mock volume
    };
  }

  private drawChart(hoverX?: number): void {
    if (!this.ctx || !this.canvas) return;

    const ctx = this.ctx;
    const width = this.canvas.width;
    const height = this.canvas.height;
    const padding = { top: 20, right: 60, bottom: 40, left: 60 };

    // Clear canvas
    ctx.clearRect(0, 0, width, height);

    if (!this.stockData.length) return;

    const chartWidth = width - padding.left - padding.right;
    const chartHeight = height - padding.top - padding.bottom;

    const values = this.stockData.map(d => d.value);
    const minValue = Math.min(...values) * 0.98;
    const maxValue = Math.max(...values) * 1.02;
    const valueRange = maxValue - minValue;

    // Helper functions
    const getX = (index: number) => padding.left + (index / (this.stockData.length - 1)) * chartWidth;
    const getY = (value: number) => padding.top + chartHeight - ((value - minValue) / valueRange) * chartHeight;

    // Draw grid lines
    ctx.strokeStyle = 'rgba(255, 255, 255, 0.1)';
    ctx.lineWidth = 1;

    // Horizontal grid lines
    for (let i = 0; i <= 5; i++) {
      const y = padding.top + (chartHeight * i / 5);
      ctx.beginPath();
      ctx.moveTo(padding.left, y);
      ctx.lineTo(width - padding.right, y);
      ctx.stroke();

      // Y-axis labels
      const value = maxValue - (valueRange * i / 5);
      ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
      ctx.font = '11px sans-serif';
      ctx.textAlign = 'right';
      ctx.fillText('₹' + value.toFixed(0), padding.left - 8, y + 4);
    }

    // Vertical grid lines (show 5-6 dates)
    const dateStep = Math.ceil(this.stockData.length / 6);
    for (let i = 0; i < this.stockData.length; i += dateStep) {
      const x = getX(i);
      ctx.beginPath();
      ctx.moveTo(x, padding.top);
      ctx.lineTo(x, height - padding.bottom);
      ctx.stroke();

      // X-axis labels
      const date = new Date(this.stockData[i].recordedAt);
      ctx.fillStyle = 'rgba(255, 255, 255, 0.6)';
      ctx.font = '10px sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(date.toLocaleDateString('en-IN', { day: 'numeric', month: 'short' }), x, height - padding.bottom + 18);
    }

    // Draw gradient area under the line
    const gradient = ctx.createLinearGradient(0, padding.top, 0, height - padding.bottom);
    gradient.addColorStop(0, 'rgba(75, 192, 192, 0.3)');
    gradient.addColorStop(1, 'rgba(75, 192, 192, 0.05)');

    ctx.beginPath();
    ctx.moveTo(getX(0), getY(this.stockData[0].value));
    
    // Smooth curve using bezier
    for (let i = 0; i < this.stockData.length - 1; i++) {
      const x1 = getX(i);
      const y1 = getY(this.stockData[i].value);
      const x2 = getX(i + 1);
      const y2 = getY(this.stockData[i + 1].value);
      const cp1x = x1 + (x2 - x1) / 2;
      const cp1y = y1;
      const cp2x = x1 + (x2 - x1) / 2;
      const cp2y = y2;
      ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x2, y2);
    }
    
    ctx.lineTo(getX(this.stockData.length - 1), height - padding.bottom);
    ctx.lineTo(getX(0), height - padding.bottom);
    ctx.closePath();
    ctx.fillStyle = gradient;
    ctx.fill();

    // Draw the line
    ctx.beginPath();
    ctx.strokeStyle = '#4BC0C0';
    ctx.lineWidth = 3;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.moveTo(getX(0), getY(this.stockData[0].value));
    
    for (let i = 0; i < this.stockData.length - 1; i++) {
      const x1 = getX(i);
      const y1 = getY(this.stockData[i].value);
      const x2 = getX(i + 1);
      const y2 = getY(this.stockData[i + 1].value);
      const cp1x = x1 + (x2 - x1) / 2;
      const cp1y = y1;
      const cp2x = x1 + (x2 - x1) / 2;
      const cp2y = y2;
      ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x2, y2);
    }
    ctx.stroke();

    // Draw hover line and point
    if (hoverX !== undefined && this.isHovering) {
      const paddingLeft = padding.left;
      const chartWidthActual = width - padding.left - padding.right;
      const dataIndex = Math.round(((hoverX - paddingLeft) / chartWidthActual) * (this.stockData.length - 1));
      
      if (dataIndex >= 0 && dataIndex < this.stockData.length) {
        const x = getX(dataIndex);
        const y = getY(this.stockData[dataIndex].value);

        // Vertical hover line
        ctx.beginPath();
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.4)';
        ctx.lineWidth = 1;
        ctx.setLineDash([5, 5]);
        ctx.moveTo(x, padding.top);
        ctx.lineTo(x, height - padding.bottom);
        ctx.stroke();
        ctx.setLineDash([]);

        // Hover point
        ctx.beginPath();
        ctx.arc(x, y, 6, 0, Math.PI * 2);
        ctx.fillStyle = '#4BC0C0';
        ctx.fill();
        ctx.strokeStyle = 'white';
        ctx.lineWidth = 2;
        ctx.stroke();

        // Outer glow
        ctx.beginPath();
        ctx.arc(x, y, 10, 0, Math.PI * 2);
        ctx.fillStyle = 'rgba(75, 192, 192, 0.3)';
        ctx.fill();
      }
    }

    // Draw data points on significant changes
    this.stockData.forEach((point, i) => {
      if (i === 0) return;
      const change = Math.abs(point.value - this.stockData[i - 1].value);
      const percentChange = change / this.stockData[i - 1].value;
      
      if (percentChange > 0.02) { // Only show points for >2% changes
        const x = getX(i);
        const y = getY(point.value);
        ctx.beginPath();
        ctx.arc(x, y, 3, 0, Math.PI * 2);
        ctx.fillStyle = point.value > this.stockData[i - 1].value ? '#4ade80' : '#f87171';
        ctx.fill();
      }
    });
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(price);
  }

  formatPercent(percent: number): string {
    const sign = percent >= 0 ? '+' : '';
    return `${sign}${percent.toFixed(2)}%`;
  }

  formatVolume(volume: number): string {
    if (volume >= 1000000) {
      return (volume / 1000000).toFixed(1) + 'M';
    } else if (volume >= 1000) {
      return (volume / 1000).toFixed(1) + 'K';
    }
    return volume.toString();
  }
}
