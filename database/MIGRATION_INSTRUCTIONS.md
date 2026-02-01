# 🗄️ Guía Completa de Migraciones de Base de Datos

## 📋 Resumen

Este documento explica cómo ejecutar todas las migraciones de esquema necesarias para el sistema.

### Archivos de Migración:
1. **update_schema.sql** - Campos Snapshot para tabla envios
2. **create_facturas_pagos.sql** - Nuevas tablas: facturas y pagos

---

## 🎯 MIGRACIÓN 1: Campos Snapshot (envios)

Se agregarán 4 columnas nuevas a la tabla `envios`:

| Columna | Tipo | Propósito |
|---------|------|----------|
| `destinatario_nombre` | VARCHAR(255) | Nombre del destinatario (Snapshot) |
| `destinatario_ciudad` | VARCHAR(255) | Ciudad de destino (Snapshot) |
| `destinatario_direccion` | VARCHAR(500) | Dirección completa (Snapshot) |
| `destinatario_telefono` | VARCHAR(20) | Teléfono contacto (Snapshot) |

---

## 🎯 MIGRACIÓN 2: Módulo de Facturas y Pagos (NUEVO)

Se crearán 2 nuevas tablas:

### Tabla `facturas`
| Campo | Tipo | Descripción |
|-------|------|------------|
| id | BIGINT | Clave primaria |
| monto | DOUBLE | Monto de la factura |
| estado | VARCHAR(50) | PENDIENTE, PAGADA, VENCIDA, ANULADA |
| numero_factura | VARCHAR(50) | Número único (ej: FAC-2026-001) |
| fecha_emision | DATETIME | Fecha de generación |
| fecha_vencimiento | DATETIME | Fecha de vencimiento |
| usuario_id | BIGINT | FK a usuarios |

### Tabla `pagos`
| Campo | Tipo | Descripción |
|-------|------|------------|
| id | BIGINT | Clave primaria |
| monto | DOUBLE | Monto pagado |
| metodo_pago | VARCHAR(50) | TARJETA_CREDITO, TRANSFERENCIA, etc |
| estado | VARCHAR(50) | PENDIENTE, CONFIRMADO, RECHAZADO |
| fecha | DATETIME | Fecha del pago |
| factura_id | BIGINT | FK a facturas |

---

## 🚀 EJECUTAR MIGRACIONES

### OPCIÓN 1: MySQL Workbench (Recomendado)

1. **Abre MySQL Workbench**
2. **Conecta a tu base de datos** `railway`
3. **Abre una nueva pestaña de Query** (Ctrl + T)
4. **Copia el contenido** de `database/update_schema.sql`
5. **Pega en el editor** y ejecuta (Ctrl + Enter)
6. **Repite pasos 3-5 con** `database/create_facturas_pagos.sql`

---

### OPCIÓN 2: Línea de Comandos (Terminal)

#### Windows:

```cmd
:: Migración 1: Campos Snapshot
mysql -h crossover.proxy.rlwy.net -P 56796 -u root -p railway < database/update_schema.sql

:: Migración 2: Tablas Facturas y Pagos
mysql -h crossover.proxy.rlwy.net -P 56796 -u root -p railway < database/create_facturas_pagos.sql
```

**Contraseña:** `avqwMdVVvsHdnxsajCCrEcMSUEBInunA`

#### Linux/Mac:

```bash
mysql -h crossover.proxy.rlwy.net -P 56796 -u root -p railway < database/update_schema.sql
mysql -h crossover.proxy.rlwy.net -P 56796 -u root -p railway < database/create_facturas_pagos.sql
```

---

### OPCIÓN 3: DBeaver IDE

1. Conecta a la BD Railway
2. Haz clic derecho en la BD `railway`
3. **SQL → Execute Script**
4. Pega el contenido de los archivos SQL
5. Ejecuta

---

## ✅ Validación Post-Ejecución

### Verificar tabla envios (Snapshot)

```sql
SELECT COLUMN_NAME, COLUMN_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'envios' AND COLUMN_NAME LIKE 'destinatario%';
```

### Verificar tablas nuevas

```sql
SHOW TABLES LIKE 'factura%';
SHOW TABLES LIKE 'pago%';
```

### Ver datos de prueba

```sql
SELECT * FROM facturas WHERE usuario_id = 1;
SELECT * FROM pagos;
```

---

## ⚙️ Configuración Java (Automática)

Después de ejecutar el SQL:

1. **Reinicia la aplicación Spring Boot**
2. **Hibernate detectará automáticamente** las nuevas columnas y tablas
3. **Los datos de prueba se cargarán** automáticamente (CargaDeDatos.java)

---

## ⚠️ Rollback (Si algo sale mal)

### Deshacer Migración 1:

```sql
ALTER TABLE envios DROP COLUMN destinatario_nombre;
ALTER TABLE envios DROP COLUMN destinatario_ciudad;
ALTER TABLE envios DROP COLUMN destinatario_direccion;
ALTER TABLE envios DROP COLUMN destinatario_telefono;
```

### Deshacer Migración 2:

```sql
DROP TABLE IF EXISTS pagos;
DROP TABLE IF EXISTS facturas;
```

---

## 📊 Endpoints Disponibles Después de Migración

### Facturas
- `GET /api/facturas/pendientes?usuarioId={id}` - Dropdown de facturas pendientes
- `GET /api/facturas/usuario/{usuarioId}` - Todas las facturas del usuario
- `POST /api/pagos` - Registrar pago

### Pagos
- `GET /api/pagos?usuarioId={id}` - Historial de pagos del usuario
- `POST /api/pagos` - Registrar nuevo pago

---

## 🔒 Seguridad

- ✅ Las columnas permiten NULL (no se pierden datos)
- ✅ No se borra información existente
- ✅ Compatible con Hibernate auto-update
- ✅ Claves foráneas con ON DELETE CASCADE
- ✅ Índices en campos de búsqueda

---

## 📞 Soporte

Si encuentras errores, verifica:
- ✅ Conexión a la BD remota Railway
- ✅ Permisos del usuario `root`
- ✅ Las tablas existen después de ejecutar CREATE
- ✅ Logs de Spring Boot muestran sin errores de FK

