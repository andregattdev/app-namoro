package com.andregattermeyer.api_encontros_casuais.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.andregattermeyer.api_encontros_casuais.model.Genero;
import com.andregattermeyer.api_encontros_casuais.repository.GeneroRepository;

@Service
public class GeneroService {

    private final GeneroRepository generoRepository;

    public GeneroService(GeneroRepository generoRepository) {
        this.generoRepository = generoRepository;
    }

    public List<Genero> listarTodos() {
        return generoRepository.findAll();
    }

    public Genero salvarGenero(Genero genero) {
        return generoRepository.save(genero);
    }

    public Genero buscarPorId(Long id) {
        return generoRepository.findById(id).orElse(null);
    }



}

