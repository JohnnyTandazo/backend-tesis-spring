package com.courrier.backend;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/paquetes")
public class PaqueteController extends BaseSecurityController {

    @Autowired
    private PaqueteRepository paqueteRepo;

    @Autowired
    private FacturaRepository facturaRepo;

    /**
     * GET /api/paquetes/todos
     * Obtener TODOS los paquetes (ADMIN ENDPOINT)
     * 🔒 SEGURIDAD: Requiere JWT válido
     */
    @GetMapping("/todos")
    public List<Paquete> obtenerTodos() {
        System.out.println("📦 [GET /api/paquetes/todos] ADMIN - Obteniendo TODOS los paquetes...");
        
        try {
            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            List<Paquete> todos = paqueteRepo.findAll();
            System.out.println("✅ Se encontraron " + todos.size() + " paquetes en total");
            
            if (!todos.isEmpty()) {
                System.out.println("   📊 Desglose por estado:");
                todos.stream()
                    .collect(java.util.stream.Collectors.groupingBy(Paquete::getEstado, java.util.stream.Collectors.counting()))
                    .forEach((estado, cantidad) -> System.out.println("     → " + estado + ": " + cantidad));
            }
            
            return todos;
        } catch (Exception e) {
            System.out.println("❌ Error obteniendo paquetes: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    // 1. Ver TODOS los paquetes (Para el Admin u Operador)
    // 🔒 SEGURIDAD: Requiere JWT válido
    @GetMapping
    public List<Paquete> listarPaquetes() {
        System.out.println("📦 [GET /api/paquetes] Listando paquetes...");
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        
        // Filtrar por usuario si no es ADMIN/OPERADOR
        String rol = usuarioActual.getRol().toUpperCase();
        if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
            return paqueteRepo.findAll();
        } else {
            // CLIENTE: Solo ver sus propios paquetes
            return paqueteRepo.findByUsuarioId(usuarioActual.getId());
        }
    }

    // 2. Registrar un Paquete nuevo (Pre-Alerta)
    // 🔒 SEGURIDAD: Requiere JWT válido
    @PostMapping
    public Paquete crearPaquete(@RequestBody Map<String, Object> payload) {
        System.out.println("📝 [POST /api/paquetes] ✅ PETICIÓN RECIBIDA - Creando nuevo paquete...");
        System.out.println("   Datos recibidos: " + payload);
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        
        Paquete p = new Paquete();
        
        p.setTrackingNumber((String) payload.get("trackingNumber"));
        System.out.println("   📦 Tracking: " + p.getTrackingNumber());
        
        // 1. DESCRIPCIÓN: Si el usuario escribió una descripción específica, úsala.
        // Si no, usa el formato "Compra en [Tienda]" como respaldo.
        String descUsuario = (String) payload.get("descripcion");
        String tienda = (String) payload.get("storeName");
        
        if (descUsuario != null && !descUsuario.isEmpty()) {
            p.setDescripcion(descUsuario);
        } else {
            p.setDescripcion("Compra en " + (tienda != null ? tienda : "General"));
        }
        
        // 2. PRECIO: Pre-alerta NO debe cobrar. Precio inicial = 0.0
        // Si el frontend envía un valor declarado, solo lo registramos en logs.
        if (payload.get("precio") != null) {
            System.out.println("   ⚠️ Precio declarado recibido en pre-alerta: " + payload.get("precio") + " (no se cobra aún)");
        }
        p.setPrecio(0.0);
        
        // 2b. TIPO DE ENVÍO: Nacional vs Internacional
        String origen = payload.get("origen") != null ? payload.get("origen").toString() : null;
        String tipoEnvioPayload = payload.get("tipoEnvio") != null ? payload.get("tipoEnvio").toString() : null;
        String indicador = (origen != null ? origen : tipoEnvioPayload);
        if (indicador != null) {
            String valor = indicador.trim().toUpperCase();
            if (valor.equals("LOCAL") || valor.equals("NACIONAL")) {
                p.setTipoEnvio(Paquete.TipoEnvio.NACIONAL);
            } else {
                p.setTipoEnvio(Paquete.TipoEnvio.INTERNACIONAL);
            }
        } else {
            // Por defecto, mantener INTERNACIONAL para importaciones
            p.setTipoEnvio(Paquete.TipoEnvio.INTERNACIONAL);
        }
        
        // 3. PESO: Se mantiene en 0.0 (Lo pondrá el Operador al pesar la caja en Miami).
        p.setPesoLibras(0.0);
        
        // 4. ESTADO: Inicia como PRE_ALERTADO
        p.setEstado("PRE_ALERTADO");
        
        // 5. USUARIO: Usa el usuario desde JWT
        p.setUsuario(usuarioActual);
        
        Paquete paqueteGuardado = paqueteRepo.save(p);
        System.out.println("✅ Paquete guardado exitosamente: ID=" + paqueteGuardado.getId() + ", Tracking=" + paqueteGuardado.getTrackingNumber());
        
        return paqueteGuardado;
    }
    
    // 3. Buscar por Tracking (Para la barra de búsqueda del Home)
    // 🔒 SEGURIDAD: Requiere JWT válido + Verifica propiedad (IDOR)
    @GetMapping("/rastreo/{tracking}")
    public Paquete buscarPorTracking(@PathVariable String tracking) {
        System.out.println("🔍 [GET /api/paquetes/rastreo/" + tracking + "] Buscando paquete por tracking");
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        
        Paquete paquete = paqueteRepo.findByTrackingNumber(tracking);
        
        if (paquete == null) {
            return null;
        }
        
        String rol = usuarioActual.getRol().toUpperCase();
        
        // ADMIN y OPERADOR tienen acceso total
        if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
            System.out.println("✅ Acceso autorizado: Usuario " + rol);
            return paquete;
        }
        
        // CLIENTE: Solo puede ver sus propios paquetes
        if (!paquete.getUsuario().getId().equals(usuarioActual.getId())) {
            System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioActual.getId() + " intentó rastrear paquete de usuario " + paquete.getUsuario().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para rastrear este paquete");
        }
        
        System.out.println("✅ Acceso autorizado: Paquete pertenece al cliente");
        return paquete;
    }

    // 3b. Buscar por código/tracking (Endpoint alternativo que espera el Frontend)
    // 🔒 SEGURIDAD: Requiere JWT válido + Verifica propiedad (IDOR)
    @GetMapping("/track/{codigo}")
    public Paquete buscarPorCodigo(@PathVariable String codigo) {
        System.out.println("🔍 [GET /api/paquetes/track/" + codigo + "] ✅ PETICIÓN RECIBIDA - Buscando paquete por código: " + codigo);
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        
        Paquete paquete = paqueteRepo.findByTrackingNumber(codigo);
        
        if (paquete == null) {
            System.out.println("❌ Paquete NO encontrado para el código: " + codigo);
            return null;
        }
        
        String rol = usuarioActual.getRol().toUpperCase();
        
        // ADMIN y OPERADOR tienen acceso total
        if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
            System.out.println("✅ Acceso autorizado: Usuario " + rol);
            System.out.println("✅ Paquete encontrado: " + paquete.getTrackingNumber());
            return paquete;
        }
        
        // CLIENTE: Solo puede ver sus propios paquetes
        if (!paquete.getUsuario().getId().equals(usuarioActual.getId())) {
            System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioActual.getId() + " intentó rastrear paquete de usuario " + paquete.getUsuario().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para rastrear este paquete");
        }
        
        System.out.println("✅ Acceso autorizado: Paquete pertenece al cliente");
        System.out.println("✅ Paquete encontrado: " + paquete.getTrackingNumber());
        return paquete;
    }

