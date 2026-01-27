# 📄 CÓDIGO FINAL - VERSIÓN LIMPIA Y VERIFICADA

## WebConfig.java
```java
package com.courrier.backend;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");

        System.out.println("✅ CORS configurado globalmente para todos los endpoints /**");
    }
}
```

---

## PaqueteController.java
```java
package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

    // 1. Ver TODOS los paquetes (Para el Admin u Operador)
    @GetMapping
    public List<Paquete> listarPaquetes() {
        System.out.println("📦 [GET /api/paquetes] Listando todos los paquetes...");
        return paqueteRepo.findAll();
    }

    // 2. Registrar un Paquete nuevo (Pre-Alerta)
    @PostMapping
    public Paquete crearPaquete(@RequestBody Map<String, Object> payload) {
        System.out.println("📝 [POST /api/paquetes] ✅ PETICIÓN RECIBIDA - Creando nuevo paquete...");
        System.out.println("   Datos recibidos: " + payload);
        
        Paquete p = new Paquete();
        
        p.setTrackingNumber((String) payload.get("trackingNumber"));
        System.out.println("   📦 Tracking: " + p.getTrackingNumber());
        
        String descUsuario = (String) payload.get("descripcion");
        String tienda = (String) payload.get("storeName");
        
        if (descUsuario != null && !descUsuario.isEmpty()) {
            p.setDescripcion(descUsuario);
        } else {
            p.setDescripcion("Compra en " + (tienda != null ? tienda : "General"));
        }
        
        if (payload.get("precio") != null) {
            p.setPrecio(Double.valueOf(payload.get("precio").toString()));
        } else {
            p.setPrecio(0.0);
        }
        
        p.setPesoLibras(0.0);
        p.setEstado("PRE_ALERTADO");
        
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

    // 3b. Buscar por código/tracking (Endpoint que espera el Frontend)
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

        if (payload.get("estado") != null) {
            paquete.setEstado((String) payload.get("estado"));
            System.out.println("   Estado actualizado a: " + paquete.getEstado());
        }

        if (payload.get("pesoLibras") != null) {
            paquete.setPesoLibras(Double.valueOf(payload.get("pesoLibras").toString()));
            System.out.println("   Peso actualizado a: " + paquete.getPesoLibras() + " libras");
        }

        if (payload.get("precio") != null) {
            paquete.setPrecio(Double.valueOf(payload.get("precio").toString()));
            System.out.println("   Precio actualizado a: " + paquete.getPrecio());
        }

        if (payload.get("categoria") != null) {
            paquete.setCategoria((String) payload.get("categoria"));
            System.out.println("   Categoría actualizada a: " + paquete.getCategoria());
        }

        Paquete paqueteActualizado = paqueteRepo.save(paquete);
        System.out.println("✅ Paquete actualizado exitosamente: ID=" + id);
        
        return paqueteActualizado;
    }
}
```

---

## UsuarioController.java
```java
package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repositorio;

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
    public Usuario login(@RequestBody Map<String, String> credenciales) {
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

        System.out.println("✅ Login exitoso para: " + email + " (ID=" + usuario.getId() + ")");
        return usuario;
    }

    // 4. POST: Registro - Guarda un nuevo usuario con rol por defecto "CLIENTE"
    @PostMapping("/registro")
    public Usuario registro(@RequestBody Usuario usuario) {
        System.out.println("📝 [POST /api/usuarios/registro] ✅ PETICIÓN RECIBIDA - Registrando nuevo usuario: " + usuario.getEmail());
        
        if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
            usuario.setRol("CLIENTE");
            System.out.println("   Rol asignado por defecto: CLIENTE");
        }
        
        Usuario usuarioGuardado = repositorio.save(usuario);
        System.out.println("✅ Usuario registrado exitosamente: ID=" + usuarioGuardado.getId() + ", Email=" + usuarioGuardado.getEmail());
        return usuarioGuardado;
    }
}
```

---

## Paquete.java
```java
package com.courrier.backend;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "paquetes")
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String trackingNumber;

    private String descripcion;

    private Double pesoLibras;

    private Double precio;

    private String estado;

    private LocalDateTime fechaCreacion = LocalDateTime.now();

    private String categoria;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}
```

---

## Usuario.java
```java
package com.courrier.backend;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String telefono;

    @Column(nullable = false)
    private String rol;

    private LocalDateTime fechaRegistro = LocalDateTime.now();
}
```

---

## PaqueteRepository.java
```java
package com.courrier.backend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaqueteRepository extends JpaRepository<Paquete, Long> {
    List<Paquete> findByUsuarioId(Long usuarioId);
    Paquete findByTrackingNumber(String trackingNumber);
}
```

---

## UsuarioRepository.java
```java
package com.courrier.backend;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
}
```

---

## BackendApplication.java
```java
package com.courrier.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
```

---

**Todos estos archivos están limpios, sin duplicados y listos para producción.**
