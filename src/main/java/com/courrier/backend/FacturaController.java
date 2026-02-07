package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * FacturaController - API REST para Facturas
 * 🔒 SEGURIDAD: Todos los endpoints usan JWT desde SecurityContextHolder
 * NO acepta parámetros manuales de usuario
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/facturas")
public class FacturaController extends BaseSecurityController {

    @Autowired
    private FacturaService facturaService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private DireccionRepository direccionRepository;

    /**
     * GET /api/facturas/pendientes
     * Obtener facturas pendientes del usuario autenticado
     * 🔒 SEGURIDAD: Requiere JWT válido
     */
    @GetMapping("/pendientes")
    public ResponseEntity<List<Factura>> obtenerPendientes() {
        System.out.println("⏳ [GET /api/facturas/pendientes] PETICIÓN RECIBIDA");
        
        try {
            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            List<Factura> pendientes = facturaService.obtenerPendientes(usuarioActual.getId());
            System.out.println("✅ Se encontraron " + pendientes.size() + " facturas pendientes");
            return ResponseEntity.ok(pendientes);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/facturas/usuario/{usuarioId}
     * Obtener todas las facturas de un usuario
     * 🔒 SEGURIDAD: Requiere JWT válido
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Factura>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        System.out.println("📋 [GET /api/facturas/usuario/" + usuarioId + "] PETICIÓN RECIBIDA");
        
        try {
            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            // 🔒 IDOR: CLIENTE solo puede ver sus propias facturas
            if (!"ADMIN".equals(usuarioActual.getRol().toUpperCase()) && 
                !"OPERADOR".equals(usuarioActual.getRol().toUpperCase()) &&
                !usuarioId.equals(usuarioActual.getId())) {
                System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioActual.getId() + " intentó acceder a facturas de " + usuarioId);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver estas facturas");
            }
            
            // ✅ SOLO PENDIENTES para dropdown/dashboard
            List<Factura> facturas = facturaService.obtenerPendientes(usuarioId);
            if (facturas == null) {
                return ResponseEntity.ok(List.of());
            }
            System.out.println("✅ Se encontraron " + facturas.size() + " facturas PENDIENTES");
            return ResponseEntity.ok(facturas);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * GET /api/facturas/{id}/pdf
     * Endpoint de demo para PDF de factura (devuelve PDF vacío si no se genera)
     * 🔒 SEGURIDAD: Requiere JWT válido + Verifica propiedad (IDOR)
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> obtenerPdfFactura(@PathVariable Long id) {
        System.out.println("📄 [GET /api/facturas/" + id + "/pdf] PETICIÓN RECIBIDA");

        try {
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            Optional<Factura> facturaOpt = facturaService.obtenerPorId(id);
            if (facturaOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Factura factura = facturaOpt.get();

            // 🔒 VERIFICACIÓN IDOR: Comprobar propiedad del recurso
            String rol = usuarioActual.getRol().toUpperCase();

            // ADMIN y OPERADOR tienen acceso total
            if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
                System.out.println("✅ Acceso autorizado: Usuario " + rol);
            } else if (factura.getUsuario() == null) {
                System.out.println("🚫 ACCESO DENEGADO: Factura sin usuario asignado (ID: " + factura.getId() + ")");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Factura sin propietario asignado");
            } else if (!factura.getUsuario().getId().equals(usuarioActual.getId())) {
                System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioActual.getEmail() +
                    " intentó acceder a factura de usuario " + factura.getUsuario().getEmail());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta factura");
            }

            // Buscar dirección en Miami del usuario (si existe)
            String direccionMiami = "N/A";
            String locker = "N/A";
            if (factura.getUsuario() != null) {
                var direccionOpt = direccionRepository.findByUsuarioId(factura.getUsuario().getId())
                        .stream()
                        .findFirst();
                if (direccionOpt.isPresent()) {
                    Direccion dir = direccionOpt.get();
                    direccionMiami = dir.getCallePrincipal() + ", " + dir.getCiudad();
                    locker = dir.getAlias();
                }
            }

            // Calcular totales
            DecimalFormat df = new DecimalFormat("#.00");
            double subtotal = factura.getMonto();
            double impuestos = subtotal * 0.20;
            double total = subtotal + impuestos;

            // Crear items
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("descripcion", factura.getDescripcion() != null ? factura.getDescripcion() : "Servicio de importación");
            item.put("peso", factura.getEnvio() != null && factura.getEnvio().getPesoLibras() != null ? factura.getEnvio().getPesoLibras() : 0.0);
            item.put("precioUnitario", 15.00);
            item.put("total", factura.getMonto());
            items.add(item);

            // Preparar datos para la plantilla
            Map<String, Object> datos = new HashMap<>();
            datos.put("numeroFactura", "FCT-" + String.format("%05d", id));
            datos.put("clienteNombre", factura.getUsuario() != null ? factura.getUsuario().getNombre() : "N/A");
            datos.put("direccionMiami", direccionMiami);
            datos.put("locker", locker);
            datos.put("fechaEmision", factura.getFechaEmision().toString());
            datos.put("items", items);
            datos.put("subtotal", subtotal);
            datos.put("impuestos", impuestos);
            datos.put("total", total);
            datos.put("estado", factura.getEstado());

            System.out.println("✅ [FacturaController] Datos preparados. Total: $" + df.format(total));

            byte[] pdfBytes = pdfService.generarPdf("factura-importacion", datos);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"factura-" + id + ".pdf\"");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.out.println("❌ Error al generar PDF: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/facturas/{id}
     * Obtener una factura por ID
     * 🔒 SEGURIDAD: Requiere JWT válido + Verifica propiedad (IDOR)
     */
    @GetMapping("/{id}")
    public ResponseEntity<Factura> obtenerPorId(@PathVariable Long id) {
        System.out.println("🔍 [GET /api/facturas/" + id + "] PETICIÓN RECIBIDA");
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        
        Optional<Factura> facturaOpt = facturaService.obtenerPorId(id);
        if (!facturaOpt.isPresent()) {
            System.out.println("❌ Factura no encontrada");
            return ResponseEntity.notFound().build();
        }
        
        Factura factura = facturaOpt.get();
        String rol = usuarioActual.getRol().toUpperCase();
        
        // ADMIN y OPERADOR tienen acceso total
        if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
            System.out.println("✅ Acceso autorizado: Usuario " + rol);
            return ResponseEntity.ok(factura);
        }
        
        // CLIENTE: Solo puede ver sus propias facturas
        if (!factura.getUsuario().getId().equals(usuarioActual.getId())) {
            System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioActual.getId() + " intentó acceder a factura de usuario " + factura.getUsuario().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta factura");
        }
        
        System.out.println("✅ Acceso autorizado: Factura pertenece al cliente");
        System.out.println("✅ Factura encontrada: " + factura.getNumeroFactura());
        return ResponseEntity.ok(factura);
    }

    /**
     * POST /api/facturas
     * Crear una nueva factura
     * 🔒 SEGURIDAD: Requiere JWT válido (solo ADMIN/OPERADOR)
     */
    @PostMapping
    public ResponseEntity<Factura> crearFactura(@RequestBody Factura factura) {
        System.out.println("✍️ [POST /api/facturas] PETICIÓN RECIBIDA - Factura: " + factura.getNumeroFactura());
        
        try {
            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();

            // Asignar siempre el usuario autenticado para evitar facturas sin propietario
            factura.setUsuario(usuarioActual);
            
            Factura nueva = facturaService.crearFactura(factura);
            System.out.println("✅ Factura creada con ID: " + nueva.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /api/facturas/{id}/estado
     * Actualizar estado de una factura
     * 🔒 SEGURIDAD: Requiere JWT válido
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Factura> actualizarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {
        System.out.println("🔄 [PUT /api/facturas/" + id + "/estado] Estado: " + nuevoEstado);
        
        try {
            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            Factura actualizada = facturaService.actualizarEstado(id, nuevoEstado);
            System.out.println("✅ Estado actualizado");
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * DELETE /api/facturas/{id}
     * Eliminar una factura
     * 🔒 SEGURIDAD: Requiere JWT válido
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarFactura(@PathVariable Long id) {
        System.out.println("🗑️ [DELETE /api/facturas/" + id + "] PETICIÓN RECIBIDA");
        
        try {
            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            facturaService.eliminarFactura(id);
            System.out.println("✅ Factura eliminada");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
