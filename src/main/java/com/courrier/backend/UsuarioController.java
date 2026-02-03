package com.courrier.backend;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios") // Esta será la URL de acceso
public class UsuarioController {

    @Autowired
    private UsuarioRepository repositorio;

    @Autowired
    private DireccionService direccionService;

    // 1. GET: Para ver todos los usuarios registrados
    @GetMapping
    public List<Usuario> listarUsuarios() {
        System.out.println("👤 [GET /api/usuarios] Listando todos los usuarios...");
        return repositorio.findAll();
    }

    // 2. POST: Para registrar un usuario nuevo (Desde Postman o React)
    @PostMapping
    public Usuario guardarUsuario(@RequestBody Usuario usuario) {
        System.out.println("✅ [POST /api/usuarios] PETICIÓN RECIBIDA - Guardando usuario: " + usuario.getEmail());
        Usuario usuarioGuardado = repositorio.save(usuario);
        System.out.println("✅ Usuario guardado exitosamente: ID=" + usuarioGuardado.getId());
        return usuarioGuardado;
    }

    // 3. POST: Login - Valida email y contraseña
    @PostMapping("/login")
    public Usuario login(@RequestBody Map<String, String> credenciales, HttpSession session) {
        String email = credenciales.get("email");
        String password = credenciales.get("password");
        System.out.println("🔐 [POST /api/usuarios/login] ✅ PETICIÓN RECIBIDA - Intentando login con: " + email);

        Usuario usuario = repositorio.findByEmail(email);
        if (usuario == null) {
            System.out.println("❌ Usuario no encontrado: " + email);
            throw new RuntimeException("Usuario no encontrado");
        }

        if (!usuario.getPassword().equals(password)) {
            System.out.println("❌ Contraseña incorrecta para: " + email);
            throw new RuntimeException("Contraseña incorrecta");
        }

        // ✅ NUEVO: Almacenar usuario en sesión HTTP (seguro)
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioEmail", usuario.getEmail());
        session.setAttribute("usuarioRol", usuario.getRol());
        
        System.out.println("✅ Login exitoso para: " + email + " (ID=" + usuario.getId() + ")");
        System.out.println("✅ Sesión creada: " + session.getId());
        
        return usuario;
    }

    // 4. POST: Registro - Guarda un nuevo usuario con rol por defecto "CLIENTE"
    @PostMapping("/registro")
    public Usuario registro(@RequestBody Usuario usuario) {
        System.out.println("📝 [POST /api/usuarios/registro] ✅ PETICIÓN RECIBIDA - Registrando nuevo usuario: " + usuario.getEmail());
        // Asignar rol por defecto si no viene especificado
        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("CLIENTE");
            System.out.println("   Rol asignado por defecto: CLIENTE");
        }
        Usuario usuarioGuardado = repositorio.save(usuario);
        System.out.println("✅ Usuario registrado exitosamente: ID=" + usuarioGuardado.getId() + ", Email=" + usuarioGuardado.getEmail());
        return usuarioGuardado;
    }

    // 5. GET: Obtener direcciones de un usuario específico
    @GetMapping("/{id}/direcciones")
    public List<Direccion> obtenerDireccionesDeUsuario(@PathVariable Long id) {
        System.out.println("📍 [GET /api/usuarios/" + id + "/direcciones] Obteniendo direcciones del usuario: " + id);
        List<Direccion> direcciones = direccionService.obtenerPorUsuario(id);
        System.out.println("✅ Se encontraron " + direcciones.size() + " direcciones");
        return direcciones;
    }
}
