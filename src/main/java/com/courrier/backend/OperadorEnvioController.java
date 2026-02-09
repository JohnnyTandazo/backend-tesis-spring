package com.courrier.backend;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import com.courrier.backend.EnvioService;
import com.courrier.backend.PagoService;
import com.courrier.backend.PaqueteRepository;
import com.courrier.backend.FacturaRepository;
import com.courrier.backend.Paquete;
import com.courrier.backend.Pago;
import com.courrier.backend.Factura;
import com.courrier.backend.Usuario;

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

    @Autowired
    private FacturaRepository facturaRepo;

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

        return ResponseEntity.ok(pago);
    }

    @PutMapping("/pagos/{id}/rechazar")
    public ResponseEntity<Pago> rechazarPago(@PathVariable Long id) {
        validarOperador();
        Pago pago = pagoService.obtenerPagoPorId(id);
        if (pago == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado");
        }
        pago.setEstado("RECHAZADO");
        pagoService.guardarPago(pago);

        Factura factura = pago.getFactura();
        if (factura != null) {
            factura.setEstado("RECHAZADA");
            facturaRepo.save(factura);
        }

        Paquete paquete = pago.getPaquete();
        if (paquete != null) {
            paquete.setEstado("RECHAZADO");
            paqueteRepo.save(paquete);
        }

        return ResponseEntity.ok(pago);
    }
}