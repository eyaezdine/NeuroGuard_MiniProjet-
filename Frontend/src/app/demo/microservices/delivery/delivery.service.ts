import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Livraison } from './delivery.model';

@Injectable({
  providedIn: 'root'
})
export class DeliveryService {
  private apiUrl = 'http://localhost:8083/api/livraisons';

  constructor(private http: HttpClient) {}

  getAllDeliveries(): Observable<Livraison[]> {
    return this.http.get<Livraison[]>(this.apiUrl);
  }

  getDeliveryById(id: number): Observable<Livraison> {
    return this.http.get<Livraison>(`${this.apiUrl}/${id}`);
  }

  createDelivery(delivery: Livraison): Observable<Livraison> {
    return this.http.post<Livraison>(this.apiUrl, delivery);
  }

  updateDelivery(id: number, delivery: Livraison): Observable<Livraison> {
    return this.http.put<Livraison>(`${this.apiUrl}/${id}`, delivery);
  }

  deleteDelivery(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
