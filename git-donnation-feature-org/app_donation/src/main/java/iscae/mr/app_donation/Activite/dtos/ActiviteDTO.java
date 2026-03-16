package iscae.mr.app_donation.Activite.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActiviteDTO {
    private String id;
    private String titre;
    private String description;
    private Double latitude;
    private Double longitude;

    private String organisationId;
    private String organisationNom;
    private String image; // nom de fichier si présent

    // Budget and progress tracking
    private Double objectifFinancier;
    private Double montantRecolte;
    private Integer pourcentageProgress;
    private Boolean budgetAtteint;
}
