package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Pagos
 * Maneja la lógica de negocio para pagos
 * 
 * ⚠️ CRITICAL: Todos los métodos son @Transactional para garantizar
 * que los cambios en BD se persistan correctamente.
 */
@Service
@Transactional  // <-- APLICA A TODOS LOS MÉTODOS PÚBLICOS
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
     * Obtener todos los pagos de un usuario (a través de sus facturas)
     * Usa query directa con JOIN
     */
    public List<Pago> obtenerPorUsuario(Long usuarioId) {
        System.out.println("💳 [PagoService] Obteniendo pagos del usuario: " + usuarioId);
        return pagoRepository.findByUsuarioId(usuarioId);
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
     * CON AUDITORÍA COMPLETA, DEBUG LOGS Y SINCRONIZACIÓN ROBUSTA
     */
    public Pago registrarPago(Long facturaId,
                              Double monto,
                              String metodoPago,
                              String referencia,
                              String comprobanteNombre) {
        
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║ INICIO: REGISTRAR PAGO (TRANSACTIONAL)                  ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        
        System.out.println("💰 [PagoService] Registrando nuevo pago: $" + monto);
        System.out.println("   📌 Factura ID: " + facturaId);
        System.out.println("   📌 Método: " + metodoPago);
        
        // PASO 1: BUSCAR LA FACTURA
        System.out.println("\n📍 PASO 1: Buscando factura...");
        Factura factura = facturaRepository.findById(facturaId)
            .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + facturaId));
        
        System.out.println("   ✓ Factura encontrada:");
        System.out.println("     • ID: " + factura.getId());
        System.out.println("     • Número: " + factura.getNumeroFactura());
        System.out.println("     • Estado: " + factura.getEstado());
        System.out.println("     • Monto: $" + factura.getMonto());
        System.out.println("     • envio_id (Campo): " + factura.getEnvioId());
        System.out.println("     • envio (Objeto): " + (factura.getEnvio() != null ? "CARGADO ID=" + factura.getEnvio().getId() : "NULL"));
        
        // PASO 2: VALIDAR MONTO
        System.out.println("\n📍 PASO 2: Validando monto...");
        if (monto > factura.getMonto()) {
            System.out.println("   ❌ ERROR: Monto de pago ($" + monto + ") excede el monto de la factura ($" + factura.getMonto() + ")");
            throw new RuntimeException("El monto del pago no puede exceder el monto de la factura");
        }
        System.out.println("   ✓ Monto válido");
        
        // PASO 3: CREAR Y GUARDAR PAGO
        System.out.println("\n📍 PASO 3: Creando y guardando pago...");
        Pago pago = new Pago();
        pago.setFactura(factura);
        pago.setMonto(monto);
        pago.setMetodoPago(metodoPago);
        pago.setReferencia(referencia);
        pago.setComprobante(comprobanteNombre);
        pago.setEstado("CONFIRMADO");
        
        Pago pagGuardado = pagoRepository.save(pago);
        System.out.println("   ✓ Pago guardado con ID: " + pagGuardado.getId());
        
        // PASO 4: ACTUALIZAR FACTURA A PAGADO
        System.out.println("\n📍 PASO 4: Actualizando factura a PAGADO...");
        factura.setEstado("PAGADO");
        Factura facturaActualizada = facturaRepository.save(factura);
        System.out.println("   ✓ Factura actualizada. Estado: " + facturaActualizada.getEstado());
        
        // ════════════════════════════════════════════════════════════
        // PASO 5: SINCRONIZAR ENVÍO - LÓGICA ROBUSTA Y SEGURA
        // ════════════════════════════════════════════════════════════
        System.out.println("\n📍 PASO 5: Sincronizando estado del envío...");
        System.out.println("   Intentando obtener ID del envío...");
        
        Long idEnvioAActualizar = null;
        String metodoObtenccion = null;

        // INTENTO 1: Por objeto relación @ManyToOne (si se cargó)
        System.out.println("     → Verificando factura.getEnvio()...");
        if (factura.getEnvio() != null) {
            idEnvioAActualizar = factura.getEnvio().getId();
            metodoObtenccion = "Objeto @ManyToOne";
            System.out.println("     ✓ Envio encontrado por objeto relación. ID: " + idEnvioAActualizar);
        } 
        // INTENTO 2: Por ID directo (campo envio_id en BD)
        else {
            System.out.println("     → Verificando factura.getEnvioId()...");
            if (factura.getEnvioId() != null) {
                idEnvioAActualizar = factura.getEnvioId();
                metodoObtenccion = "ID directo (envio_id)";
                System.out.println("     ✓ Envio encontrado por ID directo. ID: " + idEnvioAActualizar);
            }
        }
        
        // VERIFICACIÓN CRÍTICA
        if (idEnvioAActualizar == null) {
            System.out.println("     ❌ ERROR CRÍTICO: La factura " + factura.getId() + 
                             " NO tiene envío asociado.");
            System.out.println("     ❌ Ambos campos son NULL: getEnvio() y getEnvioId()");
            System.out.println("     ❌ Revisar BD: ¿Existe envio_id en tabla facturas?");
        } else {
            System.out.println("\n     📤 Obtenido por: " + metodoObtenccion);
            System.out.println("     📤 Cargando envío fresco desde BD (SINCRONIZACIÓN)...");
            
            // CARGA FRESCA DEL ENVÍO DESDE BD
            Optional<Envio> envioOpt = envioRepository.findById(idEnvioAActualizar);
            
            if (envioOpt.isPresent()) {
                Envio envio = envioOpt.get();
                System.out.println("     ✓ Envio encontrado en BD:");
                System.out.println("       • ID: " + envio.getId());
                System.out.println("       • Estado ANTES: " + envio.getEstado());
                System.out.println("       • Tracking: " + envio.getNumeroTracking());
                
                // ACTUALIZAR ESTADO
                System.out.println("     🔄 Cambiando estado a EN_TRANSITO...");
                envio.setEstado("EN_TRANSITO");
                
                // GUARDAR EN BD
                Envio envioActualizado = envioRepository.save(envio);
                System.out.println("     ✓ Envio GUARDADO en BD:");
                System.out.println("       • Estado DESPUÉS: " + envioActualizado.getEstado());
                System.out.println("       • ✅ ÉXITO: Envío sincronizado correctamente");
                
            } else {
                System.out.println("     ❌ ERROR: No existe envío con ID " + idEnvioAActualizar + " en la BD");
                System.out.println("     ❌ Revisar integridad referencial: envios.id = " + idEnvioAActualizar);
            }
        }
        
        // FINALIZACIÓN
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║ FIN: REGISTRO DE PAGO COMPLETADO                       ║");
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        
        return pagGuardado;
    }

    /**
     * Actualizar estado de un pago
     * CRÍTICO: Si el nuevo estado es APROBADO, actualiza la factura a PAGADA
     */
    public Pago actualizarEstado(Long id, String nuevoEstado) {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║ ACTUALIZAR ESTADO DE PAGO (OPERADOR)                   ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("🔄 [PagoService] Actualizando estado de pago ID: " + id + " a: " + nuevoEstado);
        
        return pagoRepository.findById(id).map(pago -> {
            System.out.println("\n📍 PASO 1: Obtener pago actual");
            System.out.println("   ✓ Pago ID: " + pago.getId());
            System.out.println("   ✓ Estado anterior: " + pago.getEstado());
            System.out.println("   ✓ Monto: $" + pago.getMonto());
            System.out.println("   ✓ Factura ID: " + pago.getFacturaId());
            
            System.out.println("\n📍 PASO 2: Actualizar estado del pago");
            pago.setEstado(nuevoEstado);
            Pago actualizado = pagoRepository.save(pago);
            System.out.println("   ✓ Estado actualizado a: " + actualizado.getEstado());
            
            // CRÍTICO: Si el estado es APROBADO, actualizar factura a PAGADA
            System.out.println("\n📍 PASO 3: Verificar si necesita sincronización con Factura");
            if ("APROBADO".equals(nuevoEstado)) {
                System.out.println("   🎯 APROBADO detectado - Actualizando factura...");
                
                Factura factura = pago.getFactura();
                if (factura != null) {
                    System.out.println("   ✓ Factura encontrada:");
                    System.out.println("     • ID: " + factura.getId());
                    System.out.println("     • Estado anterior: " + factura.getEstado());
                    System.out.println("     • Número: " + factura.getNumeroFactura());
                    
                    factura.setEstado("PAGADA");
                    Factura facturaActualizada = facturaRepository.save(factura);
                    
                    System.out.println("   ✓ Factura actualizada:");
                    System.out.println("     • Estado nuevo: " + facturaActualizada.getEstado());
                    System.out.println("     • ✅ Deuda liberada para el cliente");
                } else {
                    System.out.println("   ⚠️ ADVERTENCIA: Factura no cargada, integridad referencial en riesgo");
                }
            } else {
                System.out.println("   ℹ️ Estado '" + nuevoEstado + "' no requiere sincronización");
            }
            
            System.out.println("\n╔════════════════════════════════════════════════════════╗");
            System.out.println("║ FIN: ACTUALIZACIÓN COMPLETADA                         ║");
            System.out.println("╚════════════════════════════════════════════════════════╝\n");
            
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
