-- ================================================================
-- MIGRACIÓN DE ESQUEMA: Agregar campos de SNAPSHOT a tabla envios
-- ================================================================
-- Fecha: 2026-02-01
-- Objetivo: Implementar patrón Snapshot para captura histórica
--           de dirección de destino en envíos
-- ================================================================

-- Verificar tabla antes de actualizar
-- SHOW COLUMNS FROM envios;

-- ================================================================
-- ALTER TABLE: Agregar campos de Destinatario (Snapshot Pattern)
-- ================================================================

-- 1. Agregar nombre del destinatario
ALTER TABLE envios ADD COLUMN destinatario_nombre VARCHAR(255) 
COMMENT 'Nombre del destinatario capturado en momento de envío (Snapshot)';

-- 2. Agregar ciudad de destino
ALTER TABLE envios ADD COLUMN destinatario_ciudad VARCHAR(255) 
COMMENT 'Ciudad de destino capturada en momento de envío (Snapshot)';

-- 3. Agregar dirección completa de destino
ALTER TABLE envios ADD COLUMN destinatario_direccion VARCHAR(500) 
COMMENT 'Dirección completa capturada en momento de envío (Snapshot)';

-- 4. Agregar teléfono del destinatario
ALTER TABLE envios ADD COLUMN destinatario_telefono VARCHAR(20) 
COMMENT 'Teléfono de contacto del destinatario (Snapshot)';

-- ================================================================
-- VALIDACIÓN POST-ACTUALIZACIÓN
-- ================================================================
-- Ejecuta esto para verificar que las columnas se crearon correctamente:
-- SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_NAME = 'envios' AND COLUMN_NAME LIKE 'destinatario%';

-- ================================================================
-- NOTAS DE MIGRACIÓN:
-- ================================================================
-- • Todas las columnas permiten NULL inicialmente (IS_NULLABLE = YES)
-- • Esto es seguro para datos históricos que podrían no tener destino
-- • Si necesitas hacerlas NOT NULL después, ejecuta:
--   ALTER TABLE envios MODIFY COLUMN destinatario_nombre VARCHAR(255) NOT NULL;
-- • La aplicación Java (Hibernate) reconocerá automáticamente estas columnas
-- • El siguiente reinicio del servidor actualizará el mapeo de entidades
-- ================================================================
