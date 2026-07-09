export interface Task {
  id: number;
  title: string;
  description: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  position: number;
  columnId: number;
  reporterId: number;
  assigneeId: number | null;
  createdAt: string;
  deadlineAt: string | null;
}
