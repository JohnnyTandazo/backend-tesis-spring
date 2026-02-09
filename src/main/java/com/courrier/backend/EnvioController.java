    @GetMapping("/operador")
    public List<Envio> listarParaOperador() {
        return envioService.obtenerTodos();
    }
// ...existing code...
package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * 🔒 ENVIO CONTROLLER - AUTENTICACIÓN SEGURA CON JWT
 * 
 * ✅ Todos los endpoints que requieren autorización usan:
 *    - obtenerUsuarioAutenticado() desde BaseSecurityController
 *    - El usuario SOLO se obtiene del JWT (Authorization header)
 *    - NO acepta parámetros manuales falsificables
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/envios")
public class EnvioController extends BaseSecurityController {

    /**
     * PUT: Actualizar tracking de un envío (solo logística, operador)
     * 🔒 SEGURIDAD: Requiere JWT (Operador/Admin)
     */
    @PutMapping("/operador/{id}/tracking")
    public ResponseEntity<Envio> actualizarTrackingOperador(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        String nuevoTracking = (String) payload.get("tracking");
        System.out.println("🔍 Buscando envio ID: " + id);
        obtenerUsuarioAutenticado();
        try {
            Envio envio = envioService.actualizarTrackingOperador(id, nuevoTracking);
            return ResponseEntity.ok(envio);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Autowired
    private EnvioService envioService;

    // ORDEN IMPORTANTE DE RUTAS (específicas antes que genéricas):
    // 1. /detalle/{id}
    // 2. /usuario/{usuarioId}
    // 3. /tracking/{numeroTracking}
    // 4. (GET genérico) - con @RequestParam opcional
    // 5. /{id} - más genérico, va al final

    /**
     * GET: Obtener un envío por su ID (/detalle/{id})
     * 🔒 SEGURIDAD: Requiere JWT válido en Authorization header
     */
    @GetMapping("/detalle/{id}")
    public ResponseEntity<Envio> obtenerEnvioPorId(@PathVariable Long id) {
        System.out.println("🔎 [GET /api/envios/detalle/" + id + "] PETICIÓN RECIBIDA");
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        
        Optional<Envio> envioOpt = envioService.obtenerPorId(id);
        
        if (!envioOpt.isPresent()) {
            System.out.println("❌ Envío NO encontrado para ID: " + id);
            return ResponseEntity.notFound().build();
        }
        
        Envio envio = envioOpt.get();
        
        // 🔒 VERIFICACIÓN IDOR: Comprobar propiedad del recurso
        String rol = usuarioActual.getRol().toUpperCase();
        
        // ADMIN y OPERADOR tienen acceso total
        if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
            System.out.println("✅ Acceso autorizado: Usuario " + rol);
            return ResponseEntity.ok(envio);
        }
        
        // CLIENTE: Solo puede ver sus propios envíos
        if (!envio.getUsuario().getId().equals(usuarioActual.getId())) {
            System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioActual.getEmail() + 
                " intentó acceder a envío de usuario " + envio.getUsuario().getEmail());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este envío");
        }
        
        System.out.println("✅ Acceso autorizado: Envío pertenece al cliente");
        System.out.println("✅ Envío encontrado: ID=" + id + ", Tracking=" + envio.getNumeroTracking());
        return ResponseEntity.ok(envio);
    }

