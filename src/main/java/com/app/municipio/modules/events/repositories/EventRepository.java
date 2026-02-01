package com.app.municipio.modules.events.repositories;

import com.app.municipio.modules.events.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    //  Para listar: trae contactos y servicios
    @Query("SELECT DISTINCT e FROM Event e " +
            "LEFT JOIN FETCH e.contact " +
            "LEFT JOIN FETCH e.services")
    List<Event> findAllWithRelations();

    //  Para buscar por id: trae contactos y servicios
    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.contact " +
            "LEFT JOIN FETCH e.services " +
            "WHERE e.id = :id")
    Optional<Event> findByIdWithRelations(Long id);
}
