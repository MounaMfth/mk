package iscae.mr.app_donation.Organisation.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationDTO {
    private long id;
    private String nom;
    private String description;
    private String localisation;
    private String email;
    private String telephone;
    private String siteWeb;
    private Boolean statutVerifie;
    private Boolean valide;
    private String logo;
}
