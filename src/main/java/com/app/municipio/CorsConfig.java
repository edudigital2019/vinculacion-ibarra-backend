package com.app.municipio;
 
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.CorsRegistry;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
 
@Configuration

public class CorsConfig {
 
    @Bean

    public WebMvcConfigurer corsConfigurer() {

        return new WebMvcConfigurer() {

            @Override

            public void addCorsMappings(CorsRegistry registry) {

                registry.addMapping("/**") // Permitir todas las rutas

                        .allowedOrigins("*") // Permitir desde CUALQUIER lugar (Móvil, Web, Postman)

                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Verbos permitidos

                        .allowedHeaders("*"); // Headers permitidos

            }

        };

    }

}
 