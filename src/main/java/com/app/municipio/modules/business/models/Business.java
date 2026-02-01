package com.app.municipio.modules.business.models;

import com.app.municipio.modules.business.models.enums.DeliveryService;
import com.app.municipio.modules.business.models.enums.SalePlace;
import com.app.municipio.modules.business.models.enums.ValidationStatus;
import com.app.municipio.modules.photos.models.Photo;
import com.app.municipio.modules.users.models.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "business")
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_business")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "category_business_id", nullable = false)
    private BusinessCategory category;

    @Column(name = "commercial_name", nullable = false, length = 50)
    private String commercialName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "parish_community_sector", nullable = false, length = 100)
    private String parishCommunitySector;

    // Social Media URLs
    @Column(name = "facebook_url")
    private String facebook;

    @Column(name = "instagram_url")
    private String instagram;

    @Column(name = "tiktok_url")
    private String tiktok;

    @Column(name = "website_url")
    private String website;

    // Contact Information
    @Column(name = "contact_phone")
    private String phone;

    @Column(name = "accepts_whatsapp_orders", nullable = false)
    private Boolean acceptsWhatsappOrders;

    // WhatsApp Number
    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_service", nullable = false)
    private DeliveryService deliveryService;

    @Enumerated(EnumType.STRING)
    @Column(name = "sales_place", nullable = false)
    private SalePlace salePlace;

    @Column(name = "udel_support_received", nullable = false)
    private Boolean receivedUdelSupport;

    @Column(name = "registration_date", nullable = false)
    private LocalDate registrationDate;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "google_maps_coordinates")
    private String googleMapsCoordinates;

    @Column(name = "schedules", columnDefinition = "TEXT", nullable = false)
    private String schedules;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", nullable = false)
    private ValidationStatus validationStatus;

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Photo> photos = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "parish_id")
    private Parishes parish;

}