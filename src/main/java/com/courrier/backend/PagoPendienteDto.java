package com.courrier.backend;

import java.time.LocalDateTime;

/**
 * DTO para pagos pendientes con envioId explicito.
 */

public class PagoPendienteDto {

    private Long id;
    private Double monto;
    private String metodoPago;
    private String estado;
    private LocalDateTime fecha;
    private String comprobante;
    private String referencia;
    private String descripcion;
    private Long facturaId;
    private Long envioId;
    private Long paqueteId; // Nuevo campo

    public static PagoPendienteDto from(Pago pago) {
        PagoPendienteDto dto = new PagoPendienteDto();
        dto.id = pago.getId();
        dto.monto = pago.getMonto();
        dto.metodoPago = pago.getMetodoPago();
        dto.estado = pago.getEstado();
        dto.fecha = pago.getFecha();
        dto.comprobante = pago.getComprobante();
        dto.referencia = pago.getReferencia();
        dto.descripcion = pago.getDescripcion();
        dto.facturaId = pago.getFacturaId();

        Long envioId = null;
        Long paqueteId = null;
        if (pago.getFactura() != null) {
            if (pago.getFactura().getEnvioId() != null) {
                envioId = pago.getFactura().getEnvioId();
            } else if (pago.getFactura().getEnvio() != null) {
                envioId = pago.getFactura().getEnvio().getId();
            }
        }
        // Nueva lógica: obtener paqueteId directamente del Pago
        if (pago.getPaquete() != null) {
            paqueteId = pago.getPaquete().getId();
        }
        dto.envioId = envioId;
        dto.paqueteId = paqueteId;
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Double getMonto() {
        return monto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getComprobante() {
        return comprobante;
    }

    public String getReferencia() {
        return referencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Long getFacturaId() {
        return facturaId;
    }

    public Long getEnvioId() {
        return envioId;
    }

    public Long getPaqueteId() {
        return paqueteId;
    }
}
