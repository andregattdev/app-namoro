import { isPlatformBrowser } from '@angular/common';
import { Component, computed, inject, OnDestroy, OnInit, PLATFORM_ID, signal } from '@angular/core';
import { FormControl, FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatBadgeModule } from '@angular/material/badge';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import Swal from 'sweetalert2';

import { AuthService } from '../../services/auth.service';
import { CurtidaService } from '../../services/curtida.service';
import { Genero, GeneroService } from '../../services/genero.service';
import { Mensagem, MensagemService } from '../../services/mensagem.service';
import { UsuarioPerfil, UsuarioService } from '../../services/usuario.service';

interface ConversaResumo extends Mensagem {
  quantidade: number;
  temNaoLida: boolean;
  mensagens: Mensagem[];
}

interface AvisoPerfil {
  titulo: string;
  descricao: string;
}

const MENSAGENS_RESPEITO = [
  'Converse com respeito e trate cada pessoa com gentileza.',
  'Consentimento vem sempre em primeiro lugar. Respeite limites e decisões.',
  'Proteja sua privacidade e não compartilhe dados pessoais com desconhecidos.',
  'Se algo parecer inadequado, encerre a conversa e denuncie o comportamento.',
];

@Component({
  imports: [MatBadgeModule, MatButtonModule, MatCheckboxModule, MatFormFieldModule, MatIconModule, MatInputModule, MatSelectModule, MatTooltipModule, ReactiveFormsModule, RouterLink],
  selector: 'app-dashboard',
  styleUrl: './dashboard.css',
  templateUrl: './dashboard.html',
})
export class Dashboard implements OnInit, OnDestroy {
  private readonly authService = inject(AuthService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly generoService = inject(GeneroService);
  private readonly curtidaService = inject(CurtidaService);
  private readonly mensagemService = inject(MensagemService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly router = inject(Router);
  private readonly platformId = inject(PLATFORM_ID);
  private atualizacaoMatches?: Subscription;
  private matchesCarregados = false;

  readonly usuario = signal<UsuarioPerfil | null>(null);
  readonly generos = signal<Genero[]>([]);
  readonly matches = signal<UsuarioPerfil[]>([]);
  readonly sugestoes = signal<UsuarioPerfil[]>([]);
  readonly curtidos = signal<Set<number>>(new Set());
  readonly matchesConfirmados = signal<Set<number>>(new Set());
  readonly curtindo = signal<number | null>(null);
  readonly carregando = signal(true);
  readonly salvandoPreferencias = signal(false);
  readonly salvandoPerfil = signal(false);
  readonly editandoPerfil = signal(false);
  readonly perfilSalvo = signal(false);
  readonly erro = signal(false);
  readonly erroPreferencias = signal(false);
  readonly erroPerfil = signal(false);
  readonly salvandoGenero = signal(false);
  readonly generoSalvo = signal(false);
  readonly erroGenero = signal(false);
  readonly mensagens = signal<Mensagem[]>([]);
  readonly avisosPendentes = computed<AvisoPerfil[]>(() => {
    const usuario = this.usuario();
    if (!usuario) return [];

    const avisos: AvisoPerfil[] = [];
    if (!usuario.profissao?.trim()) {
      avisos.push({ titulo: 'Adicione sua profissão:', descricao: ' Ajude outras pessoas a conhecerem seu estilo de vida.' });
    }
    if (!usuario.interesses?.trim()) {
      avisos.push({ titulo: 'Conte seus interesses e hobbies:', descricao: ' Compartilhe atividades como viagens, esportes, culinária ou leitura.' });
    }
    if (!usuario.gostosMusicaisCulturais?.trim()) {
      avisos.push({ titulo: 'Complete seus gostos culturais:', descricao: ' Adicione bandas, gêneros musicais, filmes ou séries que você gosta.' });
    }
    if (!usuario.preferenciasPessoais?.trim()) {
      avisos.push({ titulo: 'Adicione suas preferências pessoais:', descricao: 'Conte se gosta de pets, natureza ou atividades ao ar livre.' });
    }
    if (!usuario.genero) {
      avisos.push({ titulo: 'Informe seu gênero:', descricao: ' Isso ajuda a encontrar conexões mais compatíveis.' });
    }
    if (!usuario.generosProcurados?.length) {
      avisos.push({ titulo: 'Escolha os gêneros que procura:', descricao: ' Selecione uma ou mais opções para receber sugestões melhores.' });
    }
    return avisos;
  });
  readonly conversas = computed<ConversaResumo[]>(() => {
    const agrupadas = new Map<string, ConversaResumo>();

    for (const mensagem of this.mensagens()) {
      const chave = `${mensagem.tipoRemetente}:${mensagem.remetenteId ?? mensagem.remetente}`;
      const conversa = agrupadas.get(chave);
      if (conversa) {
        conversa.quantidade += 1;
        conversa.temNaoLida ||= !mensagem.lida;
        conversa.mensagens.push(mensagem);
      } else {
        agrupadas.set(chave, {
          ...mensagem,
          quantidade: 1,
          temNaoLida: !mensagem.lida,
          mensagens: [mensagem],
        });
      }
    }

    return [...agrupadas.values()];
  });
  readonly mensagensUsuarios = signal(0);
  readonly mensagensAdministradores = signal(0);
  readonly preferenciasForm = new FormControl<number[]>([], { nonNullable: true });
  readonly generoForm = new FormControl<number | null>(null);
  readonly perfilForm = this.formBuilder.nonNullable.group({
    apelido: [''],
    profissao: [''],
    interesses: [''],
    gostosMusicaisCulturais: [''],
    preferenciasPessoais: [''],
  });

  temaGenero(): string {
    const descricao = this.usuario()?.genero?.descricao?.toLocaleLowerCase() ?? '';
    if (descricao.includes('homem') || descricao.includes('masculino')) {
      return 'tema-homem';
    }
    if (descricao.includes('mulher') || descricao.includes('feminino')) {
      return 'tema-mulher';
    }
    if (descricao.includes('não-bin') || descricao.includes('nao-bin')
      || descricao.includes('gay') || descricao.includes('lésb') || descricao.includes('lesb')) {
      return 'tema-arco-iris';
    }
    if (descricao.includes('swinger')) {
      return 'tema-swinger';
    }
    if (descricao.includes('travesti')) {
      return 'tema-travesti';
    }
    return 'tema-genero-diverso';
  }

  sair(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  abrirAvisos(): void {
    const avisos = this.avisosPendentes();
    const lista = avisos.length
      ? `<h3>Complete seu perfil</h3>${avisos.map((aviso) =>
        `<div class="profile-alert"><strong>${aviso.titulo}</strong><span>${aviso.descricao}</span></div>`).join('')}`
      : '<p>Seu perfil está preenchido e pronto para novas conexões.</p>';
    const listaRespeito = MENSAGENS_RESPEITO.map((mensagem) =>
      `<div class="profile-alert"><span>${mensagem}</span></div>`).join('');
    void Swal.fire({
      icon: 'info',
      title: 'Convivência com respeito',
      html: `${lista}<h3>Para uma boa experiência</h3><div class="profile-alert-list">${listaRespeito}</div>`,
      confirmButtonText: avisos.length ? 'Vou preencher' : 'Entendi',
      confirmButtonColor: '#17665e',
    });
  }

  ngOnInit(): void {
    const userId = this.authService.userId();
    if (!userId) {
      this.erro.set(true);
      this.carregando.set(false);
      return;
    }

    this.usuarioService.buscarPorId(userId).subscribe({
      next: (usuario) => {
        this.usuario.set(usuario);
        this.generoForm.setValue(usuario.genero?.id ?? null);
        this.preferenciasForm.setValue(usuario.generosProcurados.map((genero) => genero.id));
        this.perfilForm.setValue({
          apelido: usuario.apelido ?? '',
          profissao: usuario.profissao ?? '',
          interesses: usuario.interesses ?? '',
          gostosMusicaisCulturais: usuario.gostosMusicaisCulturais ?? '',
          preferenciasPessoais: usuario.preferenciasPessoais ?? '',
        });
        const possuiDadosDePerfil = Object.values(this.perfilForm.getRawValue())
          .some((valor) => valor.trim().length > 0);
        this.editandoPerfil.set(!possuiDadosDePerfil);
        if (possuiDadosDePerfil) this.perfilForm.disable();
        this.carregando.set(false);
      },
      error: () => {
        this.erro.set(true);
        this.carregando.set(false);
      },
    });

    this.carregarConexoes(userId);
    if (isPlatformBrowser(this.platformId)) {
      this.atualizacaoMatches = interval(5000).subscribe(() => this.carregarMatches(userId));
    }

    this.generoService.listar().subscribe({
      next: (generos) => this.generos.set(generos),
      error: () => this.erroPreferencias.set(true),
    });

    this.carregarMensagens(userId);
  }

  ngOnDestroy(): void {
    this.atualizacaoMatches?.unsubscribe();
  }

  abrirMensagem(conversa: ConversaResumo): void {
    const userId = this.authService.userId();
    if (!userId) {
      return;
    }

    const naoLidas = conversa.mensagens.filter((mensagem) => !mensagem.lida);
    naoLidas.forEach((mensagem) => this.mensagemService.marcarComoLida(mensagem.id, userId).subscribe({
      next: () => this.mensagens.update((mensagens) => mensagens.map((item) =>
        item.id === mensagem.id ? { ...item, lida: true } : item)),
    }));
    if (conversa.tipoRemetente === 'ADMIN') {
      this.mensagensAdministradores.update((total) => Math.max(0, total - naoLidas.length));
    } else {
      this.mensagensUsuarios.update((total) => Math.max(0, total - naoLidas.length));
    }
  }

  async apagarNotificacao(conversa: ConversaResumo, evento: Event): Promise<void> {
    evento.stopPropagation();
    const userId = this.authService.userId();
    if (!userId) return;

    const confirmacao = await Swal.fire({
      icon: 'warning',
      title: 'Excluir notificação?',
      text: 'A mensagem mais recente desta conversa será removida.',
      showCancelButton: true,
      confirmButtonText: 'Excluir',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#b34a3c',
      cancelButtonColor: '#71827e',
      reverseButtons: true,
    });
    if (!confirmacao.isConfirmed) return;

    this.mensagemService.apagarMensagem(userId, conversa.id).subscribe({
      next: () => {
        this.mensagens.update((mensagens) => mensagens.filter((mensagem) => mensagem.id !== conversa.id));
        if (!conversa.lida) {
          if (conversa.tipoRemetente === 'ADMIN') {
            this.mensagensAdministradores.update((total) => Math.max(0, total - 1));
          } else {
            this.mensagensUsuarios.update((total) => Math.max(0, total - 1));
          }
        }
        void Swal.fire({
          icon: 'success',
          title: 'Notificação excluída',
          text: 'A mensagem foi removida da caixa de entrada.',
          confirmButtonText: 'Fechar',
          confirmButtonColor: '#17665e',
        });
      },
      error: () => void Swal.fire({
        icon: 'error',
        title: 'Não foi possível excluir',
        text: 'Tente novamente em alguns instantes.',
        confirmButtonText: 'Fechar',
        confirmButtonColor: '#17665e',
      }),
    });
  }

  abrirMensagensUsuarios(): void {
    this.marcarMensagensComoLidas('USUARIO');
    this.router.navigate(['/match']);
  }

  abrirMensagensAdministradores(): void {
    this.marcarMensagensComoLidas('ADMIN');
  }

  private marcarMensagensComoLidas(tipo: 'USUARIO' | 'ADMIN'): void {
    const userId = this.authService.userId();
    if (!userId) return;

    const mensagensParaLer = this.mensagens().filter((mensagem) =>
      !mensagem.lida && mensagem.tipoRemetente === tipo);

    if (tipo === 'USUARIO') {
      this.mensagensUsuarios.set(0);
    } else {
      this.mensagensAdministradores.set(0);
    }

    if (!mensagensParaLer.length) return;

    mensagensParaLer.forEach((mensagem) => {
      this.mensagemService.marcarComoLida(mensagem.id, userId).subscribe({
        next: () => this.mensagens.update((mensagens) => mensagens.map((item) =>
          item.id === mensagem.id ? { ...item, lida: true } : item)),
      });
    });
  }

  private carregarMensagens(userId: number): void {
    this.mensagemService.listarRecebidas(userId).subscribe({
      next: (mensagens) => this.mensagens.set(mensagens),
    });
    this.mensagemService.contarNaoLidas(userId).subscribe({
      next: (resumo) => {
        this.mensagensUsuarios.set(resumo.usuarios);
        this.mensagensAdministradores.set(resumo.administradores);
      },
    });
  }

  salvarPreferencias(): void {
    const userId = this.authService.userId();
    if (!userId) {
      return;
    }

    this.salvandoPreferencias.set(true);
    this.usuarioService.atualizarGenerosProcurados(userId, this.preferenciasForm.getRawValue()).subscribe({
      next: (usuario) => {
        this.usuario.set(usuario);
        this.salvandoPreferencias.set(false);
        this.carregarConexoes(userId);
      },
      error: () => {
        this.salvandoPreferencias.set(false);
        this.erroPreferencias.set(true);
      },
    });
  }

  salvarGenero(): void {
    const userId = this.authService.userId();
    const generoId = this.generoForm.getRawValue();
    if (!userId || !generoId || this.usuario()?.genero?.id === generoId) return;

    this.salvandoGenero.set(true);
    this.generoSalvo.set(false);
    this.erroGenero.set(false);
    this.usuarioService.atualizarGenero(userId, generoId).subscribe({
      next: (usuario) => {
        this.usuario.set(usuario);
        this.generoForm.setValue(usuario.genero?.id ?? generoId);
        this.salvandoGenero.set(false);
        this.generoSalvo.set(true);
      },
      error: () => {
        this.salvandoGenero.set(false);
        this.erroGenero.set(true);
      },
    });
  }

  salvarPerfil(): void {
    const userId = this.authService.userId();
    if (!userId) return;

    this.salvandoPerfil.set(true);
    this.erroPerfil.set(false);
    this.perfilSalvo.set(false);
    this.usuarioService.atualizarPerfil(userId, this.perfilForm.getRawValue()).subscribe({
      next: (usuario) => {
        this.usuario.set(usuario);
        this.salvandoPerfil.set(false);
        this.perfilSalvo.set(true);
        this.editandoPerfil.set(false);
        this.perfilForm.disable();
      },
      error: () => {
        this.salvandoPerfil.set(false);
        this.erroPerfil.set(true);
      },
    });
  }

  editarPerfil(): void {
    this.perfilSalvo.set(false);
    this.editandoPerfil.set(true);
    this.perfilForm.enable();
  }

  alternarCurtida(usuario: UsuarioPerfil): void {
    const userId = this.authService.userId();
    if (!userId || this.curtindo() === usuario.id) return;

    this.curtindo.set(usuario.id);
    const requisicao = this.isCurtido(usuario.id)
      ? this.curtidaService.descurtir(usuario.id)
      : this.curtidaService.curtir(usuario.id);
    requisicao.subscribe({
      next: (resposta) => {
        this.curtidos.update((ids) => {
          const atualizados = new Set(ids);
          if (resposta.curtiu) atualizados.add(usuario.id);
          else atualizados.delete(usuario.id);
          return atualizados;
        });
        if (resposta.match) {
          this.matchesConfirmados.update((ids) => new Set(ids).add(usuario.id));
          this.matches.update((matches) => [...matches, usuario]);
        } else {
          this.matchesConfirmados.update((ids) => {
            const atualizados = new Set(ids);
            atualizados.delete(usuario.id);
            return atualizados;
          });
          this.matches.update((matches) => matches.filter((match) => match.id !== usuario.id));
        }
        this.curtindo.set(null);
      },
      error: () => {
        this.erroPreferencias.set(true);
        this.curtindo.set(null);
      },
    });
  }

  isCurtido(usuarioId: number): boolean {
    return this.curtidos().has(usuarioId);
  }

  private carregarConexoes(userId: number): void {
    this.usuarioService.buscarSugestoes(userId).subscribe({
      next: (sugestoes) => {
        this.sugestoes.set(sugestoes);
        sugestoes.forEach((sugestao) => this.carregarStatus(sugestao.id));
      },
    });
    this.carregarMatches(userId);
  }

  private carregarMatches(userId: number): void {
    this.usuarioService.buscarMatches(userId).subscribe({
      next: (matches) => {
        const matchesAnteriores = this.matches();
        const novosMatches = this.matchesCarregados
          ? matches.filter((match) => !matchesAnteriores.some((anterior) => anterior.id === match.id))
          : [];
        this.matches.set(matches);
        this.matchesConfirmados.set(new Set(matches.map((match) => match.id)));
        this.matchesCarregados = true;

        const novoMatch = novosMatches[0];
        if (!novoMatch) return;

        void Swal.fire({
          icon: 'success',
          title: 'Novo match!',
          text: `${novoMatch.apelido || novoMatch.nome} deu match em você também.`,
          showCancelButton: true,
          confirmButtonText: 'Iniciar conversa',
          cancelButtonText: 'Depois',
          confirmButtonColor: '#17665e',
          cancelButtonColor: '#71827e',
          reverseButtons: true,
        }).then((resultado) => {
          if (resultado.isConfirmed) {
            void this.router.navigate(['/match'], { queryParams: { usuarioId: novoMatch.id } });
          }
        });
      },
    });
  }

  private carregarStatus(usuarioId: number): void {
    this.curtidaService.status(usuarioId).subscribe({
      next: (status) => {
        if (status.curtiu) this.curtidos.update((ids) => new Set(ids).add(usuarioId));
      },
    });
  }
}
