package com.andregattermeyer.api_encontros_casuais.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "tb_curtida", uniqueConstraints = @UniqueConstraint(columnNames = { "usuario_id", "curtido_id" }))
public class Curtida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    @JoinColumn(name = "curtido_id", nullable = false)
    private Usuario curtido;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    @PrePersist
    void preparar() {
        if (criadaEm == null) criadaEm = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public Usuario getCurtido() { return curtido; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public void setCurtido(Usuario curtido) { this.curtido = curtido; }
}