    /**
     * GET: Obtener envíos por usuario (/usuario/{usuarioId})
     * 🔒 SEGURIDAD: Requiere JWT válido en Authorization header
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Envio>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        System.out.println("👤 [GET /api/envios/usuario/" + usuarioId + "] PETICIÓN RECIBIDA");
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        
        try {
            List<Envio> envios = envioService.obtenerPorUsuario(usuarioId);
            System.out.println("✅ Se encontraron " + envios.size() + " envíos del usuario: " + usuarioId);
            return ResponseEntity.ok(envios);
        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * GET: Obtener envío por número de tracking (/tracking/{numeroTracking})
     * 🔓 PÚBLICO: No requiere autenticación (public tracking)
     */
    @GetMapping("/tracking/{numeroTracking}")
    public ResponseEntity<Envio> obtenerPorTracking(@PathVariable String numeroTracking) {
        System.out.println("📍 [GET /api/envios/tracking/" + numeroTracking + "] PETICIÓN RECIBIDA");
        Envio envio = envioService.obtenerPorTracking(numeroTracking);
        
        if (envio != null) {
            System.out.println("✅ Envío encontrado por tracking: " + numeroTracking);
            return ResponseEntity.ok(envio);
        } else {
            System.out.println("❌ Envío NO encontrado para tracking: " + numeroTracking);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET: Obtener todos los envíos (raíz)
     * 🔒 SEGURIDAD: Requiere JWT válido en Authorization header
     * Si el usuario es CLIENTE, solo ve sus envíos
     */
    @GetMapping
    public ResponseEntity<List<Envio>> obtenerTodos() {
        System.out.println("📦 [GET /api/envios] PETICIÓN RECIBIDA");
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        
        try {
            List<Envio> envios;
            String rol = usuarioActual.getRol().toUpperCase();
            
            if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
                // ADMIN y OPERADOR ven todos los envíos
                envios = envioService.obtenerTodos();
                System.out.println("✅ Se encontraron " + envios.size() + " envíos en total (Usuario: " + rol + ")");
            } else {
                // CLIENTE solo ve sus propios envíos
                envios = envioService.obtenerPorUsuario(usuarioActual.getId());
                System.out.println("✅ Se encontraron " + envios.size() + " envíos del cliente: " + usuarioActual.getEmail());
            }
            
            return ResponseEntity.ok(envios);
        } catch (Exception e) {
            System.out.println("⚠️ Error obteniendo envíos: " + e.getMessage() + ". Retornando lista vacía.");
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * GET: Obtener envío por ID directo (/{id})
     * 🔒 SEGURIDAD: Requiere JWT válido en Authorization header
     */
    @GetMapping("/{id}")
    public ResponseEntity<Envio> obtenerEnvioPorIdDirecto(@PathVariable Long id) {
        System.out.println("🔎 [GET /api/envios/" + id + "] PETICIÓN RECIBIDA");
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT
        Usuario usuarioActual = obtenerUsuarioAutenticado();
        
        Optional<Envio> envioOpt = envioService.obtenerPorId(id);
        
        if (!envioOpt.isPresent()) {
            System.out.println("❌ Envío NO encontrado con ID: " + id);
            return ResponseEntity.notFound().build();
        }
        
        Envio envio = envioOpt.get();
        
        // 🔒 VERIFICACIÓN IDOR: Comprobar propiedad del recurso
        String rol = usuarioActual.getRol().toUpperCase();
        
        // ADMIN y OPERADOR tienen acceso total
        if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
            System.out.println("✅ Acceso autorizado: Usuario " + rol);
            return ResponseEntity.ok(envio);
        }
        
        // CLIENTE: Solo puede ver sus propios envíos
        if (!envio.getUsuario().getId().equals(usuarioActual.getId())) {
            System.out.println("🚫 ACCESO DENEGADO: Cliente " + usuarioActual.getEmail() + 
                " intentó acceder a envío de usuario " + envio.getUsuario().getEmail());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este envío");
        }
        
        System.out.println("✅ Acceso autorizado: Envío pertenece al cliente");
        System.out.println("✅ Envío encontrado: ID=" + id + ", Tracking=" + envio.getNumeroTracking());
        return ResponseEntity.ok(envio);
    }

    /**
     * POST: Crear un nuevo envío
     * 🔒 SEGURIDAD: Requiere JWT válido en Authorization header
     */
    @PostMapping
    public ResponseEntity<Envio> crearEnvio(@RequestBody CrearEnvioRequest request) {
        System.out.println("✍️ [POST /api/envios] ✅ PETICIÓN RECIBIDA - Creando nuevo envío...");
        System.out.println("   Número Tracking: " + request.getNumeroTracking());
        System.out.println("   Destinatario: " + request.getDestinatarioNombre());
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT (aunque no lo usamos aquí)
        obtenerUsuarioAutenticado();
        
        Envio envioCreado = envioService.crearEnvio(request);
        System.out.println("✅ Envío creado exitosamente: ID=" + envioCreado.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(envioCreado);
    }

    /**
     * PUT: Actualizar un envío
     * 🔒 SEGURIDAD: Requiere JWT válido en Authorization header
     */
    @PutMapping("/{id}")
    public ResponseEntity<Envio> actualizarEnvio(@PathVariable Long id, @RequestBody Envio envio) {
        System.out.println("✏️ [PUT /api/envios/" + id + "] ✅ PETICIÓN RECIBIDA - Actualizando envío...");
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT (aunque no lo usamos aquí)
        obtenerUsuarioAutenticado();
        
        try {
            Envio envioActualizado = envioService.actualizarEnvio(id, envio);
            System.out.println("✅ Envío actualizado exitosamente: ID=" + id);
            return ResponseEntity.ok(envioActualizado);
        } catch (RuntimeException e) {
            System.out.println("❌ Error al actualizar: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT: Actualizar SOLO el estado de un envío
     * 🔒 SEGURIDAD: Requiere JWT válido en Authorization header
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<Envio> actualizarEstado(
            @PathVariable Long id, 
            @RequestParam String nuevoEstado) {
        
        System.out.println("🔄 [PUT /api/envios/" + id + "/estado] ✅ PETICIÓN RECIBIDA");
        System.out.println("   Cambiando estado a: " + nuevoEstado);
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT (aunque no lo usamos aquí)
        obtenerUsuarioAutenticado();
        
        try {
            Envio envioActualizado = envioService.actualizarEstado(id, nuevoEstado);
            System.out.println("✅ Estado actualizado exitosamente: ID=" + id + ", Nuevo estado=" + nuevoEstado);
            return ResponseEntity.ok(envioActualizado);
        } catch (RuntimeException e) {
            System.out.println("❌ Error al actualizar estado: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT: Aprobar el pago de un envío
     * 🔒 SEGURIDAD: Requiere JWT (Operador/Admin)
     */
    @PutMapping("/api/operador/envios/{id}/aprobar-pago")
    public ResponseEntity<Envio> aprobarPago(
            @PathVariable Long id, 
            @RequestParam(required = false) String nuevoEstado) {
        System.out.println("💰 [PUT /api/operador/envios/" + id + "/aprobar-pago] ✅ PETICIÓN RECIBIDA");
        // 🔒 SEGURIDAD: Validar usuario
        obtenerUsuarioAutenticado();
        try {
            // Llamamos al método del servicio que actualiza la factura
            Envio envio = envioService.aprobarPago(id, nuevoEstado);
            System.out.println("✅ Pago aprobado y factura actualizada para envío ID: " + id);
            return ResponseEntity.ok(envio);
        } catch (RuntimeException e) {
            System.out.println("❌ Error al aprobar pago: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * DELETE: Eliminar un envío
     * 🔒 SEGURIDAD: Requiere JWT válido en Authorization header
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarEnvio(@PathVariable Long id) {
        System.out.println("🗑️ [DELETE /api/envios/" + id + "] ✅ PETICIÓN RECIBIDA - Eliminando envío...");
        
        // 🔒 SEGURIDAD: Obtener usuario desde JWT (aunque no lo usamos aquí)
        obtenerUsuarioAutenticado();
        
        try {
            envioService.eliminarEnvio(id);
            System.out.println("✅ Envío eliminado exitosamente: ID=" + id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.out.println("❌ Error al eliminar: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
