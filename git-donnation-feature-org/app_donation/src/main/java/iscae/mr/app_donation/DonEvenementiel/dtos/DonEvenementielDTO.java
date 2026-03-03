package iscae.mr.app_donation.DonEvenementiel.dtos;

import iscae.mr.app_donation.Don.dtos.DonDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonEvenementielDTO extends DonDTO {
    private String evenement;
    private String lieu;
}
