package iscae.mr.app_donation.dao.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("DonFinancier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public class DonFinancier extends Don {
    private Double montantFinancier;
    private String modePaiement;
    private String preuvePaiement;
}