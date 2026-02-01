package com.app.municipio.modules.users.services;

import com.app.municipio.application.config.jwt.JwtBuild;
import com.app.municipio.application.exceptions.ClientException;
import com.app.municipio.modules.users.dto.AuthResponseDto;
import com.app.municipio.modules.users.dto.LoginDto;
import com.app.municipio.modules.users.dto.WhoAmIDto;
import com.app.municipio.modules.users.models.AppUser;
import com.app.municipio.modules.users.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtBuild jwtBuild;
    private final UsersRepository userRepository;
    private final AuthenticationManager authenticationManager;

    public AuthResponseDto login(LoginDto dto) {
        String username = dto.getUsername();
        String password = dto.getPassword();

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            String accessToken = jwtBuild.createToken(auth);

            AuthResponseDto authResponse = new AuthResponseDto();
            authResponse.setUsername(username);
            authResponse.setMessage("Se ha iniciado sesión correctamente");
            authResponse.setJwt(accessToken);
            authResponse.setStatus(true);

            return authResponse;
        } catch (DisabledException e) {
            throw new BadCredentialsException("Usuario deshabilitado");
        } catch (LockedException e) {
            throw new BadCredentialsException("Cuenta bloqueada");
        } catch (AccountExpiredException e) {
            throw new BadCredentialsException("Cuenta expirada");
        } catch (CredentialsExpiredException e) {
            throw new BadCredentialsException("Credenciales expiradas");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Credenciales inválidas");
        }
    }

    public WhoAmIDto getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        AppUser user;
        if (principal instanceof AppUser appUser) {
            user = appUser;
        } else {
            String username = principal.toString();
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ClientException("Usuario no encontrado", HttpStatus.NOT_FOUND));
        }

        var roles = user.getRoles().stream().map(role -> role.getAuthority().name()).toList();
        return WhoAmIDto.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .identification(user.getIdentification())
                .lastname(user.getLastname())
                .name(user.getName())
                .phone(user.getPhone())
                .address(user.getAddress())
                .roles(roles)
                .build();
    }
}
