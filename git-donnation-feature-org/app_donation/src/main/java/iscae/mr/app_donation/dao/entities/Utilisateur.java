package iscae.mr.app_donation.dao.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "utilisateur")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true)
    private String email;

    private String prenom;
    private String nom;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private String telephone;
    private String adresse;
    private String profil;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> (GrantedAuthority) () -> "ROLE_" + role)
                .collect(Collectors.toSet());
    }

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    public void setProfil(String profil) {
        this.profil = profil;
        if (this.roles == null) {
            this.roles = new HashSet<>();
        }
        if (profil != null && !profil.isBlank()) {
            this.roles.clear();
            this.roles.addAll(switch (profil.toUpperCase()) {
                case "ADMIN" -> Set.of("ADMIN");
                case "DONATEUR" -> Set.of("USER");
                case "ORGANISATEUR" -> Set.of("ORG");
                default -> Set.of("USER");
            });
        } else {
            this.roles.add("USER");
        }
    }
}