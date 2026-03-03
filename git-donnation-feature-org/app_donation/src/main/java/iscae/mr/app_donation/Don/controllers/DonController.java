package iscae.mr.app_donation.Don.controllers;

import iscae.mr.app_donation.Don.dtos.DonDTO;
import iscae.mr.app_donation.Don.dtos.DonDetailDTO;
import iscae.mr.app_donation.Don.services.DonService;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/dons")
public class DonController {

    private final DonService donService;

    public DonController(DonService donService) {
        this.donService = donService;
    }

    
 @GetMapping("/activite/{activiteId}")
    public DonDTO getDonsByActivite(@PathVariable String activiteId) {
        List<DonDetailDTO> dons = donService.getDonsByActiviteId(activiteId).getDonateurs();


        // Construire le DTO principal même si la liste est vide
        return DonDTO.builder()
                .id(activiteId)
                .titre("Projet " + activiteId)
                .donateurs(dons)
                .montantRecolte(dons.stream().mapToDouble(d -> d.getMontant() != null ? d.getMontant() : 0).sum())
                .nombreDonateurs(dons.size())
                .pourcentage(dons.isEmpty() ? 0 : (int) ((dons.stream().mapToDouble(d -> d.getMontant() != null ? d.getMontant() : 0).sum() / 10000) * 100)) // Exemple d'objectif par défaut
                .objectifFinancier(10000.0) // Tu peux remplacer par la vraie valeur
                .description("Description du projet") // À personnaliser si besoin
                .build();
    }
}