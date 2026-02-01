package com.app.municipio.modules.photos.repository;

import com.app.municipio.modules.photos.models.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Integer> {
    List<Photo> findAllByBusiness_Id(Long businessId);
}
