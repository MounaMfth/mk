package iscae.mr.app_donation.DonParrainage.controllers;

import iscae.mr.app_donation.Don.dtos.DonDetailDTO;
import iscae.mr.app_donation.DonParrainage.services.*;
import iscae.mr.app_donation.dao.entities.DonParrainage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dons-parrainage")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DonParrainageController {

    private final DonParrainageService donParrainageService;

    // ✅ CRÉER UN DON DE PARRAINAGE
    @PostMapping
    public ResponseEntity<DonDetailDTO> createDonParrainage(@RequestBody DonParrainage donParrainage) {
        try {
            DonDetailDTO created = donParrainageService.createDonParrainage(donParrainage);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR TOUS LES DONS DE PARRAINAGE
    @GetMapping
    public ResponseEntity<List<DonDetailDTO>> getAllDonsParrainage() {
        try {
            List<DonDetailDTO> dons = donParrainageService.getAllDonsParrainage();
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR UN DON DE PARRAINAGE PAR ID
    @GetMapping("/{id}")
    public ResponseEntity<DonDetailDTO> getDonParrainageById(@PathVariable Long id) {
        try {
            DonDetailDTO don = donParrainageService.getDonParrainageById(id);
            return ResponseEntity.ok(don);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR LES DONS DE PARRAINAGE D'UN PROJET
    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<DonDetailDTO>> getDonsParrainageByProjet(@PathVariable Long projetId) {
        try {
            List<DonDetailDTO> dons = donParrainageService.getDonsParrainageByProjet(projetId);
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ METTRE À JOUR UN DON DE PARRAINAGE
    @PutMapping("/{id}")
    public ResponseEntity<DonDetailDTO> updateDonParrainage(@PathVariable Long id,
            @RequestBody DonParrainage donUpdate) {
        try {
            DonDetailDTO updated = donParrainageService.updateDonParrainage(id, donUpdate);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ SUPPRIMER UN DON DE PARRAINAGE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonParrainage(@PathVariable Long id) {
        try {
            donParrainageService.deleteDonParrainage(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR LE NOMBRE TOTAL DE BÉNÉFICIAIRES D'UN PROJET
    @GetMapping("/total-beneficiaires/{projetId}")
    public ResponseEntity<Integer> getTotalBeneficiairesbyProjet(@PathVariable Long projetId) {
        try {
            Integer total = donParrainageService.getTotalBeneficiairesbyProjet(projetId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}