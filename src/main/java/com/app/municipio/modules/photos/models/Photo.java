package com.app.municipio.modules.photos.models;

import com.app.municipio.modules.business.models.Business;
import com.app.municipio.modules.business.models.BusinessPromo;
import com.app.municipio.modules.photos.enums.PhotoType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "photo")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(name = "file_type", nullable = false)
    private String fileType;

    @Column(name = "public_id", nullable = false, unique = true)
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(name = "photo_type", nullable = false)
    private PhotoType photoType;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private BusinessPromo businessPromo;

}
