import { Task } from './task.model';

export interface Column {
  id: number;
  name: string;
  position: number;
  boardId: number;
  tasks: Task[];
}
