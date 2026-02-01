package com.app.municipio.modules.business.models;

import com.app.municipio.modules.business.models.enums.ParishType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "parishes")
public class Parishes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated (EnumType.STRING)
    @Column(name = "parish_type", nullable = false)
    private ParishType parishType;


}
