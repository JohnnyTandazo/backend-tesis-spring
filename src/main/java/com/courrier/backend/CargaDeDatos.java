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

            System.out.println("✅ DATOS DE PRUEBA CARGADOS EXITOSAMENTE");
        }
    }
}
