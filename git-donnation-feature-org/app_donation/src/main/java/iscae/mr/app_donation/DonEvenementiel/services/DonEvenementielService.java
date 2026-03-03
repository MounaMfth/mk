package iscae.mr.app_donation.DonEvenementiel.services;

import iscae.mr.app_donation.Don.dtos.DonDetailDTO;
import iscae.mr.app_donation.dao.entities.DonEvenementiel;
import iscae.mr.app_donation.dao.repositories.DonEvenementielRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonEvenementielService {

    private final DonEvenementielRepository donEvenementielRepository;

    // ✅ CRÉER UN DON ÉVÉNEMENTIEL
    public DonDetailDTO createDonEvenementiel(DonEvenementiel donEvenementiel) {
        donEvenementiel.setDateCreation(LocalDate.now());
        DonEvenementiel saved = donEvenementielRepository.save(donEvenementiel);
        return convertToDTO(saved);
    }

    // ✅ OBTENIR TOUS LES DONS ÉVÉNEMENTIELS
    public List<DonDetailDTO> getAllDonsEvenementiel() {
        return donEvenementielRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ OBTENIR UN DON ÉVÉNEMENTIEL PAR ID
    public DonDetailDTO getDonEvenementielById(Long id) {
        DonEvenementiel don = donEvenementielRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don événementiel introuvable"));
        return convertToDTO(don);
    }

    // ✅ OBTENIR LES DONS ÉVÉNEMENTIELS D'UN PROJET
    public List<DonDetailDTO> getDonsEvenementielByProjet(Long projetId) {
        return donEvenementielRepository.findByProjetId(projetId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ METTRE À JOUR UN DON ÉVÉNEMENTIEL
    public DonDetailDTO updateDonEvenementiel(Long id, DonEvenementiel donUpdate) {
        DonEvenementiel don = donEvenementielRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don événementiel introuvable"));

        don.setTitre(donUpdate.getTitre());
        don.setDescription(donUpdate.getDescription());
        don.setEvenement(donUpdate.getEvenement());
        don.setLieu(donUpdate.getLieu());
        don.setDateModification(LocalDate.now());

        DonEvenementiel updated = donEvenementielRepository.save(don);
        return convertToDTO(updated);
    }

    // ✅ SUPPRIMER UN DON ÉVÉNEMENTIEL
    public void deleteDonEvenementiel(Long id) {
        if (!donEvenementielRepository.existsById(id)) {
            throw new RuntimeException("Don événementiel introuvable");
        }
        donEvenementielRepository.deleteById(id);
    }

    // ✅ CONVERTIR EN DTO
    private DonDetailDTO convertToDTO(DonEvenementiel don) {
        return DonDetailDTO.builder()
                .nom("Donateur Événement")
                .date(don.getDateDon())
                .typeDon("EVENEMENTIEL")
                .evenement(don.getEvenement())
                .lieu(don.getLieu())
                .details(String.format("Événement: %s - Lieu: %s", don.getEvenement(), don.getLieu()))
                .build();
    }
}
