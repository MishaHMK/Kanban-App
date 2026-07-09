import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzMessageService } from 'ng-zorro-antd/message';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink,
    NzFormModule,
    NzInputModule,
    NzButtonModule,
    NzIconModule
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private auth = inject(AuthService);
  private router = inject(Router);
  private message = inject(NzMessageService);
  private cdr = inject(ChangeDetectorRef);

  form: FormGroup;
  loading = false;
  passwordVisible = false;

  constructor() {
    this.form = this.fb.group(
      {
        email: ['', [Validators.required, Validators.email]],
        nickname: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(25)]],
        password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(25)]],
        repeatPassword: ['', [Validators.required]]
      },
      { validators: this.passwordMatchValidator }
    );
  }

  submit() {
    if (this.form.invalid) {
      Object.values(this.form.controls).forEach(control => {
        control.markAsDirty();
        control.updateValueAndValidity();
      });
      return;
    }

    this.loading = true;
    this.auth.register(this.form.value).subscribe({
      next: () => {
        this.message.success('Account created! Please sign in.');
        this.router.navigate(['/login']);
      },
      error: err => {
        this.message.error(err.error?.message ?? 'Registration failed');
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const p1 = form.get('password')?.value;
    const p2 = form.get('repeatPassword')?.value;
    return p1 === p2 ? null : { passwordMismatch: true };
  }
}
