package com.andregattermeyer.api_encontros_casuais.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "tb_usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @Column(length = 80)
    private String apelido;

    @Email(message = "Email inválido")
    @NotBlank(message = "Email é obrigatório")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*(),.?\":{}|<>]).+$", message = "A senha deve conter pelo menos 1 letra maiúscula e 1 caractere especial")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    @ManyToOne
    @JoinColumn(name = "genero_id")
    private Genero genero;

        @ManyToMany
        @JoinTable(
            name = "tb_usuario_genero_procurado",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "genero_id"))
        private Set<Genero> generosProcurados = new HashSet<>();

    @JsonFormat(pattern = "dd/MM/yyyy") // formato esperado no JSON
    @NotNull(message = "Data de nascimento é obrigatória")
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(length = 500)
    private String profissao;

    @Column(length = 1000)
    private String interesses;

    @Column(length = 1000)
    private String gostosMusicaisCulturais;

    @Column(length = 1000)
    private String preferenciasPessoais;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private PerfilUsuario perfil = PerfilUsuario.USER;

    public Usuario() {
    }

    public Usuario(Long id, String nome, String email, String senha, Genero genero, Boolean ativo,
            LocalDate dataNascimento) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.genero = genero;
        this.dataNascimento = dataNascimento;
        this.ativo = ativo;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return this.nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getApelido() {
        return this.apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return this.senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Genero getGenero() {
        return this.genero;
    }

    public void setGenero(Genero genero) {
        this.genero = genero;
    }

    public Set<Genero> getGenerosProcurados() {
        return this.generosProcurados;
    }

    public void setGenerosProcurados(Set<Genero> generosProcurados) {
        this.generosProcurados = generosProcurados == null ? new HashSet<>() : generosProcurados;
    }

    public boolean isAtivo() {
        return this.ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public String getProfissao() {
        return this.profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }

    public String getInteresses() {
        return this.interesses;
    }

    public void setInteresses(String interesses) {
        this.interesses = interesses;
    }

    public String getGostosMusicaisCulturais() {
        return this.gostosMusicaisCulturais;
    }

    public void setGostosMusicaisCulturais(String gostosMusicaisCulturais) {
        this.gostosMusicaisCulturais = gostosMusicaisCulturais;
    }

    public String getPreferenciasPessoais() {
        return this.preferenciasPessoais;
    }

    public void setPreferenciasPessoais(String preferenciasPessoais) {
        this.preferenciasPessoais = preferenciasPessoais;
    }

    public PerfilUsuario getPerfil() {
        return this.perfil;
    }

    public void setPerfil(PerfilUsuario perfil) {
        this.perfil = perfil == null ? PerfilUsuario.USER : perfil;
    }

    public LocalDate getDataNascimento() {
        return this.dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

}
