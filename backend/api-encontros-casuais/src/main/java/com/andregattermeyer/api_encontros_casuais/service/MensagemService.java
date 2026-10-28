package com.andregattermeyer.api_encontros_casuais.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andregattermeyer.api_encontros_casuais.dto.MensagemResposta;
import com.andregattermeyer.api_encontros_casuais.dto.MensagensResumo;
import com.andregattermeyer.api_encontros_casuais.model.Mensagem;
import com.andregattermeyer.api_encontros_casuais.model.TipoRemetente;
import com.andregattermeyer.api_encontros_casuais.model.Usuario;
import com.andregattermeyer.api_encontros_casuais.repository.MensagemRepository;
import com.andregattermeyer.api_encontros_casuais.repository.UsuarioRepository;

@Service
public class MensagemService {

    private final MensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public MensagemService(MensagemRepository mensagemRepository, UsuarioRepository usuarioRepository,
            UsuarioService usuarioService) {
        this.mensagemRepository = mensagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional(readOnly = true)
    public List<MensagemResposta> listarRecebidas(Long usuarioId) {
        return mensagemRepository.findByDestinatarioIdOrderByDataEnvioDesc(usuarioId).stream()
                .map(MensagemResposta::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MensagemResposta> listarConversa(Long usuarioId, Long outroUsuarioId) {
        validarMatch(usuarioId, outroUsuarioId);
        return mensagemRepository.findConversation(usuarioId, outroUsuarioId).stream()
                .map(MensagemResposta::from)
                .toList();
    }

    @Transactional
    public MensagemResposta enviarMensagem(String remetenteEmail, Long destinatarioId, String conteudo) {
        Usuario remetente = usuarioRepository.findByEmail(remetenteEmail)
                .orElseThrow(() -> new RuntimeException("Remetente não encontrado"));
        validarMatch(remetente.getId(), destinatarioId);

        String texto = conteudo == null ? "" : conteudo.trim();
        if (texto.isBlank() || texto.length() > 2000) {
            throw new IllegalArgumentException("A mensagem deve conter entre 1 e 2000 caracteres");
        }

        Usuario destinatario = usuarioRepository.findById(destinatarioId)
                .orElseThrow(() -> new RuntimeException("Destinatário não encontrado"));
        Mensagem mensagem = new Mensagem();
        mensagem.setRemetente(remetente);
        mensagem.setDestinatario(destinatario);
        mensagem.setTipoRemetente(TipoRemetente.USUARIO);
        mensagem.setConteudo(texto);
        mensagem.setLida(false);
        return MensagemResposta.from(mensagemRepository.save(mensagem));
    }

    private void validarMatch(Long primeiroUsuarioId, Long segundoUsuarioId) {
        if (primeiroUsuarioId.equals(segundoUsuarioId) || !usuarioService.saoMatches(primeiroUsuarioId, segundoUsuarioId)) {
            throw new IllegalArgumentException("Mensagens só podem ser enviadas entre usuários que deram match");
        }
    }

    @Transactional(readOnly = true)
    public MensagensResumo contarNaoLidas(Long usuarioId) {
        long usuarios = mensagemRepository.countByDestinatarioIdAndTipoRemetenteAndLidaFalse(
                usuarioId, TipoRemetente.USUARIO);
        long administradores = mensagemRepository.countByDestinatarioIdAndTipoRemetenteAndLidaFalse(
                usuarioId, TipoRemetente.ADMIN);
        return new MensagensResumo(usuarios + administradores, usuarios, administradores);
    }

    @Transactional
    public void marcarComoLida(Long mensagemId, Long usuarioId, String emailAutenticado) {
        Usuario usuarioAutenticado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));
        if (!usuarioAutenticado.getId().equals(usuarioId)) {
            throw new org.springframework.security.access.AccessDeniedException("Usuário sem acesso à mensagem");
        }

        Mensagem mensagem = mensagemRepository.findByIdAndDestinatarioId(mensagemId, usuarioAutenticado.getId())
                .orElseThrow(() -> new RuntimeException("Mensagem não encontrada"));
        if (!mensagem.isLida()) {
            mensagem.setLida(true);
        }
    }

    @Transactional
    public void apagarConversa(Long usuarioId, Long outroUsuarioId, String emailAutenticado) {
        Usuario usuarioAutenticado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));
        if (!usuarioAutenticado.getId().equals(usuarioId)) {
            throw new org.springframework.security.access.AccessDeniedException("Usuário sem acesso à conversa");
        }

        validarMatch(usuarioId, outroUsuarioId);
        mensagemRepository.deleteConversation(usuarioId, outroUsuarioId);
    }

    @Transactional
    public void apagarMensagem(Long mensagemId, Long usuarioId, String emailAutenticado) {
        Usuario usuarioAutenticado = usuarioRepository.findByEmail(emailAutenticado)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado"));
        if (!usuarioAutenticado.getId().equals(usuarioId)) {
            throw new org.springframework.security.access.AccessDeniedException("Usuário sem acesso à mensagem");
        }

        if (mensagemRepository.deleteByIdAndDestinatarioId(mensagemId, usuarioId) == 0) {
            throw new RuntimeException("Mensagem não encontrada");
        }
    }
}
