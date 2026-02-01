package com.app.municipio.modules.users.repositories;

import com.app.municipio.modules.users.models.Authorities;
import com.app.municipio.modules.users.models.enums.UserRoles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthoritiesRepository extends JpaRepository<Authorities, Long> {

    Optional<Authorities> findByAuthority(UserRoles name);
    boolean existsByAuthority(UserRoles role);

}
