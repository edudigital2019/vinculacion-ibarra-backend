package com.app.municipio.modules.business.repositories;

import com.app.municipio.modules.business.models.BusinessCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessCategoryRepository extends JpaRepository<BusinessCategory, Long> {

    boolean existsByName(String categoryName);
}