    // 4. Actualizar detalles del paquete (Para el Operador)
    // 🔒 SEGURIDAD: Requiere JWT válido
    @PutMapping("/{id}/detalles")
    public Paquete actualizarDetallesPaquete(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        System.out.println("✏️ [PUT /api/paquetes/" + id + "/detalles] ✅ PETICIÓN RECIBIDA - Actualizando paquete...");
        System.out.println("   Datos a actualizar: " + payload);
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        
        Paquete paquete = paqueteRepo.findById(id).orElseThrow();

        // 1. Actualizar Estado (si viene)
        if (payload.get("estado") != null) {
            paquete.setEstado((String) payload.get("estado"));
            System.out.println("   Estado actualizado a: " + paquete.getEstado());
        }

        // 2. Actualizar Peso (Vital para el operador)
        if (payload.get("pesoLibras") != null) {
            paquete.setPesoLibras(Double.valueOf(payload.get("pesoLibras").toString()));
            System.out.println("   Peso actualizado a: " + paquete.getPesoLibras() + " libras");
        }

        // 3. Actualizar Precio/Valor (Manual o Auto-calculado)
        if (payload.get("precio") != null) {
            // CASO 1: Operador envió un precio MANUAL
            paquete.setPrecio(Double.valueOf(payload.get("precio").toString()));
            System.out.println("   💵 Precio MANUAL asignado: $" + paquete.getPrecio());
        } else if (paquete.getPesoLibras() != null && paquete.getPesoLibras() > 0) {
            // CASO 2: AUTO-CÁLCULO basado en PESO (Tarifa de flete)
            System.out.println("   🧮 [CALCULADORA AUTOMÁTICA] Calculando precio de flete...");
            
            BigDecimal tarifaBase = new BigDecimal("5.00");
            BigDecimal costoPorLibra = new BigDecimal("5.00");
            if (paquete.getTipoEnvio() == Paquete.TipoEnvio.NACIONAL) {
                costoPorLibra = new BigDecimal("2.00");
            }
            BigDecimal peso = BigDecimal.valueOf(paquete.getPesoLibras());
            BigDecimal flete = peso.multiply(costoPorLibra).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalCalculado = flete.add(tarifaBase);
            
            System.out.println("      • Tarifa Base: $" + tarifaBase);
            System.out.println("      • Peso: " + paquete.getPesoLibras() + " lbs");
            System.out.println("      • Costo por Libra: $" + costoPorLibra);
            System.out.println("      • Flete: $" + flete);
            System.out.println("      • TOTAL CALCULADO (sin seguro): $" + totalCalculado);
            System.out.println("   ✅ Flete AUTO-CALCULADO (no se guarda como valor declarado)");
        }

        // 4. Actualizar Categoría (A, B, C, etc.)
        if (payload.get("categoria") != null) {
            paquete.setCategoria((String) payload.get("categoria"));
            System.out.println("   Categoría actualizada a: " + paquete.getCategoria());
        }

        Paquete paqueteActualizado = paqueteRepo.save(paquete);
        System.out.println("✅ Paquete actualizado exitosamente: ID=" + id);
        
        // ════════════════════════════════════════════════════════════
        // 🎯 AUTO-FACTURACIÓN: Generación automática de factura
        // ════════════════════════════════════════════════════════════
        System.out.println("\n📋 [AUTO-FACTURACIÓN] Verificando si se debe generar factura...");
        
        // ⚠️ REGLA DE ORO: SIEMPRE calcular el costo basado en PESO, NUNCA sumar valorDeclarado
        if (paqueteActualizado.getPesoLibras() != null && paqueteActualizado.getPesoLibras() > 0) {
            
            // ════════════════════════════════════════════════════════════
            // 🧮 CÁLCULO OBLIGATORIO DEL COSTO DE ENVÍO
            // ════════════════════════════════════════════════════════════
            BigDecimal tarifaBase = new BigDecimal("5.00");
            BigDecimal costoPorLibra = new BigDecimal("5.00");
            if (paqueteActualizado.getTipoEnvio() == Paquete.TipoEnvio.NACIONAL) {
                costoPorLibra = new BigDecimal("2.00");
            }
            BigDecimal peso = BigDecimal.valueOf(paqueteActualizado.getPesoLibras());
            BigDecimal flete = peso.multiply(costoPorLibra).setScale(2, RoundingMode.HALF_UP);

            BigDecimal valorDeclarado = paqueteActualizado.getPrecio() != null
                ? BigDecimal.valueOf(paqueteActualizado.getPrecio())
                : BigDecimal.ZERO;
            BigDecimal seguro = BigDecimal.ZERO;
            if (valorDeclarado.compareTo(new BigDecimal("100")) > 0) {
                seguro = valorDeclarado.multiply(new BigDecimal("0.02")).setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal totalCalculado = flete.add(tarifaBase).add(seguro).setScale(2, RoundingMode.HALF_UP);

            System.out.println("\n   🧮 [CÁLCULO DE COSTO DE ENVÍO]");
            System.out.println("      • Tarifa Base: $" + tarifaBase);
            System.out.println("      • Peso: " + paqueteActualizado.getPesoLibras() + " lbs");
            System.out.println("      • Costo por Libra: $" + costoPorLibra);
            System.out.println("      • Flete: $" + flete);
            System.out.println("      • Seguro: $" + seguro + " (2% si valorDeclarado > $100)");
            System.out.println("      • TOTAL FINAL: $" + totalCalculado);
            System.out.println("      ⚠️ (NUNCA se suma valorDeclarado al total)\n");
            
            // Verificar si ya existe factura para este paquete
            String descripcionBusqueda = "Importación " + paqueteActualizado.getTrackingNumber();
            List<Factura> facturasExistentes = facturaRepo.findAll().stream()
                .filter(f -> descripcionBusqueda.equals(f.getDescripcion()))
                .toList();
            
            if (facturasExistentes.isEmpty()) {
                System.out.println("   ℹ️ No existe factura previa para este paquete");
                System.out.println("   🔄 Creando factura automática...");
                
                // ════════════════════════════════════════════════════════════
                // ✅ CREAR NUEVA FACTURA CON COSTO CALCULADO
                // ════════════════════════════════════════════════════════════
                Factura factura = new Factura();
                factura.setMonto(totalCalculado.doubleValue());  // ← Flete + Base + Seguro
                factura.setEstado("PENDIENTE");
                factura.setDescripcion("Importación " + paqueteActualizado.getTrackingNumber());
                factura.setUsuario(paqueteActualizado.getUsuario());
                factura.setEnvioId(null);  // Es importación, no envío nacional
                factura.setFechaEmision(LocalDateTime.now());
                factura.setFechaVencimiento(LocalDateTime.now().plusDays(15));
                factura.setNumeroFactura("FCT-PKG-" + String.format("%06d", paqueteActualizado.getId()));
                
                Factura facturaGuardada = facturaRepo.save(factura);
                
                System.out.println("   ✅ Factura generada automáticamente:");
                System.out.println("      • ID: " + facturaGuardada.getId());
                System.out.println("      • Número: " + facturaGuardada.getNumeroFactura());
                System.out.println("      • Monto: $" + String.format("%.2f", facturaGuardada.getMonto()));
                System.out.println("      • Usuario: " + facturaGuardada.getUsuario().getNombre());
                System.out.println("      • Estado: " + facturaGuardada.getEstado());
                System.out.println("   💰 FACTURA AUTO-GENERADA: $" + String.format("%.2f", facturaGuardada.getMonto()));
                
            } else {
                // ════════════════════════════════════════════════════════════
                // 🔧 CORRECCIÓN RETROACTIVA: Verificar si factura existente
                //    tiene el monto INCORRECTO (valorDeclarado en lugar de costo)
                // ════════════════════════════════════════════════════════════
                Factura facturaExistente = facturasExistentes.get(0);
                System.out.println("   ⚠️ Ya existe factura para este paquete (ID: " + facturaExistente.getId() + ")");
                System.out.println("      • Monto actual: $" + facturaExistente.getMonto());
                System.out.println("      • Costo correcto: $" + String.format("%.2f", totalCalculado));
                
                // Si el monto actual es diferente al costo calculado, corregirlo
                if (Math.abs(facturaExistente.getMonto() - totalCalculado.doubleValue()) > 0.01) {
                    System.out.println("      🔧 Detectado monto INCORRECTO - Corrigiendo...");
                    
                    facturaExistente.setMonto(totalCalculado.doubleValue());
                    Factura facturaCorregida = facturaRepo.save(facturaExistente);
                    
                    System.out.println("      ✅ Factura CORREGIDA:");
                    System.out.println("         • Nuevo monto: $" + String.format("%.2f", facturaCorregida.getMonto()));
                    System.out.println("      💰 FACTURA ACTUALIZADA: $" + String.format("%.2f", facturaCorregida.getMonto()));
                } else {
                    System.out.println("      ✅ Monto ya es correcto - No requiere cambios");
                }
            }
        } else {
            System.out.println("   ℹ️ Precio no asignado o es $0.00 - No se genera factura");
        }
        System.out.println("════════════════════════════════════════════════════════════\n");
        
        return paqueteActualizado;
    }
}