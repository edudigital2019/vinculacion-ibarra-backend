package com.app.municipio.modules.business.repositories;

import com.app.municipio.modules.business.models.BusinessDeletionRequest;
import com.app.municipio.modules.business.models.enums.DeletionRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BusinessDeletionRequestRepository extends JpaRepository<BusinessDeletionRequest, Long> {
    List<BusinessDeletionRequest> findAllByStatus(DeletionRequestStatus status);
    boolean existsByBusinessIdAndStatus(Long businessId, DeletionRequestStatus status);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from BusinessDeletionRequest r where r.business.id = :businessId")
    void deleteAllByBusinessId(@Param("businessId") Long businessId);
}
