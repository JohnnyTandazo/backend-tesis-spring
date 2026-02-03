package com.courrier.backend;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
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

    @Autowired
    @Lazy  // 🔧 Lazy loading para evitar BeanCurrentlyInCreationException (dependencia circular)
    private PasswordEncoder passwordEncoder;

    // 1. GET: Para ver todos los usuarios registrados
    @GetMapping
    public List<Usuario> listarUsuarios() {
        System.out.println("👤 [GET /api/usuarios] Listando todos los usuarios...");
        return repositorio.findAll();
    }

    // 2. POST: Para registrar un usuario nuevo (Desde Postman o React)
    @PostMapping
    public ResponseEntity<?> guardarUsuario(@RequestBody Usuario usuario) {
        System.out.println("✅ [POST /api/usuarios] PETICIÓN RECIBIDA - Guardando usuario: " + usuario.getEmail());
        
        try {
            // 🔒 VALIDACIÓN: Verificar email
            if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
                System.out.println("❌ Email es requerido");
                return ResponseEntity.badRequest().body(Map.of("error", "Email es requerido"));
            }
            
            // 🔒 VALIDACIÓN: Verificar password
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                System.out.println("❌ Contraseña es requerida");
                return ResponseEntity.badRequest().body(Map.of("error", "Contraseña es requerida"));
            }
            
            // 🔒 SEGURIDAD: Encriptar contraseña con BCrypt
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
                usuario.setPassword(passwordEncriptada);
                System.out.println("   🔐 Contraseña encriptada correctamente");
            }
            
            Usuario usuarioGuardado = repositorio.save(usuario);
            System.out.println("✅ Usuario guardado exitosamente: ID=" + usuarioGuardado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
            
        } catch (DataIntegrityViolationException e) {
            // Email duplicado u otra violación de constraint
            System.out.println("❌ Email ya existe o violación de constraint: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "El email ya está registrado"));
                
        } catch (Exception e) {
            System.err.println("❌ Error al guardar usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    // 3. POST: Login - Valida email y contraseña
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credenciales) {
        try {
            String email = credenciales.get("email");
            String password = credenciales.get("password");
            System.out.println("🔐 [POST /api/usuarios/login] ✅ PETICIÓN RECIBIDA - Intentando login con: " + email);

            // 🔒 VALIDACIÓN: Email y password
            if (email == null || email.isEmpty()) {
                System.out.println("❌ Email es requerido");
                return ResponseEntity.badRequest().body(Map.of("error", "Email es requerido"));
            }
            if (password == null || password.isEmpty()) {
                System.out.println("❌ Contraseña es requerida");
                return ResponseEntity.badRequest().body(Map.of("error", "Contraseña es requerida"));
            }

            Usuario usuario = repositorio.findByEmail(email);
            if (usuario == null) {
                System.out.println("❌ Usuario no encontrado: " + email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email o contraseña incorrectos"));
            }

            // 🔒 SEGURIDAD: Usar PasswordEncoder para comparar contraseñas
            // ✅ passwordEncoder.matches(passwordPlana, passwordEncriptada)
            if (!passwordEncoder.matches(password, usuario.getPassword())) {
                System.out.println("❌ Contraseña incorrecta para: " + email);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Email o contraseña incorrectos"));
            }

            System.out.println("✅ Login exitoso para: " + email + " (ID=" + usuario.getId() + ")");
            return ResponseEntity.ok(usuario);
            
        } catch (Exception e) {
            System.err.println("❌ Error en login: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
    }

    // 4. POST: Registro - Guarda un nuevo usuario con rol por defecto "CLIENTE"
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Usuario usuario) {
        System.out.println("📝 [POST /api/usuarios/registro] ✅ PETICIÓN RECIBIDA - Registrando nuevo usuario: " + usuario.getEmail());
        
        try {
            // 🔒 VALIDACIÓN: Email
            if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
                System.out.println("❌ Email es requerido");
                return ResponseEntity.badRequest().body(Map.of("error", "Email es requerido"));
            }
            
            // 🔒 VALIDACIÓN: Contraseña
            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                System.out.println("❌ Contraseña es requerida");
                return ResponseEntity.badRequest().body(Map.of("error", "Contraseña es requerida"));
            }
            
            // Asignar rol por defecto si no viene especificado
            if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
                usuario.setRol("CLIENTE");
                System.out.println("   Rol asignado por defecto: CLIENTE");
            }
            
            // 🔒 SEGURIDAD: Encriptar contraseña con BCrypt
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
                usuario.setPassword(passwordEncriptada);
                System.out.println("   🔐 Contraseña encriptada correctamente");
            }
            
            Usuario usuarioGuardado = repositorio.save(usuario);
            System.out.println("✅ Usuario registrado exitosamente: ID=" + usuarioGuardado.getId() + ", Email=" + usuarioGuardado.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
            
        } catch (DataIntegrityViolationException e) {
            // Email duplicado u otra violación de constraint
            System.out.println("❌ Email ya existe o violación de constraint: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "El email ya está registrado"));
                
        } catch (Exception e) {
            System.err.println("❌ Error al registrar usuario: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno: " + e.getMessage()));
        }
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
