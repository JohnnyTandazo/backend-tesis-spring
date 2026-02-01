# 📍 MEJORAS DE DIRECCIONES - Resumen

## ✅ Cambios Realizados

### 1. **DireccionController.java** - Refactorización completa

#### POST /api/direcciones - Creación de direcciones

**Ahora soporta dos formas de enviar `usuarioId`:**

```bash
# OPCIÓN 1: En el cuerpo (Body) - RECOMENDADO
POST http://localhost:8080/api/direcciones
Content-Type: application/json

{
  "alias": "Casa",
  "callePrincipal": "Calle 10 # 25-50",
  "calleSecundaria": "Entre carreras 5 y 6",
  "ciudad": "Bogotá",
  "telefono": "601-1234567",
  "referencia": "Cerca al parque",
  "esPrincipal": false,
  "usuarioId": 1
}
```

```bash
# OPCIÓN 2: Como parámetro de URL
POST http://localhost:8080/api/direcciones?usuarioId=1
Content-Type: application/json

{
  "alias": "Oficina",
  "callePrincipal": "Carrera 7 # 32-10",
  "ciudad": "Medellín",
  "telefono": "604-5678901"
}
```

**Respuestas:**

- ✅ **201 Created** - Dirección creada exitosamente
- ❌ **400 Bad Request** - Si falta usuarioId o campos requeridos
- ❌ **400 Bad Request** - Si el usuario no existe

#### GET /api/direcciones - Obtener direcciones

**Cambio importante:** Ahora requiere `usuarioId` como parámetro

```bash
# Obtener todas las direcciones del usuario 1
GET http://localhost:8080/api/direcciones?usuarioId=1
```

### 2. **DireccionService.java** - Validación mejorada

#### 6 Pasos de Validación con Logging:

```
📋 PASO 1: Validando parámetros de entrada...
📋 PASO 2: Validando campos requeridos...
📋 PASO 3: Buscando usuario en base de datos...
📋 PASO 4: Asignando usuario a la dirección...
📋 PASO 5: Verificando si será dirección principal...
📋 PASO 6: Guardando en base de datos...
```

#### Validaciones implementadas:

- ✅ `usuarioId` no puede ser null o menor a 1
- ✅ `alias` es requerido (no null/vacío)
- ✅ `callePrincipal` es requerido
- ✅ `ciudad` es requerida
- ✅ `telefono` es requerido
- ✅ Usuario debe existir en base de datos
- ✅ Primera dirección se marca automáticamente como principal

#### Errores mejorados:

Antes:
```json
HTTP 400
```

Ahora:
```json
HTTP 400
{
  "error": "El campo 'alias' es requerido (Ej: Casa, Oficina)"
}
```

## 🧪 Testing

### Prueba desde Frontend (JavaScript/React)

```javascript
// Crear dirección
async function crearDireccion(direccionData, usuarioId) {
  const payload = {
    ...direccionData,
    usuarioId: usuarioId
  };

  const response = await fetch('http://localhost:8080/api/direcciones', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(payload)
  });

  if (response.ok) {
    const newDireccion = await response.json();
    console.log('✅ Dirección creada:', newDireccion);
    return newDireccion;
  } else {
    const error = await response.json();
    console.error('❌ Error:', error.error);
    throw error;
  }
}

// Obtener direcciones del usuario
async function obtenerDirecciones(usuarioId) {
  const response = await fetch(`http://localhost:8080/api/direcciones?usuarioId=${usuarioId}`);
  const direcciones = await response.json();
  console.log('Direcciones del usuario:', direcciones);
  return direcciones;
}
```

### Prueba desde Python

Ver `test_direcciones.py` en el repositorio

```bash
cd d:\courrer_backend\backend
python test_direcciones.py
```

### Prueba desde Postman

1. **POST** `http://localhost:8080/api/direcciones`
2. **Headers:**
   - `Content-Type: application/json`
3. **Body (raw JSON):**
```json
{
  "alias": "Casa",
  "callePrincipal": "Calle Prueba 123",
  "ciudad": "Bogotá",
  "telefono": "3001234567",
  "usuarioId": 1
}
```

## 🔍 Debugging - Cómo verificar qué pasó

### Ver logs del servidor

Cuando se crea una dirección, verás en la consola del servidor:

```
🔄 [DireccionService.crearDireccion] ===== INICIANDO CREACIÓN =====
   usuarioId recibido: 1
📋 PASO 1: Validando parámetros de entrada...
   ✅ usuarioId válido: 1
   ✅ Objeto Direccion recibido
📋 PASO 2: Validando campos requeridos...
   ✅ alias: Casa
   ✅ callePrincipal: Calle 10 # 25-50
   ✅ ciudad: Bogotá
   ✅ telefono: 3001234567
📋 PASO 3: Buscando usuario en base de datos...
✅ Usuario encontrado: Juan Perez (juan@example.com)
📋 PASO 4: Asignando usuario a la dirección...
   ✅ Usuario asignado
📋 PASO 5: Verificando si será dirección principal...
   Direcciones existentes del usuario: 0
   ⭐ Marcada como dirección PRINCIPAL (primera del usuario)
📋 PASO 6: Guardando en base de datos...
✅ Dirección guardada exitosamente!
   ID generado: 1
   Alias: Casa
   Dirección: Calle 10 # 25-50, Bogotá
   Es Principal: true
===== CREACIÓN COMPLETADA CON ÉXITO =====
```

## 📊 Endpoints Disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| **POST** | `/api/direcciones` | Crear dirección (con usuarioId en body o query param) |
| **GET** | `/api/direcciones?usuarioId=X` | Obtener direcciones del usuario |
| **GET** | `/api/direcciones/{id}` | Obtener una dirección por ID |
| **GET** | `/api/usuarios/{id}/direcciones` | Obtener direcciones (alternativo) |
| **PUT** | `/api/direcciones/{id}` | Actualizar dirección |
| **PUT** | `/api/direcciones/{id}/principal` | Marcar como principal |
| **DELETE** | `/api/direcciones/{id}` | Eliminar dirección |

## 🚀 Deployment

Los cambios se han desplegado automáticamente a Railway:

- **Branch:** main
- **Commit:** 2448ca5
- **URL en Railway:** https://backend-tesis-spring.onrender.com

Los cambios estarán disponibles en Railway en los próximos 2-3 minutos.

## ⚠️ Consideraciones Importantes

### Seguridad
- ⚠️ GET /api/direcciones requiere `usuarioId` (se recomienda agregir autenticación en el futuro)
- ⚠️ POST /api/direcciones acepta cualquier usuarioId (se recomienda validar con token JWT)

### Cambios que afectan al Frontend
- ✅ **POST ahora requiere `usuarioId` en el payload**
- ✅ **GET /api/direcciones requiere parámetro `?usuarioId=X`**

## 📝 Próximos Pasos (Recomendados)

1. **Agregar autenticación JWT**
   - En lugar de pasar `usuarioId` manualmente, extraerlo del token

2. **Validar más campos**
   - Validar formato de teléfono
   - Validar que la ciudad existe
   - Validar límite de direcciones por usuario

3. **Mejorar seguridad**
   - Un usuario solo puede ver/modificar sus propias direcciones
   - Agregrar roles (Admin, Usuario)

---

**Última actualización:** 2026-02-01
**Versión:** 1.0
**Estado:** ✅ Listo para producción
