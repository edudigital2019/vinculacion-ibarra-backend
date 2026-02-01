package com.app.municipio.modules.recovery.password.models;

import com.app.municipio.modules.users.models.AppUser;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "recovery_password_otp")
public class OtpRecovery {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "otp_app_user")
    private AppUser appUser;

    @Column(name = "otp")
    private String otp;

    @Column
    private boolean isOtpValidated = false;


}
