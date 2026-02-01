-- ================================================================
-- SCRIPT: Insertar datos de prueba en tabla FACTURAS
-- ================================================================
-- Fecha: 2026-02-01
-- Propósito: Cargar facturas de prueba para desarrollo/testing
-- ================================================================

-- ================================================================
-- PASO 1: VERIFICAR USUARIOS EXISTENTES
-- ================================================================
-- Ejecuta esta query PRIMERO para ver qué usuarios tienes:

SELECT id, nombre, email, rol FROM usuarios LIMIT 10;

-- Toma nota del ID del usuario (normalmente será 1 para el primer cliente)

-- ================================================================
-- PASO 2: INSERTAR FACTURAS DE PRUEBA
-- ================================================================
-- NOTA: Cambia el valor de usuario_id si el ID de tu usuario es diferente

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
-- FACTURA 1: PENDIENTE - Vence en 15 días
(
    'FAC-2026-001',
    350.00,
    'PENDIENTE',
    'Envío USA-001: Laptop HP y Mouse',
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 15 DAY),
    1,
    NOW()
),
-- FACTURA 2: PENDIENTE - Vence en 10 días
(
    'FAC-2026-002',
    50.00,
    'PENDIENTE',
    'Envío USA-002: Ropa Shein',
    CURDATE(),
    DATE_ADD(CURDATE(), INTERVAL 10 DAY),
    1,
    NOW()
),
-- FACTURA 3: PAGADA - Vencida hace 20 días (ya pagada)
(
    'FAC-2026-003',
    100.00,
    'PAGADA',
    'Servicio de envío expedito',
    DATE_SUB(CURDATE(), INTERVAL 30 DAY),
    DATE_SUB(CURDATE(), INTERVAL 20 DAY),
    1,
    NOW()
);

-- ================================================================
-- PASO 3: VERIFICAR INSERCIÓN
-- ================================================================
-- Ejecuta esta query para confirmar que se insertaron correctamente:

SELECT 
    id,
    numero_factura,
    monto,
    estado,
    fecha_emision,
    fecha_vencimiento,
    usuario_id,
    descripcion
FROM facturas
WHERE usuario_id = 1
ORDER BY id DESC;

-- Deberías ver 3 facturas con los datos que insertamos

-- ================================================================
-- PASO 4 (Opcional): LIMPIAR DATOS SI NECESITAS REINTENTAR
-- ================================================================
-- Si algo salió mal y quieres limpiar, ejecuta:
-- DELETE FROM facturas WHERE numero_factura IN ('FAC-2026-001', 'FAC-2026-002', 'FAC-2026-003');

-- ================================================================
-- NOTAS IMPORTANTES:
-- ================================================================
-- • usuario_id = 1 asume que tu usuario de prueba tiene ID 1
-- • Si tu usuario tiene otro ID, cambia todos los "1" por ese ID
-- • Las fechas usan CURDATE() que es la fecha del servidor MySQL
-- • INTERVAL calcula automáticamente los días
-- • Los números de factura (FAC-2026-001, etc) son únicos
-- • El estado puede ser: PENDIENTE, PAGADA, VENCIDA, ANULADA
-- ================================================================
