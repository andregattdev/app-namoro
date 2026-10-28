package com.andregattermeyer.api_encontros_casuais.dto;

public record PerfilUsuarioRequest(
        String apelido,
        String profissao,
        String interesses,
        String gostosMusicaisCulturais,
        String preferenciasPessoais) {
}
