package com.courrier.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

import com.courrier.backend.EnvioService;
import com.courrier.backend.PagoService;
import com.courrier.backend.PaqueteRepository;
import com.courrier.backend.Usuario;
import com.courrier.backend.Envio;
import com.courrier.backend.Pago;
import com.courrier.backend.Paquete;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/operador")
public class OperadorEnvioController extends BaseSecurityController {

    @Autowired
    private EnvioService envioService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private PaqueteRepository paqueteRepo;

    private void validarOperador() {
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        String rol = usuarioActual.getRol().toUpperCase();
        if (!rol.equals("ADMIN") && !rol.equals("OPERADOR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación");
        }
    }
        @PutMapping("/pagos/{id}/verificar")
        public ResponseEntity<Pago> verificarPago(@PathVariable Long id) {
            validarOperador();
            Pago pago = pagoService.obtenerPagoPorId(id);
            if (pago == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado");
            }
            pago.setEstado("VERIFICADO");
            pagoService.guardarPago(pago);

            Factura factura = pago.getFactura();
            if (factura != null) {
                factura.setEstado("PAGADO");
                facturaRepo.save(factura);
            }

            Paquete paquete = pago.getPaquete();
            if (paquete != null) {
                paquete.setEstado("PAGO_VERIFICADO");
                paqueteRepo.save(paquete);
            }

            return ResponseEntity.ok(pago);
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

    @PutMapping("/envios/{id}/rechazar-pago")
    public ResponseEntity<Envio> rechazarPago(
            @PathVariable Long id,
            @RequestParam(value = "motivo", required = false) String motivo) {
        validarOperador();
        return ResponseEntity.ok(envioService.rechazarPago(id, motivo));
    }

    @PutMapping("/envios/{id}/estado")
    public ResponseEntity<Envio> actualizarEstadoManual(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {
        validarOperador();
        return ResponseEntity.ok(envioService.actualizarEstado(id, nuevoEstado));
    }

    @PutMapping("/envios/{id}/aprobar-pago")
    public ResponseEntity<Envio> aprobarPago(
            @PathVariable Long id,
            @RequestParam(value = "nuevoEstado", required = false) String nuevoEstado) {
        validarOperador();
        return ResponseEntity.ok(envioService.aprobarPago(id, nuevoEstado));
    }

    @PutMapping("/paquetes/{paqueteId}/aprobar-pago")
    public ResponseEntity<?> aprobarPagoPorPaquete(@PathVariable Long paqueteId) {
        validarOperador();
        Pago pago = pagoService.obtenerPagoPorPaqueteId(paqueteId);
        if (pago == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe pago pendiente para este paquete");
        }
        pago.setEstado("APROBADO");
        pagoService.guardarPago(pago);
        Paquete paquete = paqueteRepo.findById(paqueteId).orElse(null);
        if (paquete != null) {
            paquete.setEstado("EN_ALMACEN");
            paqueteRepo.save(paquete);
        }
        return ResponseEntity.ok(pago);
    }
}