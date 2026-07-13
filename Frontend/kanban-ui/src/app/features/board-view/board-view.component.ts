import {
  Component,
  OnInit,
  OnDestroy,
  signal,
  ViewChild,
  ElementRef,
  ChangeDetectorRef,
  inject
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule,
  FormsModule
} from '@angular/forms';
import { DatePipe } from '@angular/common';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzMessageService } from 'ng-zorro-antd/message';
import { BoardService } from '../../core/services/board.service';
import { ColumnService } from '../../core/services/column.service';
import { TaskService } from '../../core/services/task.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { Board } from '../../core/models/board.model';
import { Task } from '../../core/models/task.model';
import { Column } from '../../core/models/column.model';
import { User } from '../../core/models/user.model';
import { UserService } from '../../core/services/user.service';
import { UserSearchComponent } from '../user-search.component/user-search.component';
import { AuthService } from '../../core/services/auth.service';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { AvatarComponent } from '../../shared/components/avatar/avatar.component';
import { TaskFormComponent } from '../../shared/components/task-form/task-form.component';
import { TaskDetailsComponent } from '../task-details/task-details.component';
import { debounceTime, Subject } from 'rxjs';
import { DateTimePickerComponent } from '../../shared/components/date-time-picker/date-time-picker.component';
import { AssigneePickerComponent } from '../../shared/components/assignee-picker/assignee-picker.component';
import { NzTooltipModule } from 'ng-zorro-antd/tooltip';

