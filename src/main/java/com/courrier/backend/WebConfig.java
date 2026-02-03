package com.courrier.backend;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 🔒 SEGURIDAD CORS: Cambiar de allowedOrigins("*") a allowedOriginPatterns("*")
                // Esto permite wildcard con credentials: true en el frontend
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                // 🔒 CRÍTICO: Permitir credenciales (JWT tokens en Authorization header)
                .allowCredentials(true)
                // 🔒 Permitir que el frontend acceda a headers de respuesta (ej: X-Total-Count)
                .exposedHeaders("X-Total-Count", "X-Page-Number", "Authorization");

        System.out.println("✅ CORS configurado con allowCredentials(true) - JWT y sesiones habilitadas");
    }
}
