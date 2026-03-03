package iscae.mr.app_donation.Don.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonDetailDTO {
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private LocalDate date;
    private String typeDon;

    // Pour DonFinancier
    private Double montant;

    // Pour DonEvenementiel
    private String evenement;
    private String lieu;

    // Pour DonNature
    private String typeNature;
    private Integer quantite;

    // Pour DonParrainage
    private String typeLien;
    private Integer nombreBeneficiaires;

    // Détail générique
    private String details;

    // Champs pour le dashboard (Mes Dons)
    private String projetTitre;
    private String activiteTitre;
    private Double objectifFinancier;
    private Double montantCollecte;
    private String projetImage;
    private String activiteImage;
    private String organisationImage;
    private String preuvePaiement;
}
