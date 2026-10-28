import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface Genero {
  id: number;
  descricao: string;
}

@Injectable({ providedIn: 'root' })
export class GeneroService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/generos';

  listar(): Observable<Genero[]> {
    return this.http.get<Genero[]>(this.apiUrl);
  }
}
