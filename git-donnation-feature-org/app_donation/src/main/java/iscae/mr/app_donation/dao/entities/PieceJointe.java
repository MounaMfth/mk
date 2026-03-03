package iscae.mr.app_donation.dao.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PieceJointe {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nomFichier; // Nom original du fichier

    @Column(nullable = false)
    private String typeFichier; // MIME type: image/png, application/pdf, etc.

    @Column(nullable = false)
    private String url; // URL ou chemin d’accès au fichier (dans stockage ou base64)

    @Column(nullable = false)
    private LocalDateTime dateAjout;

    // Références logiques (sans relations JPA strictes, pour rester souple)
    @Column(nullable = true)
    private String donId;

    @Column(nullable = true)
    private String activiteId;

    @Column(nullable = true)
    private String utilisateurId;

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    private Organisation organisation;


    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public String getTypeFichier() {
        return typeFichier;
    }

    public void setTypeFichier(String typeFichier) {
        this.typeFichier = typeFichier;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    public String getDonId() {
        return donId;
    }

    public void setDonId(String donId) {
        this.donId = donId;
    }

    public String getActiviteId() {
        return activiteId;
    }

    public void setActiviteId(String activiteId) {
        this.activiteId = activiteId;
    }

    public String getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(String utilisateurId) {
        this.utilisateurId = utilisateurId;
    }
}

