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
        System.out.println("\n🔄 [DireccionService.crearDireccion] ===== INICIANDO CREACIÓN =====");
        System.out.println("   usuarioId recibido: " + usuarioId);
        
        // ============ VALIDACIÓN DE ENTRADA ============
        System.out.println("📋 PASO 1: Validando parámetros de entrada...");
        
        if (usuarioId == null || usuarioId <= 0) {
            System.out.println("❌ ERROR: usuarioId es nulo o inválido: " + usuarioId);
            throw new RuntimeException("usuarioId no puede ser nulo o menor que 1");
        }
        System.out.println("   ✅ usuarioId válido: " + usuarioId);
        
        if (direccion == null) {
            System.out.println("❌ ERROR: Objeto Direccion es nulo");
            throw new RuntimeException("El objeto Direccion no puede ser nulo");
        }
        System.out.println("   ✅ Objeto Direccion recibido");
        
        // ============ VALIDACIÓN DE CAMPOS REQUERIDOS ============
        System.out.println("📋 PASO 2: Validando campos requeridos...");
        
        if (direccion.getAlias() == null || direccion.getAlias().trim().isEmpty()) {
            System.out.println("❌ ERROR: alias es requerido");
            throw new RuntimeException("El campo 'alias' es requerido (Ej: Casa, Oficina)");
        }
        System.out.println("   ✅ alias: " + direccion.getAlias());
        
        if (direccion.getCallePrincipal() == null || direccion.getCallePrincipal().trim().isEmpty()) {
            System.out.println("❌ ERROR: callePrincipal es requerido");
            throw new RuntimeException("El campo 'callePrincipal' es requerido");
        }
        System.out.println("   ✅ callePrincipal: " + direccion.getCallePrincipal());
        
        if (direccion.getCiudad() == null || direccion.getCiudad().trim().isEmpty()) {
            System.out.println("❌ ERROR: ciudad es requerida");
            throw new RuntimeException("El campo 'ciudad' es requerido");
        }
        System.out.println("   ✅ ciudad: " + direccion.getCiudad());
        
        if (direccion.getTelefono() == null || direccion.getTelefono().trim().isEmpty()) {
            System.out.println("❌ ERROR: telefono es requerido");
            throw new RuntimeException("El campo 'telefono' es requerido");
        }
        System.out.println("   ✅ telefono: " + direccion.getTelefono());
        
        // ============ BUSCAR USUARIO ============
        System.out.println("📋 PASO 3: Buscando usuario en base de datos...");
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> {
                    System.out.println("❌ ERROR: Usuario no encontrado con ID: " + usuarioId);
                    return new RuntimeException("Usuario no encontrado con ID: " + usuarioId);
                });
        
        System.out.println("✅ Usuario encontrado: " + usuario.getNombre() + " (" + usuario.getEmail() + ")");
        
        // ============ ASIGNAR USUARIO ============
        System.out.println("📋 PASO 4: Asignando usuario a la dirección...");
        direccion.setUsuario(usuario);
        System.out.println("   ✅ Usuario asignado");
        
        // ============ VALIDACIÓN DE DIRECCIÓN PRINCIPAL ============
        System.out.println("📋 PASO 5: Verificando si será dirección principal...");
        long cantidadDirecciones = direccionRepository.countByUsuarioId(usuarioId);
        System.out.println("   Direcciones existentes del usuario: " + cantidadDirecciones);
        
        if (cantidadDirecciones == 0) {
            direccion.setEsPrincipal(true);
            System.out.println("   ⭐ Marcada como dirección PRINCIPAL (primera del usuario)");
        } else {
            direccion.setEsPrincipal(false);
            System.out.println("   📌 Marcada como dirección SECUNDARIA");
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
        
        // ============ GUARDAR EN BASE DE DATOS ============
        System.out.println("📋 PASO 6: Guardando en base de datos...");
        try {
            Direccion guardada = direccionRepository.save(direccion);
            System.out.println("✅ Dirección guardada exitosamente!");
            System.out.println("   ID generado: " + guardada.getId());
            System.out.println("   Alias: " + guardada.getAlias());
            System.out.println("   Dirección: " + guardada.getCallePrincipal() + ", " + guardada.getCiudad());
            System.out.println("   Es Principal: " + guardada.getEsPrincipal());
            System.out.println("===== CREACIÓN COMPLETADA CON ÉXITO =====\n");
            return guardada;
        } catch (Exception e) {
            System.out.println("❌ ERROR al guardar en BD: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error al guardar la dirección: " + e.getMessage());
        }
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
