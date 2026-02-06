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
}
