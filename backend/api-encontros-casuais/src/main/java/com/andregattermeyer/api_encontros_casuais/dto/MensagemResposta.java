package com.andregattermeyer.api_encontros_casuais.dto;

import java.time.LocalDateTime;

import com.andregattermeyer.api_encontros_casuais.model.Mensagem;
import com.andregattermeyer.api_encontros_casuais.model.TipoRemetente;

public record MensagemResposta(Long id, Long remetenteId, String remetente, TipoRemetente tipoRemetente,
        String conteudo, LocalDateTime dataEnvio, boolean lida) {

    public static MensagemResposta from(Mensagem mensagem) {
        String remetente = mensagem.getTipoRemetente() == TipoRemetente.ADMIN
                ? "Administração"
                : nomeApresentacao(mensagem.getRemetente());
        Long remetenteId = mensagem.getRemetente() == null ? null : mensagem.getRemetente().getId();
        return new MensagemResposta(mensagem.getId(), remetenteId, remetente, mensagem.getTipoRemetente(),
                mensagem.getConteudo(), mensagem.getDataEnvio(), mensagem.isLida());
    }

        private static String nomeApresentacao(com.andregattermeyer.api_encontros_casuais.model.Usuario usuario) {
                return usuario.getApelido() == null || usuario.getApelido().isBlank()
                                ? usuario.getNome()
                                : usuario.getApelido();
        }
}
