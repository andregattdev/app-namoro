import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export type TipoRemetente = 'USUARIO' | 'ADMIN';

export interface Mensagem {
  id: number;
  remetenteId: number | null;
  remetente: string;
  tipoRemetente: TipoRemetente;
  conteudo: string;
  dataEnvio: string;
  lida: boolean;
}

export interface MensagensResumo {
  total: number;
  usuarios: number;
  administradores: number;
}

export interface MensagemEnvio {
  destinatarioId: number;
  conteudo: string;
}

@Injectable({ providedIn: 'root' })
export class MensagemService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/mensagens';

  listarRecebidas(usuarioId: number): Observable<Mensagem[]> {
    return this.http.get<Mensagem[]>(`${this.apiUrl}/usuario/${usuarioId}`);
  }

  listarConversa(usuarioId: number, outroUsuarioId: number): Observable<Mensagem[]> {
    return this.http.get<Mensagem[]>(`${this.apiUrl}/usuario/${usuarioId}/com/${outroUsuarioId}`);
  }

  enviar(envio: MensagemEnvio): Observable<Mensagem> {
    return this.http.post<Mensagem>(this.apiUrl, envio);
  }

  contarNaoLidas(usuarioId: number): Observable<MensagensResumo> {
    return this.http.get<MensagensResumo>(`${this.apiUrl}/usuario/${usuarioId}/nao-lidas`);
  }

  marcarComoLida(mensagemId: number, usuarioId: number): Observable<void> {
    return this.http.patch<void>(`${this.apiUrl}/${mensagemId}/usuario/${usuarioId}/lida`, {});
  }

  apagarConversa(usuarioId: number, outroUsuarioId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/usuario/${usuarioId}/com/${outroUsuarioId}`);
  }

  apagarMensagem(usuarioId: number, mensagemId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/usuario/${usuarioId}/mensagem/${mensagemId}`);
  }
}
