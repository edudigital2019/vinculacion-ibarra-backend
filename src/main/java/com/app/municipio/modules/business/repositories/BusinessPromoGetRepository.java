package com.app.municipio.modules.business.repositories;


import com.app.municipio.modules.business.models.BusinessPromo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessPromoGetRepository extends JpaRepository<BusinessPromo, Long> {


}
