package com.courrier.backend;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;

@Component
public class CargaDeDatos implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepo;

    @Autowired
    private PaqueteRepository paqueteRepo;

    @Autowired
    private FacturaRepository facturaRepo;

    @Autowired
    private PagoRepository pagoRepo;

    @Override
    public void run(String... args) throws Exception {
        // Solo insertamos datos si la base está vacía (para no duplicar)
        if (usuarioRepo.count() == 0) {

            // 1. CREAR UN CLIENTE (Para que tú pruebes el login)
            Usuario cliente = new Usuario();
            cliente.setNombre("Argely Estudiante");
            cliente.setEmail("cliente@test.com");
            cliente.setPassword("12345"); // En un futuro la encriptaremos
            cliente.setRol("CLIENTE");
            cliente.setTelefono("0999999999");
            usuarioRepo.save(cliente);

            // 2. CREAR UN OPERADOR (El que actualiza los estados)
            Usuario operador = new Usuario();
            operador.setNombre("Sr. Operador");
            operador.setEmail("operador@test.com");
            operador.setPassword("admin123");
            operador.setRol("OPERADOR");
            usuarioRepo.save(operador);

            // 3. CREAR UN PAQUETE DE PRUEBA (Para el Cliente)
            Paquete p1 = new Paquete();
            p1.setTrackingNumber("USA-001");
            p1.setDescripcion("Laptop HP y Mouse");
            p1.setPesoLibras(4.5);
            p1.setPrecio(350.00);
            p1.setEstado("EN_MIAMI");
            p1.setUsuario(cliente); // ¡Este paquete es de Argely!
            paqueteRepo.save(p1);

            // 4. CREAR OTRO PAQUETE (Para ver variedad)
            Paquete p2 = new Paquete();
            p2.setTrackingNumber("USA-002");
            p2.setDescripcion("Ropa Shein");
            p2.setPesoLibras(2.0);
            p2.setPrecio(50.00);
            p2.setEstado("ADUANA");
            p2.setUsuario(cliente);
            paqueteRepo.save(p2);

            // ========================================
            // FACTURAS DE PRUEBA (Para el módulo de Pagos)
            // ========================================
            System.out.println("📋 Creando facturas de prueba...");

            // FACTURA 1: PENDIENTE (para que el usuario la pueda pagar)
            Factura f1 = new Factura();
            f1.setNumeroFactura("FAC-2026-001");
            f1.setMonto(350.00);  // El precio del paquete USA-001
            f1.setEstado("PENDIENTE");
            f1.setDescripcion("Envío USA-001: Laptop HP y Mouse");
            f1.setUsuario(cliente);
            f1.setFechaEmision(LocalDateTime.now());
            f1.setFechaVencimiento(LocalDateTime.now().plusDays(15));
            facturaRepo.save(f1);

            // FACTURA 2: PENDIENTE (Otra factura)
            Factura f2 = new Factura();
            f2.setNumeroFactura("FAC-2026-002");
            f2.setMonto(50.00);  // El precio del paquete USA-002
            f2.setEstado("PENDIENTE");
            f2.setDescripcion("Envío USA-002: Ropa Shein");
            f2.setUsuario(cliente);
            f2.setFechaEmision(LocalDateTime.now());
            f2.setFechaVencimiento(LocalDateTime.now().plusDays(10));
            facturaRepo.save(f2);

            // FACTURA 3: PAGADA (Con historial de pago)
            Factura f3 = new Factura();
            f3.setNumeroFactura("FAC-2026-003");
            f3.setMonto(100.00);
            f3.setEstado("PAGADA");
            f3.setDescripcion("Servicio de envío expedito");
            f3.setUsuario(cliente);
            f3.setFechaEmision(LocalDateTime.now().minusDays(30));
            f3.setFechaVencimiento(LocalDateTime.now().minusDays(20));
            facturaRepo.save(f3);

            // ========================================
            // PAGOS DE PRUEBA
            // ========================================
            System.out.println("💳 Creando pagos de prueba...");

            // PAGO 1: Pago confirmado para Factura 3
            Pago pago1 = new Pago();
            pago1.setFactura(f3);
            pago1.setMonto(100.00);
            pago1.setMetodoPago("TARJETA_CREDITO");
            pago1.setEstado("CONFIRMADO");
            pago1.setFecha(LocalDateTime.now().minusDays(25));
            pago1.setReferencia("TRX-2026-00001");
            pago1.setDescripcion("Pago con tarjeta de crédito Visa");
            pagoRepo.save(pago1);

            // PAGO 2: Pago parcial pendiente para Factura 1
            Pago pago2 = new Pago();
            pago2.setFactura(f1);
            pago2.setMonto(200.00);  // Pago parcial
            pago2.setMetodoPago("TRANSFERENCIA");
            pago2.setEstado("PENDIENTE");
            pago2.setFecha(LocalDateTime.now().minusHours(2));
            pago2.setDescripcion("Transferencia bancaria iniciada");
            pagoRepo.save(pago2);

            System.out.println("✅ DATOS DE PRUEBA CARGADOS EXITOSAMENTE");
        }
    }
}
