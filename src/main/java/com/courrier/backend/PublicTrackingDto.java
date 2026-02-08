package com.courrier.backend;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO publico para rastreo.
 */
public class PublicTrackingDto {

    private String estadoActual;
    private List<EstadoHistorialDto> historialEstados;
    private String descripcion;
    private LocalDateTime fechaEstimada;
    private String destinatarioIniciales;
    private String destinatarioCiudad;

    public PublicTrackingDto(String estadoActual,
                             List<EstadoHistorialDto> historialEstados,
                             String descripcion,
                             LocalDateTime fechaEstimada,
                             String destinatarioIniciales,
                             String destinatarioCiudad) {
        this.estadoActual = estadoActual;
        this.historialEstados = historialEstados;
        this.descripcion = descripcion;
        this.fechaEstimada = fechaEstimada;
        this.destinatarioIniciales = destinatarioIniciales;
        this.destinatarioCiudad = destinatarioCiudad;
    }

    public String getEstadoActual() {
        return estadoActual;
    }

    public List<EstadoHistorialDto> getHistorialEstados() {
        return historialEstados;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaEstimada() {
        return fechaEstimada;
    }

    public String getDestinatarioIniciales() {
        return destinatarioIniciales;
    }

    public String getDestinatarioCiudad() {
        return destinatarioCiudad;
    }

    public static class EstadoHistorialDto {
        private String estado;
        private LocalDateTime fecha;

        public EstadoHistorialDto(String estado, LocalDateTime fecha) {
            this.estado = estado;
            this.fecha = fecha;
        }

        public String getEstado() {
            return estado;
        }

        public LocalDateTime getFecha() {
            return fecha;
        }
    }
}
