import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/login/login.component').then(module => module.LoginComponent)
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/register/register.component').then(module => module.RegisterComponent)
  },
  {
    path: '',
    loadComponent: () =>
      import('./layout/main-layout/main-layout.component').then(
        module => module.MainLayoutComponent
      ),
    canActivate: [authGuard],
    children: [
      {
        path: 'boards',
        loadComponent: () =>
          import('./features/boards/boards.component').then(module => module.BoardsComponent)
      },
      {
        path: 'board/:id',
        loadComponent: () =>
          import('./features/board-view/board-view.component').then(
            module => module.BoardViewComponent
          )
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
