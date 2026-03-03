package iscae.mr.app_donation.dao.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "projet")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Projet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private Double objectifFinancier;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String statut;
    private String image;
    private LocalDate dateCreation;
    private LocalDate dateModification;

    // Budget and progress tracking
    private Double montantRecolte; // Total amount collected
    private Integer pourcentageProgress; // Progress percentage (0-100)
    private Boolean budgetAtteint; // Flag to track if budget limit is reached
    private Boolean notificationEnvoyee; // Flag to prevent duplicate notifications

    @ManyToOne
    @JoinColumn(name = "organisation_id")
    @JsonIgnore
    private Organisation organisation;
}