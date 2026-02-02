package com.courrier.backend;

import com.lowagie.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Servicio para generar PDFs desde plantillas HTML usando Thymeleaf + Flying Saucer
 */
@Service
public class PdfService {

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * Genera un PDF a partir de una plantilla Thymeleaf
     *
     * @param templateName Nombre del archivo HTML en src/main/resources/templates/ (sin extensión)
     * @param datos        Mapa con variables para inyectar en la plantilla
     * @return byte[] con el contenido del PDF generado
     * @throws IOException         Si hay error al procesar el HTML
     * @throws DocumentException   Si hay error al generar el PDF
     */
    public byte[] generarPdf(String templateName, Map<String, Object> datos) throws IOException, DocumentException {
        System.out.println("📄 [PdfService] Generando PDF desde plantilla: " + templateName);
        System.out.println("   Variables: " + datos.keySet());

        // PASO 1: Renderizar la plantilla Thymeleaf a HTML
        Context context = new Context();
        context.setVariables(datos);
        
        String htmlContent = templateEngine.process(templateName, context);
        System.out.println("   ✓ HTML generado (" + htmlContent.length() + " caracteres)");

        // PASO 2: Convertir HTML a PDF usando Flying Saucer
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            
            byte[] pdfBytes = outputStream.toByteArray();
            System.out.println("   ✓ PDF generado (" + pdfBytes.length + " bytes)");
            
            return pdfBytes;
        } finally {
            outputStream.close();
        }
    }
}
