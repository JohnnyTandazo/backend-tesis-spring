# API JSON Response Specification para Frontend

## 1. GET /api/facturas/usuario/{usuarioId}

**Descripción**: Obtiene todas las facturas de un usuario, incluyendo datos del envío asociado.

**Response 200 OK**:
```json
[
  {
    "id": 15,
    "numeroFactura": "FAC-2026-001",
    "estado": "PAGADO",
    "monto": 150.00,
    "descripcion": "Envío USA-001",
    "fechaEmision": "2026-02-01T10:30:00",
    "fechaVencimiento": "2026-02-15T00:00:00",
    "envioId": 14,
    "envio": {
      "id": 14,
      "numeroTracking": "USA-001",
      "descripcion": "Laptop HP",
      "estado": "EN_TRANSITO",
      "pesoLibras": 5.5,
      "valorDeclarado": 1200.00,
      "costoEnvio": 17.00,
      "fechaCreacion": "2026-02-01T09:00:00",
      "fechaEntrega": null,
      "categoria": "A",
      "destinatarioNombre": "Juan Pérez",
      "destinatarioCiudad": "Miami",
      "destinatarioDireccion": "123 Main St, Miami FL 33101",
      "destinatarioTelefono": "305-555-1234"
    }
  },
  {
    "id": 16,
    "numeroFactura": "FAC-2026-002",
    "estado": "PENDIENTE",
    "monto": 85.50,
    "descripcion": "Envío USA-002",
    "fechaEmision": "2026-02-01T11:00:00",
    "fechaVencimiento": "2026-02-15T00:00:00",
    "envioId": 15,
    "envio": {
      "id": 15,
      "numeroTracking": "USA-002",
      "estado": "PENDIENTE",
      ...
    }
  }
]
```

**Campos Clave**:
- `envioId`: **SIEMPRE presente** - ID del envío (nullable si factura sin envío)
- `envio`: **Objeto completo** - Información del paquete/envío
- `estado`: 'PENDIENTE', 'PAGADO', 'VENCIDA', 'ANULADA'

---

## 2. GET /api/facturas/pendientes?usuarioId={id}

**Descripción**: Obtiene solo las facturas PENDIENTES de un usuario (para dropdown de pago).

**Response 200 OK**:
```json
[
  {
    "id": 16,
    "numeroFactura": "FAC-2026-002",
    "estado": "PENDIENTE",
    "monto": 85.50,
    "fechaEmision": "2026-02-01T11:00:00",
    "envioId": 15,
    "envio": {
      "id": 15,
      "numeroTracking": "USA-002",
      "estado": "PENDIENTE",
      "destinatarioNombre": "Ana García",
      ...
    }
  }
]
```

---

## 3. GET /api/pagos?usuarioId={id}

**Descripción**: Obtiene el historial de pagos realizados por el usuario.

**Response 200 OK**:
```json
[
  {
    "id": 5,
    "monto": 150.00,
    "metodoPago": "TARJETA_CREDITO",
    "estado": "CONFIRMADO",
    "fecha": "2026-02-01T10:45:00",
    "comprobante": "compr_001.pdf",
    "referencia": "TRX-12345",
    "descripcion": null,
    "facturaId": 15
  },
  {
    "id": 6,
    "monto": 50.00,
    "metodoPago": "TRANSFERENCIA",
    "estado": "CONFIRMADO",
    "fecha": "2026-02-01T11:30:00",
    "comprobante": null,
    "referencia": "TRANS-67890",
    "descripcion": "Pago parcial",
    "facturaId": 16
  }
]
```

**Campos Clave**:
- `facturaId`: **SIEMPRE presente** - ID de la factura asociada (para linkear con envío)
- `monto`: Cantidad pagada
- `fecha`: Timestamp del pago
- `estado`: 'PENDIENTE', 'CONFIRMADO', 'RECHAZADO'

---

## 4. GET /api/pagos/factura/{facturaId}

**Descripción**: Obtiene pagos de una factura específica.

