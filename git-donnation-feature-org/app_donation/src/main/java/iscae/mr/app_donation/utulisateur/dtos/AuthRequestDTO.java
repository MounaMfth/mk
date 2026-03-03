package iscae.mr.app_donation.utulisateur.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AuthRequestDTO {
    private String username;
    private String prenom;
    private String nom;
    private String password;
    private String email;
    private String adresse;
    private String telephone;
    private String profil;
    private String organisationNom;
    private String description;
    private String localisation;
    private String orgEmail;
    private String orgTelephone;
    private String siteWeb;
}
