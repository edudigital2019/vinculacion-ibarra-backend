package com.app.municipio.modules.users.services;

import com.app.municipio.modules.users.models.AppUser;
import com.app.municipio.modules.users.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

        private final UsersRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                AppUser user = userRepository.findByUsernameOrEmail(username)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
                if (!user.isEnabled()) {
                        throw new DisabledException("Usuario pendiente de aprobación administrativa");
                }
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                user.getRoles()
                                .forEach(role -> authorities.add(new SimpleGrantedAuthority(
                                                "ROLE_".concat(role.getAuthority().name()))));

                return new User(user.getUsername(),
                                user.getPassword(),
                                user.isEnabled(),
                                user.isAccountNonExpired(),
                                user.isCredentialsNonExpired(),
                                user.isAccountNonLocked(),
                                authorities);
        }

}
