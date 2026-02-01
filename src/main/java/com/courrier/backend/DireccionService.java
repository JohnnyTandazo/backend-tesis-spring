package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class DireccionService {

    @Autowired
    private DireccionRepository direccionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Obtener todas las direcciones
    public List<Direccion> obtenerTodas() {
        System.out.println("📍 [DireccionService] Obteniendo todas las direcciones...");
        return direccionRepository.findAll();
    }

    // Obtener direcciones de un usuario específico
    public List<Direccion> obtenerPorUsuario(Long usuarioId) {
        System.out.println("👤 [DireccionService] Obteniendo direcciones del usuario: " + usuarioId);
        return direccionRepository.findByUsuarioId(usuarioId);
    }

    // Obtener una dirección por su ID
    public Optional<Direccion> obtenerPorId(Long id) {
        System.out.println("🔍 [DireccionService] Buscando dirección con ID: " + id);
        return direccionRepository.findById(id);
    }

    // Obtener la dirección principal de un usuario
    public Direccion obtenerPrincipal(Long usuarioId) {
        System.out.println("⭐ [DireccionService] Buscando dirección principal del usuario: " + usuarioId);
        return direccionRepository.findByUsuarioIdAndEsPrincipalTrue(usuarioId);
    }

    // Crear una nueva dirección
    @Transactional
    public Direccion crearDireccion(Direccion direccion, Long usuarioId) {
        System.out.println("✍️ [DireccionService] Creando nueva dirección para usuario: " + usuarioId);
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));
        
        direccion.setUsuario(usuario);
        
        // Si es la primera dirección del usuario, hacerla principal automáticamente
        long cantidadDirecciones = direccionRepository.countByUsuarioId(usuarioId);
        if (cantidadDirecciones == 0) {
            direccion.setEsPrincipal(true);
            System.out.println("   ⭐ Primera dirección del usuario - Marcada como principal");
        }
        
        // Si se marca como principal, desmarcar la anterior
        if (direccion.getEsPrincipal() != null && direccion.getEsPrincipal()) {
            Direccion antiguaPrincipal = direccionRepository.findByUsuarioIdAndEsPrincipalTrue(usuarioId);
            if (antiguaPrincipal != null) {
                antiguaPrincipal.setEsPrincipal(false);
                direccionRepository.save(antiguaPrincipal);
                System.out.println("   🔄 Dirección anterior desmarcada como principal");
            }
        }
        
        Direccion guardada = direccionRepository.save(direccion);
        System.out.println("✅ Dirección creada: " + guardada.getAlias());
        return guardada;
    }

    // Actualizar una dirección
    @Transactional
    public Direccion actualizarDireccion(Long id, Direccion direccionActualizada) {
        System.out.println("✏️ [DireccionService] Actualizando dirección con ID: " + id);
        
        return direccionRepository.findById(id).map(direccion -> {
            if (direccionActualizada.getAlias() != null) {
                direccion.setAlias(direccionActualizada.getAlias());
            }
            if (direccionActualizada.getCallePrincipal() != null) {
                direccion.setCallePrincipal(direccionActualizada.getCallePrincipal());
            }
            if (direccionActualizada.getCalleSecundaria() != null) {
                direccion.setCalleSecundaria(direccionActualizada.getCalleSecundaria());
            }
            if (direccionActualizada.getCiudad() != null) {
                direccion.setCiudad(direccionActualizada.getCiudad());
            }
            if (direccionActualizada.getTelefono() != null) {
                direccion.setTelefono(direccionActualizada.getTelefono());
            }
            if (direccionActualizada.getReferencia() != null) {
                direccion.setReferencia(direccionActualizada.getReferencia());
            }
            
            // Si se cambia a principal, desmarcar la anterior
            if (direccionActualizada.getEsPrincipal() != null && direccionActualizada.getEsPrincipal()) {
                Direccion antiguaPrincipal = direccionRepository.findByUsuarioIdAndEsPrincipalTrue(
                        direccion.getUsuario().getId());
                if (antiguaPrincipal != null && !antiguaPrincipal.getId().equals(id)) {
                    antiguaPrincipal.setEsPrincipal(false);
                    direccionRepository.save(antiguaPrincipal);
                }
                direccion.setEsPrincipal(true);
            }
            
            return direccionRepository.save(direccion);
        }).orElseThrow(() -> new RuntimeException("Dirección no encontrada con ID: " + id));
    }

    // Eliminar una dirección
    @Transactional
    public void eliminarDireccion(Long id) {
        System.out.println("🗑️ [DireccionService] Eliminando dirección con ID: " + id);
        
        Direccion direccion = direccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada con ID: " + id));
        
        Long usuarioId = direccion.getUsuario().getId();
        boolean eraPrincipal = direccion.getEsPrincipal() != null && direccion.getEsPrincipal();
        
        direccionRepository.deleteById(id);
        System.out.println("✅ Dirección eliminada");
        
        // Si era la principal, marcar otra como principal automáticamente
        if (eraPrincipal) {
            List<Direccion> direccionesRestantes = direccionRepository.findByUsuarioId(usuarioId);
            if (!direccionesRestantes.isEmpty()) {
                Direccion nuevaPrincipal = direccionesRestantes.get(0);
                nuevaPrincipal.setEsPrincipal(true);
                direccionRepository.save(nuevaPrincipal);
                System.out.println("   ⭐ Nueva dirección principal: " + nuevaPrincipal.getAlias());
            }
        }
    }

    // Marcar una dirección como principal
    @Transactional
    public Direccion marcarComoPrincipal(Long id) {
        System.out.println("⭐ [DireccionService] Marcando dirección " + id + " como principal");
        
        Direccion direccion = direccionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dirección no encontrada con ID: " + id));
        
        Long usuarioId = direccion.getUsuario().getId();
        
        // Desmarcar la anterior principal
        Direccion antiguaPrincipal = direccionRepository.findByUsuarioIdAndEsPrincipalTrue(usuarioId);
        if (antiguaPrincipal != null) {
            antiguaPrincipal.setEsPrincipal(false);
            direccionRepository.save(antiguaPrincipal);
        }
        
        // Marcar la nueva como principal
        direccion.setEsPrincipal(true);
        return direccionRepository.save(direccion);
    }
}
