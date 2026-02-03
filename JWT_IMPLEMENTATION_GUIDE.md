# 🔐 IMPLEMENTACIÓN JWT - PLAN DE ACCIÓN

## Problema Identificado ✅

```
Error 403 FORBIDDEN después de login exitoso
Causa: Backend NO devuelve JWT token en login
Solución: Implementar JWT en 4 pasos
```

---

## PASO 1: AGREGAR DEPENDENCIA JWT AL POM.XML

Busca en `pom.xml` la sección `<dependencies>` y agrega esta dependencia:

```xml
<!-- 🔐 JWT: Para generar y validar tokens -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

**Ubicación:** Agrégalas DESPUÉS de la dependencia de Spring Security.

---

## PASO 2: CREAR CLASE JwtUtil (JWT GENERATOR)

Crea el archivo: `src/main/java/com/courrier/backend/JwtUtil.java`

```java
package com.courrier.backend;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:tuSecretoSeguroDeDesarrollo123!@#}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}") // 24 horas en ms
    private long jwtExpiration;

    /**
     * Generar JWT Token
     */
    public String generarToken(String email, Long usuarioId, String rol) {
        System.out.println("🔐 [JwtUtil] Generando JWT para: " + email);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + jwtExpiration);

        String token = Jwts.builder()
                .subject(email)
                .claim("usuarioId", usuarioId)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        System.out.println("✅ JWT generado exitosamente para: " + email);
        return token;
    }

    /**
     * Extraer email del JWT
     */
    public String extraerEmail(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            System.err.println("❌ Error al extraer email del JWT: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validar JWT Token
     */
    public boolean validarToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            System.out.println("✅ JWT validado correctamente");
            return true;
        } catch (Exception e) {
            System.err.println("❌ JWT inválido o expirado: " + e.getMessage());
            return false;
        }
    }
}
```

---

## PASO 3: AGREGAR JWT A application.properties

En `src/main/resources/application.properties`, agrega:

```properties
# === JWT Configuration ===
jwt.secret=${JWT_SECRET:tuSecretoSeguroDeDesarrollo123!@#}
jwt.expiration=86400000
```

**En Railway:** Agrega variable de entorno `JWT_SECRET` con un valor seguro.

---

## PASO 4: ACTUALIZAR UsuarioController - LOGIN Y REGISTRO

En `UsuarioController.java`, inyecta JwtUtil:

```java
@Autowired
private JwtUtil jwtUtil;
```

Luego actualiza los métodos `login()` y `registro()`:

### Método login() - ANTES vs DESPUÉS

**ANTES:**
```java
return ResponseEntity.ok(Map.of(
    "mensaje", "Login exitoso",
    "id", usuarioEncontrado.getId(),
    "email", usuarioEncontrado.getEmail(),
    "rol", usuarioEncontrado.getRol()
));
```

**DESPUÉS:**
```java
// 🔐 GENERAR JWT
String jwtToken = jwtUtil.generarToken(
    usuarioEncontrado.getEmail(),
    usuarioEncontrado.getId(),
    usuarioEncontrado.getRol()
);

return ResponseEntity.ok(Map.of(
    "mensaje", "Login exitoso",
    "id", usuarioEncontrado.getId(),
    "nombre", usuarioEncontrado.getNombre(),
    "email", usuarioEncontrado.getEmail(),
    "rol", usuarioEncontrado.getRol(),
    "token", jwtToken  // ✅ NUEVO: JWT token
));
```

### Método registro() - ANTES vs DESPUÉS

**ANTES:**
```java
return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
    "mensaje", "Registro exitoso",
    "id", usuarioGuardado.getId(),
    "email", usuarioGuardado.getEmail(),
    "rol", usuarioGuardado.getRol()
));
```

**DESPUÉS:**
```java
// 🔐 GENERAR JWT
String jwtToken = jwtUtil.generarToken(
    usuarioGuardado.getEmail(),
    usuarioGuardado.getId(),
    usuarioGuardado.getRol()
);

return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
    "mensaje", "Registro exitoso",
    "id", usuarioGuardado.getId(),
    "nombre", usuarioGuardado.getNombre(),
    "email", usuarioGuardado.getEmail(),
    "rol", usuarioGuardado.getRol(),
    "token", jwtToken  // ✅ NUEVO: JWT token
));
```

---

## PASO 5: COMPILAR Y TESTEAR

```bash
# Compilar
mvn clean compile -DskipTests

# Si BUILD SUCCESS, hacer commit y push
git add -A
git commit -m "feat: Implement JWT token generation in login and registro"
git push origin main
```

---

## RESULTADO ESPERADO

Después de estos cambios, `POST /api/usuarios/login` devolverá:

```json
{
  "mensaje": "Login exitoso",
  "id": 1,
  "nombre": "Cliente Test",
  "email": "cliente@test.com",
  "rol": "CLIENTE",
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjbGllbnRlQHRlc3QuY29tIiwiY..."
}
```

Luego el Frontend almacena `token` y lo envía en cada petición:

```javascript
const token = data.token;
const response = await fetch('/api/paquetes', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
// ✅ Ahora funcionará sin Error 403
```

---

## Checklist de Implementación

- [ ] Agregar dependencia JJWT a pom.xml
- [ ] Crear JwtUtil.java
- [ ] Agregar propiedades JWT a application.properties
- [ ] Inyectar JwtUtil en UsuarioController
- [ ] Actualizar método login() para generar JWT
- [ ] Actualizar método registro() para generar JWT
- [ ] Compilar: `mvn clean compile`
- [ ] Commit y push
- [ ] Testear en Postman/Frontend
- [ ] Verificar que GET /api/paquetes ahora funciona con token

