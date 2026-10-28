package com.andregattermeyer.api_encontros_casuais.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andregattermeyer.api_encontros_casuais.dto.LoginRequest;
import com.andregattermeyer.api_encontros_casuais.dto.LoginResponse;
import com.andregattermeyer.api_encontros_casuais.model.Usuario;
import com.andregattermeyer.api_encontros_casuais.security.JwtService;
import com.andregattermeyer.api_encontros_casuais.service.UsuarioService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
            UsuarioService usuarioService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        String token = jwtService.generateToken((org.springframework.security.core.userdetails.UserDetails) authentication
                .getPrincipal());
        Usuario usuario = usuarioService.findByEmail(request.email());
        return ResponseEntity.ok(new LoginResponse(token, usuario.getId(), usuario.getNome(), usuario.getEmail(),
            usuario.getPerfil().name()));
    }
}
