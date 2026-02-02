# 🔍 AUDITORÍA DEL FLUJO DE TRABAJO DEL OPERADOR
**Fecha:** 2026-02-01  
**Analista:** GitHub Copilot (Claude Sonnet 4.5)  
**Criticidad:** ⚠️ ALTA - ESLABÓN PERDIDO DETECTADO

---

## 📊 RESUMEN EJECUTIVO

**VEREDICTO:** ❌ **INCOMPLETO - FALTA LÓGICA CRÍTICA DE FACTURACIÓN**

El sistema tiene **DOS MUNDOS DESCONECTADOS**:
- ✅ `Envio` (nacional/internacional con tracking) → **SÍ genera factura automáticamente**
- ❌ `Paquete` (pre-alerta desde Miami) → **NO genera factura automáticamente**

---

## 🔬 HALLAZGOS DETALLADOS

### 1️⃣ PUNTO DE INVESTIGACIÓN: ¿Qué JSON envía el Frontend?

**Endpoint:** `PUT /api/paquetes/{id}/detalles`

**Payload esperado:**
```json
{
  "estado": "EN_TRANSITO",
  "pesoLibras": 4.5,
  "precio": 120.00,
  "categoria": "A"
}
```

**Campos procesados:**
- ✅ `estado` → Actualiza Paquete.estado
- ✅ `pesoLibras` → Actualiza Paquete.pesoLibras
- ✅ `precio` → Actualiza Paquete.precio
- ✅ `categoria` → Actualiza Paquete.categoria

---

### 2️⃣ PUNTO DE INVESTIGACIÓN: ¿Existe lógica de creación de Factura?

**Archivo:** `PaqueteController.java`, líneas 129-165

```java
@PutMapping("/{id}/detalles")
public Paquete actualizarDetallesPaquete(@PathVariable Long id, 
                                        @RequestBody Map<String, Object> payload) {
    Paquete paquete = paqueteRepo.findById(id).orElseThrow();

    // Actualiza estado
    if (payload.get("estado") != null) {
        paquete.setEstado((String) payload.get("estado"));
    }

    // Actualiza peso
    if (payload.get("pesoLibras") != null) {
        paquete.setPesoLibras(Double.valueOf(payload.get("pesoLibras").toString()));
    }

    // Actualiza precio
    if (payload.get("precio") != null) {
        paquete.setPrecio(Double.valueOf(payload.get("precio").toString()));
    }

    // GUARDA Y RETORNA
    Paquete paqueteActualizado = paqueteRepo.save(paquete);
    return paqueteActualizado;
}
```

**❌ PROBLEMA CRÍTICO DETECTADO:**
```
NO HAY CÓDIGO QUE DIGA: "SI precio > 0, CREAR NUEVA FACTURA"
```

El método simplemente:
1. ✅ Recibe el precio
2. ✅ Lo guarda en la tabla `paquetes`
3. ❌ **SE OLVIDA - NO GENERA FACTURA**

---

### 3️⃣ PUNTO DE INVESTIGACIÓN: ¿Cómo funciona en el modelo `Envio`?

**Comparación con `EnvioService.java` (líneas 113-134):**

```java
// ✅ EJEMPLO DE CÓDIGO CORRECTO (EnvioService)
public Envio crearEnvio(CrearEnvioRequest request) {
    
    // Calcular costo
    Double costoTotal = 5.0 + (peso * 2.0) + (valorDeclarado * 0.01);
    envio.setCostoEnvio(costoTotal);
    
    Envio guardado = envioRepository.save(envio);
    
    // ════════════════════════════════════════
    // ✅ GENERACIÓN AUTOMÁTICA DE FACTURA
    // ════════════════════════════════════════
    if (guardado.getUsuario() != null) {
        Factura factura = new Factura();
        factura.setUsuario(guardado.getUsuario());
        factura.setEnvioId(guardado.getId());
        factura.setMonto(guardado.getCostoEnvio());  // ← AQUÍ SE COBRA
        factura.setEstado("PENDIENTE");
        factura.setDescripcion("Envío " + guardado.getNumeroTracking());
        factura.setNumeroFactura("FAC-2026-" + guardado.getId());
        
        facturaRepository.save(factura);
        System.out.println("✅ Factura creada: $" + factura.getMonto());
    }
    
    return guardado;
}
```

