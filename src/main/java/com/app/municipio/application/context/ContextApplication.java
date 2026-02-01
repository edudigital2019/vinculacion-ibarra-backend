package com.app.municipio.application.context;

import com.app.municipio.modules.business.models.BusinessCategory;
import com.app.municipio.modules.business.models.Parishes;
import com.app.municipio.modules.business.models.enums.ParishType;
import com.app.municipio.modules.business.repositories.BusinessCategoryRepository;
import com.app.municipio.modules.business.repositories.ParishesRepository;
import com.app.municipio.modules.users.models.AppUser;
import com.app.municipio.modules.users.models.Authorities;
import com.app.municipio.modules.users.models.enums.IdTypes;
import com.app.municipio.modules.users.models.enums.UserRoles;
import com.app.municipio.modules.users.repositories.AuthoritiesRepository;
import com.app.municipio.modules.users.repositories.UsersRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Log4j2
@Configuration
@Transactional
@RequiredArgsConstructor
public class ContextApplication {

    private final UsersRepository userRepository;
    private final AuthoritiesRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final ParishesRepository parishesRepository;
    private final BusinessCategoryRepository categoriesRepository;

    @Value("${user.admin.email}")
    private String emailAdmin;

    @Value("${user.admin.password}")
    private String passwordAdmin;

    private final List<UserRoles> roles = Arrays.asList(UserRoles.values());

    @PostConstruct
    public void init() {
        tryLoadRoles();
        tryLoadUserAdmin();
        tryLoadParishes();
        tryLoadCategories();
    }

    private void tryLoadRoles() {
        log.info("Trying to load roles");
        roles.forEach(role -> {
            if (!authorityRepository.existsByAuthority(role)) {
                authorityRepository.save(Authorities.builder().authority(role).build());
            }
        });
    }

    private void tryLoadUserAdmin() {
        log.info("Trying to load user admin");
        if (!userRepository.existsByEmail(emailAdmin)) {

            var userRole = authorityRepository.findByAuthority(UserRoles.ADMIN)
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            userRepository.save(AppUser.builder()
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .name("admin")
                    .address("Lorem ipsum")
                    .phone("0000000000")
                    .lastname("admin")
                    .idType(IdTypes.PASAPORTE)
                    .email(emailAdmin)
                    .username(emailAdmin)
                    .identification("0000000000")
                    .password(passwordEncoder.encode(passwordAdmin))
                    .roles(Set.of(userRole))
                    .build());

        }

    }

    private void tryLoadParishes() {
        log.info("Trying to load parishes from JSON");

        try {
            ClassPathResource resource = new ClassPathResource("parroquias.json");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<Map<String, String>>> parishesData = mapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {
                    }
            );

            // Cargar parroquias urbanas
            List<Map<String, String>> urbanParishes = parishesData.get("Urbanas");
            if (urbanParishes != null) {
                urbanParishes.forEach(parishData -> {
                    String parishName = parishData.get("nombre");
                    if (!parishesRepository.existsByName(parishName)) {
                        Parishes parish = Parishes.builder()
                                .name(parishName)
                                .parishType(ParishType.URBANA)
                                .build();
                        parishesRepository.save(parish);
                        log.info("Loaded urban parish: {}", parishName);
                    }
                });
            }

            // Cargar parroquias rurales
            List<Map<String, String>> ruralParishes = parishesData.get("Rurales");
            if (ruralParishes != null) {
                ruralParishes.forEach(parishData -> {
                    String parishName = parishData.get("nombre");
                    if (!parishesRepository.existsByName(parishName)) {
                        Parishes parish = Parishes.builder()
                                .name(parishName)
                                .parishType(ParishType.RURAL)
                                .build();
                        parishesRepository.save(parish);
                        log.info("Loaded rural parish: {}", parishName);
                    }
                });
            }

            log.info("Parishes loading completed successfully");

        } catch (IOException e) {
            log.error("Error loading parishes from JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to load parishes", e);
        }
    }

    private void tryLoadCategories() {
        log.info("Trying to load categories from JSON");

        try {
            ClassPathResource resource = new ClassPathResource("categorias.json");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, List<String>> categoriesData = mapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<>() {
                    }
            );

            List<String> categories = categoriesData.get("Categorias");
            if (categories != null) {
                categories.forEach(categoryName -> {
                    if (!categoriesRepository.existsByName(categoryName)) {
                        BusinessCategory category = BusinessCategory.builder()
                                .name(categoryName)
                                .build();
                        categoriesRepository.save(category);
                        log.info("Loaded category: {}", categoryName);
                    }
                });
            }

            log.info("Categories loading completed successfully");

        } catch (IOException e) {
            log.error("Error loading categories from JSON: {}", e.getMessage());
            throw new RuntimeException("Failed to load categories", e);
        }
    }
}
