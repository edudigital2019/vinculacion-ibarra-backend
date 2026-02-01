package com.app.municipio.application.config.jwt;

import com.app.municipio.modules.users.models.AppUser;
import com.app.municipio.modules.users.repositories.UsersRepository;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtTokenValidator extends OncePerRequestFilter {
    private final JwtBuild jwtBuild;
    private final UsersRepository userRepository;

    public JwtTokenValidator(JwtBuild jwtUtils, UsersRepository userRepository) {
        this.jwtBuild = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String jwtToken = request.getHeader("Authorization");

        if (jwtToken != null && jwtToken.startsWith("Bearer ")) {
            try {
                jwtToken = jwtToken.substring(7);
                DecodedJWT decodedJWT = jwtBuild.verifyToken(jwtToken);
                String username = jwtBuild.extractUsername(decodedJWT);

                AppUser user = userRepository.findByUsernameOrEmail(username)
                        .orElse(null);

                if (user != null) {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            user.getAuthorities()
                    );
                    context.setAuthentication(authenticationToken);
                    SecurityContextHolder.setContext(context);
                }
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token inválido");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
