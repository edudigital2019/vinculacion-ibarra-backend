package com.app.municipio.modules.events.repositories;

import com.app.municipio.modules.events.models.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {
}
