import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { environment } from '../environments/environment';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private url = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) {}

  search(query: string, excludeId: number) {
    return this.http.get<User[]>(`${this.url}/search`, {
      params: { query, excludeId }
    });
  }

  getByIds(ids: number[]) {
    let params = new HttpParams();
    ids.forEach(id => params = params.append('ids', id.toString()));
    return this.http.get<User[]>(`${this.url}/list`, { params });
  }
}