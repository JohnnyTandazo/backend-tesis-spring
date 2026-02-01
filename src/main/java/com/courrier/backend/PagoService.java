package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Pagos
 * Maneja la lógica de negocio para pagos
 */
@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    /**
     * Obtener pagos de una factura
     */
    public List<Pago> obtenerPorFactura(Long facturaId) {
        System.out.println("💳 [PagoService] Obteniendo pagos de factura: " + facturaId);
        return pagoRepository.findByFacturaId(facturaId, Sort.by(Sort.Direction.DESC, "fecha"));
    }

    /**
     * Obtener un pago por ID
     */
    public Optional<Pago> obtenerPorId(Long id) {
        System.out.println("🔍 [PagoService] Buscando pago con ID: " + id);
        return pagoRepository.findById(id);
    }

    /**
     * Registrar un nuevo pago
     */
    public Pago registrarPago(Pago pago) {
        System.out.println("💰 [PagoService] Registrando nuevo pago: $" + pago.getMonto());
        
        // Buscar la factura
        Factura factura = facturaRepository.findById(pago.getFactura().getId())
            .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        
        // Validar que el monto no exceda la factura
        if (pago.getMonto() > factura.getMonto()) {
            System.out.println("❌ Monto de pago excede el monto de la factura");
            throw new RuntimeException("El monto del pago no puede exceder el monto de la factura");
        }
        
        // Asignar la factura (por si no viene)
        pago.setFactura(factura);
        
        // Guardar pago
        Pago pagGuardado = pagoRepository.save(pago);
        System.out.println("✅ Pago registrado con ID: " + pagGuardado.getId());
        
        // Si el pago es confirmado y cubre la factura, marcar factura como PAGADA
        if ("CONFIRMADO".equals(pago.getEstado()) && 
            pago.getMonto() >= factura.getMonto()) {
            factura.setEstado("PAGADA");
            facturaRepository.save(factura);
            System.out.println("✅ Factura marcada como PAGADA");
        }
        
        return pagGuardado;
    }

    /**
     * Actualizar estado de un pago
     */
    public Pago actualizarEstado(Long id, String nuevoEstado) {
        System.out.println("🔄 [PagoService] Actualizando estado de pago ID: " + id + " a: " + nuevoEstado);
        
        return pagoRepository.findById(id).map(pago -> {
            pago.setEstado(nuevoEstado);
            Pago actualizado = pagoRepository.save(pago);
            
            // Si el pago fue confirmado, actualizar factura
            if ("CONFIRMADO".equals(nuevoEstado) && pago.getMonto() >= pago.getFactura().getMonto()) {
                Factura factura = pago.getFactura();
                factura.setEstado("PAGADA");
                facturaRepository.save(factura);
            }
            
            return actualizado;
        }).orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
    }

    /**
     * Eliminar un pago
     */
    public void eliminarPago(Long id) {
        System.out.println("🗑️ [PagoService] Eliminando pago con ID: " + id);
        pagoRepository.deleteById(id);
    }
}
