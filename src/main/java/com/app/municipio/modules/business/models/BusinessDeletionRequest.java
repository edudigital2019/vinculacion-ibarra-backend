package com.app.municipio.modules.business.models;

import com.app.municipio.modules.business.models.enums.DeletionRequestStatus;
import com.app.municipio.modules.users.models.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_deletion_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDeletionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id")
    private Business business;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    private AppUser requester;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String motivo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String justificacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeletionRequestStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime decidedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by_id")
    private AppUser decidedBy;
}
