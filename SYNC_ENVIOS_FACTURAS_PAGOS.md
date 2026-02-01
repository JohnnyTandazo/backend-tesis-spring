# 🔄 Flujo Integrado: Envíos → Facturas → Pagos

## 📋 Descripción General

El sistema ahora sincroniza automáticamente la creación de envíos con la generación de facturas y el procesamiento de pagos.

---

## 🔗 Flujo Completo

```
┌─────────────────────────────────────────────────────────────┐
│ 1. USUARIO CREA ENVÍO (POST /api/envios)                   │
│    JSON: {                                                   │
│      numeroTracking: "USA-001",                             │
│      pesoLibras: 4.5,                                       │
│      valorDeclarado: 350.00,                                │
│      usuarioId: 1                                           │
│    }                                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 2. ENVIO SERVICE - CÁLCULO DE COSTO                         │
│                                                             │
│    Fórmula: costo = 5.0 + (peso * 2.0) + (valor * 0.01)   │
│                                                             │
│    Base:         $5.00                                      │
│    Peso:         4.5 * 2.0  = $9.00                        │
│    Valor:        350 * 0.01 = $3.50                        │
│    ───────────────────────────                              │
│    TOTAL:        $17.50                                     │
│                                                             │
│    envio.setCostoEnvio(17.50)                              │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 3. ENVÍO GUARDADO EN BD                                     │
│                                                             │
│    Tabla: envios                                            │
│    ├─ id: 1                                                 │
│    ├─ numeroTracking: "USA-001"                             │
│    ├─ usuario_id: 1                                         │
│    └─ costo_envio: 17.50  ✅ NUEVO CAMPO                   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 4. AUTO-GENERACIÓN DE FACTURA                              │
│    (EnvioService - Inmediatamente después)                 │
│                                                             │
│    Tabla: facturas                                          │
│    ├─ id: 1                                                 │
│    ├─ numero_factura: "FAC-2026-000001"                     │
│    ├─ monto: 17.50 (= costoEnvio)                          │
│    ├─ estado: "PENDIENTE"                                   │
│    ├─ usuario_id: 1                                         │
│    ├─ envio_id: 1  ✅ VINCULACIÓN                          │
│    └─ fecha_vencimiento: HOY + 15 días                      │
│                                                             │
│    ✅ Factura lista para ser pagada                        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 5. USUARIO VE FACTURA PENDIENTE                            │
│    GET /api/facturas/pendientes?usuarioId=1               │
│                                                             │
│    Response: [                                              │
│      {                                                       │
│        id: 1,                                                │
│        numeroFactura: "FAC-2026-000001",                     │
│        monto: 17.50,                                         │
│        estado: "PENDIENTE",                                  │
│        descripcion: "Envío USA-001: ...",                   │
│        fechaVencimiento: "2026-02-16"                       │
│      }                                                       │
│    ]                                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 6. USUARIO REGISTRA PAGO (POST /api/pagos)                 │
│    JSON: {                                                   │
│      factura: { id: 1 },                                    │
│      monto: 17.50,                                          │
│      metodoPago: "TARJETA_CREDITO",                         │
│      estado: "CONFIRMADO",                                  │
│      referencia: "TRX-2026-00001"                           │
│    }                                                         │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 7. PAGO SERVICE - SINCRONIZACIÓN                           │
│                                                             │
│    Tabla: pagos                                             │
│    ├─ id: 1                                                 │
│    ├─ factura_id: 1                                         │
│    ├─ monto: 17.50                                          │
│    ├─ estado: "CONFIRMADO"                                  │
│    └─ fecha: NOW                                            │
│                                                             │
│    ✅ Pago guardado                                         │
│                                                             │
│    Luego, actualizar Factura:                              │
│    if (pago.estado == "CONFIRMADO" && pago.monto >= factura.monto)
│      → factura.estado = "PAGADA"                            │
│    else                                                     │
│      → factura.estado = "EN_REVISION"                       │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ↓
┌─────────────────────────────────────────────────────────────┐
│ 8. FACTURA ACTUALIZADA AUTOMÁTICAMENTE                      │
│                                                             │
│    Tabla: facturas                                          │
│    ├─ id: 1                                                 │
│    ├─ numeroFactura: "FAC-2026-000001"                      │
│    ├─ monto: 17.50                                          │
│    └─ estado: "PAGADA" ✅ CAMBIÓ AUTOMÁTICAMENTE            │
│                                                             │
│    La factura ya NO aparece en:                            │
│      GET /api/facturas/pendientes                          │
│                                                             │
│    Aparece en:                                              │
│      GET /api/pagos?usuarioId=1                            │
└─────────────────────────────────────────────────────────────┘
```

