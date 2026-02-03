package com.courrier.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * ✅ CONFIGURACIÓN DE SPRING SECURITY
 * 
 * Propósito: Configurar la cadena de filtros de seguridad para:
 * 1. Permitir CORS (para que el Frontend pueda acceder a la API)
 * 2. Usar JWT (Stateless, sin sesiones)
 * 3. Permitir endpoints públicos (Login, Registro)
 * 4. Proteger endpoints privados con autenticación JWT
 */
@Configuration
public class WebSecurityConfig {

    /**
     * 🔒 SECURITY FILTER CHAIN
     * Define las reglas de seguridad para todas las peticiones HTTP
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1️⃣ HABILITAR CORS Y DESHABILITAR CSRF
            // ✅ CORS: Necesario para que el Frontend pueda acceder a la API
            // ✅ CSRF DISABLED: No necesario para APIs REST con JWT
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            
            // 2️⃣ GESTIÓN DE SESIÓN: STATELESS
            // ✅ JWT no requiere sesiones en el servidor
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 3️⃣ REGLAS DE ACCESO (AUTORIZACIÓN)
            .authorizeHttpRequests(auth -> auth
                // 🟢 PERMITIR OPTIONS (Preflight CORS)
                // El navegador envía OPTIONS antes de POST/PUT/DELETE
                // Si esto falla, el navegador bloquea la petición
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // 🟢 ENDPOINTS PÚBLICOS (Sin autenticación)
                .requestMatchers("/api/usuarios/login").permitAll()
                .requestMatchers("/api/usuarios/registro").permitAll()
                .requestMatchers("/api/usuarios/crear").permitAll()
                .requestMatchers("/error").permitAll()
                
                // 🔴 TODO LO DEMÁS REQUIERE JWT VÁLIDO
                .anyRequest().authenticated()
            );
            
        return http.build();
    }

    /**
     * 🌐 CONFIGURACIÓN CORS ROBUSTA
     * Define qué orígenes, métodos y headers son permitidos
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 🌍 ORÍGENES PERMITIDOS
        // Usa Arrays.asList("*") para desarrollo/testing
        // En producción, especifica exactamente tu dominio Frontend
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        // Alternativa para producción:
        // configuration.setAllowedOriginPatterns(Arrays.asList(
        //     "https://v0-currier-tics-layout.vercel.app",
        //     "http://localhost:3000"
        // ));
        
        // 📤 MÉTODOS HTTP PERMITIDOS
        configuration.setAllowedMethods(Arrays.asList(
            "GET", 
            "POST", 
            "PUT", 
            "DELETE", 
            "OPTIONS"  // ✅ OBLIGATORIO para Preflight
        ));
        
        // 📋 HEADERS PERMITIDOS EN PETICIONES
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",           // ✅ JWT token aquí
            "Content-Type",           // ✅ application/json
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",      // Para Preflight
            "Access-Control-Request-Headers"      // Para Preflight
        ));
        
        // 📤 HEADERS EXPUESTOS EN RESPUESTAS
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"  // Si el servidor devuelve tokens
        ));
        
        // 🔐 PERMITIR CREDENCIALES
        configuration.setAllowCredentials(true);
        
        // Registrar la configuración para todos los paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        System.out.println("✅ [WebSecurityConfig] CORS configurado para todos los endpoints");
        
        return source;
    }
}
