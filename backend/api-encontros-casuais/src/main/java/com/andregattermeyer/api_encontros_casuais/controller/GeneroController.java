package com.andregattermeyer.api_encontros_casuais.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andregattermeyer.api_encontros_casuais.model.Genero;
import com.andregattermeyer.api_encontros_casuais.service.GeneroService;

@RestController
@RequestMapping("/api/generos")
@CrossOrigin(origins = "http://localhost:4200") 
public class GeneroController {

    private final GeneroService generoService;

    public GeneroController(GeneroService generoService) {
        this.generoService = generoService;
    }

    // Listar todos os gêneros ativos
    @GetMapping
    public List<Genero> listarGeneros() {
        return generoService.listarTodos();
    }

    // Buscar gênero por ID
    @GetMapping("/{id}")
    public ResponseEntity<Genero> buscarPorId(@PathVariable Long id) {
        Genero genero = generoService.buscarPorId(id);
        if (genero != null) {
            return ResponseEntity.ok(genero);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Criar novo gênero
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Genero salvarGenero(@RequestBody Genero genero) {
        return generoService.salvarGenero(genero);
    }

    // Atualizar gênero
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Genero> atualizarGenero(@PathVariable Long id, @RequestBody Genero genero) {
        genero.setId(id);
        Genero atualizado = generoService.salvarGenero(genero);
        return ResponseEntity.ok(atualizado);
    }

    // Soft delete de gênero
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletarGenero(@PathVariable Long id) {
        
        return ResponseEntity.noContent().build();
    }
}
