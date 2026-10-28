package com.andregattermeyer.api_encontros_casuais.dto;

public record LoginResponse(String token, Long id, String nome, String email, String perfil) {
}
