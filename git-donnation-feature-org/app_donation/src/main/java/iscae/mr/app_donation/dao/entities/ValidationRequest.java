package iscae.mr.app_donation.dao.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "validation_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "utilisateur_id", nullable = false)
    private Long utilisateurId;

    @Column(name = "requested_role", nullable = false)
    private String requestedRole;

    @Column(name = "status", nullable = false)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "organisation_nom")
    private String organisationNom;

    @Column(name = "organisation_description", columnDefinition = "TEXT")
    private String organisationDescription;

    @Column(name = "organisation_localisation")
    private String organisationLocalisation;

    @Column(name = "organisation_email")
    private String organisationEmail;

    @Column(name = "organisation_telephone")
    private String organisationTelephone;

    @Column(name = "organisation_site_web")
    private String organisationSiteWeb;

    @Column(name = "organisation_logo_url")
    private String organisationLogoUrl;

    @Column(name = "secteur_ids")
    private String secteurIds; // Stored as comma-separated IDs

    @Column(name = "organisation_certificate_url")
    private String organisationCertificateUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = "PENDING";
        }
    }
}
