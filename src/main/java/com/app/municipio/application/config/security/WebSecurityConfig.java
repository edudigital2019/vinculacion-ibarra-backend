package com.app.municipio.application.config.security;

import com.app.municipio.application.config.jwt.JwtBuild;
import com.app.municipio.application.config.jwt.JwtTokenValidator;
import com.app.municipio.modules.users.repositories.UsersRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {
    private final JwtBuild jwtBuild;

    public WebSecurityConfig(JwtBuild jwtUtils) {
        this.jwtBuild = jwtUtils;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity httpSecurity, UsersRepository userRepository) throws Exception {
        return httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita CORS
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtTokenValidator(jwtBuild,userRepository), BasicAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*"); // Permitir todos los orígenes
        config.addAllowedHeader("*"); // Permitir todos los headers
        config.addAllowedMethod("*"); // Permitir todos los métodos (GET, POST, PUT, DELETE, etc.)
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
