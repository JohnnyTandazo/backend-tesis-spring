# Testing Endpoints - cURL Examples

**Base URL**: `http://localhost:8080` (local) o `https://courrier.railway.app` (producción)

---

## 1. Obtener Facturas de un Usuario

```bash
curl -X GET "http://localhost:8080/api/facturas/usuario/1" \
  -H "Content-Type: application/json"
```

**Response** (200 OK):
```json
[
  {
    "id": 15,
    "numeroFactura": "FAC-2026-001",
    "estado": "PAGADO",
    "monto": 150.00,
    "envioId": 14,
    "envio": {
      "id": 14,
      "numeroTracking": "USA-001",
      "estado": "EN_TRANSITO",
      "destinatarioNombre": "Juan Pérez",
      "destinatarioCiudad": "Miami",
      "pesoLibras": 5.5,
      "valorDeclarado": 1200.00,
      "costoEnvio": 17.00
    }
  }
]
```

---

## 2. Obtener Facturas PENDIENTES (para Dropdown)

```bash
curl -X GET "http://localhost:8080/api/facturas/pendientes?usuarioId=1" \
  -H "Content-Type: application/json"
```

**Response** (200 OK):
```json
[
  {
    "id": 16,
    "numeroFactura": "FAC-2026-002",
    "estado": "PENDIENTE",
    "monto": 85.50,
    "envioId": 15,
    "envio": {
      "id": 15,
      "numeroTracking": "USA-002",
      "estado": "PENDIENTE"
    }
  }
]
```

---

## 3. Obtener Historial de Pagos

```bash
curl -X GET "http://localhost:8080/api/pagos?usuarioId=1" \
  -H "Content-Type: application/json"
```

**Response** (200 OK):
```json
[
  {
    "id": 5,
    "monto": 150.00,
    "metodoPago": "TARJETA_CREDITO",
    "estado": "CONFIRMADO",
    "fecha": "2026-02-01T10:45:00",
    "referencia": "TRX-12345",
    "facturaId": 15
  },
  {
    "id": 6,
    "monto": 50.00,
    "metodoPago": "TRANSFERENCIA",
    "estado": "CONFIRMADO",
    "fecha": "2026-02-01T11:30:00",
    "facturaId": 16
  }
]
```

---

## 4. Obtener Pagos de una Factura Específica

```bash
curl -X GET "http://localhost:8080/api/pagos/factura/15" \
  -H "Content-Type: application/json"
```

**Response** (200 OK):
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

## 5. Registrar un Pago (POST - Multipart)

```bash
curl -X POST "http://localhost:8080/api/pagos" \
  -F "facturaId=16" \
  -F "monto=85.50" \
  -F "metodoPago=TARJETA_CREDITO" \
  -F "referencia=TRX-67890" \
  -F "comprobante=@/path/to/receipt.pdf"
```

**Response** (201 CREATED):
```json
{
  "id": 6,
  "monto": 85.50,
  "metodoPago": "TARJETA_CREDITO",
  "estado": "CONFIRMADO",
  "fecha": "2026-02-01T11:30:00",
  "comprobante": "compr_67890.pdf",
  "referencia": "TRX-67890",
  "facturaId": 16
}
```

**Logs en Backend** (verás en Railway):
```
╔════════════════════════════════════════════════════════╗
║ INICIO: REGISTRAR PAGO (TRANSACTIONAL)                ║
╚════════════════════════════════════════════════════════╝

💰 [PagoService] Registrando nuevo pago: $85.50
   📌 Factura ID: 16

📍 PASO 1: Buscando factura...
   ✓ Factura encontrada:
     • envio_id (Campo): 15
     • envio (Objeto): CARGADO ID=15

📍 PASO 5: Sincronizando estado del envío...
   ✓ Envio encontrado por objeto. ID: 15
   ✓ ÉXITO: Envío sincronizado correctamente

╔════════════════════════════════════════════════════════╗
║ FIN: REGISTRO DE PAGO COMPLETADO                       ║
╚════════════════════════════════════════════════════════╝
```

---

## 6. Verificar Sincronización (GET Factura Específica)

Después de registrar el pago anterior, consulta:

```bash
curl -X GET "http://localhost:8080/api/facturas/16" \
  -H "Content-Type: application/json"
```

**Response** (200 OK):
```json
{
  "id": 16,
  "numeroFactura": "FAC-2026-002",
  "estado": "PAGADO",        // ← ACTUALIZADO
  "monto": 85.50,
  "envioId": 15,
  "envio": {
    "id": 15,
    "numeroTracking": "USA-002",
    "estado": "EN_TRANSITO",  // ← ACTUALIZADO (era PENDIENTE)
    "destinatarioNombre": "Ana García",
    "destinatarioCiudad": "Miami"
  }
}
```

