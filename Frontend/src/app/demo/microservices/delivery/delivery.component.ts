import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DeliveryService } from './delivery.service';
import { Livraison } from './delivery.model';
import { FormsModule } from '@angular/forms';
import { NgApexchartsModule, ApexOptions } from 'ng-apexcharts';

@Component({
  selector: 'app-delivery',
  standalone: true,
  imports: [CommonModule, FormsModule, NgApexchartsModule],
  templateUrl: './delivery.component.html'
})
export class DeliveryComponent implements OnInit {
  deliveries: Livraison[] = [];
  loading = false;
  error = '';

  // Onglet actif : 'table' | 'stats'
  activeTab: 'table' | 'stats' = 'table';

  // Variables de gestion de formulaire
  showForm = false;
  isEditing = false;
  currentDelivery: Livraison = {};

  // Variables de recherche, tri et pagination
  searchTerm = '';
  sortColumn: keyof Livraison | '' = '';
  sortDirection: 'asc' | 'desc' = 'asc';
  currentPage = 1;
  pageSize = 5;

  // ApexCharts options
  donutChartOptions!: Partial<ApexOptions>;
  barChartOptions!: Partial<ApexOptions>;
  areaChartOptions!: Partial<ApexOptions>;

  constructor(private deliveryService: DeliveryService) {}

  ngOnInit(): void {
    this.fetchDeliveries();
  }

  fetchDeliveries(): void {
    this.loading = true;
    this.error = '';
    
    this.deliveryService.getAllDeliveries().subscribe({
      next: (data) => {
        this.deliveries = data;
        this.loading = false;
        this.buildCharts();
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des livraisons.';
        this.loading = false;
        console.error('Fetch error:', err);
      }
    });
  }

  openCreateForm(): void {
    this.isEditing = false;
    this.currentDelivery = {
      statut: 'En cours', // Default status
      dateLivraison: new Date().toISOString().split('T')[0] // Default HTML date format
    };
    this.showForm = true;
  }

  openEditForm(delivery: Livraison): void {
    this.isEditing = true;
    let formattedDate = delivery.dateLivraison;
    if (formattedDate && typeof formattedDate === 'string' && formattedDate.includes('T')) {
      formattedDate = formattedDate.split('T')[0];
    }

    this.currentDelivery = { ...delivery, dateLivraison: formattedDate };
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.currentDelivery = {};
  }

  saveDelivery(): void {
    if (this.isEditing && this.currentDelivery.id) {
      this.deliveryService.updateDelivery(this.currentDelivery.id, this.currentDelivery).subscribe({
        next: () => {
          this.fetchDeliveries();
          this.cancelForm();
        },
        error: (err) => {
          console.error('Update error:', err);
          alert('Erreur lors de la mise à jour de la livraison.');
        }
      });
    } else {
      this.deliveryService.createDelivery(this.currentDelivery).subscribe({
        next: () => {
          this.fetchDeliveries();
          this.cancelForm();
          this.currentPage = 1; // retourner à la première page
        },
        error: (err) => {
          console.error('Create error:', err);
          alert('Erreur lors de la création de la livraison.');
        }
      });
    }
  }

  deleteDelivery(id: number | undefined): void {
    if (!id) return;
    
    if (confirm('Êtes-vous sûr de vouloir supprimer cette livraison ?')) {
      this.deliveryService.deleteDelivery(id).subscribe({
        next: () => {
          this.fetchDeliveries();
        },
        error: (err) => {
          console.error('Delete error:', err);
          alert('Erreur lors de la suppression.');
        }
      });
    }
  }

  // --- LOGIQUE DE RECHERCHE, TRI ET PAGINATION ---

  get processedDeliveries(): Livraison[] {
    let result = this.deliveries;

    // 1. Filtrage (Recherche)
    if (this.searchTerm) {
      const term = this.searchTerm.toLowerCase();
      result = result.filter(d => 
        (d.adresse && d.adresse.toLowerCase().includes(term)) ||
        (d.codePostal && d.codePostal.toLowerCase().includes(term)) ||
        (d.phone && d.phone.toLowerCase().includes(term)) ||
        (d.statut && d.statut.toLowerCase().includes(term))
      );
    }

    // 2. Tri (Sorting)
    if (this.sortColumn) {
      result = [...result].sort((a, b) => {
        const valA: any = a[this.sortColumn as keyof Livraison];
        const valB: any = b[this.sortColumn as keyof Livraison];
        
        if (valA === valB) return 0;
        if (valA == null) return 1;
        if (valB == null) return -1;

        if (valA < valB) return this.sortDirection === 'asc' ? -1 : 1;
        if (valA > valB) return this.sortDirection === 'asc' ? 1 : -1;
        
        return 0;
      });
    }

    return result;
  }

