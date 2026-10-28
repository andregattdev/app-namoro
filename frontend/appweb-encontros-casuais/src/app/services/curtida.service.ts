import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface CurtidaResposta {
  curtiu: boolean;
  match: boolean;
}

@Injectable({ providedIn: 'root' })
export class CurtidaService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/curtidas';

  curtir(usuarioId: number): Observable<CurtidaResposta> {
    return this.http.post<CurtidaResposta>(`${this.apiUrl}/${usuarioId}`, {});
  }

  descurtir(usuarioId: number): Observable<CurtidaResposta> {
    return this.http.delete<CurtidaResposta>(`${this.apiUrl}/${usuarioId}`);
  }

  status(usuarioId: number): Observable<CurtidaResposta> {
    return this.http.get<CurtidaResposta>(`${this.apiUrl}/${usuarioId}/status`);
  }
}