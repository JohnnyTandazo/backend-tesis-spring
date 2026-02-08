package com.courrier.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

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
                .requestMatchers("/api/public/**").permitAll()

                // 🟢 ENDPOINTS FACTURAS (PDF y CRUD): cualquier usuario autenticado
                .requestMatchers("/api/facturas/**").authenticated()
                .requestMatchers("/api/pdf/factura/**").authenticated()

                // 🟢 ENDPOINTS OPERADOR/ADMIN
                .requestMatchers("/api/operador/**").hasAnyAuthority("ROLE_OPERADOR", "ROLE_ADMIN", "OPERADOR", "ADMIN")

                // 🟢 ENDPOINTS CLIENTE/ADMIN
                .requestMatchers("/api/direcciones/**").hasAnyAuthority("ROLE_CLIENTE", "ROLE_ADMIN", "CLIENTE", "ADMIN")
                
                // 🔴 TODO LO DEMÁS REQUIERE JWT VÁLIDO
                .anyRequest().authenticated()
            )
            // 🔐 JWT FILTER
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
            
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
        // Permitir frontend Vercel y localhost con wildcard
        configuration.setAllowedOriginPatterns(List.of(
            "https://*.vercel.app",
            "http://localhost:*"
        ));
        
        // 📤 MÉTODOS HTTP PERMITIDOS
        configuration.setAllowedMethods(Arrays.asList(
            "GET", 
            "POST", 
            "PUT", 
            "DELETE", 
            "OPTIONS"  // ✅ OBLIGATORIO para Preflight
        ));
        
        // 📋 HEADERS PERMITIDOS EN PETICIONES
        configuration.setAllowedHeaders(List.of("*"));
        
        // 📤 HEADERS EXPUESTOS EN RESPUESTAS
        configuration.setExposedHeaders(Arrays.asList(
            "Access-Control-Allow-Origin",
            "Access-Control-Allow-Credentials",
            "Authorization"  // Si el servidor devuelve tokens
        ));
        
        // 🔐 PERMITIR CREDENCIALES
        // ✅ Compatible con allowedOrigins explícitos
        configuration.setAllowCredentials(true);
        
        // Registrar la configuración para todos los paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        System.out.println("✅ [WebSecurityConfig] CORS configurado para orígenes específicos");
        
        return source;
    }

    /**
     * 🔐 PASSWORD ENCODER
     * Encriptador de contraseñas usando BCrypt
     * ✅ Necesario para registrar usuarios y validar passwords en login
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        System.out.println("✅ [WebSecurityConfig] PasswordEncoder bean registrado (BCryptPasswordEncoder)");
        return new BCryptPasswordEncoder();
    }

    /**
     * 🔑 AUTHENTICATION MANAGER
     * Gestor de autenticación necesario para el login
     * ✅ Inyecta el AuthenticationManager en controllers para authenticate()
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        System.out.println("✅ [WebSecurityConfig] AuthenticationManager bean registrado");
        return config.getAuthenticationManager();
    }
}
