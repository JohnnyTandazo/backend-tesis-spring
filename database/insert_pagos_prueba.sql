-- ================================================================
-- SCRIPT: Insertar datos de prueba en tabla PAGOS
-- ================================================================
-- Fecha: 2026-02-01
-- Propósito: Cargar pagos de prueba para desarrollo/testing
-- ================================================================

-- ================================================================
-- PASO 1: VERIFICAR FACTURAS EXISTENTES
-- ================================================================
-- Ejecuta esta query PRIMERO para ver qué facturas tienes:

SELECT id, numero_factura, monto, estado FROM facturas ORDER BY id DESC LIMIT 10;

-- Toma nota de los IDs de las facturas que deseas vincular con pagos

-- ================================================================
-- PASO 2: INSERTAR PAGOS DE PRUEBA
-- ================================================================
-- NOTA: Cambia los valores de factura_id según los IDs reales de tus facturas

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
-- PAGO 1: Pago confirmado para Factura 3 (la que ya estaba PAGADA)
(
    3,  -- ID de la factura FAC-2026-003 (cambiar si es diferente)
    100.00,
    'TARJETA_CREDITO',
    'CONFIRMADO',
    DATE_SUB(CURDATE(), INTERVAL 5 DAY),
    'TRX-2026-00001',
    'Pago con tarjeta de crédito Visa - Factura FAC-2026-003',
    NOW()
),
-- PAGO 2: Pago parcial pendiente para Factura 1
(
    1,  -- ID de la factura FAC-2026-001 (cambiar si es diferente)
    200.00,  -- Pago parcial de $350
    'TRANSFERENCIA',
    'PENDIENTE',
    CURDATE(),
    'TRANSF-2026-00002',
    'Transferencia bancaria iniciada - Pago parcial de FAC-2026-001',
    NOW()
);

-- ================================================================
-- PASO 3: VERIFICAR INSERCIÓN
-- ================================================================
-- Ejecuta esta query para confirmar que se insertaron los pagos:

SELECT 
    p.id,
    f.numero_factura,
    p.monto,
    p.metodo_pago,
    p.estado,
    p.fecha,
    p.referencia,
    p.descripcion
FROM pagos p
INNER JOIN facturas f ON p.factura_id = f.id
WHERE f.usuario_id = 1
ORDER BY p.id DESC;

-- Deberías ver 2 pagos vinculados a las facturas

-- ================================================================
-- PASO 4 (Opcional): LIMPIAR DATOS SI NECESITAS REINTENTAR
-- ================================================================
-- Si algo salió mal y quieres limpiar, ejecuta:
-- DELETE FROM pagos WHERE referencia IN ('TRX-2026-00001', 'TRANSF-2026-00002');

-- ================================================================
-- NOTAS IMPORTANTES:
-- ================================================================
-- • factura_id debe coincidir con los IDs reales de las facturas
-- • Si las facturas tienen IDs diferentes (ej: 4, 5, 6), actualiza los valores
-- • metodo_pago puede ser: TARJETA_CREDITO, TRANSFERENCIA, EFECTIVO, CHEQUE
-- • estado puede ser: PENDIENTE, CONFIRMADO, RECHAZADO
-- • Si cambias factura_id a 1 y el monto = 100 o más, Factura se marcará PAGADA automáticamente
-- • La fecha de pago se puede ajustar con DATE_SUB para simular pagos pasados
-- ================================================================
