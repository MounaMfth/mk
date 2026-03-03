package iscae.mr.app_donation.utulisateur.dtos;

import lombok.Data;
import java.util.List;

@Data
public class ValidationRequestDTO {
    private String organisationNom;
    private String organisationDescription;
    private String localisation;
    private String email;
    private String telephone;
    private String siteWeb;
    private List<String> secteurIds;
    private String logoUrl;
    private String certificateUrl;
}
