import { Component, OnInit, OnDestroy, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
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
import { FormsModule } from '@angular/forms';
import { BoardService } from '../../core/services/board.service';
import { ColumnService } from '../../core/services/column.service';
import { TaskService } from '../../core/services/task.service';
import { WebSocketService } from '../../core/services/websocket.service';
import { Board } from '../../core/models/board.model';
import { Task } from '../../core/models/task.model';
import { Column } from '../../core/models/column.model';
import { User } from '../../core/models/user.model';
import { ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';
import { UserService } from '../../core/services/user.service';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';

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
    NzSelectModule,
    NzTagModule,
    NzSpinModule,
    NzPopconfirmModule,
    FormsModule,
    DragDropModule
  ],
  templateUrl: './board-view.component.html',
  styleUrl: './board-view.component.scss'
})
export class BoardViewComponent implements OnInit, OnDestroy {
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

  collaborators = signal<User[]>([]);
  collaboratorModalVisible = false;
  searchResults = signal<User[]>([]);
  searchLoading = false;

  private searchSubject = new Subject<string>();

  priorities = [
    { label: 'Low', value: 'LOW' },
    { label: 'Medium', value: 'MEDIUM' },
    { label: 'High', value: 'HIGH' }
  ];

  private boardId!: number;

  private boardSnapshot: Board | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private formBuilder: FormBuilder,
    private boardService: BoardService,
    private columnService: ColumnService,
    private taskService: TaskService,
    private authService: AuthService,
    private wsService: WebSocketService,
    private userService: UserService,
    private message: NzMessageService,
    private changeDetector: ChangeDetectorRef 
  ) {
    this.columnForm = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(50)]]
    });

    this.taskForm = this.formBuilder.group({
      title: ['', [Validators.required]],
      description: [''],
      priority: ['MEDIUM', [Validators.required]]
    });

    this.editTaskForm = this.formBuilder.group({
      title: ['', [Validators.required]],
      description: [''],
      priority: ['MEDIUM', [Validators.required]]
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
      next: (board) => {
        this.board.set(board);
        this.loading.set(false);
        this.loadCollaborators(board.collaboratorIds); 
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
      console.log('WS update received:', board); 
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

  addColumn() {
    if (this.columnForm.invalid) return;
    const columns = this.board()?.columns ?? [];
    const position = columns.length;

    this.columnService.add(this.boardId, this.columnForm.value.name, position).subscribe({
      next: (board) => {
        this.board.set(board);
        this.message.success('Column added');
        this.closeColumnModal();
      },
      error: (err) => this.message.error(err.error?.message ?? 'Failed to add column')
    });
  }

  deleteColumn(columnId: number) {
    this.columnService.delete(columnId).subscribe({
      next: (board) => {
        this.board.set(board);
        this.message.success('Column deleted');
      },
      error: () => this.message.error('Failed to delete column')
    });
  }

    cancelColumnRename() {
    this.editingColumnId = null;
  }

  openTaskModal(columnId: number) {
    this.activeColumnId = columnId;
    this.taskForm.reset({ priority: 'MEDIUM' });
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

    const { title, description, priority } = this.taskForm.value;

    this.taskService.create(this.activeColumnId, title, description, priority, position).subscribe({
      next: (result) => {
        this.message.success('Task added');
        this.taskModalLoading = false;
        this.closeTaskModal();
        this.changeDetector.detectChanges();
      },
      error: (err) => {
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
      priority: task.priority
    });
    this.editTaskModalVisible = true;
  }

  saveEditTask() {
    if (this.editTaskForm.invalid || !this.editingTask) return;
    const { title, description, priority } = this.editTaskForm.value;

    this.taskService.edit(this.editingTask.id, title, description, priority).subscribe({
      next: (updated) => {
        this.board.update(board => {
          if (!board) return board;
          return {
            ...board,
            columns: board.columns.map(column => ({
              ...column,
              tasks: column.tasks.map(task => task.id === updated.id ? updated : task)
            }))
          };
        });
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

  startColumnRename(column: Column) {
    this.editingColumnId = column.id;
    this.editingColumnName = column.name;
    setTimeout(() => {
      const el = document.querySelector('.column-name-input') as HTMLInputElement;
      el?.focus();
      el?.select();
    }, 50);
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
            columns: board.columns.map(column =>
              column.id === column.id ? { ...column, name: this.editingColumnName.trim() } : column
            )
          };
        });
        this.message.success('Column renamed');
      },
      error: () => this.message.error('Failed to rename column')
    });
    this.editingColumnId = null;
  }

  priorityColor(priority: string): string {
    switch (priority) {
      case 'HIGH':   return 'red';
      case 'MEDIUM': return 'orange';
      case 'LOW':    return 'green';
      default:       return 'default';
    }
  }

  loadCollaborators(ids: number[]) {
    if (!ids || ids.length === 0) {
      this.collaborators.set([]);
      return;
    }
    console.log('Loading collaborators for ids:', ids);
    this.userService.getByIds(ids).subscribe({
    next: (users) => {
      console.log('Collaborators loaded:', users);
      this.collaborators.set(users);
    },
    error: (err) => {
      console.error('Failed to load collaborators:', err); 
    }
  });
  }

  setupSearch() {
    this.searchSubject.pipe(
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(query => {
        if (!query.trim()) {
          this.searchResults.set([]);
          this.searchLoading = false;
          return [];
        }
        this.searchLoading = true;
        return this.userService.search(query, this.authService.currentUser()!.id);
      })
    ).subscribe({
      next: (users) => {
        this.searchResults.set(users as User[]);
        this.searchLoading = false;
      },
      error: () => { this.searchLoading = false; }
    });
  }

  onSearchInput(query: string) {
    this.searchSubject.next(query);
  }

  isCollaborator(userId: number): boolean {
    return this.board()?.collaboratorIds.includes(userId) ?? false;
  }

  openCollaboratorModal() {
    this.searchResults.set([]);
    this.collaboratorModalVisible = true;
  }

  addCollaborator(user: User) {
    this.boardService.addCollaborator(this.boardId, user.id).subscribe({
      next: (board) => {
        this.board.set(board);
        this.loadCollaborators(board.collaboratorIds);
        this.message.success(`${user.email} added`);
      },
      error: () => this.message.error('Failed to add collaborator')
    });
  }

  removeCollaborator(userId: number) {
    this.boardService.removeCollaborator(this.boardId, userId).subscribe({
      next: (board) => {
        this.board.set(board);
        this.loadCollaborators(board.collaboratorIds);
        this.message.success('Collaborator removed');
      },
      error: () => this.message.error('Failed to remove collaborator')
    });
  }

  dropTask(event: CdkDragDrop<Task[]>, targetColumnId: number) {
    if (event.previousIndex === event.currentIndex &&
        event.previousContainer === event.container) return;

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
        next: () => {
        },
        error: (err) => {
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
}