package com.andregattermeyer.api_encontros_casuais.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andregattermeyer.api_encontros_casuais.dto.MensagemEnvio;
import com.andregattermeyer.api_encontros_casuais.dto.MensagemResposta;
import com.andregattermeyer.api_encontros_casuais.dto.MensagensResumo;
import com.andregattermeyer.api_encontros_casuais.service.MensagemService;

@RestController
@RequestMapping("/api/mensagens")
@CrossOrigin(origins = "http://localhost:4200")
public class MensagemController {

    private final MensagemService mensagemService;

    public MensagemController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @GetMapping("/usuario/{usuarioId}")
    public List<MensagemResposta> listarRecebidas(@PathVariable Long usuarioId) {
        return mensagemService.listarRecebidas(usuarioId);
    }

    @GetMapping("/usuario/{usuarioId}/com/{outroUsuarioId}")
    public List<MensagemResposta> listarConversa(@PathVariable Long usuarioId,
            @PathVariable Long outroUsuarioId) {
        return mensagemService.listarConversa(usuarioId, outroUsuarioId);
    }

    @PostMapping
    public MensagemResposta enviar(@RequestBody MensagemEnvio envio, Principal principal) {
        return mensagemService.enviarMensagem(principal.getName(), envio.destinatarioId(), envio.conteudo());
    }

    @GetMapping("/usuario/{usuarioId}/nao-lidas")
    public MensagensResumo contarNaoLidas(@PathVariable Long usuarioId) {
        return mensagemService.contarNaoLidas(usuarioId);
    }

    @PatchMapping("/{mensagemId}/usuario/{usuarioId}/lida")
    public ResponseEntity<Void> marcarComoLida(@PathVariable Long mensagemId, @PathVariable Long usuarioId,
            Principal principal) {
        mensagemService.marcarComoLida(mensagemId, usuarioId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuario/{usuarioId}/com/{outroUsuarioId}")
    public ResponseEntity<Void> apagarConversa(@PathVariable Long usuarioId,
            @PathVariable Long outroUsuarioId, Principal principal) {
        mensagemService.apagarConversa(usuarioId, outroUsuarioId, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/usuario/{usuarioId}/mensagem/{mensagemId}")
    public ResponseEntity<Void> apagarMensagem(@PathVariable Long usuarioId,
            @PathVariable Long mensagemId, Principal principal) {
        mensagemService.apagarMensagem(mensagemId, usuarioId, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
