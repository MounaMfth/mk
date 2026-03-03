package iscae.mr.app_donation.dao.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Data
public class Organisation {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(length = 36)
    private String id;

    private String nom;
    private String description;
    private String localisation;
    private String email;
    private String telephone;
    private String siteWeb;
    private Boolean statutVerifie;
    private String typeFichier;
    
    @OneToMany(mappedBy = "organisation")
    @JsonIgnore
    private List<Activite> activites;

    @OneToMany(mappedBy = "organisation")
    @JsonIgnore  // Add this annotation to prevent serialization
    private Set<Utilisateur> utilisateurs = new HashSet<>();

    private Boolean valide;
    private String logo;

    // ✅ IMPORTANT: Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Relation ManyToMany avec Secteur
    @ManyToMany
    @JoinTable(
            name = "organisation_secteur",
            joinColumns = @JoinColumn(name = "organisation_id"),
            inverseJoinColumns = @JoinColumn(name = "secteur_id")
    )
    @JsonIgnore
    private List<Secteur> secteurs;

    // ========================================
    // CONSTRUCTEURS
    // ========================================
    public Organisation() {}

    public Organisation(String nom, String adresse) {
        this.nom = nom;
    }

    // ========================================
    // GETTERS ET SETTERS
    // ========================================
    public String getId() { 
        return id; 
    }
    
    public void setId(String id) { 
        this.id = id; 
    }
    
    public String getNom() { 
        return nom; 
    }
    
    public void setNom(String nom) { 
        this.nom = nom; 
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public Boolean getStatutVerifie() {
        return statutVerifie;
    }

    public void setStatutVerifie(Boolean statutVerifie) {
        this.statutVerifie = statutVerifie;
    }

    public String getTypeFichier() {
        return typeFichier;
    }

    public void setTypeFichier(String typeFichier) {
        this.typeFichier = typeFichier;
    }

    public List<Activite> getActivites() {
        return activites;
    }

    public void setActivites(List<Activite> activites) {
        this.activites = activites;
    }

    public Set<Utilisateur> getUtilisateurs() {
        return utilisateurs;
    }

    public void setUtilisateurs(Set<Utilisateur> utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    public Boolean getValide() {
        return valide;
    }

    public void setValide(Boolean valide) {
        this.valide = valide;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    // ✅ IMPORTANT: Getters et setters pour les timestamps
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Secteur> getSecteurs() {
        return secteurs;
    }

    public void setSecteurs(List<Secteur> secteurs) {
        this.secteurs = secteurs;
    }
}