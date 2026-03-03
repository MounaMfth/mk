package iscae.mr.app_donation.dao.entities;

import jakarta.persistence.*;
import lombok.*;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Secteur {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String nom;

    private String description;

    private String statut; // actif, inactif, etc.

    @ManyToMany(mappedBy = "secteurs")
    private List<Organisation> organisations;
}


