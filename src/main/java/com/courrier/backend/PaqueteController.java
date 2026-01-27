package com.courrier.backend;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/paquetes")
@CrossOrigin(origins = "https://v0-currier-tics-layout.vercel.app")
public class PaqueteController {

    @Autowired
    private PaqueteRepository paqueteRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    // 1. Ver TODOS los paquetes (Para el Admin u Operador)
    @GetMapping
    public List<Paquete> listarPaquetes() {
        return paqueteRepo.findAll();
    }

    // 2. Registrar un Paquete nuevo (Pre-Alerta)
    @PostMapping
    public Paquete crearPaquete(@RequestBody Map<String, Object> payload) {
        Paquete p = new Paquete();
        
        p.setTrackingNumber((String) payload.get("trackingNumber"));
        
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
        
        return paqueteRepo.save(p);
    }
    
    // 3. Buscar por Tracking (Para la barra de búsqueda del Home)
    @GetMapping("/rastreo/{tracking}")
    public Paquete buscarPorTracking(@PathVariable String tracking) {
        return paqueteRepo.findByTrackingNumber(tracking);
    }

    // 4. Actualizar detalles del paquete (Para el Operador)
    @PutMapping("/{id}/detalles")
    public Paquete actualizarDetallesPaquete(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Paquete paquete = paqueteRepo.findById(id).orElseThrow();

        // 1. Actualizar Estado (si viene)
        if (payload.get("estado") != null) {
            paquete.setEstado((String) payload.get("estado"));
        }

        // 2. Actualizar Peso (Vital para el operador)
        if (payload.get("pesoLibras") != null) {
            paquete.setPesoLibras(Double.valueOf(payload.get("pesoLibras").toString()));
        }

        // 3. Actualizar Precio/Valor (Si el operador lo corrige)
        if (payload.get("precio") != null) {
            paquete.setPrecio(Double.valueOf(payload.get("precio").toString()));
        }

        // 4. Actualizar Categoría (A, B, C, etc.)
        if (payload.get("categoria") != null) {
            paquete.setCategoria((String) payload.get("categoria"));
        }

        return paqueteRepo.save(paquete);
    }
}