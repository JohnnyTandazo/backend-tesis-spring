
package com.courrier.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/operador")
public class OperadorEnvioController {
    @Autowired
    private PaqueteRepository paqueteRepo;

    private void validarOperador() {
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        String rol = usuarioActual.getRol().toUpperCase();
        if (!rol.equals("ADMIN") && !rol.equals("OPERADOR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación");
        }
    }

    @GetMapping("/envios")
    public ResponseEntity<List<Envio>> listarEnviosOperador(
            @RequestParam(value = "estados", required = false) List<String> estados) {
        validarOperador();
        if (estados == null || estados.isEmpty()) {
            return ResponseEntity.ok(envioService.obtenerTodos());
        }
        return ResponseEntity.ok(envioService.obtenerPorEstados(estados));
    }

    /**
     * PUT /api/operador/envios/{id}/rechazar-pago
     * Cambia estado a PAGO_RECHAZADO
     */
    @PutMapping("/envios/{id}/rechazar-pago")
    public ResponseEntity<Envio> rechazarPago(
            @PathVariable Long id,
            @RequestParam(value = "motivo", required = false) String motivo) {
        validarOperador();
        return ResponseEntity.ok(envioService.rechazarPago(id, motivo));
    }

    /**
     * PUT /api/operador/envios/{id}/estado
     * Cambia el estado del envío a cualquier valor permitido por el operador
     */
    @PutMapping("/envios/{id}/estado")
    public ResponseEntity<Envio> actualizarEstadoManual(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {
        validarOperador();
        return ResponseEntity.ok(envioService.actualizarEstado(id, nuevoEstado));
    }

    /**
     * PUT /api/operador/envios/{id}/aprobar-pago
     * Cambia estado a PAGO_APROBADO o al estado indicado
     */
    @PutMapping("/envios/{id}/aprobar-pago")
    public ResponseEntity<Envio> aprobarPago(
            @PathVariable Long id,
            @RequestParam(value = "nuevoEstado", required = false) String nuevoEstado) {
        validarOperador();
        return ResponseEntity.ok(envioService.aprobarPago(id, nuevoEstado));
    }

    /**
     * PUT /api/operador/paquetes/{paqueteId}/aprobar-pago
     * Aprueba el pago asociado a un paquete antes de ser envío
     */
    @PutMapping("/paquetes/{paqueteId}/aprobar-pago")
    public ResponseEntity<?> aprobarPagoPorPaquete(
            @PathVariable Long paqueteId) {
        validarOperador();
        // Buscar pago asociado al paquete
        Pago pago = pagoService.obtenerPagoPorPaqueteId(paqueteId);
        if (pago == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe pago pendiente para este paquete");
        }
        // Cambiar estado del pago a APROBADO
        pago.setEstado("APROBADO");
        pagoService.guardarPago(pago);
        // (Opcional) Cambiar estado del paquete
        Paquete paquete = paqueteRepo.findById(paqueteId).orElse(null);
        if (paquete != null) {
            paquete.setEstado("EN_ALMACEN"); // O "PAGADO" según lógica
            paqueteRepo.save(paquete);
        }
        return ResponseEntity.ok(pago);
    }
}
