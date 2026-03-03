package iscae.mr.app_donation.utulisateur.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilisateurDTO {
    private Long id; // ✅ Long, pas long
    private String username;
    private String email;
    private String telephone;
    private String adresse;
    private String profil;
    private String password; // Ne jamais exposer dans les réponses
}