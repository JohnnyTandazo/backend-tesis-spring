# 🎯 ESPECIFICACIÓN DE API - CREAR ENVÍO (POST)

**Documento para: Equipo de Frontend**  
**Fecha:** 2026-02-01  
**Versión:** 1.0  
**Status:** ✅ Listo para producción

---

## 📌 CREAR ENVÍO NACIONAL (POST)

### URL
```
POST http://localhost:8080/api/envios
```

### Headers Requeridos
```
Content-Type: application/json
```

### Body Exacto (JSON) - EJEMPLO VÁLIDO

```json
{
  "usuarioId": 1,
  "numeroTracking": "NAC-001",
  "descripcion": "Laptop HP color plata",
  "pesoLibras": 3.5,
  "valorDeclarado": 850.00,
  "estado": "EN_MIAMI",
  "categoria": "A",
  "usuario": {
    "id": 1
  }
}
```

---

## 📋 CAMPOS - OBLIGATORIOS vs OPCIONALES

### Campos OBLIGATORIOS (DEBEN IR EN EL JSON)

| Campo | Tipo | Ejemplo | Validación |
|-------|------|---------|-----------|
| `usuarioId` | Integer | `1` | Usuario debe existir en BD |
| `numeroTracking` | String | `"NAC-001"` | No puede estar vacío |
| `descripcion` | String | `"Laptop HP"` | No puede estar vacío |
| `pesoLibras` | Double | `3.5` | Valor > 0 |
| `valorDeclarado` | Double | `850.00` | Valor > 0 |
| `estado` | String | `"EN_MIAMI"` | Ver estados válidos abajo |
| `categoria` | String | `"A"` | A, B, C, etc. |

### Campos OPCIONALES (PUEDEN OMITIRSE)
- `fechaEntrega` - Se asigna automáticamente cuando estado = "ENTREGADO"

---

## 🚩 VALORES VÁLIDOS

### Estado (MAYÚSCULAS)
```
"EN_MIAMI"      - Envío en Miami
"EN_TRANSITO"   - En camino a destino
"ENTREGADO"     - Enviado completamente
"PENDIENTE"     - Pendiente de procesamiento
```

> ⚠️ **IMPORTANTE:** Los estados deben ir en **MAYÚSCULAS con GUIONES BAJOS**

### Categoría
```
"A" - Categoría A
"B" - Categoría B
"C" - Categoría C
```

---

## ✅ RESPUESTA EXITOSA (201 Created)

```json
{
  "id": 15,
  "numeroTracking": "NAC-001",
  "descripcion": "Laptop HP color plata",
  "pesoLibras": 3.5,
  "valorDeclarado": 850.00,
  "estado": "EN_MIAMI",
  "fechaCreacion": "2026-02-01T10:30:00",
  "fechaEntrega": null,
  "categoria": "A",
  "usuario": {
    "id": 1,
    "nombre": "Juan Pérez",
    "email": "juan@example.com"
  }
}
```

---

## ❌ POSIBLES ERRORES 500 y CAUSAS

### Error 500 - Campo faltante obligatorio

```json
{
  "error": "El campo 'numeroTracking' no puede estar vacío"
}
```

**Causa probable:** Falta `numeroTracking` en el JSON

### Error 500 - Usuario no existe

```json
{
  "error": "Usuario no encontrado con ID: 999"
}
```

**Causa:** El `usuarioId` no existe en la BD

### Error 500 - Tipo de dato incorrecto

```json
{
  "error": "pesoLibras debe ser un número decimal"
}
```

**Causa:** Enviaste string en lugar de number
- ❌ `"pesoLibras": "3.5"` → Error
- ✅ `"pesoLibras": 3.5` → OK

### Error 500 - Estado inválido

```json
{
  "error": "Estado 'en_miami' inválido. Usa mayúsculas: EN_MIAMI"
}
```

**Causa:** Estado en minúsculas
- ❌ `"estado": "en_miami"` → Error
- ✅ `"estado": "EN_MIAMI"` → OK

---

## 📊 COMPARACIÓN: ENVIOS vs PAQUETES

| Concepto | ENVIOS | PAQUETES |
|----------|--------|----------|
| **Tipo** | Salida (Outbound) | Entrada (Inbound) |
| **Endpoint** | `/api/envios` | `/api/paquetes` |
| **Flujo** | Usuario → Destino | Proveedor → Centro |
| **usuarioId** | ✅ Obligatorio | ❌ No tiene |
| **Ejemplo** | Enviando laptop a cliente | Recibiendo paquete de Amazon |

