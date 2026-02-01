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

    @Autowired
    private EnvioRepository envioRepository;

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
     * Registrar un nuevo pago (multipart/form-data)
     * CON AUDITORÍA COMPLETA Y DEBUG LOGS
     */
    public Pago registrarPago(Long facturaId,
                              Double monto,
                              String metodoPago,
                              String referencia,
                              String comprobanteNombre) {
        System.out.println("💰 [PagoService] Registrando nuevo pago: $" + monto);
        
        // Buscar la factura
        Factura factura = facturaRepository.findById(facturaId)
            .orElseThrow(() -> new RuntimeException("Factura no encontrada"));
        
        System.out.println("DEBUG: Factura encontrada - ID: " + factura.getId() + 
                         ", Estado: " + factura.getEstado() + 
                         ", Monto: " + factura.getMonto());
        
        // Validar que el monto no exceda la factura
        if (monto > factura.getMonto()) {
            System.out.println("❌ Monto de pago excede el monto de la factura");
            throw new RuntimeException("El monto del pago no puede exceder el monto de la factura");
        }
        
        // Crear el pago
        Pago pago = new Pago();
        pago.setFactura(factura);
        pago.setMonto(monto);
        pago.setMetodoPago(metodoPago);
        pago.setReferencia(referencia);
        pago.setComprobante(comprobanteNombre);
        pago.setEstado("CONFIRMADO");
        
        // Guardar pago
        Pago pagGuardado = pagoRepository.save(pago);
        System.out.println("✅ Pago registrado con ID: " + pagGuardado.getId());
        
        // ========================================
        // ACTUALIZACIÓN DE FACTURA A PAGADO
        // ========================================
        System.out.println("📋 [FACTURA] Marcando factura como PAGADO...");
        
        factura.setEstado("PAGADO");
        facturaRepository.save(factura);
        System.out.println("✅ Factura sincronizada: " + factura.getNumeroFactura() + " - Estado: " + factura.getEstado());

        // ========================================
        // INICIO ACTUALIZACIÓN AUTOMÁTICA DE ENVÍO
        // ========================================
        System.out.println("\n--- INICIO ACTUALIZACIÓN AUTOMÁTICA DE ENVÍO ---");
        Long idEnvioAActualizar = null;

        // INTENTO 1: Obtener por objeto relación @ManyToOne
        if (factura.getEnvio() != null) {
            idEnvioAActualizar = factura.getEnvio().getId();
            System.out.println("✓ DEBUG: Envio encontrado por objeto. ID: " + idEnvioAActualizar);
        } 
        // INTENTO 2: Obtener por ID directo (campo envio_id)
        else if (factura.getEnvioId() != null) {
            idEnvioAActualizar = factura.getEnvioId();
            System.out.println("✓ DEBUG: Envio encontrado por ID directo. ID: " + idEnvioAActualizar);
        } 
        // ERROR CRÍTICO
        else {
            System.out.println("❌ ERROR CRÍTICO: La factura " + factura.getId() + 
                             " no tiene envío asociado (ambos nulos).");
        }

        // Ejecutar actualización si tenemos ID
        if (idEnvioAActualizar != null) {
            System.out.println("\n📤 Intentando actualizar envío con ID: " + idEnvioAActualizar);
            
            Envio envio = envioRepository.findById(idEnvioAActualizar).orElse(null);
            
            if (envio != null) {
                System.out.println("   → Envio encontrado en BD. Estado actual: " + envio.getEstado());
                
                envio.setEstado("EN_TRANSITO");
                envioRepository.save(envio);
                
                System.out.println("   ✓ ÉXITO: Envío " + idEnvioAActualizar + 
                                 " actualizado a EN_TRANSITO.");
            } else {
                System.out.println("   ❌ ERROR: No existe envío con ID " + idEnvioAActualizar + 
                                 " en la BD.");
            }
        }
        System.out.println("--------------------------------------------------\n");
        
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
