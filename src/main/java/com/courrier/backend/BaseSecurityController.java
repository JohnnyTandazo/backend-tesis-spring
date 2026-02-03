package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

/**
 * ✅ BASE CONTROLLER CON SEGURIDAD
 * 
 * Proporciona el método obtenerUsuarioAutenticado() a todos los controllers.
 * Este es el ÚNICO lugar donde se extrae el usuario del JWT.
 * 
 * 🔒 SEGURIDAD:
 * - El usuario se obtiene SOLO desde el Token JWT (Authorization header)
 * - NO acepta parámetros manuales que el usuario pueda falsificar
 * - Valida que el token sea válido y que el usuario exista en BD
 */
public class BaseSecurityController {

    @Autowired
    protected UsuarioRepository usuarioRepository;

    /**
     * 🔒 MÉTODO SEGURO: Obtener usuario desde el JWT
     * 
     * @return Usuario autenticado desde el token JWT
     * @throws ResponseStatusException Si no hay token válido o usuario no existe
     */
    protected Usuario obtenerUsuarioAutenticado() {
        // 1️⃣ Obtener autenticación desde Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // 2️⃣ Validar que hay un token válido
        if (auth == null || !auth.isAuthenticated()) {
            System.out.println("❌ [SEGURIDAD] No hay autenticación en el contexto");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "❌ Token JWT requerido en header Authorization: Bearer <token>");
        }
        
        // 3️⃣ Validar que no es usuario anónimo
        if ("anonymousUser".equals(auth.getPrincipal())) {
            System.out.println("❌ [SEGURIDAD] Usuario anónimo detectado");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "❌ Token JWT inválido o expirado");
        }
        
        // 4️⃣ Extraer email del token (Spring Security almacena el subject aquí)
        String email = auth.getName();
        System.out.println("🔓 [SEGURIDAD] Token válido para: " + email);
        
        // 5️⃣ Validar que email no es nulo
        if (email == null || email.isEmpty()) {
            System.out.println("❌ [SEGURIDAD] Token sin email válido");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "❌ Token JWT sin información de usuario");
        }
        
        // 6️⃣ Buscar usuario REAL en base de datos
        Usuario usuario = usuarioRepository.findByEmail(email);
        if (usuario == null) {
            System.out.println("❌ [SEGURIDAD] Usuario no encontrado en BD: " + email);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "❌ Usuario no encontrado en base de datos");
        }
        
        System.out.println("✅ [SEGURIDAD] Usuario autenticado: " + usuario.getEmail() + " (ID: " + usuario.getId() + ")");
        return usuario;
    }
}
