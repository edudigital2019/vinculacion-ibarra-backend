package com.app.municipio.modules.business.repositories;

import com.app.municipio.modules.business.models.Business;
import com.app.municipio.modules.business.models.enums.ValidationStatus;
import com.app.municipio.modules.business.models.projections.BusinessOwnerProjection;
import com.app.municipio.modules.business.models.projections.BusinessProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {


    @Query("SELECT b FROM Business b WHERE b.category.name = :category")
    Page<Business> findAllByCategory(String category, Pageable pageable);

    @Query("SELECT b FROM Business b WHERE b.category.name = :category AND b.user.id = :userId")
    Page<Business> findAllByCategoryAndUserId(String category,Long userId, Pageable pageable);

    @Query("SELECT b FROM Business b WHERE b.user.id = :userId")
    Page<Business> findAllByUserId(Long userId, Pageable pageable);

    @Query("SELECT b FROM Business b WHERE b.user.id = :userId")
    List<Business> findAllByUserId(Long userId);

    @Query("SELECT b FROM Business b WHERE b.category.name = :category AND b.validationStatus = :status")
    Page<Business> findAllByCategoryAndValidationStatus(String category, ValidationStatus status, Pageable pageable);

    Optional <Business> findByIdAndValidationStatus(Long id, ValidationStatus status);

    Page<Business> findByValidationStatus(ValidationStatus validationStatus, Pageable pageable);

    Optional <Business> findByIdAndUserId(Long id, Long userId);

    /**
     * Obtiene owners únicos con negocios pendientes - Paginado
     */
    @Query(value = """
        SELECT u.id as userId, CONCAT(u.name, ' ', COALESCE(u.lastname, '')) as ownerName, u.phone as phoneNumber, u.identification as identification
        FROM business b INNER JOIN app_user u ON b.user_id = u.id
        WHERE b.validation_status = :#{#status.name()}
        GROUP BY u.id, u.name, u.lastname
        ORDER BY u.name, u.lastname
        """,
            countQuery = """
        SELECT COUNT(DISTINCT u.id)
        FROM business b  INNER JOIN app_user u ON b.user_id = u.id WHERE b.validation_status = :#{#status.name()}
        """,
            nativeQuery = true)
    Page<BusinessOwnerProjection> findPendingBusinessesGroupedByOwner(
            @Param("status") ValidationStatus status,
            Pageable pageable
    );

    /**
     * Obtiene negocios específicos por owner IDs
     */
    @Query("""
        SELECT b.id as id, b.commercialName as commercialName, b.validationStatus as status, b.user.id as userId FROM Business b WHERE b.validationStatus = :status AND b.user.id IN :ownerIds
        ORDER BY b.user.name, b.commercialName
        """)
    List<BusinessProjection> findBusinessesByOwnerIds(
            @Param("status") ValidationStatus status,
            @Param("ownerIds") List<Long> ownerIds
    );
    //Actualizar Negocios
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Business b WHERE b.id = :businessId AND b.user.id = :userId")
    boolean existsByIdAndUserId(Long businessId, Long userId);

    /**
     * Contar negocios por estado de validación
     */
    long countByValidationStatus(ValidationStatus status);

    /**
     * Método para búsqueda y filtrado
     * Ambos parámetros son opcionales
     */
    @Query("""
    SELECT b FROM Business b
    WHERE 
        (:status IS NULL OR b.validationStatus = :status) AND
        (:searchTerm IS NULL OR :searchTerm = '' OR
         LOWER(CONCAT(b.user.name, ' ', COALESCE(b.user.lastname, ''))) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR 
         b.user.identification LIKE CONCAT('%', :searchTerm, '%') OR
         LOWER(b.commercialName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
    ORDER BY b.registrationDate DESC
    """)
    Page<Business> findBusinessesWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("status") ValidationStatus status,
            Pageable pageable);
}

