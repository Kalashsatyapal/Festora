package com.festora_backend.config;

import com.festora_backend.auth.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        private final JwtAuthenticationFilter jwtFilter;

        public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
                this.jwtFilter = jwtFilter;
        }

       @Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    return http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                    .requestMatchers(
                            "/api/auth/register",
                            "/api/auth/register/organizer",
                            "/api/auth/login",
                            "/actuator/health")
                    .permitAll()

                    .requestMatchers(
                            HttpMethod.GET,
                            "/uploads/avatars/**")
                    .permitAll()

                    .requestMatchers(
                            HttpMethod.GET,
                            "/api/events",
                            "/api/events/**")
                    .permitAll()

                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")

                    .requestMatchers("/api/organizer/**")
                    .hasRole("ORGANIZER")

                    .anyRequest()
                    .authenticated())

            .addFilterBefore(
                    jwtFilter,
                    UsernamePasswordAuthenticationFilter.class)

            .build();
}}