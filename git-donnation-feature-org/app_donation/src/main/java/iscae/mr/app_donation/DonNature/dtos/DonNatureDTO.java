package iscae.mr.app_donation.DonNature.dtos;

import iscae.mr.app_donation.Don.dtos.DonDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonNatureDTO extends DonDTO {
    private String typeObjet;
    private Integer quantite;
}