✅ **Esto demuestra que la sincronización funcionó correctamente**

---

## 7. Caso de Uso: Dashboard "Mis Envíos"

**Paso 1**: Obtén facturas
```bash
curl -X GET "http://localhost:8080/api/facturas/usuario/1"
```

**Paso 2**: Itera y renderiza
```javascript
data.forEach(factura => {
  console.log(`
    Tracking: ${factura.envio.numeroTracking}
    Estado Envío: ${factura.envio.estado}
    Destino: ${factura.envio.destinatarioCiudad}
    Factura: ${factura.numeroFactura}
    Estado Pago: ${factura.estado}
    Monto: $${factura.monto}
  `);
});
```

**Output**:
```
Tracking: USA-001
Estado Envío: EN_TRANSITO
Destino: Miami
Factura: FAC-2026-001
Estado Pago: PAGADO
Monto: $150.00

Tracking: USA-002
Estado Envío: EN_TRANSITO
Destino: Miami
Factura: FAC-2026-002
Estado Pago: PAGADO
Monto: $85.50
```

---

## 8. Caso de Uso: Historial de Pagos

**Paso 1**: Obtén pagos
```bash
curl -X GET "http://localhost:8080/api/pagos?usuarioId=1"
```

**Paso 2**: Renderiza tabla
```javascript
data.forEach(pago => {
  console.log(`
    Fecha: ${pago.fecha}
    Monto: $${pago.monto}
    Método: ${pago.metodoPago}
    Estado: ${pago.estado}
    Factura: #${pago.facturaId}
  `);
});
```

**Output**:
```
Fecha: 2026-02-01T10:45:00
Monto: $150.00
Método: TARJETA_CREDITO
Estado: CONFIRMADO
Factura: #15

Fecha: 2026-02-01T11:30:00
Monto: $85.50
Método: TARJETA_CREDITO
Estado: CONFIRMADO
Factura: #16
```

---

## 9. Errores Comunes y Soluciones

### Error: Factura sin Envío Asociado
**Response** (200 OK):
```json
{
  "id": 17,
  "numeroFactura": "FAC-2026-003",
  "estado": "PENDIENTE",
  "envioId": null,
  "envio": null
}
```

**Causa**: La factura se creó sin `envio_id` en BD.
**Solución**: Verificar datos en Railway DB.

### Error: No existe envío con ID
**Logs del Backend**:
```
❌ ERROR: No existe envío con ID 999 en la BD.
❌ Revisar integridad referencial: envios.id = 999
```

**Causa**: envio_id en facturas apunta a un ID inexistente.
**Solución**: Ejecutar SQL de integridad referencial.

### Error: CircularReference (JSON Loop)
**Si ves**: `{"usuario": {"facturas": {"usuario": ...}}}`

**Causa**: @JsonIgnore no está aplicado correctamente.
**Solución**: Verificar que las anotaciones están en su lugar (Commit b9e9ce8).

---

## 10. PowerShell Testing (Windows)

```powershell
# Obtener facturas
$response = Invoke-WebRequest -Uri "http://localhost:8080/api/facturas/usuario/1" `
  -Headers @{"Content-Type"="application/json"} `
  -Method Get

$data = $response.Content | ConvertFrom-Json
$data | ForEach-Object {
  Write-Host "Factura: $($_.numeroFactura) | Estado: $($_.estado) | Envío: $($_.envio.numeroTracking)"
}
```

---

## 11. Python Testing

```python
import requests
import json

BASE_URL = "http://localhost:8080"

# Obtener facturas
response = requests.get(f"{BASE_URL}/api/facturas/usuario/1")
facturas = response.json()

for factura in facturas:
    print(f"""
    Factura: {factura['numeroFactura']}
    Estado: {factura['estado']}
    Envío: {factura['envio']['numeroTracking']}
    Destino: {factura['envio']['destinatarioCiudad']}
    """)

# Registrar pago
files = {
    'facturaId': (None, '16'),
    'monto': (None, '85.50'),
    'metodoPago': (None, 'TARJETA_CREDITO'),
    'referencia': (None, 'TRX-67890'),
    'comprobante': ('receipt.pdf', open('path/to/receipt.pdf', 'rb'))
}

response = requests.post(f"{BASE_URL}/api/pagos", files=files)
pago = response.json()
print(f"Pago registrado: {pago['id']}")
```

---

Última actualización: 2026-02-01
API Version: v1.0.0
