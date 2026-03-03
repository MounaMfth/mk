package iscae.mr.app_donation.DonEvenementiel.controllers;

import iscae.mr.app_donation.Don.dtos.DonDetailDTO;
import iscae.mr.app_donation.DonEvenementiel.services.*;
import iscae.mr.app_donation.dao.entities.DonEvenementiel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dons-evenementiel")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DonEvenementielController {

    private final DonEvenementielService donEvenementielService;

    // ✅ CRÉER UN DON ÉVÉNEMENTIEL
    @PostMapping
    public ResponseEntity<DonDetailDTO> createDonEvenementiel(@RequestBody DonEvenementiel donEvenementiel) {
        try {
            DonDetailDTO created = donEvenementielService.createDonEvenementiel(donEvenementiel);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR TOUS LES DONS ÉVÉNEMENTIELS
    @GetMapping
    public ResponseEntity<List<DonDetailDTO>> getAllDonsEvenementiel() {
        try {
            List<DonDetailDTO> dons = donEvenementielService.getAllDonsEvenementiel();
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR UN DON ÉVÉNEMENTIEL PAR ID
    @GetMapping("/{id}")
    public ResponseEntity<DonDetailDTO> getDonEvenementielById(@PathVariable Long id) {
        try {
            DonDetailDTO don = donEvenementielService.getDonEvenementielById(id);
            return ResponseEntity.ok(don);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR LES DONS ÉVÉNEMENTIELS D'UN PROJET
    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<DonDetailDTO>> getDonsEvenementielByProjet(@PathVariable Long projetId) {
        try {
            List<DonDetailDTO> dons = donEvenementielService.getDonsEvenementielByProjet(projetId);
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ METTRE À JOUR UN DON ÉVÉNEMENTIEL
    @PutMapping("/{id}")
    public ResponseEntity<DonDetailDTO> updateDonEvenementiel(@PathVariable Long id,
            @RequestBody DonEvenementiel donUpdate) {
        try {
            DonDetailDTO updated = donEvenementielService.updateDonEvenementiel(id, donUpdate);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ SUPPRIMER UN DON ÉVÉNEMENTIEL
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonEvenementiel(@PathVariable Long id) {
        try {
            donEvenementielService.deleteDonEvenementiel(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}