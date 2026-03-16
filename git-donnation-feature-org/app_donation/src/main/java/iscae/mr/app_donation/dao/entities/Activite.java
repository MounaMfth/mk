package iscae.mr.app_donation.dao.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class Activite {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String titre;

    @Column
    private String description;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    // 🔹 Nouveau: nom de fichier image (upload sauvegardé côté serveur)
    @Column
    private String image;

    // Budget and progress tracking
    @Column
    private Double objectifFinancier; // Budget goal for the activity

    @Column
    private Double montantRecolte; // Total amount collected

    @Column
    private Integer pourcentageProgress; // Progress percentage (0-100)

    @Column
    private Boolean budgetAtteint; // Flag to track if budget limit is reached

    @Column
    private Boolean notificationEnvoyee; // Flag to prevent duplicate notifications

    @ManyToOne
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    // ===== Getters & Setters =====
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Organisation getOrganisation() {
        return organisation;
    }

    public void setOrganisation(Organisation organisation) {
        this.organisation = organisation;
    }

    @Transient
    public String getOrganisationId() {
        return organisation != null ? organisation.getId() : null;
    }

    public void setOrganisationId(String organisationId) {
        if (this.organisation == null)
            this.organisation = new Organisation();
        this.organisation.setId(organisationId);
    }

    // Getters and setters for new fields
    public Double getObjectifFinancier() {
        return objectifFinancier;
    }

    public void setObjectifFinancier(Double objectifFinancier) {
        this.objectifFinancier = objectifFinancier;
    }

    public Double getMontantRecolte() {
        return montantRecolte;
    }

    public void setMontantRecolte(Double montantRecolte) {
        this.montantRecolte = montantRecolte;
    }

    public Integer getPourcentageProgress() {
        return pourcentageProgress;
    }

    public void setPourcentageProgress(Integer pourcentageProgress) {
        this.pourcentageProgress = pourcentageProgress;
    }

    public Boolean getBudgetAtteint() {
        return budgetAtteint != null ? budgetAtteint : false;
    }

    public void setBudgetAtteint(Boolean budgetAtteint) {
        this.budgetAtteint = budgetAtteint;
    }

    public Boolean getNotificationEnvoyee() {
        return notificationEnvoyee != null ? notificationEnvoyee : false;
    }

    public void setNotificationEnvoyee(Boolean notificationEnvoyee) {
        this.notificationEnvoyee = notificationEnvoyee;
    }
}
