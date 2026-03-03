package iscae.mr.app_donation.dao.entities;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DonParrainage extends Don {
    private String typeLien;
    private Integer nombreBeneficiaires;
}