package com.tecnologico.aires.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/img/**", "/favicon.ico", "/login").permitAll()

                        // Admins
                        .requestMatchers("/usuarios/**", "/edificios/**", "/espacios/**")
                        .hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR")

                        // Admins y Servicio Social
                        .requestMatchers("/equipos/**", "/qr/**", "/inventario/**")
                        .hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR", "PERSONAL_SERVICIO_SOCIAL",
                                "ROLE_PERSONAL_SERVICIO_SOCIAL")

                        // Todos los roles
                        .requestMatchers("/reportes/**", "/bitacoras/**", "/historial/**")
                        .hasAnyAuthority("ADMINISTRADOR", "ROLE_ADMINISTRADOR", "TECNICO", "ROLE_TECNICO",
                                "PERSONAL_SERVICIO_SOCIAL", "ROLE_PERSONAL_SERVICIO_SOCIAL")

                        .anyRequest().authenticated())
                .formLogin(login -> login
                        .loginPage("/login")
                        // Usamos nuestro manejador personalizado en lugar de defaultSuccessUrl fijo
                        .successHandler(customSuccessHandler())
                        .failureUrl("/login?error=true")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll());

        return http.build();
    }

    /**
     * Redirige al usuario a su pantalla correspondiente según su Rol al iniciar
     * sesión.
     */
    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return (request, response, authentication) -> {
            var roles = authentication.getAuthorities();

            boolean esAdmin = roles.stream()
                    .anyMatch(r -> r.getAuthority().contains("ADMINISTRADOR"));
            boolean esTecnico = roles.stream()
                    .anyMatch(r -> r.getAuthority().contains("TECNICO"));
            boolean esServicioSocial = roles.stream()
                    .anyMatch(r -> r.getAuthority().contains("PERSONAL_SERVICIO_SOCIAL"));

            if (esAdmin) {
                response.sendRedirect("/edificios");
            } else if (esTecnico) {
                response.sendRedirect("/reportes");
            } else if (esServicioSocial) {
                response.sendRedirect("/equipos");
            } else {
                // Si el usuario está autenticado pero no coincide con los anteriores,
                // lo mandamos a una ruta raíz autenticada en vez de ciclar en /login
                response.sendRedirect("/");
            }
        };
    }
}