  get paginatedDeliveries(): Livraison[] {
    const startIndex = (this.currentPage - 1) * this.pageSize;
    return this.processedDeliveries.slice(startIndex, startIndex + this.pageSize);
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.processedDeliveries.length / this.pageSize));
  }

  get pagesArray(): number[] {
    return Array(this.totalPages).fill(0).map((x, i) => i + 1);
  }

  sortBy(column: keyof Livraison): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
  }

  changePage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  onSearchChange(): void {
    this.currentPage = 1;
  }

  // ---- KPI Statistiques ----
  get totalLivraisons(): number {
    return this.deliveries.length;
  }

  get totalCOD(): number {
    return this.deliveries.reduce((sum, d) => sum + (d.montantCOD || 0), 0);
  }

  get tauxLivre(): number {
    if (!this.deliveries.length) return 0;
    const livrées = this.deliveries.filter(d => d.statut === 'Livré').length;
    return Math.round((livrées / this.deliveries.length) * 100);
  }

  get countEnCours(): number {
    return this.deliveries.filter(d => d.statut === 'En cours').length;
  }

  get countLivre(): number {
    return this.deliveries.filter(d => d.statut === 'Livré').length;
  }

  get countAnnule(): number {
    return this.deliveries.filter(d => d.statut === 'Annulé').length;
  }

  // ---- Construction des graphiques ----
  buildCharts(): void {
    // 1) Donut: répartition par statut
    this.donutChartOptions = {
      chart: { type: 'donut', height: 320, background: 'transparent' },
      series: [this.countEnCours, this.countLivre, this.countAnnule],
      labels: ['En cours', 'Livré', 'Annulé'],
      colors: ['#f59e0b', '#10b981', '#ef4444'],
      legend: { position: 'bottom' },
      plotOptions: {
        pie: {
          donut: {
            size: '65%',
            labels: {
              show: true,
              total: {
                show: true,
                label: 'Total',
                formatter: () => String(this.totalLivraisons)
              }
            }
          }
        }
      },
      dataLabels: { enabled: true },
      responsive: [{ breakpoint: 480, options: { chart: { height: 260 } } }]
    };

    // 2) Bar: montant COD par livraison
    const validCOD = this.deliveries.filter(d => (d.montantCOD ?? 0) > 0);
    this.barChartOptions = {
      chart: { type: 'bar', height: 320, toolbar: { show: false }, background: 'transparent' },
      series: [{ name: 'Montant COD (TND)', data: validCOD.map(d => d.montantCOD ?? 0) }],
      xaxis: {
        categories: validCOD.map(d => d.adresse?.substring(0, 12) || 'N/A'),
        labels: { style: { colors: '#8c8c8c' } }
      },
      yaxis: { labels: { style: { colors: ['#8c8c8c'] } } },
      colors: ['#6366f1'],
      plotOptions: { bar: { borderRadius: 6, columnWidth: '50%' } },
      dataLabels: { enabled: false },
      grid: { strokeDashArray: 4, borderColor: '#f0f0f0' },
      tooltip: { y: { formatter: (val: number) => val + ' TND' } }
    };

    // 3) Area: livraisons cumulées par date
    const byDate: { [key: string]: number } = {};
    this.deliveries.forEach(d => {
      const key = d.dateLivraison
        ? (typeof d.dateLivraison === 'string'
            ? d.dateLivraison.substring(0, 10)
            : (d.dateLivraison as Date).toISOString().substring(0, 10))
        : 'N/A';
      byDate[key] = (byDate[key] || 0) + 1;
    });
    const sortedDates = Object.keys(byDate).sort();
    let cumul = 0;
    const cumulData = sortedDates.map(k => { cumul += byDate[k]; return cumul; });
    this.areaChartOptions = {
      chart: { type: 'area', height: 280, toolbar: { show: false }, background: 'transparent' },
      series: [{ name: 'Livraisons cumulées', data: cumulData }],
      xaxis: {
        categories: sortedDates,
        labels: { style: { colors: '#8c8c8c' }, rotate: -30 }
      },
      yaxis: { labels: { style: { colors: ['#8c8c8c'] } }, min: 0 },
      colors: ['#3b82f6'],
      stroke: { curve: 'smooth', width: 2 },
      fill: { type: 'gradient', gradient: { shadeIntensity: 1, opacityFrom: 0.4, opacityTo: 0.05 } },
      dataLabels: { enabled: false },
      grid: { strokeDashArray: 4, borderColor: '#f0f0f0' },
      tooltip: { y: { formatter: (val: number) => String(val) + ' livraison(s)' } }
    };
  }
}
