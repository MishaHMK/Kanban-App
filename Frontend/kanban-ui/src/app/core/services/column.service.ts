import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { Board } from '../models/board.model';
import { Column } from '../models/column.model';

@Injectable({ providedIn: 'root' })
export class ColumnService {
  private url = `${environment.apiUrl}/column`;

  constructor(private http: HttpClient) {}

  add(boardId: number, name: string, position: number) {
    return this.http.post<Board>(`${this.url}/create`, { boardId, name, position });
  }

  rename(columnId: number, newName: string) {
    return this.http.patch<Column>(`${this.url}/${columnId}`, { newName });
  }

  delete(columnId: number) {
    return this.http.delete<Board>(`${this.url}/${columnId}`);
  }
}