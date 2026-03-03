package iscae.mr.app_donation.Don.dtos;

import lombok.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonDTO {
    private String id;
    private String titre;
    private String description;
    private LocalDate dateDon;          // ← Ajouter ce champ
    private Long projetId;
    private String activiteId;
    private Double objectifFinancier;
    private Double montantRecolte;
    private int pourcentage;
    private int nombreDonateurs;
    private List<DonDetailDTO> donateurs;
}
