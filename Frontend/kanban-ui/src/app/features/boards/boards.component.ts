import { Component, OnInit, signal, ViewChild, ElementRef, inject } from '@angular/core';
import { Router } from '@angular/router';
import {
  FormsModule,
  FormBuilder,
  FormGroup,
  Validators,
  ReactiveFormsModule
} from '@angular/forms';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzMessageService } from 'ng-zorro-antd/message';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { BoardService } from '../../core/services/board.service';
import { AuthService } from '../../core/services/auth.service';
import { Board } from '../../core/models/board.model';

@Component({
  selector: 'app-boards',
  standalone: true,
  imports: [
    FormsModule,
    ReactiveFormsModule,
    NzCardModule,
    NzButtonModule,
    NzModalModule,
    NzFormModule,
    NzInputModule,
    NzIconModule,
    NzPopconfirmModule,
    NzEmptyModule,
    NzTagModule,
    NzSpinModule
  ],
  templateUrl: './boards.component.html',
  styleUrl: './boards.component.scss'
})
export class BoardsComponent implements OnInit {
  private boardService = inject(BoardService);
  private auth = inject(AuthService);
  private router = inject(Router);
  private formBuilder = inject(FormBuilder);
  private message = inject(NzMessageService);

  boards = signal<Board[]>([]);
  loading = signal(true);
  modalVisible = false;
  modalLoading = false;
  form: FormGroup;
  editingBoard: Board | null = null;
  editBoardForm: FormGroup;
  editingBoardName = '';
  currentUserId: number;

  @ViewChild('renameInput') renameInput?: ElementRef;

  constructor() {
    this.currentUserId = this.auth.currentUser()!.id;
    this.form = this.formBuilder.group({
      name: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]]
    });
    this.editBoardForm = this.formBuilder.group({
      name: ['', [Validators.required]]
    });
  }

  ngOnInit() {
    this.loadBoards();
  }

  loadBoards() {
    this.loading.set(true);
    this.boardService.getAll().subscribe({
      next: boards => {
        this.boards.set(boards);
        this.loading.set(false);
      },
      error: () => {
        this.message.error('Failed to load boards');
        this.loading.set(false);
      }
    });
  }

  openBoard(id: number) {
    this.router.navigate(['/board', id]);
  }

  openModal() {
    this.modalVisible = true;
  }

  closeModal() {
    this.modalVisible = false;
    this.form.reset();
  }

  createBoard() {
    if (this.form.invalid) return;
    this.modalLoading = true;
    this.boardService.create(this.form.value.name).subscribe({
      next: board => {
        this.boards.update(newBoard => [...newBoard, board]);
        this.message.success('Board created!');
        this.modalLoading = false;
        this.closeModal();
      },
      error: () => {
        this.message.error('Failed to create board');
        this.modalLoading = false;
      }
    });
  }

  deleteBoard(id: number) {
    this.boardService.delete(id).subscribe({
      next: () => {
        this.boards.update(b => b.filter(board => board.id !== id));
        this.message.success('Board deleted');
      },
      error: () => this.message.error('Failed to delete board')
    });
  }

  isOwner(board: Board): boolean {
    return board.ownerId === this.currentUserId;
  }

  openEditBoard(event: MouseEvent, board: Board) {
    event.stopPropagation();
    this.editingBoard = board;
    this.editBoardForm.patchValue({ name: board.name });
  }

  closeEditBoard() {
    this.editingBoard = null;
  }

  saveEditBoard() {
    if (this.editBoardForm.invalid || !this.editingBoard) return;
    this.boardService.rename(this.editingBoard.id, this.editBoardForm.value.name).subscribe({
      next: updated => {
        this.boards.update(currentBoards =>
          currentBoards.map(original => (original.id === updated.id ? updated : original))
        );
        this.message.success('Board renamed');
        this.closeEditBoard();
      },
      error: () => this.message.error('Failed to rename board')
    });
  }
}
