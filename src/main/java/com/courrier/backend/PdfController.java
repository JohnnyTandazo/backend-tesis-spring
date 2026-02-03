package com.courrier.backend;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.*;

@RestController
@RequestMapping("/api/pdf")
@CrossOrigin(origins = "*")
public class PdfController extends BaseSecurityController {

    @Autowired
    private PdfService pdfService;

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private DireccionRepository direccionRepository;

    /**
     * Endpoint: GET /api/pdf/guia/{envioId}
     * Genera PDF de Guía de Remisión para un envío nacional
     * 🔒 SEGURIDAD: Requiere JWT válido + Verifica propiedad (IDOR)
     */
    @GetMapping("/guia/{envioId}")
    public ResponseEntity<byte[]> generarGuiaRemision(@PathVariable Long envioId) {
        try {
            System.out.println("📄 [PdfController] Generando Guía de Remisión para envioId: " + envioId);

            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            // Buscar el envío
            Envio envio = envioRepository.findById(envioId).orElse(null);
            if (envio != null) {
                System.out.println("✅ Envío encontrado: " + envio.getNumeroTracking());
                System.out.println("✅ Propietario del envío: Usuario ID " + envio.getUsuario().getId());

                // 🔒 VERIFICACIÓN IDOR: Validar propiedad
                String rol = usuarioActual.getRol().toUpperCase();
                if (!rol.equals("ADMIN") && !rol.equals("OPERADOR") && 
                    !envio.getUsuario().getId().equals(usuarioActual.getId())) {
                    System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioActual.getId() + " intentó generar guía de envío de usuario " + envio.getUsuario().getId());
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para generar guía de este envío");
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
            }

            // Salvavidas: intentar por Paquete si no existe Envío con ese ID
            Paquete paquete = paqueteRepository.findById(envioId).orElse(null);
            if (paquete == null) {
                System.out.println("❌ [PdfController] Envío/Paquete no encontrado: " + envioId);
                return ResponseEntity.notFound().build();
            }

            String rol = usuarioActual.getRol().toUpperCase();
            if (!rol.equals("ADMIN") && !rol.equals("OPERADOR") && 
                paquete.getUsuario() != null && !paquete.getUsuario().getId().equals(usuarioActual.getId())) {
                System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioActual.getId() + " intentó generar guía de paquete de usuario " + paquete.getUsuario().getId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para generar guía de este paquete");
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                Document document = new Document();
                PdfWriter.getInstance(document, out);
                document.open();

                document.add(new Paragraph("GUÍA DE REMISIÓN - CURRIER TICS"));
                document.add(new Paragraph("------------------------------------------------"));
                document.add(new Paragraph("Tracking: " + paquete.getTrackingNumber()));
                document.add(new Paragraph("Descripción: " + (paquete.getDescripcion() != null ? paquete.getDescripcion() : "N/A")));
                document.add(new Paragraph("Categoría: " + (paquete.getCategoria() != null ? paquete.getCategoria() : "N/A")));
                document.add(new Paragraph("Estado: " + (paquete.getEstado() != null ? paquete.getEstado() : "N/A")));
                document.add(new Paragraph("Fecha: " + new Date()));

                document.close();

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=guia_" + envioId + ".pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(out.toByteArray());
            }

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            System.err.println("💥 [PdfController] Error al generar Guía de Remisión: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint: GET /api/pdf/factura/{facturaId}
     * Genera PDF de Factura de Importación desde USA
     * 🔒 SEGURIDAD: Requiere JWT válido + Verifica propiedad (IDOR)
     */
    @GetMapping("/factura/{facturaId}")
    public ResponseEntity<byte[]> generarFacturaImportacion(@PathVariable Long facturaId) {
        try {
            System.out.println("📄 [PdfController] Generando Factura de Importación para facturaId: " + facturaId);

            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            // Buscar la factura
            Factura factura = facturaRepository.findById(facturaId).orElse(null);
            if (factura == null) {
                System.out.println("❌ [PdfController] Factura no encontrada: " + facturaId);
                return ResponseEntity.notFound().build();
            }

            System.out.println("✅ Factura encontrada: " + factura.getNumeroFactura());
            System.out.println("✅ Propietario de factura: Usuario ID " + factura.getUsuario().getId());

            // 🔒 VERIFICACIÓN IDOR: Validar propiedad
            String rol = usuarioActual.getRol().toUpperCase();
            if (!rol.equals("ADMIN") && !rol.equals("OPERADOR") && 
                !factura.getUsuario().getId().equals(usuarioActual.getId())) {
                System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioActual.getId() + " intentó generar factura de usuario " + factura.getUsuario().getId());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para generar factura");
            }

            // Buscar dirección en Miami del usuario
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
