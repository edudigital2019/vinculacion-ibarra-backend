package com.app.municipio.modules.business.models;

import com.app.municipio.modules.business.models.enums.PromoType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "promo")
public class BusinessPromo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_business_promo")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @Enumerated(EnumType.STRING)
    @Column(name = "business_promo_type", nullable = false)
    private PromoType tipoPromocion;

    @Column(name = "promotion_title", nullable = false, length = 50)
    private String tituloPromocion;

    @Column(name = "start_date", nullable = false )
    private LocalDate fechaPromoInicio;

    @Column(name = "end_date", nullable = false )
    private LocalDate fechaPromoFin;

    @Column(name = "conditions", nullable = false, length = 50)
    private String condiciones;

    @OneToMany(mappedBy = "businessPromo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List <com.app.municipio.modules.photos.models.Photo> photos;
}

