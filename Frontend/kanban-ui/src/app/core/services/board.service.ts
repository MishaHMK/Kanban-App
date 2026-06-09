import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { Board } from '../models/board.model';

@Injectable({ providedIn: 'root' })
export class BoardService {
  private url = `${environment.apiUrl}/board`;

  constructor(private http: HttpClient) {}

  getAll(){ 
    return this.http.get<Board[]>(`${this.url}/all`); 
   }

  getById(id: number){ 
    return this.http.get<Board>(`${this.url}/${id}`); 
   }

  create(name: string){ 
    return this.http.post<Board>(`${this.url}/create`, { name }); 
   }

  delete(id: number){ 
    return this.http.delete<void>(`${this.url}/${id}`); 
   }

  rename(id: number, name: string) {
    return this.http.patch<Board>(`${this.url}/${id}`, { name });
  }

  addCollaborator(boardId: number, collaboratorId: number) {
    return this.http.patch<Board>(`${this.url}/${boardId}/collaborator`, {
    collaborator_id: collaboratorId,
    action: 'ADD'});
  }

  removeCollaborator(boardId: number, collaboratorId: number) {
    return this.http.patch<Board>(`${this.url}/${boardId}/collaborator`, {
      collaborator_id: collaboratorId,
      action: 'REMOVE'
    });
  } 
}