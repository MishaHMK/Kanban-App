import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NzLayoutModule } from 'ng-zorro-antd/layout';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { AuthService } from '../../core/services/auth.service';
import { HeaderActionsService } from '../../core/services/header-actions.service';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [RouterOutlet, FormsModule, NzLayoutModule, NzButtonModule, NzIconModule, NzInputModule],
  templateUrl: './main-layout.component.html',
  styleUrl: './main-layout.component.scss'
})
export class MainLayoutComponent {
  auth = inject(AuthService);
  headerActions = inject(HeaderActionsService);

  currentYear = new Date().getFullYear();
}
