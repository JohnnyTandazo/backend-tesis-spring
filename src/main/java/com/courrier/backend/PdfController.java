package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.DecimalFormat;
import java.util.*;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Endpoint: GET /api/pdf/guia/{envioId}
     * Genera PDF de Guía de Remisión para un envío nacional
     * SEGURIDAD: Verifica que el usuario tenga permiso para ver este PDF
     */
    @GetMapping("/guia/{envioId}")
    public ResponseEntity<byte[]> generarGuiaRemision(
            @PathVariable Long envioId,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioActualId,
            @RequestParam(value = "usuarioActualId", required = false) Long usuarioActualIdParam) {
        try {
            // Priorizar header, luego query param
            Long usuarioId = usuarioActualId != null ? usuarioActualId : usuarioActualIdParam;
            
            System.out.println("📄 [PdfController] Generando Guía de Remisión para envioId: " + envioId + " - Usuario autenticado: " + usuarioId);

            // Buscar el envío
            Envio envio = envioRepository.findById(envioId).orElse(null);
            if (envio == null) {
                System.out.println("❌ [PdfController] Envío no encontrado: " + envioId);
                return ResponseEntity.notFound().build();
            }

            // 🔒 VERIFICACIÓN IDOR: Comprobar propiedad del recurso antes de generar PDF
            if (usuarioId != null) {
                Usuario usuarioActual = usuarioRepository.findById(usuarioId).orElse(null);
                
                if (usuarioActual != null) {
                    String rol = usuarioActual.getRol().toUpperCase();
                    
                    // ADMIN y OPERADOR tienen acceso total
                    if (!rol.equals("ADMIN") && !rol.equals("OPERADOR")) {
                        // CLIENTE: Solo puede ver PDFs de sus propios envíos
                        if (!envio.getUsuario().getId().equals(usuarioActual.getId())) {
                            System.out.println("🚫 ACCESO DENEGADO AL PDF: Cliente " + usuarioId + " intentó descargar guía de envío de usuario " + envio.getUsuario().getId());
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "⛔ ACCESO DENEGADO: No eres el dueño de este documento.");
                        }
                        System.out.println("✅ Acceso autorizado: Envío pertenece al cliente");
                    } else {
                        System.out.println("✅ Acceso autorizado: Usuario " + rol);
                    }
                }
            }

            // Preparar datos para la plantilla
            Map<String, Object> datos = new HashMap<>();
            datos.put("numeroTracking", envio.getNumeroTracking());
            datos.put("usuario", envio.getUsuario() != null ? envio.getUsuario().getNombre() : "N/A");
            datos.put("telefono", envio.getUsuario() != null ? envio.getUsuario().getTelefono() : "N/A");
            datos.put("fechaCreacion", envio.getFechaCreacion().toString());
            datos.put("destinatarioNombre", envio.getDestinatarioNombre());
            datos.put("destinatarioCiudad", envio.getDestinatarioCiudad());
            datos.put("destinatarioDireccion", envio.getDestinatarioDireccion());
            datos.put("destinatarioTelefono", envio.getDestinatarioTelefono());
            datos.put("descripcion", envio.getDescripcion() != null ? envio.getDescripcion() : "Sin descripción");
            datos.put("pesoLibras", envio.getPesoLibras() != null ? envio.getPesoLibras() : 0.0);
            datos.put("categoria", envio.getCategoria() != null ? envio.getCategoria() : "N/A");
            datos.put("estado", envio.getEstado());

            System.out.println("✅ [PdfController] Datos preparados. Generando PDF...");

            // Generar PDF
            byte[] pdfBytes = pdfService.generarPdf("guia-remision", datos);

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"guia-remision-" + envio.getNumeroTracking() + ".pdf\"");
            headers.setContentLength(pdfBytes.length);

            System.out.println("🎉 [PdfController] PDF generado correctamente (" + pdfBytes.length + " bytes)");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (ResponseStatusException e) {
            throw e; // Re-lanzar excepciones de seguridad
        } catch (Exception e) {
            System.err.println("💥 [PdfController] Error al generar Guía de Remisión: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint: GET /api/pdf/factura/{facturaId}
     * Genera PDF de Factura de Importación desde USA
     * SEGURIDAD: Verifica que el usuario tenga permiso para ver este PDF
     */
    @GetMapping("/factura/{facturaId}")
    public ResponseEntity<byte[]> generarFacturaImportacion(
            @PathVariable Long facturaId,
            @RequestHeader(value = "X-Usuario-Id", required = false) Long usuarioActualId,
            @RequestParam(value = "usuarioActualId", required = false) Long usuarioActualIdParam) {
        try {
            // Priorizar header, luego query param
            Long usuarioId = usuarioActualId != null ? usuarioActualId : usuarioActualIdParam;
            
            System.out.println("📄 [PdfController] Generando Factura de Importación para facturaId: " + facturaId + " - Usuario autenticado: " + usuarioId);

            // Buscar la factura
            Factura factura = facturaRepository.findById(facturaId).orElse(null);
            if (factura == null) {
                System.out.println("❌ [PdfController] Factura no encontrada: " + facturaId);
                return ResponseEntity.notFound().build();
            }

            // 🔒 VERIFICACIÓN IDOR: Comprobar propiedad del recurso antes de generar PDF
            if (usuarioId != null) {
                Usuario usuarioActual = usuarioRepository.findById(usuarioId).orElse(null);
                
                if (usuarioActual != null) {
                    String rol = usuarioActual.getRol().toUpperCase();
                    
                    // ADMIN y OPERADOR tienen acceso total
                    if (!rol.equals("ADMIN") && !rol.equals("OPERADOR")) {
                        // CLIENTE: Solo puede ver PDFs de sus propias facturas
                        if (!factura.getUsuario().getId().equals(usuarioActual.getId())) {
                            System.out.println("🚫 ACCESO DENEGADO AL PDF: Cliente " + usuarioId + " intentó descargar factura de usuario " + factura.getUsuario().getId());
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "⛔ ACCESO DENEGADO: No eres el dueño de este documento.");
                        }
                        System.out.println("✅ Acceso autorizado: Factura pertenece al cliente");
                    } else {
                        System.out.println("✅ Acceso autorizado: Usuario " + rol);
                    }
                }
            }

            // Buscar dirección en Miami del usuario
            String direccionMiami = "N/A";
            String locker = "N/A";
            if (factura.getUsuario() != null) {
                Optional<Direccion> direccionOpt = direccionRepository.findByUsuarioId(factura.getUsuario().getId())
                        .stream()
                        .findFirst();
                
                if (direccionOpt.isPresent()) {
                    Direccion dir = direccionOpt.get();
                    direccionMiami = dir.getCallePrincipal() + ", " + dir.getCiudad();
                    locker = dir.getAlias(); // Usamos alias como locker
                }
            }

            // Calcular totales
            DecimalFormat df = new DecimalFormat("#.00");
            double subtotal = factura.getMonto();
            double impuestos = subtotal * 0.20; // 20% de aranceles
            double total = subtotal + impuestos;

            // Crear items (simulado - en producción vendría de una tabla relacionada)
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            item.put("descripcion", factura.getDescripcion() != null ? factura.getDescripcion() : "Servicio de importación");
            item.put("peso", factura.getEnvio() != null && factura.getEnvio().getPesoLibras() != null ? factura.getEnvio().getPesoLibras() : 0.0);
            item.put("precioUnitario", 15.00); // Precio fijo por defecto
            item.put("total", factura.getMonto());
            items.add(item);

            // Preparar datos para la plantilla
            Map<String, Object> datos = new HashMap<>();
            datos.put("numeroFactura", "FCT-" + String.format("%05d", facturaId));
            datos.put("clienteNombre", factura.getUsuario() != null ? factura.getUsuario().getNombre() : "N/A");
            datos.put("direccionMiami", direccionMiami);
            datos.put("locker", locker);
            datos.put("fechaEmision", factura.getFechaEmision().toString());
            datos.put("items", items);
            datos.put("subtotal", subtotal);
            datos.put("impuestos", impuestos);
            datos.put("total", total);
            datos.put("estado", factura.getEstado());

            System.out.println("✅ [PdfController] Datos preparados. Total: $" + df.format(total));

            // Generar PDF
            byte[] pdfBytes = pdfService.generarPdf("factura-importacion", datos);

            // Configurar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"factura-" + facturaId + ".pdf\"");
            headers.setContentLength(pdfBytes.length);

            System.out.println("🎉 [PdfController] PDF generado correctamente (" + pdfBytes.length + " bytes)");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (ResponseStatusException e) {
            throw e; // Re-lanzar excepciones de seguridad
        } catch (Exception e) {
            System.err.println("💥 [PdfController] Error al generar Factura de Importación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
