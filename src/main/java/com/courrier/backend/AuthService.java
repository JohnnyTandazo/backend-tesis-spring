package com.courrier.backend;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AuthService - Servicio de autenticación
 * Maneja la obtención del usuario autenticado desde la sesión HTTP
 * 
 * NOTA: Esta es una solución transitoria. En producción se debe usar JWT.
 */
@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene el usuario autenticado desde la sesión HTTP
     * 
     * @param session Sesión HTTP de la petición
     * @return Usuario autenticado, o null si no está autenticado
     */
    public Usuario obtenerUsuarioAutenticado(HttpSession session) {
        if (session == null) {
            System.out.println("⚠️ [AuthService] Sesión es nula");
            return null;
        }

        // Obtener el ID del usuario almacenado en la sesión
        Object usuarioIdObj = session.getAttribute("usuarioId");
        if (usuarioIdObj == null) {
            System.out.println("⚠️ [AuthService] No hay usuarioId en la sesión");
            return null;
        }

        Long usuarioId = null;
        try {
            usuarioId = Long.valueOf(usuarioIdObj.toString());
        } catch (NumberFormatException e) {
            System.out.println("❌ [AuthService] Error al parsear usuarioId de sesión: " + usuarioIdObj);
            return null;
        }

        // Buscar usuario en BD
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) {
            System.out.println("❌ [AuthService] Usuario no encontrado en BD: " + usuarioId);
            return null;
        }

        System.out.println("✅ [AuthService] Usuario autenticado obtenido desde sesión: " + usuario.getEmail() + " (ID: " + usuario.getId() + ")");
        return usuario;
    }

    /**
     * Obtiene el usuario autenticado o lanza excepción si no existe
     * 
     * @param session Sesión HTTP de la petición
     * @return Usuario autenticado
     * @throws RuntimeException si el usuario no está autenticado
     */
    public Usuario obtenerUsuarioAutenticadoOThrow(HttpSession session) {
        Usuario usuario = obtenerUsuarioAutenticado(session);
        if (usuario == null) {
            System.out.println("❌ [AuthService] Usuario no autenticado");
            throw new RuntimeException("Usuario no autenticado. Por favor, inicie sesión.");
        }
        return usuario;
    }

    /**
     * Verifica si un usuario tiene acceso a un recurso
     * Los ADMIN y OPERADOR tienen acceso a TODO.
     * Los CLIENTE solo tienen acceso a sus propios recursos.
     * 
     * @param usuarioActual Usuario autenticado
     * @param usuarioDuenoRecurso Usuario propietario del recurso
     * @return true si tiene acceso, false si no
     */
    public boolean tieneAcceso(Usuario usuarioActual, Usuario usuarioDuenoRecurso) {
        if (usuarioActual == null || usuarioDuenoRecurso == null) {
            return false;
        }

        // ADMIN y OPERADOR tienen acceso total
        String rol = usuarioActual.getRol().toUpperCase();
        if (rol.equals("ADMIN") || rol.equals("OPERADOR")) {
            System.out.println("✅ Acceso autorizado: Usuario " + rol);
            return true;
        }

        // CLIENTE solo puede acceder a sus propios recursos
        if (usuarioActual.getId().equals(usuarioDuenoRecurso.getId())) {
            System.out.println("✅ Acceso autorizado: Recurso pertenece al usuario");
            return true;
        }

        System.out.println("❌ Acceso denegado: Usuario " + usuarioActual.getId() + 
            " intenta acceder a recurso de usuario " + usuarioDuenoRecurso.getId());
        return false;
    }
}
