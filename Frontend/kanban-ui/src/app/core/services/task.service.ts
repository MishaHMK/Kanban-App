import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../environments/environment';
import { Task } from '../models/task.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private url = `${environment.apiUrl}/task`;

  constructor(private http: HttpClient) {}

  create(columnId: number, title: string, description: string, priority: string, position: number) {
    return this.http.post<Task>(`${this.url}/create`, { columnId, title, description, priority, position });
  }

  edit(taskId: number, title: string, description: string, priority: string) {
    return this.http.patch<Task>(`${this.url}/${taskId}`, { title, description, priority });
  }

  move(taskId: number, targetColumnId: number, nextTaskPosition: number) {
    return this.http.patch<void>(`${this.url}/move/${taskId}`, { targetColumnId, nextTaskPosition });
  }

  delete(taskId: number) {
    return this.http.delete<void>(`${this.url}/${taskId}`);
  }
}