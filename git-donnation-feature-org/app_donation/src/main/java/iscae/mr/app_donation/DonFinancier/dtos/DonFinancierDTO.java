package iscae.mr.app_donation.DonFinancier.dtos;
import iscae.mr.app_donation.Don.dtos.DonDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonFinancierDTO extends DonDTO {
    private Double montant;
}