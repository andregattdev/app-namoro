import { isPlatformBrowser } from '@angular/common';
import { inject, Injectable, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  token: string;
  id: number;
  nome: string;
  email: string;
  perfil: 'USER' | 'ADMIN';
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly tokenKey = 'auth_token';
  private readonly userIdKey = 'auth_user_id';
  private readonly profileKey = 'auth_profile';

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', credentials).pipe(
      tap((response) => {
        this.storeToken(response.token);
        this.storeUserId(response.id);
        this.storeProfile(response.perfil);
      }),
    );
  }

  token(): string | null {
    return isPlatformBrowser(this.platformId) ? localStorage.getItem(this.tokenKey) : null;
  }

  userId(): number | null {
    if (!isPlatformBrowser(this.platformId)) {
      return null;
    }

    const userId = localStorage.getItem(this.userIdKey);
    return userId ? Number(userId) : null;
  }

  perfil(): 'USER' | 'ADMIN' | null {
    if (!isPlatformBrowser(this.platformId)) return null;
    const perfil = localStorage.getItem(this.profileKey);
    return perfil === 'ADMIN' || perfil === 'USER' ? perfil : null;
  }

  isAdmin(): boolean {
    return this.perfil() === 'ADMIN';
  }

  logout(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem(this.tokenKey);
      localStorage.removeItem(this.userIdKey);
      localStorage.removeItem(this.profileKey);
    }
  }

  private storeToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(this.tokenKey, token);
    }
  }

  private storeUserId(id: number): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(this.userIdKey, String(id));
    }
  }

  private storeProfile(perfil: 'USER' | 'ADMIN'): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem(this.profileKey, perfil);
    }
  }
}
