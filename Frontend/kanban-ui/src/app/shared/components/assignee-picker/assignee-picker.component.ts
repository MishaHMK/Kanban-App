import { Component, input, output, inject } from '@angular/core';
import { NzDropDownModule } from 'ng-zorro-antd/dropdown';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { User } from '../../../core/models/user.model';
import { AvatarComponent } from '../../../shared/components/avatar/avatar.component';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-assignee-picker',
  standalone: true,
  imports: [NzDropDownModule, NzButtonModule, NzIconModule, AvatarComponent],
  templateUrl: './assignee-picker.component.html',
  styleUrl: './assignee-picker.component.scss'
})
export class AssigneePickerComponent {
  members = input<User[]>([]);
  selectedId = input<number | null>(null);
  assigneeChange = output<number | null>();

  private readonly auth = inject(AuthService);

  get selectedUser(): User | undefined {
    return this.members().find(m => m.id === this.selectedId());
  }

  isCurrentUser(userId: number): boolean {
    return this.auth.currentUser()?.id === userId;
  }

  select(user: User) {
    this.assigneeChange.emit(user.id);
  }

  clear() {
    this.assigneeChange.emit(null);
  }
}
