package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/direcciones")
public class DireccionController extends BaseSecurityController {

    @Autowired
    private DireccionService direccionService;

    // 1. GET: Obtener direcciones del usuario autenticado
    // 🔒 SEGURIDAD: Requiere JWT válido
    @GetMapping
    public ResponseEntity<?> obtenerDireccionesDeUsuario() {
        System.out.println("📍 [GET /api/direcciones] Obteniendo direcciones del usuario autenticado");
        try {
            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            List<Direccion> direcciones = direccionService.obtenerPorUsuario(usuarioActual.getId());
            System.out.println("✅ Se encontraron " + direcciones.size() + " direcciones");
            return ResponseEntity.ok(direcciones);
        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    // 2. GET: Obtener una dirección por su ID
    // 🔒 SEGURIDAD: Requiere JWT válido + Verifica propiedad (IDOR)
    @GetMapping("/{id}")
    public ResponseEntity<Direccion> obtenerPorId(@PathVariable Long id) {
        System.out.println("🔎 [GET /api/direcciones/" + id + "] Buscando dirección por ID: " + id);
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        
        var direccionOpt = direccionService.obtenerPorId(id);
        if (!direccionOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Direccion direccion = direccionOpt.get();
        String rol = usuarioActual.getRol().toUpperCase();
        
        // ADMIN y OPERADOR tienen acceso total
        if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
            System.out.println("✅ Acceso autorizado: Usuario " + rol);
            return ResponseEntity.ok(direccion);
        }
        
        // CLIENTE: Solo puede ver sus propias direcciones
        if (!direccion.getUsuario().getId().equals(usuarioActual.getId())) {
            System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioActual.getId() + " intentó acceder a dirección de usuario " + direccion.getUsuario().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver esta dirección");
        }
        
        System.out.println("✅ Acceso autorizado: Dirección pertenece al cliente");
        return ResponseEntity.ok(direccion);
    }

    // 3. POST: Crear una nueva dirección
    // 🔒 SEGURIDAD: Requiere JWT válido
    @PostMapping
    public ResponseEntity<?> crearDireccion(@RequestBody java.util.Map<String, Object> payload) {
        System.out.println("✍️ [POST /api/direcciones] ✅ PETICIÓN RECIBIDA");
        
        try {
            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            System.out.println("   Creando para usuario ID: " + usuarioActual.getId());
            System.out.println("   Payload keys: " + payload.keySet());
            
            // Crear objeto Direccion desde el payload
            Direccion direccion = new Direccion();
            direccion.setAlias((String) payload.get("alias"));
            direccion.setCallePrincipal((String) payload.get("callePrincipal"));
            direccion.setCalleSecundaria((String) payload.get("calleSecundaria"));
            direccion.setCiudad((String) payload.get("ciudad"));
            direccion.setTelefono((String) payload.get("telefono"));
            direccion.setReferencia((String) payload.get("referencia"));
            
            if (payload.containsKey("esPrincipal")) {
                direccion.setEsPrincipal(Boolean.valueOf(payload.get("esPrincipal").toString()));
            }
            
            System.out.println("   Datos: " + direccion.getAlias() + " - " + direccion.getCiudad());
            
            Direccion direccionCreada = direccionService.crearDireccion(direccion, usuarioActual.getId());
            System.out.println("✅ Dirección creada exitosamente: ID=" + direccionCreada.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(direccionCreada);
            
        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.out.println("❌ Error inesperado: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(java.util.Map.of("error", "Error interno del servidor"));
        }
    }

    // 4. PUT: Actualizar una dirección
    // 🔒 SEGURIDAD: Requiere JWT válido
    @PutMapping("/{id}")
    public ResponseEntity<Direccion> actualizarDireccion(
            @PathVariable Long id,
            @RequestBody Direccion direccion) {
        System.out.println("✏️ [PUT /api/direcciones/" + id + "] ✅ PETICIÓN RECIBIDA - Actualizando dirección...");
        
        try {
            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            Direccion direccionActualizada = direccionService.actualizarDireccion(id, direccion);
            System.out.println("✅ Dirección actualizada exitosamente: ID=" + id);
            return ResponseEntity.ok(direccionActualizada);
        } catch (RuntimeException e) {
            System.out.println("❌ Error al actualizar: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // 5. DELETE: Eliminar una dirección
    // 🔒 SEGURIDAD: Requiere JWT válido
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDireccion(@PathVariable Long id) {
        System.out.println("🗑️ [DELETE /api/direcciones/" + id + "] ✅ PETICIÓN RECIBIDA - Eliminando dirección...");
        
        try {
            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            direccionService.eliminarDireccion(id);
            System.out.println("✅ Dirección eliminada exitosamente: ID=" + id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.out.println("❌ Error al eliminar: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    // 6. PUT: Marcar una dirección como principal
    // 🔒 SEGURIDAD: Requiere JWT válido
    @PutMapping("/{id}/principal")
    public ResponseEntity<Direccion> marcarComoPrincipal(@PathVariable Long id) {
        System.out.println("⭐ [PUT /api/direcciones/" + id + "/principal] Marcando como principal...");
        
        try {
            // 🔒 SEGURIDAD: Obtener usuario desde JWT
            Usuario usuarioActual = obtenerUsuarioAutenticado();
            
            Direccion direccion = direccionService.marcarComoPrincipal(id);
            System.out.println("✅ Dirección marcada como principal: " + direccion.getAlias());
            return ResponseEntity.ok(direccion);
        } catch (RuntimeException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }}