package com.andregattermeyer.api_encontros_casuais.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andregattermeyer.api_encontros_casuais.dto.CurtidaResposta;
import com.andregattermeyer.api_encontros_casuais.service.CurtidaService;

@RestController
@RequestMapping("/api/curtidas")
@CrossOrigin(origins = "http://localhost:4200")
public class CurtidaController {
    private final CurtidaService curtidaService;

    public CurtidaController(CurtidaService curtidaService) {
        this.curtidaService = curtidaService;
    }

    @PostMapping("/{usuarioId}")
    public ResponseEntity<CurtidaResposta> curtir(@PathVariable Long usuarioId, Principal principal) {
        return ResponseEntity.ok(curtidaService.curtir(principal.getName(), usuarioId));
    }

    @DeleteMapping("/{usuarioId}")
    public ResponseEntity<CurtidaResposta> descurtir(@PathVariable Long usuarioId, Principal principal) {
        return ResponseEntity.ok(curtidaService.descurtir(principal.getName(), usuarioId));
    }

    @GetMapping("/{usuarioId}/status")
    public ResponseEntity<CurtidaResposta> status(@PathVariable Long usuarioId, Principal principal) {
        return ResponseEntity.ok(curtidaService.status(principal.getName(), usuarioId));
    }
}