package com.app.municipio.modules.business.repositories;

import com.app.municipio.modules.business.models.Parishes;
import com.app.municipio.modules.business.models.enums.ParishType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParishesRepository extends JpaRepository<Parishes, Long> {
    boolean existsByName(String name);

    // Opcional: método para encontrar por nombre
    Optional<Parishes> findByName(String name);

    List<Parishes> findAllByParishType(ParishType parishType);
}
