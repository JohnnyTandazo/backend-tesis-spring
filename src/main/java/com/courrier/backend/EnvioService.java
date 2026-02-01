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

    // Crear un nuevo envío
    public Envio crearEnvio(Envio envio) {
        System.out.println("✍️ [EnvioService] Creando nuevo envío: " + envio.getNumeroTracking());
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
