# 🗄️ Actualización de Esquema de Base de Datos - Guía de Ejecución

## 📋 Resumen

Este documento explica cómo ejecutar la migración de esquema para agregar los campos de **Snapshot** a la tabla `envios` en MySQL.

---

## 🎯 ¿Qué se va a hacer?

Se agregarán 4 columnas nuevas a la tabla `envios`:

| Columna | Tipo | Tamaño | Propósito |
|---------|------|--------|----------|
| `destinatario_nombre` | VARCHAR | 255 | Nombre del destinatario (Snapshot) |
| `destinatario_ciudad` | VARCHAR | 255 | Ciudad de destino (Snapshot) |
| `destinatario_direccion` | VARCHAR | 500 | Dirección completa (Snapshot) |
| `destinatario_telefono` | VARCHAR | 20 | Teléfono contacto (Snapshot) |

---

## 🚀 OPCIÓN 1: Ejecutar en MySQL Workbench

### Pasos:

1. **Abre MySQL Workbench**
2. **Conecta a tu base de datos** `railway`
3. **Abre una nueva pestaña de Query** (Ctrl + T)
4. **Copia el contenido** de `database/update_schema.sql`
5. **Pega en el editor** de Query
6. **Selecciona todas las sentencias** (Ctrl + A)
7. **Ejecuta** (Ctrl + Enter o botón ejecutar ⚡)

**Output esperado:**
```
Query OK, 0 rows affected (0.05 sec)
Query OK, 0 rows affected (0.04 sec)
Query OK, 0 rows affected (0.04 sec)
Query OK, 0 rows affected (0.03 sec)
```

---

## 🚀 OPCIÓN 2: Ejecutar vía Terminal MySQL

### Windows (CMD):

```cmd
mysql -h crossover.proxy.rlwy.net -P 56796 -u root -p railway < database/update_schema.sql
```

Cuando pida contraseña, ingresa:
```
avqwMdVVvsHdnxsajCCrEcMSUEBInunA
```

### Linux/Mac:

```bash
mysql -h crossover.proxy.rlwy.net -P 56796 -u root -p railway < database/update_schema.sql
```

---

## 🚀 OPCIÓN 3: Ejecutar vía DBeaver

1. **Conecta a la BD Railway**
2. **Click derecho** en tabla `envios`
3. **Script SQL → Execute Script**
4. **Pega el contenido** de `update_schema.sql`
5. **Ejecuta el script**

---

## ✅ Validación Post-Ejecución

Después de ejecutar, verifica que las columnas se crearon:

```sql
SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'envios' AND COLUMN_NAME LIKE 'destinatario%'
ORDER BY ORDINAL_POSITION;
```

**Output esperado:**

| COLUMN_NAME | COLUMN_TYPE | IS_NULLABLE |
|------------|-------------|------------|
| destinatario_nombre | varchar(255) | YES |
| destinatario_ciudad | varchar(255) | YES |
| destinatario_direccion | varchar(500) | YES |
| destinatario_telefono | varchar(20) | YES |

---

## ⚙️ Configuración Java (Automática)

Después de ejecutar el SQL:

1. **Reinicia la aplicación Spring Boot**
2. **Hibernate detectará automáticamente** las nuevas columnas
3. **Los logs mostrarán:**
   ```
   spring.jpa.show-sql=true → Verás los mapeos de tablas
   ```

---

## ⚠️ Rollback (Si algo sale mal)

Si necesitas deshacer los cambios:

```sql
ALTER TABLE envios DROP COLUMN destinatario_nombre;
ALTER TABLE envios DROP COLUMN destinatario_ciudad;
ALTER TABLE envios DROP COLUMN destinatario_direccion;
ALTER TABLE envios DROP COLUMN destinatario_telefono;
```

---

## 🔒 Seguridad

- ✅ Las columnas permiten NULL (no se pierden datos históricos)
- ✅ No se borra información existente
- ✅ Compatible con Hibernate auto-update
- ✅ Reversible si es necesario

---

## 📞 Soporte

Si encuentras errores, verifica:
- ✅ Conexión a la BD remota
- ✅ Permisos del usuario `root`
- ✅ La tabla `envios` existe y es accesible

