import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import { AuthService } from '../../services/auth.service';

@Component({
  imports: [ReactiveFormsModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, RouterLink],
  selector: 'app-login',
  styleUrl: './login.css',
  templateUrl: './login.html',
})
export class Login {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  readonly entrando = signal(false);
  readonly mostrarSenha = signal(false);

  readonly loginForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    senha: ['', Validators.required],
  });

  entrar(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.entrando.set(true);
    this.authService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.entrando.set(false);
        this.router.navigateByUrl('/dashboard');
      },
      error: () => {
        this.entrando.set(false);
        Swal.fire({
          icon: 'error',
          title: 'Não foi possível entrar',
          text: 'Confira seu e-mail e sua senha e tente novamente.',
          confirmButtonText: 'Fechar',
          confirmButtonColor: '#17665e',
        });
      },
    });
  }
}
