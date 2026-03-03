package iscae.mr.app_donation.dao.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "dons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_don")
@SuperBuilder
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class Don {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;

    @JsonProperty("dateDon")
    private LocalDate dateDon;

    private String activiteId;
    private Long projetId;
    private String organisationId; // For direct donations to organisations

    // Informations du donateur
    private String nom;
    private String prenom;
    private String email;
    private String telephone;

    private LocalDate dateCreation;
    private LocalDate dateModification;
}