package com.app.municipio.modules.recovery.password.repositories;

import com.app.municipio.modules.recovery.password.models.OtpRecovery;
import com.app.municipio.modules.users.models.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRecoveryRepository extends JpaRepository<OtpRecovery, UUID> {

    @Transactional
    void deleteAllByAppUser(AppUser appUser);

    boolean existsByAppUser(AppUser appUser);

    Optional<OtpRecovery> findByAppUser(AppUser appUser);

}
