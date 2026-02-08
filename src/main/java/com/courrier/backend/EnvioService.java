// ...existing code...
package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EnvioService {

    @Autowired
    private EnvioRepository envioRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private FacturaRepository facturaRepository;

    // Obtener un envío por su ID
    public Optional<Envio> obtenerPorId(Long id) {
        System.out.println("🔍 [EnvioService] Buscando envío con ID: " + id);
        return envioRepository.findById(id);
    }

    // Obtener todos los envíos (ordenados descendentemente - más recientes primero)
    public List<Envio> obtenerTodos() {
        System.out.println("📦 [EnvioService] Obteniendo todos los envíos (ordenados DESC)...");
        return envioRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }

    // Obtener envíos por lista de estados (ordenados descendentemente)
    public List<Envio> obtenerPorEstados(List<String> estados) {
        System.out.println("📦 [EnvioService] Obteniendo envíos por estados: " + estados);
        return envioRepository.findByEstadoIn(estados, Sort.by(Sort.Direction.DESC, "id"));
    }

    // Obtener envíos de un usuario específico (ordenados descendentemente - más recientes primero)
    public List<Envio> obtenerPorUsuario(Long usuarioId) {
        System.out.println("👤 [EnvioService] Obteniendo envíos del usuario: " + usuarioId + " (ordenados DESC)");
        return envioRepository.findByUsuarioId(usuarioId, Sort.by(Sort.Direction.DESC, "id"));
    }

    // Obtener envío por número de tracking
    public Envio obtenerPorTracking(String numeroTracking) {
        System.out.println("📍 [EnvioService] Buscando envío por tracking: " + numeroTracking);
        return envioRepository.findByNumeroTracking(numeroTracking);
    }

    // Crear un nuevo envío (con mapeo del DTO al Envio)
    public Envio crearEnvio(CrearEnvioRequest request) {
        System.out.println("✍️ [EnvioService] Creando nuevo envío: " + request.getNumeroTracking());
        
        // Crear la entidad Envio
        Envio envio = new Envio();
        
        // Mapear campos básicos
        envio.setNumeroTracking(request.getNumeroTracking());
        envio.setDescripcion(request.getDescripcion());
        envio.setPesoLibras(request.getPesoLibras());
        envio.setValorDeclarado(request.getValorDeclarado());
        envio.setEstado(request.getEstado());
        envio.setCategoria(request.getCategoria());
        
        // IMPORTANTE: Mapear campos del PATRÓN SNAPSHOT - Dirección Destinatario
        envio.setDestinatarioNombre(request.getDestinatarioNombre());
        envio.setDestinatarioCiudad(request.getDestinatarioCiudad());
        envio.setDestinatarioDireccion(request.getDestinatarioDireccion());
        envio.setDestinatarioTelefono(request.getDestinatarioTelefono());
        
        System.out.println("📸 [SNAPSHOT] Capturando dirección de destino:");
        System.out.println("   - Nombre: " + envio.getDestinatarioNombre());
        System.out.println("   - Ciudad: " + envio.getDestinatarioCiudad());
        System.out.println("   - Dirección: " + envio.getDestinatarioDireccion());
        System.out.println("   - Teléfono: " + envio.getDestinatarioTelefono());
        
        // ========================================
        // CRÍTICO: Buscar y Asignar el Usuario
        // ========================================
        if (request.getUsuarioId() != null) {
            System.out.println("👤 [USUARIO] Buscando usuario con ID: " + request.getUsuarioId());
            
            Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> {
                    System.out.println("❌ Usuario NO encontrado con ID: " + request.getUsuarioId());
                    return new RuntimeException("Usuario no encontrado con ID: " + request.getUsuarioId());
                });
            
            // ASIGNAR el usuario al envío (esto es CRÍTICO para evitar usuario_id NULL)
            envio.setUsuario(usuario);
            System.out.println("✅ Usuario asignado: " + usuario.getId());
        } else {
            System.out.println("⚠️ [USUARIO] No se proporcionó usuarioId en el request");
        }
        
        // ========================================
        // CÁLCULO DE COSTO DEL ENVÍO
        // ========================================
        // Fórmula: costo = 5.0 (Base) + (peso * 2.0) + (valorDeclarado * 0.01)
        Double costoBase = 5.0;
        Double costoPorPeso = (envio.getPesoLibras() != null ? envio.getPesoLibras() : 0.0) * 2.0;
        Double costoValorDeclarado = (envio.getValorDeclarado() != null ? envio.getValorDeclarado() : 0.0) * 0.01;
        Double costoTotal = costoBase + costoPorPeso + costoValorDeclarado;
        
        envio.setCostoEnvio(costoTotal);
        System.out.println("💰 [COSTO] Cálculo del envío:");
        System.out.println("   Base: $" + costoBase);
        System.out.println("   Por peso (" + envio.getPesoLibras() + " lbs * 2.0): $" + costoPorPeso);
        System.out.println("   Por valor ($" + envio.getValorDeclarado() + " * 0.01): $" + costoValorDeclarado);
        System.out.println("   TOTAL: $" + costoTotal);
        
        // Guardar en la base de datos
        Envio guardado = envioRepository.save(envio);
        System.out.println("✅ Envío guardado en BD con ID: " + guardado.getId() + ", Usuario ID: " + guardado.getUsuario().getId());
        
        // ========================================
        // GENERACIÓN AUTOMÁTICA DE FACTURA
        // ========================================
        if (guardado.getUsuario() != null) {
            System.out.println("📋 [FACTURA] Generando factura automática para envío: " + guardado.getId());
            
            Factura factura = new Factura();
            factura.setUsuario(guardado.getUsuario());
            factura.setEnvioId(guardado.getId());  // Vincular con el envío
            factura.setMonto(guardado.getCostoEnvio());  // El monto es el costo del envío
            factura.setEstado("PENDIENTE");
            factura.setDescripcion("Envío " + guardado.getNumeroTracking() + ": " + guardado.getDescripcion());
            factura.setFechaEmision(LocalDateTime.now());
            factura.setFechaVencimiento(LocalDateTime.now().plusDays(15));  // Vencimiento en 15 días
            
            // Generar número de factura: FAC-{AÑO}-{ID}
            factura.setNumeroFactura("FAC-" + java.time.Year.now().getValue() + "-" + String.format("%06d", guardado.getId()));
            
            Factura facturaGuardada = facturaRepository.save(factura);
            System.out.println("✅ Factura creada: " + facturaGuardada.getNumeroFactura() + " por $" + facturaGuardada.getMonto());
        }
        
        return guardado;
    }
    
    // Crear un nuevo envío (Sobrecarga para compatibilidad - recibe Envio directamente)
    public Envio crearEnvio(Envio envio) {
        System.out.println("✍️ [EnvioService] Creando nuevo envío (Entidad directa): " + envio.getNumeroTracking());
        return envioRepository.save(envio);
    }

    // Actualizar un envío
    public Envio actualizarEnvio(Long id, Envio envioActualizado) {
        System.out.println("✏️ [EnvioService] Actualizando envío con ID: " + id);
        return envioRepository.findById(id).map(envio -> {
            if (envioActualizado.getNumeroTracking() != null) {
                envio.setNumeroTracking(envioActualizado.getNumeroTracking());
            }
            if (envioActualizado.getDescripcion() != null) {
                envio.setDescripcion(envioActualizado.getDescripcion());
            }
            if (envioActualizado.getPesoLibras() != null) {
                envio.setPesoLibras(envioActualizado.getPesoLibras());
            }
            if (envioActualizado.getValorDeclarado() != null) {
                envio.setValorDeclarado(envioActualizado.getValorDeclarado());
            }
            if (envioActualizado.getEstado() != null) {
                envio.setEstado(envioActualizado.getEstado());
            }
            if (envioActualizado.getCategoria() != null) {
                envio.setCategoria(envioActualizado.getCategoria());
            }
            return envioRepository.save(envio);
        }).orElseThrow(() -> new RuntimeException("Envío no encontrado"));
    }

    // Actualizar tracking y mover a EN_TRANSITO
    public Envio actualizarTrackingOperador(Long id, String nuevoTracking) {
        System.out.println("🚚 [EnvioService] Actualizando tracking de envío ID: " + id + " a: " + nuevoTracking);
        return envioRepository.findById(id).map(envio -> {
            envio.setNumeroTracking(nuevoTracking);
            envio.setEstado("EN_TRANSITO");
            return envioRepository.save(envio);
        }).orElseThrow(() -> new RuntimeException("Envío no encontrado"));
    }

    // Actualizar solo el estado de un envío
    public Envio actualizarEstado(Long id, String nuevoEstado) {
        System.out.println("🔄 [EnvioService] Actualizando estado del envío ID: " + id + " a: " + nuevoEstado);
        
        return envioRepository.findById(id).map(envio -> {
            envio.setEstado(nuevoEstado);
            
            // Si el estado es ENTREGADO, registrar fecha de entrega
            if ("ENTREGADO".equals(nuevoEstado)) {
                envio.setFechaEntrega(java.time.LocalDateTime.now());
                System.out.println("📅 Fecha de entrega registrada: " + envio.getFechaEntrega());
            }
            
            Envio guardado = envioRepository.save(envio);
            System.out.println("✅ Estado actualizado en BD: " + nuevoEstado);
            return guardado;
        }).orElseThrow(() -> {
            System.out.println("❌ Envío no encontrado con ID: " + id);
            return new RuntimeException("Envío no encontrado con ID: " + id);
        });
    }

    // Marcar pago rechazado
    public Envio rechazarPago(Long id, String motivo) {
        System.out.println("❌ [EnvioService] Rechazando pago del envío ID: " + id + (motivo != null ? " Motivo: " + motivo : ""));
        return envioRepository.findById(id).map(envio -> {
            envio.setEstado("PAGO_RECHAZADO");
            return envioRepository.save(envio);
        }).orElseThrow(() -> new RuntimeException("Envío no encontrado"));
    }

    // Aprobar pago
    public Envio aprobarPago(Long id, String nuevoEstado) {
        System.out.println("✅ [EnvioService] Aprobando pago del envío ID: " + id);
        Envio envio = envioRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Envío no encontrado"));

        Factura factura = facturaRepository.findByEnvioId(envio.getId());
        if (factura != null) {
            factura.setEstado("PAGADA"); // O EstadoFactura.PAGADA si usas enum
            facturaRepository.save(factura);
            System.out.println("✅ Factura actualizada a PAGADA para envío ID: " + envio.getId());
        } else {
            System.out.println("❌ No se encontró factura asociada al envío ID: " + envio.getId());
        }

        // Retornar el envío sin modificar su estado logístico
        return envio;
    }

    // Eliminar un envío
    public void eliminarEnvio(Long id) {
        System.out.println("🗑️ [EnvioService] Eliminando envío con ID: " + id);
        envioRepository.deleteById(id);
    }
}
