package com.app.municipio.modules.users.models;

import com.app.municipio.modules.business.models.Business;
import com.app.municipio.modules.users.models.enums.IdTypes;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "app_user")
@SuppressWarnings("java:S1948")
public class AppUser implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Size(min = 2, max = 50)
    private String name;

    @Column(nullable = false)
    @Size(min = 2, max = 50)
    private String lastname;

    @Column(nullable = false)
    @Size(min = 10, max = 15)
    private String phone;

    @Column(nullable = false)
    private String address;

    @Column(unique = true, nullable = false)
    @Size(min = 5, max = 20)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    @Size(min = 5, max = 50)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "app_user_role",
            joinColumns = @JoinColumn(name = "app_user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Authorities> roles = new HashSet<>();

    @Column(name = "id_type", nullable = false)
    private IdTypes idType;

    @Column(nullable = false, unique = true)
    @Size(min = 5, max = 20)
    private String identification;


    @Column(name = "is_enabled", nullable = false, columnDefinition = "BIT(1) DEFAULT 0") 
    private boolean enabled;

    @Column(name = "is_account_non_expired", nullable = false)
    private boolean accountNonExpired;

    @Column(name = "is_account_non_locked", nullable = false)
    private boolean accountNonLocked;

    @Column(name = "is_credentials_non_expired", nullable = false)
    private boolean credentialsNonExpired;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getAuthority().name()))
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
    }

    @OneToOne(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private IdentityDocuments identityDocument;

    @OneToOne(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private Certificates certificate;

    @OneToOne(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private SignedDocuments signedDocument;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Business> businesses;


}
