# 🎯 ESPECIFICACIÓN EXACTA DE API - DIRECCIONES

**Documento para: Equipo de Frontend**  
**Fecha:** 2026-02-01  
**Versión:** 1.0  
**Status:** ✅ Listo para producción

---

## 📌 CREAR DIRECCIÓN (POST)

### URL
```
POST http://localhost:8080/api/direcciones
```

### Headers Requeridos
```
Content-Type: application/json
```

### Body Exacto (JSON)

```json
{
  "usuarioId": 1,
  "alias": "Casa",
  "callePrincipal": "Calle 10 # 25-50",
  "calleSecundaria": "Entre carreras 5 y 6",
  "ciudad": "Bogotá",
  "telefono": "3001234567",
  "referencia": "Puerta azul, portero disponible"
}
```

### Campos Requeridos (OBLIGATORIOS)
| Campo | Tipo | Ejemplo | Validación |
|-------|------|---------|-----------|
| `usuarioId` | Integer | `1` | **SÍ, OBLIGATORIO** - Must be > 0, usuario debe existir |
| `alias` | String | `"Casa"` | **SÍ, OBLIGATORIO** - No puede estar vacío |
| `callePrincipal` | String | `"Calle 10 # 25-50"` | **SÍ, OBLIGATORIO** - No puede estar vacío |
| `ciudad` | String | `"Bogotá"` | **SÍ, OBLIGATORIO** - No puede estar vacío |
| `telefono` | String | `"3001234567"` | **SÍ, OBLIGATORIO** - No puede estar vacío |

### Campos Opcionales (PUEDEN OMITIRSE)
| Campo | Tipo | Ejemplo |
|-------|------|---------|
| `calleSecundaria` | String | `"Entre carreras 5 y 6"` |
| `referencia` | String | `"Puerta azul, portero disponible"` |
| `esPrincipal` | Boolean | `true` / `false` |

> ⚠️ **NOTA:** `esPrincipal` se asigna automáticamente (la primera dirección = true)

### Respuesta Exitosa (201 Created)

```json
{
  "id": 15,
  "alias": "Casa",
  "callePrincipal": "Calle 10 # 25-50",
  "calleSecundaria": "Entre carreras 5 y 6",
  "ciudad": "Bogotá",
  "telefono": "3001234567",
  "referencia": "Puerta azul, portero disponible",
  "esPrincipal": true,
  "fechaCreacion": "2026-02-01T10:30:00",
  "usuario": {
    "id": 1,
    "nombre": "Juan Pérez",
    "email": "juan@example.com"
  }
}
```

### Respuestas de Error

#### ❌ Error 400 - Campo faltante

```json
{
  "error": "El campo 'alias' es requerido (Ej: Casa, Oficina)"
}
```

#### ❌ Error 400 - usuarioId inválido

```json
{
  "error": "usuarioId no puede ser nulo o menor que 1"
}
```

#### ❌ Error 400 - Usuario no existe

```json
{
  "error": "Usuario no encontrado con ID: 999"
}
```

---

## 📌 OBTENER DIRECCIONES (GET)

### URL Exacta
```
GET http://localhost:8080/api/direcciones?usuarioId=1
```

> ⚠️ **IMPORTANTE:** `usuarioId` es **OBLIGATORIO** como parámetro de URL

### Respuesta Exitosa (200 OK)

```json
[
  {
    "id": 15,
    "alias": "Casa",
    "callePrincipal": "Calle 10 # 25-50",
    "calleSecundaria": "Entre carreras 5 y 6",
    "ciudad": "Bogotá",
    "telefono": "3001234567",
    "referencia": "Puerta azul",
    "esPrincipal": true,
    "fechaCreacion": "2026-02-01T10:30:00",
    "usuario": {
      "id": 1,
      "nombre": "Juan Pérez",
      "email": "juan@example.com"
    }
  },
  {
    "id": 16,
    "alias": "Oficina",
    "callePrincipal": "Carrera 7 # 32-10",
    "calleSecundaria": "",
    "ciudad": "Medellín",
    "telefono": "604-5678901",
    "referencia": "Edificio administrativo",
    "esPrincipal": false,
    "fechaCreacion": "2026-02-01T11:15:00",
    "usuario": {
      "id": 1,
      "nombre": "Juan Pérez",
      "email": "juan@example.com"
    }
  }
]
```

---

## ✅ RESPUESTAS A TUS PREGUNTAS

### Pregunta 1: ¿El campo `usuarioId` es obligatorio en el JSON?

**RESPUESTA: SÍ, ABSOLUTAMENTE OBLIGATORIO**

- Debe estar en el body del POST
- Debe ser un número entero > 0
- Debe corresponder a un usuario que existe en la BD
- Si falta o es inválido, recibirás error 400

### Pregunta 2: ¿Para listar direcciones (GET), la URL exacta debe ser `/api/direcciones?usuarioId=1`?

**RESPUESTA: SÍ, EXACTAMENTE ASÍ**

Estructura correcta:
- Base: `http://localhost:8080/api/direcciones`
- Parámetro: `?usuarioId=1`
- URL Completa: `http://localhost:8080/api/direcciones?usuarioId=1`

**NO funciona sin el parámetro:**
- ❌ `GET /api/direcciones` → Error 400
- ✅ `GET /api/direcciones?usuarioId=1` → OK

---

## 🔐 RESUMEN PARA EL FRONTEND

### POST (Crear Dirección)
```
📍 Endpoint: POST http://localhost:8080/api/direcciones
📍 Header: Content-Type: application/json
📍 Body: { usuarioId, alias, callePrincipal, ciudad, telefono, ... }
📍 Response: 201 Created (con ID asignado)
```

### GET (Listar Direcciones)
```
📍 Endpoint: GET http://localhost:8080/api/direcciones?usuarioId=1
📍 Response: 200 OK (array de direcciones)
```

### Validaciones Estrictas en Backend
- ✅ usuarioId > 0
- ✅ alias ≠ vacío
- ✅ callePrincipal ≠ vacío
- ✅ ciudad ≠ vacío
- ✅ telefono ≠ vacío
- ✅ usuario existe en BD

---

## 📋 COPIAR Y PEGAR - EJEMPLO COMPLETO

### Para Frontend Developer:

```javascript
// CREAR DIRECCIÓN
const crearDireccion = async (usuarioId) => {
  const payload = {
    "usuarioId": 1,
    "alias": "Casa",
    "callePrincipal": "Calle 10 # 25-50",
    "calleSecundaria": "Entre carreras 5 y 6",
    "ciudad": "Bogotá",
    "telefono": "3001234567",
    "referencia": "Puerta azul"
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
    console.log('✅ Creada:', newDireccion);
    return newDireccion;
  } else {
    const error = await response.json();
    console.error('❌ Error:', error.error);
    throw error;
  }
};

// OBTENER DIRECCIONES
const obtenerDirecciones = async (usuarioId) => {
  const response = await fetch(`http://localhost:8080/api/direcciones?usuarioId=${usuarioId}`);
  
  if (response.ok) {
    const direcciones = await response.json();
    console.log('✅ Direcciones:', direcciones);
    return direcciones;
  } else {
    const error = await response.json();
    console.error('❌ Error:', error.error);
    throw error;
  }
};
```

---

## 🚀 URLs SEGÚN AMBIENTE

| Ambiente | URL |
|----------|-----|
| Desarrollo (Local) | `http://localhost:8080/api/direcciones` |
| Producción (Railway) | `https://backend-tesis-spring.onrender.com/api/direcciones` |

---

**¿Preguntas o dudas? Revisar los logs del servidor cuando haya error 400/500**
