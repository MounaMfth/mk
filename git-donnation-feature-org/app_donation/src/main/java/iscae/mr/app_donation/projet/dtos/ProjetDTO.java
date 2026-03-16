package iscae.mr.app_donation.projet.dtos;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjetDTO {
    private String id;
    private String titre;
    private String description;
    private Double objectifFinancier;
    private String statut;
    private String image;
    private LocalDate dateCreation;
    private LocalDate dateModification;
    private String organisationId;

    // Budget and progress tracking
    private Double montantRecolte;
    private Integer pourcentageProgress;
    private Boolean budgetAtteint;
}
