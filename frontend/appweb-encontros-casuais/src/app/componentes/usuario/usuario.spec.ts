import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { Usuario } from './usuario';
import { GeneroService } from '../../services/genero.service';
import { UsuarioService } from '../../services/usuario.service';

describe('Usuario', () => {
  let component: Usuario;
  let fixture: ComponentFixture<Usuario>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Usuario],
      providers: [
        {
          provide: UsuarioService,
          useValue: { cadastrar: () => of({}) },
        },
        {
          provide: GeneroService,
          useValue: { listar: () => of([]) },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Usuario);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
