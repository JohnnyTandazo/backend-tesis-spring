package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpSession;

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

    @Autowired
    private AuthService authService;

    /**
     * Endpoint: GET /api/pdf/guia/{envioId}
     * Genera PDF de Guía de Remisión para un envío nacional
     * SEGURIDAD: Obtiene usuario del contexto de sesión (NO de parámetros)
     */
    @GetMapping("/guia/{envioId}")
    public ResponseEntity<byte[]> generarGuiaRemision(
            @PathVariable Long envioId,
            HttpSession session) {
        try {
            System.out.println("📄 [PdfController] Generando Guía de Remisión para envioId: " + envioId);

            // 🔒 SEGURIDAD: Obtener usuario desde sesión HTTP (NO de parámetros)
            Usuario usuarioActual = authService.obtenerUsuarioAutenticadoOThrow(session);
            System.out.println("   Usuario autenticado: " + usuarioActual.getEmail() + " (ID: " + usuarioActual.getId() + ")");

            // Buscar el envío
            Envio envio = envioRepository.findById(envioId).orElse(null);
            if (envio == null) {
                System.out.println("❌ [PdfController] Envío no encontrado: " + envioId);
                return ResponseEntity.notFound().build();
            }

            // 🔒 VERIFICACIÓN IDOR: Verificar propiedad del recurso
            if (!authService.tieneAcceso(usuarioActual, envio.getUsuario())) {
                System.out.println("🚫 ACCESO DENEGADO AL PDF: Usuario " + usuarioActual.getEmail() + 
                    " intentó descargar guía de envío de usuario " + envio.getUsuario().getEmail());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "⛔ ACCESO DENEGADO: No eres el dueño de este documento.");
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
        } catch (RuntimeException e) {
            System.out.println("❌ [PdfController] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            System.err.println("💥 [PdfController] Error al generar Guía de Remisión: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint: GET /api/pdf/factura/{facturaId}
     * Genera PDF de Factura de Importación desde USA
     * SEGURIDAD: Obtiene usuario del contexto de sesión (NO de parámetros)
     */
    @GetMapping("/factura/{facturaId}")
    public ResponseEntity<byte[]> generarFacturaImportacion(
            @PathVariable Long facturaId,
            HttpSession session) {
        try {
            System.out.println("📄 [PdfController] Generando Factura de Importación para facturaId: " + facturaId);

            // 🔒 SEGURIDAD: Obtener usuario desde sesión HTTP (NO de parámetros)
            Usuario usuarioActual = authService.obtenerUsuarioAutenticadoOThrow(session);
            System.out.println("   Usuario autenticado: " + usuarioActual.getEmail() + " (ID: " + usuarioActual.getId() + ")");

            // Buscar la factura
            Factura factura = facturaRepository.findById(facturaId).orElse(null);
            if (factura == null) {
                System.out.println("❌ [PdfController] Factura no encontrada: " + facturaId);
                return ResponseEntity.notFound().build();
            }

            // 🔒 VERIFICACIÓN IDOR: Verificar propiedad del recurso
            if (!authService.tieneAcceso(usuarioActual, factura.getUsuario())) {
                System.out.println("🚫 ACCESO DENEGADO AL PDF: Usuario " + usuarioActual.getEmail() + 
                    " intentó descargar factura de usuario " + factura.getUsuario().getEmail());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "⛔ ACCESO DENEGADO: No eres el dueño de este documento.");
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
        } catch (RuntimeException e) {
            System.out.println("❌ [PdfController] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            System.err.println("💥 [PdfController] Error al generar Factura de Importación: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
