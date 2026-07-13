import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Task } from '../models/task.model';
import { environment } from '../environments/environment';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private http = inject(HttpClient);

  private url = `${environment.apiUrl}/task`;

  create(
    columnId: number,
    title: string,
    description: string,
    priority: string,
    position: number,
    deadlineAt?: Date | null,
    assigneeId?: number | null
  ) {
    return this.http.post<Task>(`${this.url}/create`, {
      columnId,
      title,
      description,
      priority,
      position,
      deadlineAt: deadlineAt ? deadlineAt.toISOString() : null,
      assigneeId: assigneeId ?? null
    });
  }

  edit(
    taskId: number,
    title: string,
    description: string,
    priority: string,
    deadlineAt?: Date | null,
    assigneeId?: number | null
  ) {
    return this.http.patch<Task>(`${this.url}/${taskId}`, {
      title,
      description,
      priority,
      deadlineAt: deadlineAt ? deadlineAt.toISOString() : null,
      assigneeId: assigneeId ?? null
    });
  }

  move(taskId: number, targetColumnId: number, nextTaskPosition: number) {
    return this.http.patch<void>(`${this.url}/move/${taskId}`, {
      targetColumnId,
      nextTaskPosition
    });
  }

  delete(taskId: number) {
    return this.http.delete<void>(`${this.url}/${taskId}`);
  }

  assign(taskId: number, assigneeId: number) {
    return this.http.patch<Task>(`${this.url}/${taskId}/assign`, {
      assigneeId
    });
  }

  unassign(taskId: number) {
    return this.http.patch<Task>(`${this.url}/${taskId}/unassign`, {});
  }

  toLocalDateTimeString(date: Date): string {
    const isoString = date.toISOString();
    const withoutMillisAndZone = isoString.split('.')[0];
    return withoutMillisAndZone;
  }

  search(params: {
    boardId: number;
    title?: string;
    priority?: string;
    assigneeId?: number;
    reporterId?: number;
    createdFrom?: Date;
    createdTo?: Date;
    deadlineFrom?: Date;
    deadlineTo?: Date;
  }) {
    let httpParams = new HttpParams().set('boardId', params.boardId.toString());

    if (params.title) httpParams = httpParams.set('title', params.title);
    if (params.priority) httpParams = httpParams.set('priority', params.priority);
    if (params.assigneeId) httpParams = httpParams.set('assigneeId', params.assigneeId.toString());
    if (params.reporterId) httpParams = httpParams.set('reporterId', params.reporterId.toString());
    if (params.createdFrom) httpParams = httpParams.set('createdFrom', this.toLocalDateTimeString(params.createdFrom));
    if (params.createdTo) httpParams = httpParams.set('createdTo', this.toLocalDateTimeString(params.createdTo));
    if (params.deadlineFrom) httpParams = httpParams.set('deadlineFrom', this.toLocalDateTimeString(params.deadlineFrom));
    if (params.deadlineTo) httpParams = httpParams.set('deadlineTo', this.toLocalDateTimeString(params.deadlineTo));

    return this.http.get<Task[]>(`${this.url}/search`, { params: httpParams });
  }
}
