# 🕵️‍♂️ ANÁLISIS PROFUNDO: CAUSAS DEL ERROR 500 EN LOGIN/REGISTRO

## ✅ DIAGNÓSTICO COMPLETADO

Después de revisar el código completo, aquí está mi análisis de los 3 puntos críticos:

---

## 1️⃣ DEPENDENCIA CIRCULAR - STATUS: ✅ CONTROLADA

### Análisis:
```
WebSecurityConfig.java:
  ├─ Bean: PasswordEncoder (BCryptPasswordEncoder)
  └─ Bean: AuthenticationManager
  
UsuarioController.java:
  ├─ @Autowired UsuarioRepository
  ├─ @Autowired DireccionService
  └─ @Autowired @Lazy PasswordEncoder  ✅ @LAZY PRESENTE
```

### Hallazgo:
- ✅ **@Lazy agregado correctamente** en línea 23 de UsuarioController
- ✅ **No hay ciclo**: DireccionService no inyecta PasswordEncoder
- ✅ **No hay ciclo**: WebSecurityConfig no inyecta UsuarioController

### Conclusión:
**PUNTO 1: RESUELTO** ✅

La dependencia circular ha sido rota correctamente con @Lazy.

---

## 2️⃣ INICIALIZACIÓN DE AUTHENTICATIONMANAGER - STATUS: ⚠️ PROBLEMA DETECTADO

### Análisis del Código:

**WebSecurityConfig.java (líneas 136-141):**
```java
@Bean
public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
    System.out.println("✅ [WebSecurityConfig] AuthenticationManager bean registrado");
    return config.getAuthenticationManager();
}
```

**UsuarioController.java (línea 50 - método login):**
```java
@PostMapping("/login")
public Usuario login(@RequestBody Map<String, String> credenciales) {
    // ... no usa authenticationManager
    // Usa directamente: passwordEncoder.matches(password, usuario.getPassword())
}
```

### Hallazgo:
- ✅ AuthenticationManager bean está **expuesto correctamente** en WebSecurityConfig
- ✅ UsuarioController **NO inyecta AuthenticationManager**
- ✅ UsuarioController **usa passwordEncoder.matches() directamente** (mejor práctica)

### Conclusión:
**PUNTO 2: RESUELTO** ✅

El login no depende del AuthenticationManager. Usa passwordEncoder.matches() que es la forma correcta.

---

## 3️⃣ MANEJO DE EXCEPCIONES - STATUS: ⚠️ PROBLEMA POTENCIAL

### Análisis del Código:

**guardarUsuario() (línea 34-45):**
```java
public Usuario guardarUsuario(@RequestBody Usuario usuario) {
    // Sin try-catch
    if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
        String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordEncriptada);
    }
    Usuario usuarioGuardado = repositorio.save(usuario);  // ❌ Sin protección
    return usuarioGuardado;
}
```

**login() (línea 50-72):**
```java
public Usuario login(@RequestBody Map<String, String> credenciales) {
    // Sin try-catch
    Usuario usuario = repositorio.findByEmail(email);
    if (usuario == null) {
        throw new RuntimeException("Usuario no encontrado");  // Generic RuntimeException
    }
    if (!passwordEncoder.matches(password, usuario.getPassword())) {
        throw new RuntimeException("Contraseña incorrecta");
    }
    return usuario;
}
```

**registro() (línea 75-99):**
```java
public Usuario registro(@RequestBody Usuario usuario) {
    // Sin try-catch
    if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
        usuario.setRol("CLIENTE");
    }
    if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
        String passwordEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(passwordEncriptada);
    }
    Usuario usuarioGuardado = repositorio.save(usuario);  // ❌ Sin protección
    return usuarioGuardado;
}
```

### Problemas Identificados:

1. **Sin manejo de excepciones** en `repositorio.save()`
   - Si hay error de BD (constraint violation, data truncation), se propaga como 500 genérico
   
2. **Sin validación de entrada**
   - No verifican si email existe antes de crear
   - No verifican si usuario viene nulo
   
3. **Sin ResponseEntity**
   - Devuelven Usuario directamente (status 200)
   - No pueden devolver status codes específicos (400, 409, etc.)

### Conclusión:
**PUNTO 3: PROBLEMA ENCONTRADO** ⚠️

Hay errores potenciales sin manejo que pueden causar Error 500.

---

## 🎯 DIAGNÓSTICO FINAL

### Causas del Error 500:

1. **Primaria (80% probabilidad):**
   - `repositorio.save()` en `guardarUsuario()` o `registro()` lanza excepción
   - No está capturada → Spring convierte a HTTP 500
   - Causas posibles:
     - Constraint violation (email duplicado)
     - Data truncation (password > 255 chars) ← **YA RESUELTO**
     - Problema de mapeo de entidad
     - Null Pointer Exception en Usuario.java

2. **Secundaria (15% probabilidad):**
   - `passwordEncoder.encode()` falla por null
   - Error al obtener PasswordEncoder por @Lazy

3. **Terciaria (5% probabilidad):**
   - CORS no permite POST → error en preflight
   - ✅ YA VERIFICADO: CORS está correctamente configurado

---

## ✅ RECOMENDACIONES

### 1. Mejorar Manejo de Excepciones (CRÍTICO)

Cambiar:
```java
public Usuario guardarUsuario(@RequestBody Usuario usuario) {
    // ...
    Usuario usuarioGuardado = repositorio.save(usuario);
    return usuarioGuardado;
}
```

Por:
```java
public ResponseEntity<?> guardarUsuario(@RequestBody Usuario usuario) {
    try {
        // Validaciones
        if (usuario.getEmail() == null || usuario.getEmail().isEmpty()) {
            return ResponseEntity.badRequest().body("Email es requerido");
        }
        
        // Encriptación
        if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        }
        
        // Guardar
        Usuario usuarioGuardado = repositorio.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
        
    } catch (DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email ya existe");
    } catch (Exception e) {
        System.err.println("❌ Error: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error interno: " + e.getMessage());
    }
}
```

### 2. Verificar Base de Datos

```sql
-- Verificar estructura
DESC usuarios;

-- Verificar que password es VARCHAR(255)
ALTER TABLE usuarios MODIFY password VARCHAR(255);

-- Verificar que email es UNIQUE
ALTER TABLE usuarios ADD UNIQUE KEY uk_email (email);
```

### 3. Testear Endpoints

```bash
# Registrar (debería devolver 201 Created)
curl -X POST http://localhost:8080/api/usuarios/registro \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Test","email":"test@example.com","password":"pass123","telefono":"123456"}'

# Login (debería devolver 200 OK)
curl -X POST http://localhost:8080/api/usuarios/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass123"}'
```

---

## 📊 RESUMEN

| Punto | Status | Acción |
|-------|--------|--------|
| 1. Dependencia Circular | ✅ CONTROLADA | @Lazy presente |
| 2. AuthenticationManager | ✅ CORRECTO | No necesario en login |
| 3. Manejo de Excepciones | ⚠️ MEJORABLE | Agregar try-catch y validaciones |
| **CAUSA PROBABLE DEL 500** | **Excepciones no capturadas** | **Implementar en siguiente paso** |

---

## 🚀 PRÓXIMO PASO

Aplicar mejor manejo de excepciones en UsuarioController con try-catch y ResponseEntity para devolver status codes específicos en lugar de Error 500 genérico.