@Component({
  selector: 'app-board-view',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    NzButtonModule,
    NzIconModule,
    NzModalModule,
    NzFormModule,
    NzInputModule,
    NzTooltipModule,
    NzSelectModule,
    NzTagModule,
    NzSpinModule,
    NzPopconfirmModule,
    FormsModule,
    DragDropModule,
    DatePipe,
    AvatarComponent,
    UserSearchComponent,
    TaskFormComponent,
    TaskDetailsComponent,
    DateTimePickerComponent,
    AssigneePickerComponent
  ],
  templateUrl: './board-view.component.html',
  styleUrl: './board-view.component.scss'
})
export class BoardViewComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly formBuilder = inject(FormBuilder);
  private readonly boardService = inject(BoardService);
  private readonly columnService = inject(ColumnService);
  private readonly taskService = inject(TaskService);
  private readonly authService = inject(AuthService);
  private readonly wsService = inject(WebSocketService);
  private readonly userService = inject(UserService);
  private readonly message = inject(NzMessageService);
  private readonly changeDetector = inject(ChangeDetectorRef);

  board = signal<Board | null>(null);
  loading = signal(true);

  columnModalVisible = false;
  columnForm: FormGroup;
  editingColumnId: number | null = null;
  editingColumnName = '';

  taskModalVisible = false;
  taskModalLoading = false;
  activeColumnId: number | null = null;
  taskForm: FormGroup;

  editTaskModalVisible = false;
  editingTask: Task | null = null;
  editTaskForm: FormGroup;

  taskDetailsModalVisible = false;
  selectedTask: Task | null = null;

  collaborators = signal<User[]>([]);
  collaboratorModalVisible = false;

  owner = signal<User | null>(null);

  @ViewChild('columnNameInput') columnNameInput?: ElementRef<HTMLInputElement>;

  private boardId!: number;
  private boardSnapshot: Board | null = null;

  searchQuery = '';
  filterPanelVisible = false;
  filterForm: FormGroup;
  matchingTaskIds = signal<Set<number> | null>(null);

  private searchSubject = new Subject<void>();

  constructor() {
    this.columnForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(50)]]
    });

    this.taskForm = this.formBuilder.group({
      title: ['', [Validators.required]],
      description: [''],
      priority: ['MEDIUM', [Validators.required]],
      deadlineAt: [null],
      assigneeId: [null]
    });

    this.editTaskForm = this.formBuilder.group({
      title: ['', [Validators.required]],
      description: [''],
      priority: ['MEDIUM', [Validators.required]],
      deadlineAt: [null],
      assigneeId: [null]
    });

    this.filterForm = this.formBuilder.group({
      priority: [null],
      assigneeId: [null],
      reporterId: [null],
      createdFrom: [null],
      createdTo: [null],
      deadlineFrom: [null],
      deadlineTo: [null]
    });
  }

  ngOnInit() {
    this.boardId = Number(this.route.snapshot.paramMap.get('id'));
    this.loadBoard();
    this.connectWebSocket();
    this.setupSearch();
  }

  ngOnDestroy() {
    this.wsService.disconnect();
  }

  loadBoard() {
    this.loading.set(true);
    this.boardService.getById(this.boardId).subscribe({
      next: board => {
        this.board.set(board);
        this.loading.set(false);
        this.loadCollaborators(board.collaboratorIds);
        this.loadOwner(board.ownerId);
      },
      error: () => {
        this.message.error('Failed to load board');
        this.loading.set(false);
      }
    });
  }

  connectWebSocket() {
    this.wsService.connect(this.boardId);
    this.wsService.boardUpdates$.subscribe(board => {
      this.board.set(board);
    });
  }

  back() {
    this.router.navigate(['/boards']);
  }

  isOwner(): boolean {
    return this.board()?.ownerId === this.authService.currentUser()?.id;
  }

  openColumnModal() {
    this.columnForm.reset({ name: '' });
    this.columnModalVisible = true;
  }

  closeColumnModal() {
    this.columnModalVisible = false;
  }

  loadOwner(ownerId: number) {
    this.userService.getByIds([ownerId]).subscribe({
      next: users => this.owner.set(users[0] ?? null),
      error: () => this.owner.set(null)
    });
  }

  addColumn() {
    if (this.columnForm.invalid) return;
    const columns = this.board()?.columns ?? [];
    const position = columns.length + 1;

    this.columnService.add(this.boardId, this.columnForm.value.name, position).subscribe({
      next: board => {
        this.board.set(board);
        this.message.success('Column added');
        this.closeColumnModal();
      },
      error: err => this.message.error(err.error?.message ?? 'Failed to add column')
    });
  }

  deleteColumn(columnId: number) {
    this.columnService.delete(columnId).subscribe({
      next: board => {
        this.board.set(board);
        this.message.success('Column deleted');
      },
      error: () => this.message.error('Failed to delete column')
    });
  }

  cancelColumnRename() {
    this.editingColumnId = null;
  }

  startColumnRename(column: Column) {
    this.editingColumnId = column.id;
    this.editingColumnName = column.name;

    setTimeout(() => {
      this.columnNameInput?.nativeElement.focus();
      this.columnNameInput?.nativeElement.select();
    });
  }

  finishColumnRename(column: Column) {
    if (!this.editingColumnName.trim() || this.editingColumnName === column.name) {
      this.editingColumnId = null;
      return;
    }

    this.columnService.rename(column.id, this.editingColumnName.trim()).subscribe({
      next: () => {
        this.board.update(board => {
          if (!board) return board;
          return {
            ...board,
            columns: board.columns.map(col =>
              col.id === column.id ? { ...col, name: this.editingColumnName.trim() } : col
            )
          };
        });
        this.message.success('Column renamed');
      },
      error: () => this.message.error('Failed to rename column')
    });
    this.editingColumnId = null;
  }

  openTaskModal(columnId: number) {
    this.activeColumnId = columnId;
    this.taskForm.reset({ priority: 'MEDIUM', deadlineAt: null, assigneeId: null });
    this.taskModalVisible = true;
  }

  closeTaskModal() {
    this.taskModalVisible = false;
    this.activeColumnId = null;
  }

  addTask() {
    if (this.taskForm.invalid || !this.activeColumnId || this.taskModalLoading) return;
    this.taskModalLoading = true;

    const column = this.board()?.columns.find(column => column.id === this.activeColumnId);
    const position = (column?.tasks.length ?? 0) + 1;

    const { title, description, priority, deadlineAt, assigneeId } = this.taskForm.value;

    this.taskService
      .create(this.activeColumnId, title, description, priority, position, deadlineAt, assigneeId)
      .subscribe({
        next: () => {
          this.message.success('Task added');
          this.taskModalLoading = false;
          this.closeTaskModal();
          this.changeDetector.detectChanges();
        },
        error: err => {
          this.message.error(err.error?.message ?? 'Failed to add task');
          this.taskModalLoading = false;
          this.changeDetector.detectChanges();
        }
      });
  }

  openEditTask(task: Task) {
    this.editingTask = task;
    this.editTaskForm.patchValue({
      title: task.title,
      description: task.description,
      priority: task.priority,
      deadlineAt: task.deadlineAt ? new Date(task.deadlineAt) : null,
      assigneeId: task.assigneeId ?? null
    });
    this.editTaskModalVisible = true;
  }

  saveEditTask() {
    if (this.editTaskForm.invalid || !this.editingTask) return;
    const { title, description, priority, deadlineAt, assigneeId } = this.editTaskForm.value;

    this.taskService
      .edit(this.editingTask.id, title, description, priority, deadlineAt, assigneeId)
      .subscribe({
        next: updated => {
          this.editingTask = updated;
          this.updateTaskInBoard(updated);
          this.message.success('Task updated');
          this.editTaskModalVisible = false;
        },
        error: () => this.message.error('Failed to update task')
      });
  }

  deleteTask(taskId: number) {
    this.taskService.delete(taskId).subscribe({
      next: () => {
        this.board.update(board => {
          if (!board) return board;
          return {
            ...board,
            columns: board.columns.map(column => ({
              ...column,
              tasks: column.tasks.filter(task => task.id !== taskId)
            }))
          };
        });
        this.message.success('Task deleted');
      },
      error: () => this.message.error('Failed to delete task')
    });
  }

  openTaskDetails(task: Task) {
    this.selectedTask = task;
    this.taskDetailsModalVisible = true;
  }

  closeTaskDetails() {
    this.taskDetailsModalVisible = false;
    this.selectedTask = null;
  }

  allBoardUsers(): User[] {
    const owner = this.owner();
    return owner ? [owner, ...this.collaborators()] : this.collaborators();
  }

  assigneeNickname(userId: number): string {
    return this.allBoardUsers().find(u => u.id === userId)?.nickname ?? '?';
  }

  isOverdue(deadline: string | null): boolean {
    if (!deadline) return false;
    return new Date(deadline) < new Date();
  }

  private updateTaskInBoard(updated: Task) {
    this.board.update(board => {
      if (!board) return board;
      return {
        ...board,
        columns: board.columns.map(column => ({
          ...column,
          tasks: column.tasks.map(task => (task.id === updated.id ? updated : task))
        }))
      };
    });
  }

  priorityColor(priority: string): string {
    switch (priority) {
      case 'HIGH':
        return 'red';
      case 'MEDIUM':
        return 'orange';
      case 'LOW':
        return 'green';
      default:
        return 'default';
    }
  }

  loadCollaborators(ids: number[]) {
    if (!ids || ids.length === 0) {
      this.collaborators.set([]);
      return;
    }
    this.userService.getByIds(ids).subscribe({
      next: users => this.collaborators.set(users),
      error: () => this.collaborators.set([])
    });
  }

  isCollaborator(userId: number): boolean {
    return this.board()?.collaboratorIds.includes(userId) ?? false;
  }

  openCollaboratorModal() {
    this.collaboratorModalVisible = true;
  }

  addCollaborator(user: User) {
    this.boardService.addCollaborator(this.boardId, user.id).subscribe({
      next: board => {
        this.board.set(board);
        this.loadCollaborators(board.collaboratorIds);
        this.message.success(`${user.email} added`);
      },
      error: () => this.message.error('Failed to add collaborator')
    });
  }

  removeCollaborator(userId: number) {
    this.boardService.removeCollaborator(this.boardId, userId).subscribe({
      next: board => {
        this.board.set(board);
        this.loadCollaborators(board.collaboratorIds);
        this.message.success('Collaborator removed');
      },
      error: () => this.message.error('Failed to remove collaborator')
    });
  }

  dropColumn(event: CdkDragDrop<Column[]>) {
    if (event.previousIndex === event.currentIndex) return;

    const columns = this.board()?.columns ?? [];
    const movedColumn = columns[event.previousIndex];
    const newPosition = event.currentIndex + 1; // backend positions are 1-indexed

    const snapshot = structuredClone(this.board());

    this.board.update(board => {
      if (!board) return board;
      const reordered = [...board.columns];
      moveItemInArray(reordered, event.previousIndex, event.currentIndex);
      return { ...board, columns: reordered };
    });

    this.columnService.move(movedColumn.id, newPosition).subscribe({
      next: () => {},
      error: () => {
        if (snapshot) this.board.set(snapshot);
        this.message.error('Failed to move column');
      }
    });
  }

  dropTask(event: CdkDragDrop<Task[]>, targetColumnId: number) {
    if (event.previousIndex === event.currentIndex && event.previousContainer === event.container)
      return;

    this.boardSnapshot = structuredClone(this.board());

    const task = event.previousContainer.data[event.previousIndex];
    const sourceColumnId = Number(event.previousContainer.id);
    const newPosition = event.currentIndex + 1;

    if (event.previousContainer === event.container) {
      this.board.update(board => {
        if (!board) return board;
        return {
          ...board,
          columns: board.columns.map(column => {
            if (column.id !== targetColumnId) return column;
            const tasks = [...column.tasks];
            moveItemInArray(tasks, event.previousIndex, event.currentIndex);
            return { ...column, tasks };
          })
        };
      });
    } else {
      this.board.update(board => {
        if (!board) return board;
        return {
          ...board,
          columns: board.columns.map(column => {
            if (column.id === sourceColumnId) {
              const tasks = [...column.tasks];
              tasks.splice(event.previousIndex, 1);
              return { ...column, tasks };
            }
            if (column.id === targetColumnId) {
              const tasks = [...column.tasks];
              tasks.splice(event.currentIndex, 0, task);
              return { ...column, tasks };
            }
            return column;
          })
        };
      });
    }

    this.taskService.move(task.id, targetColumnId, newPosition).subscribe({
      next: () => {},
      error: err => {
        if (this.boardSnapshot) {
          this.board.set(this.boardSnapshot);
          this.boardSnapshot = null;
        }
        this.message.error(
          err.error?.message === 'COLUMN_TASK_LIMIT_REACHED'
            ? 'Column is full (max 10 tasks)'
            : 'Failed to move task'
        );
      }
    });
  }

  getColumnIds(): string[] {
    return this.board()?.columns.map(column => column.id.toString()) ?? [];
  }

  setupSearch() {
    this.searchSubject.pipe(debounceTime(300)).subscribe(() => this.runSearch());
  }

  onSearchInput(query: string) {
    this.searchQuery = query;
    this.searchSubject.next();
  }

  toggleFilterPanel() {
    this.filterPanelVisible = !this.filterPanelVisible;

    if (this.filterPanelVisible) {
      setTimeout(() => {
        this.changeDetector.detectChanges();
      }, 0);
    }
  }

  applyFilters() {
    this.filterPanelVisible = false;
    this.runSearch();
  }

  clearFilters() {
    this.filterForm.reset();
    this.searchQuery = '';
    this.matchingTaskIds.set(null);
    this.filterPanelVisible = false;
  }

  private runSearch() {
    const { priority, assigneeId, reporterId, createdFrom, createdTo, deadlineFrom, deadlineTo } =
      this.filterForm.value;

    const hasAnyFilter =
      this.searchQuery.trim() ||
      priority ||
      assigneeId ||
      reporterId ||
      createdFrom ||
      createdTo ||
      deadlineFrom ||
      deadlineTo;

    if (!hasAnyFilter) {
      this.matchingTaskIds.set(null);
      return;
    }

    this.taskService
      .search({
        boardId: this.boardId,
        title: this.searchQuery.trim() || undefined,
        priority: priority ?? undefined,
        assigneeId: assigneeId ?? undefined,
        reporterId: reporterId ?? undefined,
        createdFrom: createdFrom ?? undefined,
        createdTo: createdTo ?? undefined,
        deadlineFrom: deadlineFrom ?? undefined,
        deadlineTo: deadlineTo ?? undefined
      })
      .subscribe({
        next: results => this.matchingTaskIds.set(new Set(results.map(t => t.id))),
        error: () => this.message.error('Search failed')
      });
  }

  taskMatchesFilter(task: Task): boolean {
    const ids = this.matchingTaskIds();
    return ids === null || ids.has(task.id);
  }
}