---

## 💰 Ejemplos de Cálculo de Costo

| Escenario | Peso | Valor | Cálculo | Total |
|-----------|------|-------|---------|-------|
| Laptop HP | 4.5 lbs | $350 | 5 + (4.5×2) + (350×0.01) | $17.50 |
| Ropa | 2.0 lbs | $50 | 5 + (2×2) + (50×0.01) | $10.50 |
| Documentos | 0.1 lbs | $100 | 5 + (0.1×2) + (100×0.01) | $6.20 |
| Piezas Pesadas | 10 lbs | $1000 | 5 + (10×2) + (1000×0.01) | $35.00 |

---

## 🔄 Estados de Factura

| Estado | Descripción | Transición |
|--------|-------------|-----------|
| **PENDIENTE** | Factura recién creada, sin pago | → EN_REVISION / PAGADA |
| **EN_REVISION** | Pago recibido, validando | → PAGADA / PENDIENTE |
| **PAGADA** | Pago confirmado y completado | ✅ Final |
| **VENCIDA** | Factura pasó fecha vencimiento | → Recordatorio |
| **ANULADA** | Factura cancelada | ✅ Final |

---

## 📊 Estados de Pago

| Estado | Descripción | Impacto en Factura |
|--------|-------------|-------------------|
| **PENDIENTE** | Pago iniciado | Factura: PENDIENTE |
| **CONFIRMADO** | Pago verificado | Factura: EN_REVISION / PAGADA |
| **RECHAZADO** | Pago falló | Factura: PENDIENTE |

---

## 🎯 Endpoints Principales

### Envíos
```
POST /api/envios
GET /api/envios/usuario/{usuarioId}
```

### Facturas (Auto-generadas)
```
GET /api/facturas/pendientes?usuarioId={id}
GET /api/facturas/usuario/{usuarioId}
```

### Pagos
```
POST /api/pagos
GET /api/pagos?usuarioId={id}
```

---

## 🔍 Rastreo Completo

**Ejemplo:** Rastrear un envío desde creación hasta pago

```sql
-- 1. Ver envío creado
SELECT id, numeroTracking, costo_envio FROM envios WHERE id = 1;

-- 2. Ver factura auto-generada
SELECT id, numero_factura, monto, estado, envio_id FROM facturas WHERE envio_id = 1;

-- 3. Ver pago registrado
SELECT id, factura_id, monto, estado FROM pagos WHERE factura_id = 1;

-- 4. Verificar sincronización
SELECT 
  e.numeroTracking,
  e.costo_envio,
  f.numero_factura,
  f.estado as factura_estado,
  p.estado as pago_estado
FROM envios e
LEFT JOIN facturas f ON e.id = f.envio_id
LEFT JOIN pagos p ON f.id = p.factura_id
WHERE e.id = 1;
```

---

## 📝 Migración SQL Necesaria

**Archivo:** `database/sync_envios_facturas_pagos.sql`

```sql
ALTER TABLE envios ADD COLUMN costo_envio DOUBLE;
ALTER TABLE facturas ADD COLUMN envio_id BIGINT;
```

**Ejecución:**
1. Abre Railway Database > Data tab
2. Pega el contenido de `sync_envios_facturas_pagos.sql`
3. Ejecuta

---

## ✨ Beneficios

✅ **Automatización:** Factura se genera automáticamente  
✅ **Sincronización:** Pago actualiza factura en tiempo real  
✅ **Rastreo:** Envío → Factura → Pago completamente vinculados  
✅ **Sin Errores:** Validaciones automáticas de montos  
✅ **Logs Detallados:** Debug fácil con logs de cada paso  
✅ **Escalable:** Base para futuras integraciones de pago

---

## 🚀 Próximos Pasos

1. ✅ Ejecutar migración SQL
2. ✅ Crear envío
3. ✅ Verificar factura auto-generada
4. ✅ Registrar pago
5. ✅ Confirmar sincronización de estado

