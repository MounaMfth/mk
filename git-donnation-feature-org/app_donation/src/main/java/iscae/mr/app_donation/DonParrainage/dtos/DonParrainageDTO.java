package iscae.mr.app_donation.DonParrainage.dtos;

import iscae.mr.app_donation.Don.dtos.DonDTO;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DonParrainageDTO extends DonDTO {
    private String enfantParraine;
    private Integer dureeMois; // durée du parrainage
    private String suiviMensuel;
    public void setSuiviMensuel(String suiviMensuel) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setSuiviMensuel'");
    }
}