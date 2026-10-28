package com.andregattermeyer.api_encontros_casuais.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.andregattermeyer.api_encontros_casuais.model.Curtida;

public interface CurtidaRepository extends JpaRepository<Curtida, Long> {
    Optional<Curtida> findByUsuarioIdAndCurtidoId(Long usuarioId, Long curtidoId);
    boolean existsByUsuarioIdAndCurtidoId(Long usuarioId, Long curtidoId);
    List<Curtida> findByUsuarioId(Long usuarioId);
}