import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { User } from '../models/user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private http = inject(HttpClient);

  private url = `${environment.apiUrl}/users`;

  search(query: string, excludeId: number) {
    return this.http.get<User[]>(`${this.url}/search`, {
      params: { query, excludeId }
    });
  }

  getByIds(ids: number[]) {
    const params = new URLSearchParams();
    ids.forEach(id => params.append('ids', id.toString()));
    return this.http.get<User[]>(`${this.url}?${params.toString()}`);
  }
}
