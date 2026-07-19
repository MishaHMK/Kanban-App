import { Component, ChangeDetectorRef, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzMessageService } from 'ng-zorro-antd/message';
import { AuthService } from '../../core/services/auth.service';
import { resolveErrorMessage } from '../../core/constants/error-messages';

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
        password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(50)]],
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
        this.message.error(resolveErrorMessage(err.error?.exceptionMessage, 'Registration failed'));
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const repeat = form.get('repeatPassword');

    if (repeat?.value && password !== repeat.value) {
      repeat.setErrors({ ...repeat.errors, passwordMismatch: true });
    } else if (repeat?.errors?.['passwordMismatch']) {
      const errors = { ...repeat.errors };
      delete errors['passwordMismatch'];
      repeat.setErrors(Object.keys(errors).length ? errors : null);
    }

    return null;
  }
}
