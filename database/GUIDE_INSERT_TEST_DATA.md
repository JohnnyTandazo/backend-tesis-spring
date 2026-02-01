# 📝 Guía: Insertar Datos de Prueba en Railway

## 🎯 Objetivo

Cargar datos de prueba (facturas y pagos) en la base de datos Railway para testing del módulo de Pagos.

---

## 📋 Archivos SQL Proporcionados

1. **insert_facturas_prueba.sql** - Inserta 3 facturas de prueba
2. **insert_pagos_prueba.sql** - Inserta 2 pagos de prueba

---

## 🚀 PASO 1: Acceder a Railway Database

1. Abre tu panel de Railway: https://railway.app
2. Selecciona tu proyecto
3. Haz clic en la base de datos MySQL
4. Selecciona la pestaña **"Data"** (o "Database" según la versión)
5. Verás una interfaz para ejecutar SQL

---

## 🔍 PASO 2: Verificar Usuarios Existentes

Antes de insertar facturas, necesitas saber qué usuario_id usar.

**En Railway Data tab, ejecuta:**

```sql
SELECT id, nombre, email, rol FROM usuarios LIMIT 10;
```

**Resultado esperado:**
```
| id | nombre              | email           | rol      |
|----|---------------------|-----------------|----------|
| 1  | Argely Estudiante   | cliente@test.com| CLIENTE  |
| 2  | Sr. Operador        | operador@test...| OPERADOR |
```

✅ **Toma nota del usuario_id que quieres usar (normalmente será 1)**

---

## 💾 PASO 3: Insertar Facturas

En Railway Data tab, **copia y pega** el contenido del archivo:

**database/insert_facturas_prueba.sql**

```sql
INSERT INTO facturas (
    numero_factura,
    monto,
    estado,
    descripcion,
    fecha_emision,
    fecha_vencimiento,
    usuario_id,
    created_at
) VALUES
('FAC-2026-001', 350.00, 'PENDIENTE', 'Envío USA-001: Laptop HP y Mouse', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 15 DAY), 1, NOW()),
('FAC-2026-002', 50.00, 'PENDIENTE', 'Envío USA-002: Ropa Shein', CURDATE(), DATE_ADD(CURDATE(), INTERVAL 10 DAY), 1, NOW()),
('FAC-2026-003', 100.00, 'PAGADA', 'Servicio de envío expedito', DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 20 DAY), 1, NOW());
```

**Resultado esperado:**
```
Query OK, 3 rows affected
```

---

## ✅ PASO 4: Verificar Facturas Insertadas

En Railway Data tab, ejecuta:

```sql
SELECT 
    id,
    numero_factura,
    monto,
    estado,
    fecha_emision,
    fecha_vencimiento,
    usuario_id
FROM facturas
WHERE usuario_id = 1
ORDER BY id DESC;
```

**Deberías ver 3 facturas recién insertadas**

---

## 💳 PASO 5: Insertar Pagos (Opcional)

En Railway Data tab, ejecuta el contenido de:

**database/insert_pagos_prueba.sql**

```sql
INSERT INTO pagos (
    factura_id,
    monto,
    metodo_pago,
    estado,
    fecha,
    referencia,
    descripcion,
    created_at
) VALUES
(3, 100.00, 'TARJETA_CREDITO', 'CONFIRMADO', DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'TRX-2026-00001', 'Pago con tarjeta de crédito Visa', NOW()),
(1, 200.00, 'TRANSFERENCIA', 'PENDIENTE', CURDATE(), 'TRANSF-2026-00002', 'Transferencia bancaria iniciada', NOW());
```

**Resultado esperado:**
```
Query OK, 2 rows affected
```

---

## 🔍 PASO 6: Verificar Pagos Insertados

```sql
SELECT 
    p.id,
    f.numero_factura,
    p.monto,
    p.metodo_pago,
    p.estado,
    p.fecha
FROM pagos p
INNER JOIN facturas f ON p.factura_id = f.id
WHERE f.usuario_id = 1
ORDER BY p.id DESC;
```

---

## 🧪 PASO 7: Probar los Endpoints del Frontend

Ahora que tienes datos en la BD, prueba estos endpoints:

### Obtener facturas pendientes (dropdown)
```
GET http://localhost:8080/api/facturas/pendientes?usuarioId=1
```

**Respuesta esperada:**
```json
[
  {
    "id": 2,
    "numeroFactura": "FAC-2026-002",
    "monto": 50.00,
    "estado": "PENDIENTE",
    "fechaVencimiento": "2026-02-11"
  },
  {
    "id": 1,
    "numeroFactura": "FAC-2026-001",
    "monto": 350.00,
    "estado": "PENDIENTE",
    "fechaVencimiento": "2026-02-16"
  }
]
```

### Obtener historial de pagos
```
GET http://localhost:8080/api/pagos?usuarioId=1
```

### Registrar un nuevo pago
```
POST http://localhost:8080/api/pagos
Content-Type: application/json

{
  "monto": 150.00,
  "metodoPago": "TRANSFERENCIA",
  "estado": "CONFIRMADO",
  "factura": {
    "id": 1
  },
  "referencia": "TRANSF-2026-00003"
}
```

---

## 🔧 SOLUCIÓN DE PROBLEMAS

### ❌ Error: "usuario_id no existe"
**Solución:** Cambia el usuario_id en el SQL al ID que verificaste en PASO 2

### ❌ Error: "factura_id no existe" (al insertar pagos)
**Solución:** Usa los IDs reales de las facturas que se insertaron

### ❌ Error: "Duplicate entry for numero_factura"
**Solución:** Usa números de factura diferentes, o borra los datos previos:
```sql
DELETE FROM pagos WHERE factura_id IN (SELECT id FROM facturas WHERE numero_factura LIKE 'FAC-2026-%');
DELETE FROM facturas WHERE numero_factura LIKE 'FAC-2026-%';
```

### ❌ Error: "Column not found"
**Solución:** Verifica que las tablas existan:
```sql
SHOW TABLES LIKE 'factura%';
SHOW TABLES LIKE 'pago%';
DESC facturas;
DESC pagos;
```

---

## 📊 Referencia Rápida

| Campo | Valores | Ejemplo |
|-------|---------|---------|
| estado (facturas) | PENDIENTE, PAGADA, VENCIDA, ANULADA | PENDIENTE |
| estado (pagos) | PENDIENTE, CONFIRMADO, RECHAZADO | CONFIRMADO |
| metodo_pago | TARJETA_CREDITO, TRANSFERENCIA, EFECTIVO, CHEQUE | TRANSFERENCIA |

---

## ✨ Próximos Pasos

1. ✅ Datos cargados en BD
2. ✅ Endpoints funcionando sin error 404
3. 🔄 Frontend puede consumir `/api/facturas/pendientes`
4. 🔄 Módulo de Pagos y Facturación completamente funcional

