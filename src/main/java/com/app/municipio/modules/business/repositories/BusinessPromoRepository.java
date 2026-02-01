package com.app.municipio.modules.business.repositories;

import com.app.municipio.modules.business.models.BusinessPromo;
import com.app.municipio.modules.business.models.enums.PromoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BusinessPromoRepository extends JpaRepository<BusinessPromo, Long> {

        @Query("SELECT p FROM BusinessPromo p WHERE p.fechaPromoFin >= :today")
        List<BusinessPromo> findAllActivePromosPublic(@Param("today") LocalDate today);

        @Query("SELECT p FROM BusinessPromo p " +
                        "WHERE p.fechaPromoFin >= :today " +
                        "AND p.tipoPromocion = :type")
        List<BusinessPromo> findAllActivePromosPublicFiltered(@Param("today") LocalDate today,
                        @Param("type") PromoType type);

        @Query("SELECT p FROM BusinessPromo p " +
                        "WHERE p.fechaPromoFin >= :today " +
                        "AND p.business.id = :business")
        List<BusinessPromo> findAllActivePromosByBusinessId(@Param("today") LocalDate today,
                        @Param("business") Long businessId);

        @Query("SELECT p FROM BusinessPromo p " +
                        "WHERE p.fechaPromoFin >= :today " +
                        "AND (:type IS NULL OR p.tipoPromocion = :type) " +
                        "AND (:categoryId IS NULL OR p.business.category.id = :categoryId) " +
                        "AND p.business.validationStatus = com.app.municipio.modules.business.models.enums.ValidationStatus.VALIDATED")
        List<BusinessPromo> searchPromos(@Param("today") LocalDate today,
                        @Param("type") PromoType type,
                        @Param("categoryId") Long categoryId);

}
