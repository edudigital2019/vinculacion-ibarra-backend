package com.app.municipio.modules.events.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "event_images")
public class EventImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_event_image")
    private Long id;

    // nombre lógico: banner, principal, galeria_1, etc.
    @Column(nullable = false, length = 80)
    private String name;

    // URL o ruta
    @Column(nullable = false, length = 500)
    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_event", nullable = false)
    private Event event;

    @Column(name = "public_id", length = 255)
    private String publicId;

    @Column(name = "resource_type", length = 20)
    private String resourceType; // image / raw
}
