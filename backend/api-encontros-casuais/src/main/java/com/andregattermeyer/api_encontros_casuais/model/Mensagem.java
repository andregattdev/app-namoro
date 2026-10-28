package com.andregattermeyer.api_encontros_casuais.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_mensagem")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destinatario_id", nullable = false)
    private Usuario destinatario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remetente_id")
    private Usuario remetente;

    @Column(nullable = false)
    private TipoRemetente tipoRemetente;

    @Column(nullable = false, length = 2000)
    private String conteudo;

    @Column(nullable = false)
    private LocalDateTime dataEnvio;

    @Column(nullable = false)
    private boolean lida;

    @PrePersist
    void prepararMensagem() {
        if (dataEnvio == null) {
            dataEnvio = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public Usuario getRemetente() {
        return remetente;
    }

    public TipoRemetente getTipoRemetente() {
        return tipoRemetente;
    }

    public String getConteudo() {
        return conteudo;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public boolean isLida() {
        return lida;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public void setRemetente(Usuario remetente) {
        this.remetente = remetente;
    }

    public void setTipoRemetente(TipoRemetente tipoRemetente) {
        this.tipoRemetente = tipoRemetente;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public void setLida(boolean lida) {
        this.lida = lida;
    }
}
