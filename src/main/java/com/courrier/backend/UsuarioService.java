package com.courrier.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario guardarUsuario(Usuario usuario) {
        String email = usuario.getEmail();
        if (email != null && usuarioRepository.findByEmail(email) != null) {
            throw new UserAlreadyExistsException("El email ya está registrado");
        }
        return usuarioRepository.save(usuario);
    }

    // 1. Buscar usuario por ID (Vital para el Dashboard)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    // 2. Actualizar datos de contacto (Teléfono)
    public Usuario actualizarPerfil(Long id, String nuevoTelefono, String nuevaDireccion) {
        Usuario usuario = obtenerPorId(id);
        if (usuario != null) {
            // Solo actualizamos si el dato no es nulo
            if (nuevoTelefono != null) usuario.setTelefono(nuevoTelefono);
            // El campo 'direccion' no existe en Usuario, así que se ignora nuevaDireccion
            return usuarioRepository.save(usuario);
        }
        return null;
    }
}
