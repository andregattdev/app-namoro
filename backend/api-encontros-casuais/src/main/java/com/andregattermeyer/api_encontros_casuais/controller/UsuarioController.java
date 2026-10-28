package com.andregattermeyer.api_encontros_casuais.controller;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andregattermeyer.api_encontros_casuais.dto.PerfilUsuarioRequest;
import com.andregattermeyer.api_encontros_casuais.model.Usuario;
import com.andregattermeyer.api_encontros_casuais.service.UsuarioService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "http://localhost:4200") 
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Listar todos os usuários ativos
    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioService.buscarTodosUsuarios();
    }

    // Buscar usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id, Principal principal) {
        verificarAcesso(id, principal);
        Usuario usuario = usuarioService.buscarUsuarioPorId(id);
        if (usuario == null || !usuario.isAtivo()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(usuario);
    }

    // Criar novo usuário
    @PostMapping
    public Usuario salvarUsuario(@RequestBody @Valid  Usuario usuario) {
        return usuarioService.salvarUsuario(usuario);
    }

    // Atualizar usuário
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> atualizarUsuario(@PathVariable Long id, @RequestBody @Valid  Usuario usuario,
            Principal principal) {
        verificarAcesso(id, principal);
        usuario.setId(id);
        Usuario atualizado = usuarioService.atualizarUsuario(usuario);
        return ResponseEntity.ok(atualizado);
    }

    // Soft delete de usuário
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarUsuario(@PathVariable Long id) {
        usuarioService.softDeleteUsuario(id);
        return ResponseEntity.noContent().build();
    }

    // Atualizar gênero do usuário
    @PutMapping("/{id}/genero/{generoId}")
    public ResponseEntity<Usuario> atualizarGenero(@PathVariable Long id, @PathVariable Long generoId,
            Principal principal) {
        verificarAcesso(id, principal);
        Usuario usuarioAtualizado = usuarioService.atualizarGenero(id, generoId);
        return ResponseEntity.ok(usuarioAtualizado);
    }

    @PutMapping("/{id}/perfil")
    public ResponseEntity<Usuario> atualizarPerfil(@PathVariable Long id,
            @RequestBody PerfilUsuarioRequest perfil, Principal principal) {
        verificarAcesso(id, principal);
        return ResponseEntity.ok(usuarioService.atualizarPerfil(id, perfil));
    }

    @PutMapping("/{id}/generos-procurados")
    public ResponseEntity<Usuario> atualizarGenerosProcurados(@PathVariable Long id,
            @RequestBody Set<Long> generoIds, Principal principal) {
        verificarAcesso(id, principal);
        return ResponseEntity.ok(usuarioService.atualizarGenerosProcurados(id, generoIds));
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<List<Usuario>> buscarMatches(@PathVariable Long id, Principal principal) {
        verificarAcesso(id, principal);
        return ResponseEntity.ok(usuarioService.buscarMatches(id));
    }

    @GetMapping("/{id}/sugestoes")
    public ResponseEntity<List<Usuario>> buscarSugestoes(@PathVariable Long id, Principal principal) {
        verificarAcesso(id, principal);
        return ResponseEntity.ok(usuarioService.buscarSugestoes(id));
    }

    private void verificarAcesso(Long id, Principal principal) {
        boolean admin = principal instanceof Authentication authentication
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        if (!admin && !usuarioService.pertenceAoUsuario(id, principal.getName())) {
            throw new AccessDeniedException("Usuário sem acesso a este perfil");
        }
    }
}

