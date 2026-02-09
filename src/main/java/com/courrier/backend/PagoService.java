package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private FacturaRepository facturaRepository;

    @Autowired
    private EnvioRepository envioRepository;

    @Autowired
    private PaqueteRepository paqueteRepo;

    // Obtener pagos de una factura
    public List<Pago> obtenerPorFactura(Long facturaId) {
        return pagoRepository.findByFacturaId(facturaId, Sort.by(Sort.Direction.DESC, "fecha"));
    }

    // Obtener todos los pagos de un usuario (a través de sus facturas)
    public List<Pago> obtenerPorUsuario(Long usuarioId) {
        return pagoRepository.findByUsuarioId(usuarioId);
    }

    // Obtener un pago por ID (versión simple)
    public Pago obtenerPagoPorId(Long id) {
        return pagoRepository.findById(id).orElse(null);
    }

    // Obtener todos los pagos PENDIENTES
    public List<Pago> obtenerPendientes() {
        return pagoRepository.findByEstado("PENDIENTE", Sort.by(Sort.Direction.DESC, "fecha"));
    }

    // Buscar pago pendiente por paqueteId
    public Pago obtenerPagoPorPaqueteId(Long paqueteId) {
        return pagoRepository.findPagoPendienteByPaqueteId(paqueteId);
    }

    // Registrar un nuevo pago
    public Pago registrarPago(Long facturaId, Double monto, String metodoPago, String referencia, String comprobanteNombre) {
        Factura factura = facturaRepository.findById(facturaId)
            .orElseThrow(() -> new RuntimeException("Factura no encontrada con ID: " + facturaId));
        Pago pago = new Pago();
        pago.setFactura(factura);
        pago.setMonto(monto);
        pago.setMetodoPago(metodoPago);
        pago.setReferencia(referencia);
        pago.setComprobante(comprobanteNombre);
        pago.setEstado("PENDIENTE");
        return pagoRepository.save(pago);
    }

    // Actualizar estado de pago y sincronizar factura/paquete
    public Pago actualizarEstado(Long pagoId, String nuevoEstado) {
        Pago pago = obtenerPagoPorId(pagoId);
        if (pago == null) throw new RuntimeException("Pago no encontrado");
        pago.setEstado(nuevoEstado);
        Pago actualizado = pagoRepository.save(pago);

        // Si el estado es VERIFICADO, solo actualiza la factura
        if ("VERIFICADO".equals(nuevoEstado)) {
            Factura factura = pago.getFactura();
            if (factura != null) {
                factura.setEstado("PAGADO");
                facturaRepository.save(factura);
            }
        }
        return actualizado;
    }

    // Guardar pago (para uso en controlador)
    public Pago guardarPago(Pago pago) {
        return pagoRepository.save(pago);
    }

    public void eliminarPago(Long id) {
        pagoRepository.deleteById(id);
    }

    public Optional<Pago> obtenerPorId(Long id) {
        return pagoRepository.findById(id);
    }
}
