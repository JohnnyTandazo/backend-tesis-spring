package com.courrier.backend;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/paquetes")
public class PaqueteController {

    @Autowired
    private PaqueteRepository paqueteRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private FacturaRepository facturaRepo;

    /**
     * GET /api/paquetes/todos
     * Obtener TODOS los paquetes (ADMIN ENDPOINT)
     * Para la Torre de Control - ordenados por fecha
     */
    @GetMapping("/todos")
    public List<Paquete> obtenerTodos() {
        System.out.println("📦 [GET /api/paquetes/todos] ADMIN - Obteniendo TODOS los paquetes...");
        
        try {
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
    @GetMapping
    public List<Paquete> listarPaquetes(@RequestParam(value = "usuarioId", required = false) Long usuarioId) {
        System.out.println("📦 [GET /api/paquetes] Listando paquetes...");
        if (usuarioId != null) {
            System.out.println("   🔎 Filtrando por usuarioId: " + usuarioId);
            return paqueteRepo.findByUsuarioId(usuarioId);
        } else {
            return paqueteRepo.findAll();
        }
    }

    // 2. Registrar un Paquete nuevo (Pre-Alerta)
    @PostMapping
    public Paquete crearPaquete(@RequestBody Map<String, Object> payload) {
        System.out.println("📝 [POST /api/paquetes] ✅ PETICIÓN RECIBIDA - Creando nuevo paquete...");
        System.out.println("   Datos recibidos: " + payload);
        
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
        
        // 2. PRECIO: Captura el valor declarado.
        if (payload.get("precio") != null) {
            p.setPrecio(Double.valueOf(payload.get("precio").toString()));
        } else {
            p.setPrecio(0.0);
        }
        
        // 3. PESO: Se mantiene en 0.0 (Lo pondrá el Operador al pesar la caja en Miami).
        p.setPesoLibras(0.0);
        
        // 4. ESTADO: Inicia como PRE_ALERTADO
        p.setEstado("PRE_ALERTADO");
        
        // 5. USUARIO: Asignación normal
        Object userIdObj = payload.get("usuarioId");
        Long usuarioId = userIdObj != null ? Long.valueOf(userIdObj.toString()) : 1L;
        Usuario u = usuarioRepo.findById(usuarioId).orElseThrow();
        p.setUsuario(u);
        
        Paquete paqueteGuardado = paqueteRepo.save(p);
        System.out.println("✅ Paquete guardado exitosamente: ID=" + paqueteGuardado.getId() + ", Tracking=" + paqueteGuardado.getTrackingNumber());
        
        return paqueteGuardado;
    }
    
    // 3. Buscar por Tracking (Para la barra de búsqueda del Home)
    @GetMapping("/rastreo/{tracking}")
    public Paquete buscarPorTracking(@PathVariable String tracking) {
        System.out.println("🔍 [GET /api/paquetes/rastreo/" + tracking + "] Buscando paquete por tracking...");
        return paqueteRepo.findByTrackingNumber(tracking);
    }

    // 3b. Buscar por código/tracking (Endpoint alternativo que espera el Frontend)
    @GetMapping("/track/{codigo}")
    public Paquete buscarPorCodigo(@PathVariable String codigo) {
        System.out.println("🔍 [GET /api/paquetes/track/" + codigo + "] ✅ PETICIÓN RECIBIDA - Buscando paquete por código: " + codigo);
        Paquete paquete = paqueteRepo.findByTrackingNumber(codigo);
        if (paquete != null) {
            System.out.println("✅ Paquete encontrado: " + paquete.getTrackingNumber());
        } else {
            System.out.println("❌ Paquete NO encontrado para el código: " + codigo);
        }
        return paquete;
    }

    // 4. Actualizar detalles del paquete (Para el Operador)
    @PutMapping("/{id}/detalles")
    public Paquete actualizarDetallesPaquete(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        System.out.println("✏️ [PUT /api/paquetes/" + id + "/detalles] ✅ PETICIÓN RECIBIDA - Actualizando paquete...");
        System.out.println("   Datos a actualizar: " + payload);
        
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
            
            Double tarifaBase = 10.00;      // Tarifa base por envío
            Double costoPorLibra = 2.50;    // $2.50 por libra
            Double precioEnvio = tarifaBase + (paquete.getPesoLibras() * costoPorLibra);
            
            paquete.setPrecio(precioEnvio);
            
            System.out.println("      • Tarifa Base: $" + tarifaBase);
            System.out.println("      • Peso: " + paquete.getPesoLibras() + " lbs");
            System.out.println("      • Costo por Libra: $" + costoPorLibra);
            System.out.println("      • TOTAL CALCULADO: $" + String.format("%.2f", precioEnvio));
            System.out.println("   ✅ Precio AUTO-CALCULADO: $" + String.format("%.2f", precioEnvio));
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
        
        if (paqueteActualizado.getPrecio() != null && paqueteActualizado.getPrecio() > 0) {
            System.out.println("   ✓ Precio detectado: $" + paqueteActualizado.getPrecio());
            
            // Verificar si ya existe factura para este paquete (evitar duplicados)
            String descripcionBusqueda = "Importación " + paqueteActualizado.getTrackingNumber();
            List<Factura> facturasExistentes = facturaRepo.findAll().stream()
                .filter(f -> descripcionBusqueda.equals(f.getDescripcion()))
                .toList();
            
            if (facturasExistentes.isEmpty()) {
                System.out.println("   ℹ️ No existe factura previa para este paquete");
                System.out.println("   🔄 Creando factura automática...");
                
                // ════════════════════════════════════════════════════════════
                // ⚠️ CRÍTICO: La factura debe cobrar el COSTO DEL FLETE,
                //            NO el valor declarado del producto
                // ════════════════════════════════════════════════════════════
                // CORRECTO: paquete.getPrecio() → Costo del servicio de courier
                // INCORRECTO: paquete.getValorDeclarado() → Valor del producto
                
                // Crear nueva factura
                Factura factura = new Factura();
                factura.setMonto(paqueteActualizado.getPrecio());  // ← COSTO DEL FLETE, NO DEL PRODUCTO
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
                System.out.println("      • Monto: $" + facturaGuardada.getMonto());
                System.out.println("      • Usuario: " + facturaGuardada.getUsuario().getNombre());
                System.out.println("      • Estado: " + facturaGuardada.getEstado());
                System.out.println("   💰 FACTURA AUTO-GENERADA: $" + facturaGuardada.getMonto());
            } else {
                System.out.println("   ⚠️ Ya existe factura para este paquete (ID: " + 
                                 facturasExistentes.get(0).getId() + ") - Se omite creación");
            }
        } else {
            System.out.println("   ℹ️ Precio no asignado o es $0.00 - No se genera factura");
        }
        System.out.println("════════════════════════════════════════════════════════════\n");
        
        return paqueteActualizado;
    }
}