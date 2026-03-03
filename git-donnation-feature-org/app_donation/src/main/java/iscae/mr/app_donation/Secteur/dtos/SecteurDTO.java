package iscae.mr.app_donation.Secteur.dtos;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecteurDTO {
    private String id;
    private String nom;
    private String description;
    private String statut;
}
