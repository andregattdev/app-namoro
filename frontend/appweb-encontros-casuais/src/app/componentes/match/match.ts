import { DatePipe, isPlatformBrowser } from '@angular/common';
import { Component, inject, OnDestroy, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { interval, Subscription, switchMap } from 'rxjs';
import Swal from 'sweetalert2';

import { AuthService } from '../../services/auth.service';
import { Mensagem, MensagemService } from '../../services/mensagem.service';
import { UsuarioPerfil, UsuarioService } from '../../services/usuario.service';

@Component({
  imports: [DatePipe, FormsModule, MatButtonModule, MatIconModule, RouterLink],
  selector: 'app-match',
  styleUrl: './match.css',
  templateUrl: './match.html',
})
export class Match implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly mensagemService = inject(MensagemService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly route = inject(ActivatedRoute);
  private readonly platformId = inject(PLATFORM_ID);

  readonly matches = signal<UsuarioPerfil[]>([]);
  readonly matchSelecionado = signal<UsuarioPerfil | null>(null);
  readonly mensagens = signal<Mensagem[]>([]);
  readonly carregandoMatches = signal(true);
  readonly carregandoMensagens = signal(false);
  readonly enviando = signal(false);
  readonly apagandoConversa = signal(false);
  readonly erro = signal('');
  texto = '';

  private readonly usuarioId = this.authService.userId();
  private atualizacao?: Subscription;

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId) || !this.usuarioId) {
      this.carregandoMatches.set(false);
      return;
    }

    this.usuarioService.buscarMatches(this.usuarioId).subscribe({
      next: (matches) => {
        this.matches.set(matches);
        this.carregandoMatches.set(false);
        if (matches.length > 0) {
          const matchSolicitado = Number(this.route.snapshot.queryParamMap.get('usuarioId'));
          const matchInicial = matches.find((match) => match.id === matchSolicitado) ?? matches[0];
          this.selecionarMatch(matchInicial);
        }
      },
      error: () => {
        this.erro.set('Não foi possível carregar seus matches.');
        this.carregandoMatches.set(false);
      },
    });
  }

  selecionarMatch(match: UsuarioPerfil): void {
    if (!this.usuarioId) return;

    this.matchSelecionado.set(match);
    this.carregarMensagens();
    this.atualizacao?.unsubscribe();
    this.atualizacao = interval(5000).pipe(
      switchMap(() => this.mensagemService.listarConversa(this.usuarioId!, match.id)),
    ).subscribe({
      next: (mensagens) => this.mensagens.set(mensagens),
    });
  }

  private carregarMensagens(): void {
    const match = this.matchSelecionado();
    if (!this.usuarioId || !match) return;

    this.carregandoMensagens.set(true);
    this.mensagemService.listarConversa(this.usuarioId, match.id).subscribe({
      next: (mensagens) => {
        this.mensagens.set(mensagens);
        this.carregandoMensagens.set(false);
        this.marcarRecebidasComoLidas(mensagens);
      },
      error: () => {
        this.erro.set('Não foi possível carregar a conversa.');
        this.carregandoMensagens.set(false);
      },
    });
  }

  private marcarRecebidasComoLidas(mensagens: Mensagem[]): void {
    if (!this.usuarioId) return;

    mensagens
      .filter((mensagem) => mensagem.tipoRemetente === 'USUARIO'
        && !mensagem.lida
        && mensagem.remetenteId !== this.usuarioId)
      .forEach((mensagem) => {
        this.mensagemService.marcarComoLida(mensagem.id, this.usuarioId!).subscribe({
          next: () => this.mensagens.update((itens) => itens.map((item) =>
            item.id === mensagem.id ? { ...item, lida: true } : item)),
        });
      });
  }

  enviarMensagem(): void {
    const match = this.matchSelecionado();
    const conteudo = this.texto.trim();
    if (!match || !conteudo || this.enviando()) return;

    this.enviando.set(true);
    this.mensagemService.enviar({ destinatarioId: match.id, conteudo }).subscribe({
      next: (mensagem) => {
        this.mensagens.update((mensagens) => [...mensagens, mensagem]);
        this.texto = '';
        this.enviando.set(false);
      },
      error: () => {
        this.erro.set('Não foi possível enviar a mensagem.');
        this.enviando.set(false);
      },
    });
  }

  async apagarConversa(): Promise<void> {
    const match = this.matchSelecionado();
    if (!this.usuarioId || !match || this.apagandoConversa()) return;

    const confirmacao = await Swal.fire({
      icon: 'warning',
      title: 'Apagar conversa?',
      text: `Todas as mensagens com ${match.nome} serão apagadas permanentemente.`,
      showCancelButton: true,
      confirmButtonText: 'Apagar conversa',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#b34a3c',
      cancelButtonColor: '#71827e',
      reverseButtons: true,
    });
    if (!confirmacao.isConfirmed) return;

    this.apagandoConversa.set(true);
    this.mensagemService.apagarConversa(this.usuarioId, match.id).subscribe({
      next: () => {
        this.mensagens.set([]);
        this.apagandoConversa.set(false);
        void Swal.fire({
          icon: 'success',
          title: 'Conversa apagada',
          text: 'As mensagens foram removidas.',
          confirmButtonText: 'Fechar',
          confirmButtonColor: '#17665e',
        });
      },
      error: () => {
        this.erro.set('Não foi possível apagar a conversa.');
        this.apagandoConversa.set(false);
        void Swal.fire({
          icon: 'error',
          title: 'Não foi possível apagar',
          text: 'Tente novamente em alguns instantes.',
          confirmButtonText: 'Fechar',
          confirmButtonColor: '#17665e',
        });
      },
    });
  }

  souEu(mensagem: Mensagem): boolean {
    return mensagem.remetenteId === this.usuarioId;
  }

  ngOnDestroy(): void {
    this.atualizacao?.unsubscribe();
  }
}
