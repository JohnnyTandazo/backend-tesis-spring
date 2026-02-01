package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Facturas
 * Maneja la lógica de negocio para facturas
 */
@Service
public class FacturaService {

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtener todas las facturas de un usuario
     */
    public List<Factura> obtenerPorUsuario(Long usuarioId) {
        System.out.println("📋 [FacturaService] Obteniendo facturas del usuario: " + usuarioId);
        return facturaRepository.findByUsuarioIdWithEnvioAndUsuario(usuarioId);
    }

    /**
     * Obtener facturas pendientes de un usuario
     */
    public List<Factura> obtenerPendientes(Long usuarioId) {
        System.out.println("⏳ [FacturaService] Obteniendo facturas PENDIENTES del usuario: " + usuarioId);
        return facturaRepository.findPendientesByUsuarioWithEnvio(usuarioId);
    }

    /**
     * Obtener una factura por ID
     */
    public Optional<Factura> obtenerPorId(Long id) {
        System.out.println("🔍 [FacturaService] Buscando factura con ID: " + id);
        return Optional.ofNullable(facturaRepository.findByIdWithEnvio(id));
    }

    /**
     * Crear una nueva factura
     */
    public Factura crearFactura(Factura factura) {
        System.out.println("✍️ [FacturaService] Creando nueva factura: " + factura.getNumeroFactura());
        return facturaRepository.save(factura);
    }

    /**
     * Actualizar estado de una factura (ej: de PENDIENTE a PAGADA)
     */
    public Factura actualizarEstado(Long id, String nuevoEstado) {
        System.out.println("🔄 [FacturaService] Actualizando estado de factura ID: " + id + " a: " + nuevoEstado);
        
        return facturaRepository.findById(id).map(factura -> {
            factura.setEstado(nuevoEstado);
            return facturaRepository.save(factura);
        }).orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + id));
    }

    /**
     * Eliminar una factura
     */
    public void eliminarFactura(Long id) {
        System.out.println("🗑️ [FacturaService] Eliminando factura con ID: " + id);
        facturaRepository.deleteById(id);
    }
}
