package com.app.municipio.modules.users.repositories;

import com.app.municipio.modules.users.models.PaymentReceipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentReceiptRepository extends JpaRepository<PaymentReceipt, Long> {
    Optional<PaymentReceipt> findByAppUserId(Long appUserId);
}
