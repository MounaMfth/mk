package iscae.mr.app_donation.DonNature.controllers;

import iscae.mr.app_donation.Don.dtos.DonDetailDTO;
import iscae.mr.app_donation.DonNature.services.*;
import iscae.mr.app_donation.dao.entities.DonNature;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dons-nature")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DonNatureController {

    private final DonNatureService donNatureService;

    // ✅ CRÉER UN DON EN NATURE
    @PostMapping
    public ResponseEntity<DonDetailDTO> createDonNature(@RequestBody DonNature donNature) {
        try {
            DonDetailDTO created = donNatureService.createDonNature(donNature);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR TOUS LES DONS EN NATURE
    @GetMapping
    public ResponseEntity<List<DonDetailDTO>> getAllDonsNature() {
        try {
            List<DonDetailDTO> dons = donNatureService.getAllDonsNature();
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR UN DON EN NATURE PAR ID
    @GetMapping("/{id}")
    public ResponseEntity<DonDetailDTO> getDonNatureById(@PathVariable Long id) {
        try {
            DonDetailDTO don = donNatureService.getDonNatureById(id);
            return ResponseEntity.ok(don);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR LES DONS EN NATURE D'UN PROJET
    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<DonDetailDTO>> getDonsNatureByProjet(@PathVariable Long projetId) {
        try {
            List<DonDetailDTO> dons = donNatureService.getDonsNatureByProjet(projetId);
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ METTRE À JOUR UN DON EN NATURE
    @PutMapping("/{id}")
    public ResponseEntity<DonDetailDTO> updateDonNature(@PathVariable Long id, @RequestBody DonNature donUpdate) {
        try {
            DonDetailDTO updated = donNatureService.updateDonNature(id, donUpdate);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ SUPPRIMER UN DON EN NATURE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonNature(@PathVariable Long id) {
        try {
            donNatureService.deleteDonNature(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR LA QUANTITÉ TOTALE D'UN PROJET
    @GetMapping("/total-quantite/{projetId}")
    public ResponseEntity<Integer> getTotalQuantiteByProjet(@PathVariable Long projetId) {
        try {
            Integer total = donNatureService.getTotalQuantiteByProjet(projetId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
