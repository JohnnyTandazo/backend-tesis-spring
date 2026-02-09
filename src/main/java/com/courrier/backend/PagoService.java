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
        // ...existing code...
        // Implementar lógica aquí
        return null; // TODO: Implementar
    }

    /**
     * Obtener un pago por ID (versión simple)
     */
    public Pago obtenerPagoPorId(Long id) {
        return pagoRepository.findById(id).orElse(null);
    }

    /**
     * Obtener todos los pagos PENDIENTES (ADMIN/CAJERO)
     * Sin filtro de usuario - devuelve la lista completa
     */
    public List<Pago> obtenerPendientes() {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║ REPORTE: PAGOS PENDIENTES (ADMIN)                      ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("💳 [PagoService] Obteniendo TODOS los pagos PENDIENTES...");
        
        List<Pago> pagosPendientes = pagoRepository.findByEstado("PENDIENTE", Sort.by(Sort.Direction.DESC, "fecha"));
        
        System.out.println("   ✅ Se encontraron " + pagosPendientes.size() + " pagos pendientes");
        if (!pagosPendientes.isEmpty()) {
            double totalPendiente = pagosPendientes.stream().mapToDouble(Pago::getMonto).sum();
            System.out.println("   💰 Total pendiente: $" + String.format("%.2f", totalPendiente));
            
            pagosPendientes.forEach(p -> {
                System.out.println("     → Pago ID: " + p.getId() + " | Factura: " + p.getFacturaId() + 
                                 " | Monto: $" + p.getMonto() + " | Usuario: " + 
                                 (p.getFactura() != null && p.getFactura().getUsuario() != null ? 
                                  p.getFactura().getUsuario().getNombre() : "N/A"));
            });
        }
        
        System.out.println("╚════════════════════════════════════════════════════════╝\n");
        return pagosPendientes;
    }

    /**
     * Obtener un pago por ID
     */
    public Optional<Pago> obtenerPorId(Long id) {
        System.out.println("🔍 [PagoService] Buscando pago con ID: " + id);
        return pagoRepository.findById(id);
    }

    /**
     * Buscar pago pendiente por paqueteId
     */
    public Pago obtenerPagoPorPaqueteId(Long paqueteId) {
        return pagoRepository.findPagoPendienteByPaqueteId(paqueteId);
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
        
        // ════════════════════════════════════════════════════════════
        // 🔒 REGLA DE SEGURIDAD: Un pago nuevo SIEMPRE nace PENDIENTE
        // ════════════════════════════════════════════════════════════
        // NUNCA puede nacer como CONFIRMADO o APROBADO
        // Requiere validación manual del operador en su dashboard
        pago.setEstado("PENDIENTE");
        System.out.println("   🔒 Estado FORZADO a: PENDIENTE (requiere validación del operador)");
        
        Pago pagGuardado = pagoRepository.save(pago);
        System.out.println("   ✓ Pago guardado con ID: " + pagGuardado.getId());
        System.out.println("   ✓ Estado: " + pagGuardado.getEstado());
        
        // ════════════════════════════════════════════════════════════
        // ⚠️ IMPORTANTE: La factura NO se marca como PAGADO aquí
        // ════════════════════════════════════════════════════════════
        // La factura solo cambiará a PAGADO cuando el operador
        // APRUEBE el pago en su dashboard (PUT /api/pagos/{id})
        System.out.println("\n📍 PASO 4: Factura mantiene estado actual (será actualizada al aprobar pago)");
        System.out.println("   ℹ️ Factura ID: " + factura.getId() + " | Estado actual: " + factura.getEstado());
        
        // ════════════════════════════════════════════════════════════
        // PASO 5: SINCRONIZACIÓN DE ENVÍO - DESACTIVADA
        // ════════════════════════════════════════════════════════════
        // ⚠️ El envío NO se actualiza aquí porque el pago está PENDIENTE
        // El envío cambiará a EN_TRANSITO cuando el operador APRUEBE
        // el pago en PUT /api/pagos/{id} con estado=APROBADO
        System.out.println("\n📍 PASO 5: Sincronización de envío OMITIDA (pago pendiente de aprobación)");
        System.out.println("   ℹ️ El envío se actualizará cuando el operador apruebe el pago");
        
        // FINALIZACIÓN
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║ FIN: REGISTRO DE PAGO COMPLETADO                       ║");
        System.out.println("║ Estado: PENDIENTE - Requiere validación del operador   ║");
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
            
            // CRÍTICO: Si el estado es VERIFICADO, actualizar factura a PAGADA
            System.out.println("\n📍 PASO 3: Verificar si necesita sincronización con Factura y Envío");
            if ("VERIFICADO".equals(nuevoEstado)) {
                System.out.println("   🎯 VERIFICADO detectado - Actualizando factura y envío...");
                
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
                    
                    // ════════════════════════════════════════════════════════════
                    // SINCRONIZAR ENVÍO (si existe)
                    // ════════════════════════════════════════════════════════════
                    if (factura.getEnvioId() != null) {
                        System.out.println("\n   📦 Sincronizando envío asociado...");
                        Optional<Envio> envioOpt = envioRepository.findById(factura.getEnvioId());
                        
                        if (envioOpt.isPresent()) {
                            Envio envio = envioOpt.get();
                            System.out.println("     • Envío ID: " + envio.getId());
                            System.out.println("     • Estado anterior: " + envio.getEstado());
                            
                            envio.setEstado("EN_TRANSITO");
                            Envio envioActualizado = envioRepository.save(envio);
                            
                            System.out.println("     • Estado nuevo: " + envioActualizado.getEstado());
                            System.out.println("     • ✅ Envío sincronizado correctamente");
                        } else {
                            System.out.println("     ⚠️ Envío no encontrado con ID: " + factura.getEnvioId());
                        }
                    } else {
                        System.out.println("   ℹ️ Factura sin envío asociado (probablemente importación de paquete)");
                    }
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

    public void guardarPago(Pago pago) {
        pagoRepository.save(pago);
    }
}
