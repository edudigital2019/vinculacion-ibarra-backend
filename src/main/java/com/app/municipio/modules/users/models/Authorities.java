package com.app.municipio.modules.users.models;

import com.app.municipio.modules.users.models.enums.UserRoles;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "authorities")
public class Authorities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "authority")
    @Enumerated(EnumType.STRING)
    private UserRoles authority;
}