---

## 🔍 VALIDACIONES ESTRICTAS EN BACKEND

✅ `usuarioId` > 0 y existe en BD  
✅ `numeroTracking` ≠ vacío  
✅ `descripcion` ≠ vacío  
✅ `pesoLibras` > 0  
✅ `valorDeclarado` > 0  
✅ `estado` en valores permitidos  
✅ `categoria` ≠ vacío  

---

## 📝 EJEMPLO JAVASCRIPT - COPIAR Y PEGAR

```javascript
// CREAR ENVÍO - Para Frontend
const crearEnvio = async (usuarioId) => {
  const payload = {
    "usuarioId": 1,
    "numeroTracking": "NAC-001",
    "descripcion": "Laptop HP color plata",
    "pesoLibras": 3.5,
    "valorDeclarado": 850.00,
    "estado": "EN_MIAMI",
    "categoria": "A",
    "usuario": {
      "id": 1
    }
  };

  try {
    const response = await fetch('http://localhost:8080/api/envios', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(payload)
    });

    if (response.ok) {
      const newEnvio = await response.json();
      console.log('✅ Envío creado:', newEnvio);
      return newEnvio;
    } else {
      const error = await response.json();
      console.error('❌ Error:', error);
      throw error;
    }
  } catch (err) {
    console.error('❌ Error en request:', err);
    throw err;
  }
};

// OBTENER ENVÍO - Por ID
const obtenerEnvio = async (envioId) => {
  const response = await fetch(`http://localhost:8080/api/envios/${envioId}`);
  const envio = await response.json();
  console.log('✅ Envío obtenido:', envio);
  return envio;
};

// OBTENER ENVÍOS DEL USUARIO
const obtenerEnviosUsuario = async (usuarioId) => {
  const response = await fetch(`http://localhost:8080/api/envios/usuario/${usuarioId}`);
  const envios = await response.json();
  console.log('✅ Envíos del usuario:', envios);
  return envios;
};

// ACTUALIZAR ESTADO
const actualizarEstado = async (envioId, nuevoEstado) => {
  const response = await fetch(`http://localhost:8080/api/envios/${envioId}/estado?nuevoEstado=${nuevoEstado}`, {
    method: 'PUT'
  });
  const envio = await response.json();
  console.log('✅ Estado actualizado:', envio);
  return envio;
};
```

---

## 📌 RESPUESTA A TUS PREGUNTAS

### ¿El campo `tipo` debe ser "ENVIO" o "envio"?

**RESPUESTA:** No existe un campo `tipo` en la entidad Envio.

**Lo que existe es:**
- `estado` - El estado actual del envío (EN_MIAMI, EN_TRANSITO, ENTREGADO)
- `categoria` - Categoría del envío (A, B, C)

**No confundir con:**
- Envios ← Son enviados POR el usuario (outbound)
- Paquetes ← Son recibidos POR el usuario (inbound)

---

## 🚀 ENDPOINTS DISPONIBLES

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| **POST** | `/api/envios` | Crear envío |
| **GET** | `/api/envios` | Listar todos |
| **GET** | `/api/envios/{id}` | Obtener por ID |
| **GET** | `/api/envios/usuario/{usuarioId}` | Envíos del usuario |
| **GET** | `/api/envios/tracking/{numeroTracking}` | Buscar por tracking |
| **PUT** | `/api/envios/{id}` | Actualizar envío |
| **PUT** | `/api/envios/{id}/estado?nuevoEstado=X` | Cambiar estado |
| **DELETE** | `/api/envios/{id}` | Eliminar envío |

---

## 🚀 URLs SEGÚN AMBIENTE

| Ambiente | URL |
|----------|-----|
| Desarrollo (Local) | `http://localhost:8080/api/envios` |
| Producción (Railway) | `https://backend-tesis-spring.onrender.com/api/envios` |

---

## 📝 CHECKLIST ANTES DE ENVIAR REQUEST

- [ ] ¿`usuarioId` está en el JSON?
- [ ] ¿El usuario con ese ID existe en BD?
- [ ] ¿`numeroTracking` no está vacío?
- [ ] ¿`pesoLibras` y `valorDeclarado` son números (no strings)?
- [ ] ¿`estado` está en MAYÚSCULAS? (EN_MIAMI, no en_miami)
- [ ] ¿Todos los campos obligatorios están presentes?
- [ ] ¿Headers incluyen `Content-Type: application/json`?

---

**¿Problema resuelto? Si aún recibes error 500, comparte el mensaje de error exacto en los logs del servidor.**
