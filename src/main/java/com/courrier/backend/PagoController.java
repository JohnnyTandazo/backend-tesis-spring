package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

/**
 * PagoController - API REST para Pagos
 * Endpoints: GET, POST, PUT, DELETE
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private FacturaService facturaService;

    /**
     * GET /api/pagos/pendientes
     * Obtener TODOS los pagos PENDIENTES (sin filtro de usuario)
     * ADMIN ENDPOINT: Cajero accede a esta lista
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<Pago>> obtenerPendientes() {
        System.out.println("💳 [GET /api/pagos/pendientes] PETICIÓN DEL ADMIN - Listando pagos pendientes...");
        
        try {
            List<Pago> pagosPendientes = pagoService.obtenerPendientes();
            System.out.println("✅ Se devuelven " + pagosPendientes.size() + " pagos pendientes");
            return ResponseEntity.ok(pagosPendientes);
        } catch (Exception e) {
            System.out.println("❌ Error obteniendo pagos pendientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * GET /api/pagos?usuarioId={id}
     * Obtener historial de pagos del usuario (query directo con JOIN)
     */
    @GetMapping
    public ResponseEntity<List<Pago>> obtenerHistorial(@RequestParam Long usuarioId) {
        System.out.println("💳 [GET /api/pagos] PETICIÓN RECIBIDA - Usuario: " + usuarioId);
        
        try {
            List<Pago> pagos = pagoService.obtenerPorUsuario(usuarioId);
            System.out.println("✅ Se encontraron " + pagos.size() + " pagos del usuario: " + usuarioId);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            System.out.println("❌ Error obteniendo pagos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * GET /api/pagos/factura/{facturaId}
     * Obtener pagos de una factura específica
     */
    @GetMapping("/factura/{facturaId}")
    public ResponseEntity<List<Pago>> obtenerPorFactura(@PathVariable Long facturaId) {
        System.out.println("💳 [GET /api/pagos/factura/" + facturaId + "] PETICIÓN RECIBIDA");
        
        try {
            List<Pago> pagos = pagoService.obtenerPorFactura(facturaId);
            System.out.println("✅ Se encontraron " + pagos.size() + " pagos para factura: " + facturaId);
            return ResponseEntity.ok(pagos);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/pagos/{id}
     * Obtener un pago por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pago> obtenerPorId(@PathVariable Long id) {
        System.out.println("🔍 [GET /api/pagos/" + id + "] PETICIÓN RECIBIDA");
        
        Optional<Pago> pago = pagoService.obtenerPorId(id);
        if (pago.isPresent()) {
            System.out.println("✅ Pago encontrado: $" + pago.get().getMonto());
            return ResponseEntity.ok(pago.get());
        } else {
            System.out.println("❌ Pago no encontrado");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * POST /api/pagos
     * Registrar un nuevo pago (multipart/form-data)
     */
    @PostMapping
    public ResponseEntity<Pago> registrarPago(
            @RequestParam("facturaId") Long facturaId,
            @RequestParam("monto") Double monto,
            @RequestParam("metodoPago") String metodoPago,
            @RequestParam("referencia") String referencia,
            @RequestParam(value = "comprobante", required = false) MultipartFile comprobante) {
        System.out.println("💰 [POST /api/pagos] PETICIÓN RECIBIDA - Monto: $" + monto);
        
        try {
            String comprobanteNombre = (comprobante != null ? comprobante.getOriginalFilename() : null);
            Pago nuevo = pagoService.registrarPago(facturaId, monto, metodoPago, referencia, comprobanteNombre);
            System.out.println("✅ Pago registrado con ID: " + nuevo.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * PUT /api/pagos/{id}
     * Actualizar estado de un pago (JSON body)
     * Body: { "estado": "APROBADO" }
     * 
     * CRÍTICO: Si estado = APROBADO, la factura asociada se marcará como PAGADA
     */
    @PutMapping("/{id}")
    public ResponseEntity<Pago> actualizarPago(
            @PathVariable Long id,
            @RequestBody ActualizarPagoRequest request) {
        System.out.println("🔄 [PUT /api/pagos/" + id + "] Nuevo estado: " + request.getEstado());
        
        try {
            if (request.getEstado() == null || request.getEstado().trim().isEmpty()) {
                System.out.println("❌ Error: El campo 'estado' no puede estar vacío");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            
            Pago actualizado = pagoService.actualizarEstado(id, request.getEstado());
            System.out.println("✅ Pago actualizado exitosamente");
            return ResponseEntity.ok(actualizado);
        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/pagos/{id}/estado
     * Actualizar estado de un pago (Query Parameter)
     * Deprecated: Usar PUT /api/pagos/{id} con JSON body
     */
    @Deprecated
    @PutMapping("/{id}/estado")
    public ResponseEntity<Pago> actualizarEstadoLegacy(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {
        System.out.println("⚠️ [PUT /api/pagos/" + id + "/estado] DEPRECATED - Usar PUT /api/pagos/{id} con JSON");
        
        try {
            Pago actualizado = pagoService.actualizarEstado(id, nuevoEstado);
            System.out.println("✅ Estado actualizado a: " + nuevoEstado);
            return ResponseEntity.ok(actualizado);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * DELETE /api/pagos/{id}
     * Eliminar un pago (solo si es PENDIENTE)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarPago(@PathVariable Long id) {
        System.out.println("🗑️ [DELETE /api/pagos/" + id + "] PETICIÓN RECIBIDA");
        
        try {
            Optional<Pago> pago = pagoService.obtenerPorId(id);
            
            if (pago.isPresent() && "PENDIENTE".equals(pago.get().getEstado())) {
                pagoService.eliminarPago(id);
                System.out.println("✅ Pago eliminado");
                return ResponseEntity.noContent().build();
            } else {
                System.out.println("❌ Solo se pueden eliminar pagos PENDIENTE");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
