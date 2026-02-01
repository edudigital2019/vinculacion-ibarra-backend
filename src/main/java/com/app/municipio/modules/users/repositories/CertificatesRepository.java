package com.app.municipio.modules.users.repositories;

import com.app.municipio.modules.users.models.Certificates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertificatesRepository extends JpaRepository<Certificates, Long> {
    Optional<Certificates> findByAppUserId(Long appUserId);
}
