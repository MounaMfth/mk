package iscae.mr.app_donation.Piecejointe.dtos;

import java.time.LocalDateTime;

public class PiecejointeDTO {

    private String id;
    private String nomFichier;
    private String typeFichier;
    private String url;
    private LocalDateTime dateAjout;
    private String donId;
    private String activiteId;
    private String utilisateurId;

    // ✅ Constructeur complet
    public PiecejointeDTO(String id, String nomFichier, String typeFichier, String url,
                          LocalDateTime dateAjout, String donId, String activiteId, String utilisateurId) {
        this.id = id;
        this.nomFichier = nomFichier;
        this.typeFichier = typeFichier;
        this.url = url;
        this.dateAjout = dateAjout;
        this.donId = donId;
        this.activiteId = activiteId;
        this.utilisateurId = utilisateurId;
    }

    // ✅ Constructeur vide
    public PiecejointeDTO() {}

    // ✅ Getters et Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNomFichier() { return nomFichier; }
    public void setNomFichier(String nomFichier) { this.nomFichier = nomFichier; }

    public String getTypeFichier() { return typeFichier; }
    public void setTypeFichier(String typeFichier) { this.typeFichier = typeFichier; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public LocalDateTime getDateAjout() { return dateAjout; }
    public void setDateAjout(LocalDateTime dateAjout) { this.dateAjout = dateAjout; }

    public String getDonId() { return donId; }
    public void setDonId(String donId) { this.donId = donId; }

    public String getActiviteId() { return activiteId; }
    public void setActiviteId(String activiteId) { this.activiteId = activiteId; }

    public String getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(String utilisateurId) { this.utilisateurId = utilisateurId; }
}
