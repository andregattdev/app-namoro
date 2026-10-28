package com.andregattermeyer.api_encontros_casuais.config;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.andregattermeyer.api_encontros_casuais.model.PerfilUsuario;
import com.andregattermeyer.api_encontros_casuais.model.Usuario;
import com.andregattermeyer.api_encontros_casuais.repository.UsuarioRepository;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senha;

    public AdminInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:}") String email, @Value("${app.admin.password:}") String senha) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (email.isBlank() || senha.isBlank()) return;

        Usuario admin = usuarioRepository.findByEmail(email).orElseGet(Usuario::new);
        admin.setNome("Administrador");
        admin.setEmail(email);
        if (admin.getId() == null || !senha.startsWith("$2")) {
            admin.setSenha(passwordEncoder.encode(senha));
        }
        admin.setDataNascimento(LocalDate.of(1970, 1, 1));
        admin.setAtivo(true);
        admin.setPerfil(PerfilUsuario.ADMIN);
        usuarioRepository.save(admin);
    }
}