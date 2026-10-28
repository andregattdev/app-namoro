package com.andregattermeyer.api_encontros_casuais.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andregattermeyer.api_encontros_casuais.dto.CurtidaResposta;
import com.andregattermeyer.api_encontros_casuais.model.Curtida;
import com.andregattermeyer.api_encontros_casuais.model.Mensagem;
import com.andregattermeyer.api_encontros_casuais.model.TipoRemetente;
import com.andregattermeyer.api_encontros_casuais.repository.CurtidaRepository;
import com.andregattermeyer.api_encontros_casuais.repository.MensagemRepository;
import com.andregattermeyer.api_encontros_casuais.repository.UsuarioRepository;

@Service
public class CurtidaService {
    private final CurtidaRepository curtidaRepository;
    private final MensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public CurtidaService(CurtidaRepository curtidaRepository, MensagemRepository mensagemRepository,
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService) {
        this.curtidaRepository = curtidaRepository;
        this.mensagemRepository = mensagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public CurtidaResposta curtir(String email, Long curtidoId) {
        Long usuarioId = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();
        if (usuarioId.equals(curtidoId) || !usuarioService.buscarSugestoes(usuarioId).stream()
                .anyMatch(usuario -> usuario.getId().equals(curtidoId))) {
            throw new IllegalArgumentException("Só é possível curtir uma sugestão compatível");
        }

        boolean matchJaExistia = curtidaRepository.existsByUsuarioIdAndCurtidoId(usuarioId, curtidoId)
            && curtidaRepository.existsByUsuarioIdAndCurtidoId(curtidoId, usuarioId);
        if (!curtidaRepository.existsByUsuarioIdAndCurtidoId(usuarioId, curtidoId)) {
            Curtida curtida = new Curtida();
            curtida.setUsuario(usuarioRepository.getReferenceById(usuarioId));
            curtida.setCurtido(usuarioRepository.getReferenceById(curtidoId));
            curtidaRepository.save(curtida);
        }
        if (!matchJaExistia && curtidaRepository.existsByUsuarioIdAndCurtidoId(curtidoId, usuarioId)) {
            Mensagem mensagem = new Mensagem();
            mensagem.setRemetente(usuarioRepository.getReferenceById(usuarioId));
            mensagem.setDestinatario(usuarioRepository.getReferenceById(curtidoId));
            mensagem.setTipoRemetente(TipoRemetente.USUARIO);
                mensagem.setConteudo(nomeApresentacao(usuarioRepository.getReferenceById(usuarioId))
                + " deu match em você também. Inicie uma conversa!");
            mensagem.setLida(false);
            mensagemRepository.save(mensagem);
        }
        return status(usuarioId, curtidoId);
    }

    private String nomeApresentacao(com.andregattermeyer.api_encontros_casuais.model.Usuario usuario) {
        return usuario.getApelido() == null || usuario.getApelido().isBlank()
                ? usuario.getNome()
                : usuario.getApelido();
    }

    @Transactional
    public CurtidaResposta descurtir(String email, Long curtidoId) {
        Long usuarioId = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();
        curtidaRepository.findByUsuarioIdAndCurtidoId(usuarioId, curtidoId)
                .ifPresent(curtidaRepository::delete);
        return status(usuarioId, curtidoId);
    }

    @Transactional(readOnly = true)
    public CurtidaResposta status(String email, Long outroUsuarioId) {
        Long usuarioId = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado")).getId();
        return status(usuarioId, outroUsuarioId);
    }

    private CurtidaResposta status(Long usuarioId, Long outroUsuarioId) {
        boolean curtiu = curtidaRepository.existsByUsuarioIdAndCurtidoId(usuarioId, outroUsuarioId);
        boolean match = curtiu && curtidaRepository.existsByUsuarioIdAndCurtidoId(outroUsuarioId, usuarioId);
        return new CurtidaResposta(curtiu, match);
    }
}