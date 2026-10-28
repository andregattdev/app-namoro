import { isPlatformBrowser } from '@angular/common';
import { Component, inject, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { RouterLink } from '@angular/router';
import Swal from 'sweetalert2';
import { Genero, GeneroService } from '../../services/genero.service';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  imports: [
    ReactiveFormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    RouterLink,
  ],
  selector: 'app-usuario',
  styleUrl: './usuario.css',
  templateUrl: './usuario.html',
})
export class Usuario implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly usuarioService = inject(UsuarioService);
  private readonly generoService = inject(GeneroService);
  private readonly platformId = inject(PLATFORM_ID);

  readonly generos = signal<Genero[]>([]);
  readonly carregandoGeneros = signal(true);
  readonly salvando = signal(false);
  readonly erroGeneros = signal(false);
  readonly dataMaxima = this.calcularDataMaxima();
  readonly mostrarSenha = signal(false);
  readonly mostrarConfirmacaoSenha = signal(false);

  readonly cadastroForm = this.formBuilder.nonNullable.group({
    nome: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    dataNascimento: ['', [Validators.required, this.maiorDeIdade]],
    generoId: [null as number | null, Validators.required],
    profissao: [''],
    interesses: [''],
    gostosMusicaisCulturais: [''],
    preferenciasPessoais: [''],
    senha: ['', [Validators.required, Validators.minLength(8), Validators.pattern(/^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?":{}|<>]).+$/)]],
    confirmarSenha: ['', Validators.required],
  }, { validators: this.senhasIguais });

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.carregarGeneros();
    } else {
      this.carregandoGeneros.set(false);
    }
  }

  cadastrar(): void {
    if (this.cadastroForm.invalid) {
      this.cadastroForm.markAllAsTouched();
      return;
    }

    const valores = this.cadastroForm.getRawValue();
    this.salvando.set(true);
    this.usuarioService.cadastrar({
      nome: valores.nome,
      email: valores.email,
      senha: valores.senha,
      dataNascimento: this.formatarData(valores.dataNascimento),
      ativo: true,
      genero: { id: valores.generoId as number },
      profissao: valores.profissao,
      interesses: valores.interesses,
      gostosMusicaisCulturais: valores.gostosMusicaisCulturais,
      preferenciasPessoais: valores.preferenciasPessoais,
    }).subscribe({
      next: () => {
        this.salvando.set(false);
        this.cadastroForm.reset();
        Swal.fire({
          icon: 'success',
          title: 'Cadastro realizado',
          text: 'Seu perfil foi criado com sucesso.',
          confirmButtonText: 'Continuar',
          confirmButtonColor: '#17665e',
        });
      },
      error: (erro: { error?: { message?: string } }) => {
        this.salvando.set(false);
        Swal.fire({
          icon: 'error',
          title: 'Não foi possível cadastrar',
          text: erro.error?.message ?? 'Confira os dados e tente novamente.',
          confirmButtonText: 'Fechar',
          confirmButtonColor: '#17665e',
        });
      },
    });
  }

  private carregarGeneros(): void {
    this.generoService.listar().subscribe({
      next: (generos) => {
        this.generos.set(generos);
        this.carregandoGeneros.set(false);
      },
      error: () => {
        this.erroGeneros.set(true);
        this.carregandoGeneros.set(false);
      },
    });
  }

  private formatarData(data: string | null): string {
    const [ano, mes, dia] = (data ?? '').split('-');
    return `${dia}/${mes}/${ano}`;
  }

  private calcularDataMaxima(): string {
    const hoje = new Date();
    hoje.setFullYear(hoje.getFullYear() - 18);
    const mes = String(hoje.getMonth() + 1).padStart(2, '0');
    const dia = String(hoje.getDate()).padStart(2, '0');
    return `${hoje.getFullYear()}-${mes}-${dia}`;
  }

  private maiorDeIdade(control: AbstractControl): ValidationErrors | null {
    const valor = control.value as string;
    if (!valor) return null;

    const nascimento = new Date(`${valor}T00:00:00`);
    if (Number.isNaN(nascimento.getTime())) return { dataInvalida: true };

    const limite = new Date();
    limite.setHours(0, 0, 0, 0);
    limite.setFullYear(limite.getFullYear() - 18);
    return nascimento <= limite ? null : { menorDeIdade: true };
  }

  private senhasIguais(control: AbstractControl): ValidationErrors | null {
    const senha = control.get('senha')?.value;
    const confirmarSenha = control.get('confirmarSenha')?.value;
    return senha && confirmarSenha && senha !== confirmarSenha ? { senhasDiferentes: true } : null;
  }
}
