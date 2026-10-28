package com.andregattermeyer.api_encontros_casuais.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.andregattermeyer.api_encontros_casuais.dto.PerfilUsuarioRequest;
import com.andregattermeyer.api_encontros_casuais.model.Genero;
import com.andregattermeyer.api_encontros_casuais.model.Usuario;
import com.andregattermeyer.api_encontros_casuais.repository.CurtidaRepository;
import com.andregattermeyer.api_encontros_casuais.repository.GeneroRepository;
import com.andregattermeyer.api_encontros_casuais.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    private final GeneroRepository generoRepository;
    private final CurtidaRepository curtidaRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, GeneroRepository generoRepository,
            CurtidaRepository curtidaRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.generoRepository = generoRepository;
        this.curtidaRepository = curtidaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> buscarTodosUsuarios() {
        return usuarioRepository.findAll();
    }

    public Usuario salvarUsuario(Usuario usuario) {
        if (!isMaiorDeIdade(usuario.getDataNascimento())) {
            throw new RuntimeException("Usuário deve ser maior de 18 anos");
        }
        if (usuario.getGenero() != null) {
            usuario.setGenero(buscarGenero(usuario.getGenero()));
        }
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario atualizarUsuario(Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuarioExistente.setNome(usuario.getNome());
        usuarioExistente.setEmail(usuario.getEmail());
        if (usuario.getSenha() != null && !usuario.getSenha().startsWith("$2")) {
            usuarioExistente.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        usuarioExistente.setDataNascimento(usuario.getDataNascimento());
        usuarioExistente.setAtivo(usuario.isAtivo());
        usuarioExistente.setProfissao(usuario.getProfissao());
        usuarioExistente.setInteresses(usuario.getInteresses());
        usuarioExistente.setGostosMusicaisCulturais(usuario.getGostosMusicaisCulturais());
        usuarioExistente.setPreferenciasPessoais(usuario.getPreferenciasPessoais());

        if (usuario.getGenero() != null) {
            usuarioExistente.setGenero(buscarGenero(usuario.getGenero()));
        }

        return usuarioRepository.save(usuarioExistente);
    }

    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email).orElse(null);
    }

    public boolean pertenceAoUsuario(Long id, String email) {
        return usuarioRepository.findById(id)
                .map(usuario -> usuario.getEmail().equalsIgnoreCase(email))
                .orElse(false);
    }

    public Usuario atualizarGenero(Long usuarioId, Long generoId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setGenero(generoRepository.findById(generoId)
                .orElseThrow(() -> new RuntimeException("Gênero não encontrado")));
        return usuarioRepository.save(usuario);
    }

    public Usuario atualizarPerfil(Long usuarioId, PerfilUsuarioRequest perfil) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        usuario.setApelido(perfil.apelido());
        usuario.setProfissao(perfil.profissao());
        usuario.setInteresses(perfil.interesses());
        usuario.setGostosMusicaisCulturais(perfil.gostosMusicaisCulturais());
        usuario.setPreferenciasPessoais(perfil.preferenciasPessoais());
        return usuarioRepository.save(usuario);
    }

        public Usuario atualizarGenerosProcurados(Long usuarioId, Set<Long> generoIds) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Set<Genero> generos = generoIds == null ? new HashSet<>() : generoRepository.findAllById(generoIds).stream()
            .collect(Collectors.toSet());

        if (generos.size() != (generoIds == null ? 0 : generoIds.size())) {
            throw new RuntimeException("Um ou mais gêneros não foram encontrados");
        }

        usuario.setGenerosProcurados(generos);
        return usuarioRepository.save(usuario);
        }

    public List<Usuario> buscarSugestoes(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (usuario.getGenero() == null || usuario.getGenerosProcurados().isEmpty()) {
            return List.of();
        }

        return usuarioRepository.findAllByAtivoTrue().stream()
            .filter(candidato -> !candidato.getId().equals(usuarioId))
            .filter(candidato -> candidato.getGenero() != null)
            .filter(candidato -> usuario.getGenerosProcurados().stream()
                .anyMatch(genero -> genero.getId().equals(candidato.getGenero().getId())))
            .filter(candidato -> candidato.getGenerosProcurados().stream()
                .anyMatch(genero -> genero.getId().equals(usuario.getGenero().getId())))
            .toList();
        }

    public List<Usuario> buscarMatches(Long usuarioId) {
        return buscarSugestoes(usuarioId).stream()
                .filter(candidato -> curtidaRepository.existsByUsuarioIdAndCurtidoId(usuarioId, candidato.getId()))
                .filter(candidato -> curtidaRepository.existsByUsuarioIdAndCurtidoId(candidato.getId(), usuarioId))
                .toList();
    }

    public boolean saoMatches(Long primeiroUsuarioId, Long segundoUsuarioId) {
        return buscarMatches(primeiroUsuarioId).stream()
                .anyMatch(usuario -> usuario.getId().equals(segundoUsuarioId));
    }

    private Genero buscarGenero(Genero generoInformado) {
        if (generoInformado.getId() == null) {
            throw new RuntimeException("O ID do gênero é obrigatório");
        }

        return generoRepository.findById(generoInformado.getId())
                .orElseThrow(() -> new RuntimeException("Gênero não encontrado"));
    }

    private boolean isMaiorDeIdade(LocalDate dataNascimento) {
        if (dataNascimento == null) return false;
        return Period.between(dataNascimento, LocalDate.now()).getYears() >= 18;
    }

    public Usuario softDeleteUsuario(Long id) {
    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    usuario.setAtivo(false);
    return usuarioRepository.save(usuario);
}


}
