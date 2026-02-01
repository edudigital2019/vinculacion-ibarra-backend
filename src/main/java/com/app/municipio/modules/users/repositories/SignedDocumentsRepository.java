package com.app.municipio.modules.users.repositories;

import com.app.municipio.modules.users.models.SignedDocuments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SignedDocumentsRepository extends JpaRepository<SignedDocuments, Long> {
    Optional<SignedDocuments> findByAppUserId(Long appUserId);

    boolean existsByAppUserId(Long userId);
}
