package com.courrier.backend;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {
    // ESTA ES LA LÍNEA QUE EVITA EL ERROR DE COMPILACIÓN
    Factura findByEnvioId(Long envioId);

    // Obtener todas las facturas de un usuario
    List<Factura> findByUsuarioId(Long usuarioId);

    // Obtener todas las facturas de un usuario con ordenamiento
    List<Factura> findByUsuarioId(Long usuarioId, Sort sort);

    // Obtener facturas pendientes de un usuario
    List<Factura> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    // Obtener facturas pendientes de un usuario con ordenamiento
    List<Factura> findByUsuarioIdAndEstado(Long usuarioId, String estado, Sort sort);

    // ========================================
    // QUERY PERSONALIZADAS CON JOIN FETCH (Evita N+1)
    // ========================================

    /**
     * Obtener facturas de usuario con JOIN FETCH de Envio y Usuario
     * Evita N+1 queries al cargar relaciones en una sola consulta
     */
    @Query("SELECT DISTINCT f FROM Factura f " +
           "LEFT JOIN FETCH f.envio " +
           "LEFT JOIN FETCH f.usuario " +
           "WHERE f.usuario.id = :usuarioId " +
           "ORDER BY f.id DESC")
    List<Factura> findByUsuarioIdWithEnvioAndUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * Obtener facturas pendientes con JOIN FETCH
     * Solo facturas con estado PENDIENTE
     */
    @Query("SELECT DISTINCT f FROM Factura f " +
           "LEFT JOIN FETCH f.envio " +
           "LEFT JOIN FETCH f.usuario " +
           "WHERE f.usuario.id = :usuarioId " +
           "AND f.estado = 'PENDIENTE' " +
           "ORDER BY f.fechaVencimiento ASC")
    List<Factura> findPendientesByUsuarioWithEnvio(@Param("usuarioId") Long usuarioId);

    /**
     * Obtener una factura por ID con JOIN FETCH
     */
    @Query("SELECT f FROM Factura f " +
           "LEFT JOIN FETCH f.envio " +
           "LEFT JOIN FETCH f.usuario " +
           "WHERE f.id = :id")
    Factura findByIdWithEnvio(@Param("id") Long id);
}