**🎯 ESTE ES EL CÓDIGO QUE FALTA EN `PaqueteController`**

---

## 🚨 IMPACTO DEL PROBLEMA

### Escenario Actual (CON EL BUG):

1. ✅ Cliente crea pre-alerta → `Paquete` creado (precio = 0, peso = 0)
2. ✅ Operador recibe paquete en Miami
3. ✅ Operador pesa la caja: 4.5 lbs
4. ✅ Operador calcula costo: $120
5. ✅ Operador guarda: `PUT /api/paquetes/{id}/detalles`
6. ❌ **NADA PASA** → El precio se guarda en BD pero **NO HAY FACTURA**
7. ❌ Cliente NO ve deuda en su dashboard
8. ❌ Cajero NO puede cobrar (no hay registro en `facturas`)

### Resultado:
```
💰 Operador trabajó gratis
📦 Paquete con costo $120 guardado en BD
💸 $0 facturas generadas
```

---

## ✅ SOLUCIÓN PROPUESTA

### Opción A: Creación Automática (RECOMENDADO)

Agregar lógica en `PaqueteController.actualizarDetallesPaquete()`:

```java
// DESPUÉS DE GUARDAR EL PAQUETE
if (paquete.getPrecio() != null && paquete.getPrecio() > 0 && 
    paquete.getEstado().equals("EN_TRANSITO")) {
    
    // Verificar si ya existe factura para este paquete
    List<Factura> facturasExistentes = facturaRepository
        .findByDescripcion("Paquete " + paquete.getTrackingNumber());
    
    if (facturasExistentes.isEmpty()) {
        // Crear nueva factura
        Factura factura = new Factura();
        factura.setUsuario(paquete.getUsuario());
        factura.setMonto(paquete.getPrecio());
        factura.setEstado("PENDIENTE");
        factura.setDescripcion("Paquete " + paquete.getTrackingNumber());
        factura.setNumeroFactura("FCT-PKG-" + paquete.getId());
        
        facturaRepository.save(factura);
        System.out.println("✅ Factura AUTO-GENERADA: $" + factura.getMonto());
    }
}
```

### Opción B: Botón Manual "Generar Factura"

Crear nuevo endpoint:
```
POST /api/paquetes/{id}/facturar
```

❌ **NO RECOMENDADO** - Requiere acción manual y puede olvidarse

---

## 📋 RECOMENDACIONES

1. **INMEDIATO:** Implementar Opción A en `PaqueteController`
2. **TESTING:** Crear test unitario para verificar facturación automática
3. **LOGGING:** Agregar logs detallados cuando se genere factura desde paquete
4. **DOCUMENTACIÓN:** Actualizar diagramas de flujo

---

## 📎 ARCHIVOS AFECTADOS

| Archivo | Líneas | Estado | Acción Requerida |
|---------|--------|--------|------------------|
| `PaqueteController.java` | 129-165 | ❌ Incompleto | Agregar lógica de facturación |
| `EnvioService.java` | 113-134 | ✅ Completo | Usar como referencia |
| `FacturaRepository.java` | - | ✅ OK | Sin cambios |

---

## 🎯 CONCLUSIÓN

**El eslabón perdido está en `PaqueteController.java`**

El sistema es capaz de transformar un "Paquete Pesado" en una "Factura Por Pagar", pero **la lógica NO ESTÁ IMPLEMENTADA**.

Actualmente:
- ✅ `Envio` → Factura automática ✅
- ❌ `Paquete` → Solo guarda precio, no factura ❌

**SIGUIENTE PASO:** Implementar generación automática de factura cuando:
1. `precio > 0`
2. `estado = 'EN_TRANSITO'`
3. No exista factura previa para ese paquete
