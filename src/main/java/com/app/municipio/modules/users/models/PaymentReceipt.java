package com.app.municipio.modules.users.models;

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
@Table(name = "receipt_payment")
public class PaymentReceipt {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column
    private String paymentUrl;

    @Column
    private String fileType;

    @Column
    private String publicId;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "app_user_id", referencedColumnName = "id")
    private AppUser appUser;
}
