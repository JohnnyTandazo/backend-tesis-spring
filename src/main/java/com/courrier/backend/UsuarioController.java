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
    private UsuarioService usuarioService;

    @Autowired
    @Lazy  // 🔧 Lazy loading para evitar BeanCurrentlyInCreationException (dependencia circular)
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

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
            
            Usuario usuarioGuardado = usuarioService.guardarUsuario(usuario);
            System.out.println("✅ Usuario guardado exitosamente: ID=" + usuarioGuardado.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Usuario creado exitosamente",
                "id", usuarioGuardado.getId(),
                "nombre", usuarioGuardado.getNombre() != null ? usuarioGuardado.getNombre() : "",
                "email", usuarioGuardado.getEmail(),
                "rol", usuarioGuardado.getRol()
            ));
            
        } catch (UserAlreadyExistsException e) {
            System.out.println("❌ Email ya existe: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));

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
            String token = jwtUtil.generarToken(usuario.getEmail(), usuario.getId(), usuario.getRol());
            return ResponseEntity.ok(Map.of(
                "mensaje", "Login exitoso",
                "id", usuario.getId(),
                "nombre", usuario.getNombre() != null ? usuario.getNombre() : "",
                "email", usuario.getEmail(),
                "rol", usuario.getRol(),
                "token", token
            ));
            
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
            
            Usuario usuarioGuardado = usuarioService.guardarUsuario(usuario);
            System.out.println("✅ Usuario registrado exitosamente: ID=" + usuarioGuardado.getId() + ", Email=" + usuarioGuardado.getEmail());
            String token = jwtUtil.generarToken(usuarioGuardado.getEmail(), usuarioGuardado.getId(), usuarioGuardado.getRol());
            
            // 🔒 SEGURIDAD: Devolver DTO sin password + token
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Registro exitoso",
                "id", usuarioGuardado.getId(),
                "nombre", usuarioGuardado.getNombre() != null ? usuarioGuardado.getNombre() : "",
                "email", usuarioGuardado.getEmail(),
                "rol", usuarioGuardado.getRol(),
                "token", token
            ));
            
        } catch (UserAlreadyExistsException e) {
            System.out.println("❌ Email ya existe: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));

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

        // ENDPOINT 1: Ver Perfil
        @GetMapping("/{id}")
        public ResponseEntity<?> obtenerPerfil(@PathVariable Long id) {
            Usuario usuario = usuarioService.obtenerPorId(id);
            if (usuario != null) {
                // Importante: El password ya va encriptado o null para seguridad,
                // pero enviamos el objeto para que el front lea nombre/email/casillero.
                return ResponseEntity.ok(usuario);
            }
            return ResponseEntity.notFound().build();
        }

        // ENDPOINT 2: Actualizar Contacto
        @PutMapping("/{id}")
        public ResponseEntity<?> actualizarDatos(@PathVariable Long id, @RequestBody Map<String, String> body) {
            String telefono = body.get("telefono");
            String direccion = body.get("direccion"); // Opcional si el front lo manda
            Usuario actualizado = usuarioService.actualizarPerfil(id, telefono, direccion);
            if (actualizado != null) {
                return ResponseEntity.ok(actualizado);
            }
            return ResponseEntity.badRequest().body("No se pudo actualizar el perfil. Usuario no encontrado.");
        }
}
