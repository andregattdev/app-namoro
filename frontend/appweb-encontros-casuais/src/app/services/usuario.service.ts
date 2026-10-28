import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Genero } from './genero.service';

export interface UsuarioCadastro {
  nome: string;
  email: string;
  senha: string;
  dataNascimento: string;
  ativo: boolean;
  genero: { id: number };
  profissao?: string;
  interesses?: string;
  gostosMusicaisCulturais?: string;
  preferenciasPessoais?: string;
}

export interface UsuarioResposta extends UsuarioCadastro {
  id: number;
}

export interface UsuarioPerfil {
  id: number;
  nome: string;
  apelido: string | null;
  email: string;
  genero: Genero | null;
  generosProcurados: Genero[];
  dataNascimento: string;
  ativo: boolean;
  profissao: string | null;
  interesses: string | null;
  gostosMusicaisCulturais: string | null;
  preferenciasPessoais: string | null;
}

export interface PerfilUsuarioAtualizacao {
  apelido: string;
  profissao: string;
  interesses: string;
  gostosMusicaisCulturais: string;
  preferenciasPessoais: string;
}

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/usuarios';

  cadastrar(usuario: UsuarioCadastro): Observable<UsuarioResposta> {
    return this.http.post<UsuarioResposta>(this.apiUrl, usuario);
  }

  buscarPorId(id: number): Observable<UsuarioPerfil> {
    return this.http.get<UsuarioPerfil>(`${this.apiUrl}/${id}`);
  }

  buscarMatches(id: number): Observable<UsuarioPerfil[]> {
    return this.http.get<UsuarioPerfil[]>(`${this.apiUrl}/${id}/matches`);
  }

  buscarSugestoes(id: number): Observable<UsuarioPerfil[]> {
    return this.http.get<UsuarioPerfil[]>(`${this.apiUrl}/${id}/sugestoes`);
  }

  atualizarGenerosProcurados(id: number, generoIds: number[]): Observable<UsuarioPerfil> {
    return this.http.put<UsuarioPerfil>(`${this.apiUrl}/${id}/generos-procurados`, generoIds);
  }

  atualizarGenero(id: number, generoId: number): Observable<UsuarioPerfil> {
    return this.http.put<UsuarioPerfil>(`${this.apiUrl}/${id}/genero/${generoId}`, {});
  }

  atualizarPerfil(id: number, perfil: PerfilUsuarioAtualizacao): Observable<UsuarioPerfil> {
    return this.http.put<UsuarioPerfil>(`${this.apiUrl}/${id}/perfil`, perfil);
  }
}
