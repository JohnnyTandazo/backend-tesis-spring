# 🚀 CHEAT SHEET - FRONTEND INTEGRATION GUIDE

**Backend API URL:** `https://backend-tesis-spring-production.up.railway.app`

---

## ⚠️ PROBLEMA IDENTIFICADO: FALTA GENERACIÓN DE JWT

**Status Actual:**
- ✅ Login funciona y retorna datos del usuario
- ❌ **NO devuelve JWT Token**
- ❌ Frontend no puede autenticarse en peticiones posteriores

**Por eso da Error 403 FORBIDDEN en GET /api/paquetes**

---

## 📋 ENDPOINTS POR CATEGORÍA

### 🔓 PÚBLICOS (Sin autenticación)

```
1. REGISTRO DE USUARIO
   POST /api/usuarios/registro
   Body: {
     "nombre": "Juan Pérez",
     "email": "juan@example.com",
     "password": "contraseña123",
     "telefono": "0999999999"
   }
   Response: {
     "mensaje": "Registro exitoso",
     "id": 1,
     "nombre": "Juan Pérez",
     "email": "juan@example.com",
     "rol": "CLIENTE"
   }

2. LOGIN
   POST /api/usuarios/login
   Body: {
     "email": "juan@example.com",
     "password": "contraseña123"
   }
   Response: {
     "mensaje": "Login exitoso",
     "id": 1,
     "nombre": "Juan Pérez",
     "email": "juan@example.com",
     "rol": "CLIENTE"
   }
   
   ⚠️ PROBLEMA: NO devuelve JWT Token
   ❌ Frontend no sabe qué enviar en Authorization header
```

---

### 🔒 PRIVADOS (Requieren JWT - CLIENTE)

```
1. LISTAR MIS PAQUETES
   GET /api/paquetes
   Header: Authorization: Bearer <JWT_TOKEN_AQUI>
   Response: [
     {
       "id": 1,
       "trackingNumber": "USA-001",
       "descripcion": "Laptop HP",
       "peso": 4.5,
       "precio": 350.00,
       "estado": "EN_MIAMI",
       "usuarioId": 1
     }
   ]
   Rol: CLIENTE, OPERADOR, ADMIN
   Nota: CLIENTE solo ve sus paquetes. OPERADOR/ADMIN ven todos.

2. BUSCAR PAQUETE POR TRACKING (PÚBLICO)
   GET /api/paquetes/rastreo/{tracking}
   Parámetro: tracking = "USA-001"
   Response: { ...paquete... }
   Rol: PÚBLICO (no requiere autenticación)

3. OBTENER MIS ENVÍOS
   GET /api/envios/usuario/{usuarioId}
   Parámetro: usuarioId = ID del usuario logueado
   Header: Authorization: Bearer <JWT_TOKEN>
   Response: [
     {
       "id": 1,
       "numeroTracking": "USA-001",
       "descripcion": "Envío Miami",
       "estado": "EN_TRANSITO",
       "usuarioId": 1
     }
   ]
   Rol: CLIENTE (solo puede ver los suyos)
   
   ⚠️ IMPORTANTE: {usuarioId} debe ser el ID del usuario autenticado

4. OBTENER MIS FACTURAS
   GET /api/facturas/usuario/{usuarioId}
   Parámetro: usuarioId = ID del usuario logueado
   Header: Authorization: Bearer <JWT_TOKEN>
   Response: [
     {
       "id": 1,
       "numeroFactura": "FAC-2026-001",
       "monto": 350.00,
       "estado": "PENDIENTE",
       "descripcion": "Envío USA-001",
       "usuarioId": 1
     }
   ]
   Rol: CLIENTE (solo puede ver las suyas)
   
   ⚠️ IMPORTANTE: {usuarioId} debe ser el ID del usuario autenticado

5. OBTENER MIS DIRECCIONES
   GET /api/direcciones/usuario/{usuarioId}
   Parámetro: usuarioId = ID del usuario logueado
   Header: Authorization: Bearer <JWT_TOKEN>
   Response: [
     {
       "id": 1,
       "calle": "Av. Amazonas 123",
       "ciudad": "Quito",
       "provincia": "Pichincha",
       "codigoPostal": "170150",
       "usuario_id": 1
     }
   ]
   Rol: CLIENTE
```

