package com.app.municipio.modules.users.repositories;

import com.app.municipio.modules.users.models.IdentityDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDocumentsRepository extends JpaRepository<IdentityDocuments, Long> {
    Optional<IdentityDocuments> findByAppUserId(Long appUserId);
}
