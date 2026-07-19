import { Component, input, output, signal, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';
import { User } from '../../core/models/user.model';
import { AvatarComponent } from '../../shared/components/avatar/avatar.component';

@Component({
  selector: 'app-user-search',
  standalone: true,
  imports: [FormsModule, NzInputModule, NzIconModule, NzButtonModule, NzTagModule, AvatarComponent],
  templateUrl: './user-search.component.html',
  styleUrl: './user-search.component.scss'
})
export class UserSearchComponent implements OnInit {
  private userService = inject(UserService);
  private authService = inject(AuthService);

  selectedIds = input<number[]>([]);

  userSelected = output<User>();

  private readonly searchSubject = new Subject<string>();

  protected searchResults = signal<User[]>([]);
  protected searchLoading = signal(false);

  ngOnInit(): void {
    this.searchSubject
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap(query => {
          if (!query.trim()) {
            this.searchResults.set([]);
            this.searchLoading.set(false);
            return [];
          }
          this.searchLoading.set(true);
          return this.userService.search(query, this.authService.currentUser()!.id);
        })
      )
      .subscribe({
        next: users => {
          this.searchResults.set(users as User[]);
          this.searchLoading.set(false);
        },
        error: () => {
          this.searchLoading.set(false);
        }
      });
  }

  onSearchInput(query: string): void {
    this.searchSubject.next(query);
  }

  isSelected(userId: number): boolean {
    return this.selectedIds().includes(userId);
  }

  select(user: User): void {
    this.userSelected.emit(user);
  }
}
