package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class EnvioService {

    @Autowired
    private EnvioRepository envioRepository;

    // Obtener un envío por su ID
    public Optional<Envio> obtenerPorId(Long id) {
        System.out.println("🔍 [EnvioService] Buscando envío con ID: " + id);
        return envioRepository.findById(id);
    }

    // Obtener todos los envíos
    public List<Envio> obtenerTodos() {
        System.out.println("📦 [EnvioService] Obteniendo todos los envíos...");
        return envioRepository.findAll();
    }

    // Obtener envíos de un usuario específico
    public List<Envio> obtenerPorUsuario(Long usuarioId) {
        System.out.println("👤 [EnvioService] Obteniendo envíos del usuario: " + usuarioId);
        return envioRepository.findByUsuarioId(usuarioId);
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
        
        // Si viene un usuarioId, asociar el usuario
        // NOTA: En producción, obtener el usuario del contexto de seguridad
        // por ahora se recibe en el request si es necesario
        
        // Guardar en la base de datos
        Envio guardado = envioRepository.save(envio);
        System.out.println("✅ Envío guardado en BD con ID: " + guardado.getId());
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

    // Eliminar un envío
    public void eliminarEnvio(Long id) {
        System.out.println("🗑️ [EnvioService] Eliminando envío con ID: " + id);
        envioRepository.deleteById(id);
    }
}
