package iscae.mr.app_donation.utulisateur.dtos;

public class UpdateProfileDTO {
  private String email;
  private String adresse;
  private String telephone;
  private String profil; // URL de la photo
  private String prenom;
  private String nom;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAdresse() {
    return adresse;
  }

  public void setAdresse(String adresse) {
    this.adresse = adresse;
  }

  public String getTelephone() {
    return telephone;
  }

  public void setTelephone(String telephone) {
    this.telephone = telephone;
  }

  public String getProfil() {
    return profil;
  }

  public void setProfil(String profil) {
    this.profil = profil;
  }

  public String getPrenom() {
    return prenom;
  }

  public void setPrenom(String prenom) {
    this.prenom = prenom;
  }

  public String getNom() {
    return nom;
  }

  public void setNom(String nom) {
    this.nom = nom;
  }
}
