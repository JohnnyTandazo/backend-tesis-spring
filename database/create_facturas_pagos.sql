-- ================================================================
-- MIGRACIÓN DE ESQUEMA: Crear tablas para Módulo de Facturas y Pagos
-- ================================================================
-- Fecha: 2026-02-01
-- Objetivo: Implementar sistema de facturación y gestión de pagos
-- ================================================================

-- ================================================================
-- TABLA 1: FACTURAS
-- ================================================================

CREATE TABLE facturas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    monto DOUBLE NOT NULL,
    estado VARCHAR(50) NOT NULL COMMENT 'PENDIENTE, PAGADA, VENCIDA, ANULADA',
    descripcion VARCHAR(500),
    numero_factura VARCHAR(50) UNIQUE,
    fecha_emision DATETIME DEFAULT CURRENT_TIMESTAMP,
    fecha_vencimiento DATETIME,
    usuario_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Índices
    INDEX idx_usuario_id (usuario_id),
    INDEX idx_estado (estado),
    INDEX idx_fecha_vencimiento (fecha_vencimiento),
    
    -- Llave Foránea
    CONSTRAINT fk_factura_usuario FOREIGN KEY (usuario_id) 
        REFERENCES usuarios(id) ON DELETE CASCADE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Facturas generadas por envíos y servicios';

-- ================================================================
-- TABLA 2: PAGOS
-- ================================================================

CREATE TABLE pagos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    monto DOUBLE NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL COMMENT 'TARJETA_CREDITO, TRANSFERENCIA, EFECTIVO, CHEQUE',
    estado VARCHAR(50) DEFAULT 'PENDIENTE' COMMENT 'PENDIENTE, CONFIRMADO, RECHAZADO',
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    comprobante VARCHAR(500) COMMENT 'URL o ID del comprobante',
    referencia VARCHAR(100) COMMENT 'Referencia de transacción bancaria',
    descripcion VARCHAR(500),
    factura_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Índices
    INDEX idx_factura_id (factura_id),
    INDEX idx_estado (estado),
    INDEX idx_fecha (fecha),
    
    -- Llave Foránea
    CONSTRAINT fk_pago_factura FOREIGN KEY (factura_id) 
        REFERENCES facturas(id) ON DELETE CASCADE
    
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Registro de pagos realizados contra facturas';

-- ================================================================
-- VALIDACIÓN POST-CREACIÓN
-- ================================================================

-- Verificar tablas creadas:
-- SHOW TABLES LIKE 'factura%';
-- SHOW TABLES LIKE 'pago%';

-- Ver estructura:
-- DESC facturas;
-- DESC pagos;

-- Ver datos de prueba:
-- SELECT * FROM facturas WHERE usuario_id = 1;
-- SELECT * FROM pagos WHERE factura_id IN (SELECT id FROM facturas WHERE usuario_id = 1);

-- ================================================================
-- NOTAS IMPORTANTES:
-- ================================================================
-- • Las tablas se crean con collation utf8mb4_unicode_ci para soportar caracteres especiales
-- • Se incluyen índices en campos de búsqueda frecuente (usuario_id, estado, fecha)
-- • Las claves foráneas tienen ON DELETE CASCADE para eliminar pagos/facturas en cascada
-- • La aplicación Spring Boot generará automáticamente las tablas si spring.jpa.hibernate.ddl-auto=create
-- • Los datos de prueba se cargarán automáticamente al iniciar la aplicación
-- ================================================================