---

## 🔐 CONFIGURACIÓN ACTUAL DE CORS

**Orígenes Permitidos:**
- ✅ `https://v0-currier-tics-layout.vercel.app` (Frontend en Vercel)
- ✅ `http://localhost:3000` (Desarrollo local)
- ✅ `http://localhost:8080` (Mismo host)

**Headers Requeridos:**
```javascript
// En cada petición privada, envía:
{
  "Authorization": "Bearer <JWT_TOKEN>",
  "Content-Type": "application/json"
}
```

---

## 🚨 PROBLEMA CRÍTICO: FALTA JWT

### Síntomas:
```
✅ POST /api/usuarios/login → 200 OK (devuelve datos de usuario)
❌ GET /api/paquetes → 403 FORBIDDEN (Usuario no autenticado)
❌ GET /api/envios/usuario/1 → 403 FORBIDDEN
```

### Causa:
- Backend NO genera ni retorna JWT token en login
- Frontend NO sabe qué enviar en `Authorization` header
- Spring Security requiere `Authorization: Bearer <token>` válido

### Solución Requerida:
**Implementar JWT en UsuarioController:**

1. Agregar dependencia JWT (jsonwebtoken)
2. Generar JWT en `login()` y `registro()`
3. Devolver JWT en la respuesta
4. Crear JWT Filter para validar tokens en peticiones posteriores
5. Actualizar respuesta de login:

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody Usuario usuario) {
    // ... validaciones ...
    
    // ✅ GENERAR JWT
    String jwtToken = generarJWT(usuarioEncontrado);
    
    // ✅ RETORNAR JWT AL FRONTEND
    return ResponseEntity.ok(Map.of(
        "mensaje", "Login exitoso",
        "id", usuarioEncontrado.getId(),
        "nombre", usuarioEncontrado.getNombre(),
        "email", usuarioEncontrado.getEmail(),
        "rol", usuarioEncontrado.getRol(),
        "token", jwtToken  // ← JWT AQUI
    ));
}
```

6. Frontend almacena token:
```javascript
// Frontend (React/Vue)
const response = await fetch('/api/usuarios/login', {...});
const data = await response.json();
localStorage.setItem('jwtToken', data.token);
```

7. Frontend envía token en cada petición:
```javascript
const token = localStorage.getItem('jwtToken');
const response = await fetch('/api/paquetes', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

---

## ✅ USUARIOS DE PRUEBA

**Cliente:**
- Email: `cliente@test.com`
- Password: `12345`
- Rol: `CLIENTE`

**Operador:**
- Email: `operador@test.com`
- Password: `admin123`
- Rol: `OPERADOR`

---

## 📝 FLUJO ESPERADO (Una vez se implemente JWT)

```
1. Usuario abre app
2. POST /api/usuarios/login
   → Recibe: { "token": "eyJhbGc...", "id": 1, ... }
3. Frontend almacena token en localStorage
4. GET /api/paquetes
   + Header: Authorization: Bearer eyJhbGc...
   → Recibe: [{ paquete1 }, { paquete2 }]
5. GET /api/envios/usuario/1
   + Header: Authorization: Bearer eyJhbGc...
   → Recibe: [{ envio1 }, { envio2 }]
6. GET /api/facturas/usuario/1
   + Header: Authorization: Bearer eyJhbGc...
   → Recibe: [{ factura1 }, { factura2 }]
```

---

## 🎯 RESUMEN: QUÉ FALTA

| Ítem | Status | Crítico |
|------|--------|---------|
| CORS Config | ✅ OK | No |
| Login Endpoint | ✅ OK | No |
| Endpoints Privados | ✅ OK | No |
| **JWT Generation** | ❌ FALTA | **SÍ** |
| **JWT Filter** | ❌ FALTA | **SÍ** |
| **JWT en Response** | ❌ FALTA | **SÍ** |

**ACCIÓN INMEDIATA:** Implementar generación y validación de JWT en backend.

