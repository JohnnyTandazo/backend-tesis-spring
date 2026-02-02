package com.courrier.backend;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualizar estado de pago
 * Body: { "estado": "APROBADO" }
 */
@Data
@NoArgsConstructor
public class ActualizarPagoRequest {
    private String estado;  // 'APROBADO', 'RECHAZADO', 'PENDIENTE', etc.
}
