package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/operador/envios")
public class OperadorEnvioController extends BaseSecurityController {

    @Autowired
    private EnvioService envioService;

    private void validarOperador() {
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        String rol = usuarioActual.getRol().toUpperCase();
        if (!rol.equals("ADMIN") && !rol.equals("OPERADOR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación");
        }
    }

    /**
     * GET /api/operador/envios
     * Listar envíos para operador/admin. Puede filtrar por estados.
     * Ej: /api/operador/envios?estados=PENDIENTE_PAGO&estados=EN_PROCESO
     */
    @GetMapping
    public ResponseEntity<List<Envio>> listarEnviosOperador(
            @RequestParam(value = "estados", required = false) List<String> estados) {
        validarOperador();

        if (estados == null || estados.isEmpty()) {
            return ResponseEntity.ok(envioService.obtenerTodos());
        }

        return ResponseEntity.ok(envioService.obtenerPorEstados(estados));
    }

    /**
     * PUT /api/operador/envios/{id}/tracking
     * Actualiza tracking y cambia estado a EN_TRANSITO
     */
    @PutMapping("/{id}/tracking")
    public ResponseEntity<Envio> actualizarTracking(
            @PathVariable Long id,
            @RequestParam String nuevoTracking) {
        validarOperador();
        return ResponseEntity.ok(envioService.actualizarTrackingOperador(id, nuevoTracking));
    }

    /**
     * PUT /api/operador/envios/{id}/rechazar-pago
     * Cambia estado a PAGO_RECHAZADO
     */
    @PutMapping("/{id}/rechazar-pago")
    public ResponseEntity<Envio> rechazarPago(
            @PathVariable Long id,
            @RequestParam(value = "motivo", required = false) String motivo) {
        validarOperador();
        return ResponseEntity.ok(envioService.rechazarPago(id, motivo));
    }

    /**
     * PUT /api/operador/envios/{id}/aprobar-pago
     * Cambia estado a PAGO_APROBADO o al estado indicado
     */
    @PutMapping("/{id}/aprobar-pago")
    public ResponseEntity<Envio> aprobarPago(
            @PathVariable Long id,
            @RequestParam(value = "nuevoEstado", required = false) String nuevoEstado) {
        validarOperador();
        return ResponseEntity.ok(envioService.aprobarPago(id, nuevoEstado));
    }
}
