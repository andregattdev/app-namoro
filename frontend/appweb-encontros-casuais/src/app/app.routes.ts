import { Routes } from '@angular/router';
import { Login } from './componentes/login/login';
import { Usuario } from './componentes/usuario/usuario';
import { Match } from './componentes/match/match';
import { Dashboard } from './componentes/dashboard/dashboard';
import { authGuard } from './services/auth.guard';

export const routes: Routes = [
	{ path: '', component: Login },
	{ path: 'login', component: Login },
	{ path: 'cadastro', component: Usuario },
	{ path: 'match', component: Match, canActivate: [authGuard] },
	{ path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
	{ path: '**', redirectTo: '' },
];
