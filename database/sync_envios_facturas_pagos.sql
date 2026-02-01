-- ================================================================
-- MIGRACIÓN: Sincronizar Envíos con Facturas y Pagos
-- ================================================================
-- Fecha: 2026-02-01
-- Objetivo: Agregar campos para vincular envíos con facturas
-- ================================================================

-- ================================================================
-- ALTER TABLE 1: Agregar campo costo_envio a tabla envios
-- ================================================================

ALTER TABLE envios ADD COLUMN costo_envio DOUBLE 
COMMENT 'Costo calculado del envío: Base $5 + (peso * $2) + (valorDeclarado * 0.01)';

-- ================================================================
-- ALTER TABLE 2: Agregar campo envio_id a tabla facturas
-- ================================================================

ALTER TABLE facturas ADD COLUMN envio_id BIGINT 
COMMENT 'ID del envío que genera esta factura (para rastreo)';

-- ================================================================
-- VALIDACIÓN POST-ACTUALIZACIÓN
-- ================================================================

-- Verificar que se agregaron las columnas:
-- SELECT COLUMN_NAME, COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_NAME IN ('envios', 'facturas') 
-- AND COLUMN_NAME IN ('costo_envio', 'envio_id');

-- Ver estructura actualizada:
-- DESC envios;
-- DESC facturas;

-- ================================================================
-- NOTAS:
-- ================================================================
-- • costo_envio se calcula automáticamente al crear envío en EnvioService
-- • envio_id permite rastrear qué envío genera cada factura
-- • Las relaciones Envío → Factura → Pago están ahora completamente sincronizadas
-- • Cuando se crea un envío, se auto-genera la factura asociada
-- • Cuando se paga una factura, cambia su estado en tiempo real
-- ================================================================
