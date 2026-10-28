package com.andregattermeyer.api_encontros_casuais.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.andregattermeyer.api_encontros_casuais.model.Mensagem;
import com.andregattermeyer.api_encontros_casuais.model.TipoRemetente;

public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByDestinatarioIdOrderByDataEnvioDesc(Long destinatarioId);

        @Query("""
            select m from Mensagem m
            where (m.remetente.id = :primeiroId and m.destinatario.id = :segundoId)
               or (m.remetente.id = :segundoId and m.destinatario.id = :primeiroId)
            order by m.dataEnvio asc
            """)
        List<Mensagem> findConversation(Long primeiroId, Long segundoId);

    long countByDestinatarioIdAndLidaFalse(Long destinatarioId);

    long countByDestinatarioIdAndTipoRemetenteAndLidaFalse(Long destinatarioId, TipoRemetente tipoRemetente);

    @Query("select m from Mensagem m where m.id = :id and m.destinatario.id = :destinatarioId")
    java.util.Optional<Mensagem> findByIdAndDestinatarioId(Long id, Long destinatarioId);

    @Modifying
    @Query("delete from Mensagem m where m.id = :mensagemId and m.destinatario.id = :destinatarioId")
    int deleteByIdAndDestinatarioId(Long mensagemId, Long destinatarioId);

    @Modifying
    @Query("""
            delete from Mensagem m
            where (m.remetente.id = :primeiroId and m.destinatario.id = :segundoId)
               or (m.remetente.id = :segundoId and m.destinatario.id = :primeiroId)
            """)
    void deleteConversation(Long primeiroId, Long segundoId);
}
