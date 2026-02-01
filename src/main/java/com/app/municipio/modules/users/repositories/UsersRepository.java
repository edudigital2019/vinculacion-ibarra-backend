package com.app.municipio.modules.users.repositories;

import com.app.municipio.modules.users.models.AppUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends JpaRepository<AppUser, Long> {

    @Query("SELECT u FROM AppUser u WHERE u.username = :username OR u.email = :username")
    Optional<AppUser> findByUsernameOrEmail(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<AppUser> findByUsername(String username);

    boolean existsByIdentification(String identification);

    long countByEnabled(boolean enabled);
    
    Page<AppUser> findByEnabled(boolean enabled, Pageable pageable);
     
    Optional<AppUser> findByIdentification(String identification);

    @Query("SELECT u FROM AppUser u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR u.identification = :searchTerm")
    Page<AppUser> findByNameOrIdentification(String searchTerm, Pageable pageable);

    Page<AppUser> findAll(Pageable pageable);
}