**Response 200 OK**:
```json
[
  {
    "id": 5,
    "monto": 150.00,
    "metodoPago": "TARJETA_CREDITO",
    "estado": "CONFIRMADO",
    "fecha": "2026-02-01T10:45:00",
    "facturaId": 15
  }
]
```

---

## 5. POST /api/pagos (Registrar Pago)

**Content-Type**: `multipart/form-data`

**Request**:
```
facturaId: 15
monto: 150.00
metodoPago: TARJETA_CREDITO
referencia: TRX-12345
comprobante: [archivo PDF opcional]
```

**Response 201 CREATED**:
```json
{
  "id": 5,
  "monto": 150.00,
  "metodoPago": "TARJETA_CREDITO",
  "estado": "CONFIRMADO",
  "fecha": "2026-02-01T10:45:00",
  "comprobante": "compr_12345.pdf",
  "referencia": "TRX-12345",
  "facturaId": 15
}
```

---

## 6. Flujo de Sincronización (Transactional)

Cuando registras un pago (`POST /api/pagos`), automáticamente:

1. **Pago se crea** → Estado: CONFIRMADO
2. **Factura se actualiza** → Estado: PAGADO
3. **Envío se actualiza** → Estado: EN_TRANSITO (NEW!)

```
POST /api/pagos (facturaId=15, monto=150)
  ↓
  ├─→ Pago 5 creado (CONFIRMADO)
  ├─→ Factura 15 → Estado PAGADO
  └─→ Envío 14 → Estado EN_TRANSITO ✅
```

Esto significa que si consultas `GET /api/facturas/usuario/{id}` después de un pago, verás:
```json
{
  "id": 15,
  "estado": "PAGADO",
  "envio": {
    "id": 14,
    "estado": "EN_TRANSITO"  // ← Actualizado automáticamente
  }
}
```

---

## 7. @JsonIgnore - Campos Que NO Se Serializan

Para evitar loops infinitos, los siguientes campos se ignoran:

- **Factura.usuario** → @JsonIgnore (Usuario sería redundante)
- **Pago.factura** → @JsonIgnore (Ya tienes facturaId)
- **Envio.usuario** → @JsonIgnore (Usuario sería redundante)

**Por qué**: Previene estructuras recursivas como:
```
Factura { usuario { facturas { usuario { ... infinito } } } }
```

---

## 8. Dashboard "Mis Envíos" - Cómo Linkear Datos

**Frontend Logic**:
```javascript
// GET /api/facturas/usuario/{usuarioId}
const facturas = await fetch(endpoint).then(r => r.json());

facturas.forEach(factura => {
  const envio = factura.envio; // ← Objeto completo aquí
  const estado = envio.estado; // ← "EN_TRANSITO", "PENDIENTE", etc.
  const facturaNro = factura.numeroFactura;
  const estado_pago = factura.estado; // ← "PAGADO", "PENDIENTE"
  
  // Renderizar en tabla
  displayRow({
    tracking: envio.numeroTracking,
    destino: envio.destinatarioCiudad,
    estado_envio: envio.estado,
    factura: facturaNro,
    estado_pago: factura.estado,
    monto: factura.monto
  });
});
```

---

## 9. Historial de Pagos - Cómo Renderizar

**Frontend Logic**:
```javascript
// GET /api/pagos?usuarioId={usuarioId}
const pagos = await fetch(endpoint).then(r => r.json());

pagos.forEach(pago => {
  displayPaymentRow({
    fecha: pago.fecha,
    monto: pago.monto,
    metodo: pago.metodoPago,
    estado: pago.estado,
    factura_id: pago.facturaId,
    referencia: pago.referencia
  });
});
```

---

## 10. Resumen de Cambios (Commit b9e9ce8)

✅ `Factura.java`: @JsonIgnore en usuario, envio se serializa completo
✅ `Pago.java`: @JsonIgnore en factura, getter getFacturaId() expone ID
✅ `Envio.java`: @JsonIgnore en usuario

**Resultado**: APIs listas para Frontend con datos completos y sin ciclos infinitos.

---

Última actualización: 2026-02-01
Backend Version: v1.0.0
