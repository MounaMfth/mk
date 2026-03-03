package iscae.mr.app_donation.DonParrainage.services;

import iscae.mr.app_donation.Don.dtos.DonDetailDTO;
import iscae.mr.app_donation.dao.entities.DonParrainage;
import iscae.mr.app_donation.dao.repositories.DonParrainageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonParrainageService {

    private final DonParrainageRepository donParrainageRepository;

    // ✅ CRÉER UN DON DE PARRAINAGE
    public DonDetailDTO createDonParrainage(DonParrainage donParrainage) {
        donParrainage.setDateCreation(LocalDate.now());
        DonParrainage saved = donParrainageRepository.save(donParrainage);
        return convertToDTO(saved);
    }

    // ✅ OBTENIR TOUS LES DONS DE PARRAINAGE
    public List<DonDetailDTO> getAllDonsParrainage() {
        return donParrainageRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ OBTENIR UN DON DE PARRAINAGE PAR ID
    public DonDetailDTO getDonParrainageById(Long id) {
        DonParrainage don = donParrainageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don de parrainage introuvable"));
        return convertToDTO(don);
    }

    // ✅ OBTENIR LES DONS DE PARRAINAGE D'UN PROJET
    public List<DonDetailDTO> getDonsParrainageByProjet(Long projetId) {
        return donParrainageRepository.findByProjetId(projetId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ METTRE À JOUR UN DON DE PARRAINAGE
    public DonDetailDTO updateDonParrainage(Long id, DonParrainage donUpdate) {
        DonParrainage don = donParrainageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don de parrainage introuvable"));

        don.setTitre(donUpdate.getTitre());
        don.setDescription(donUpdate.getDescription());
        don.setTypeLien(donUpdate.getTypeLien());
        don.setNombreBeneficiaires(donUpdate.getNombreBeneficiaires());
        don.setDateModification(LocalDate.now());

        DonParrainage updated = donParrainageRepository.save(don);
        return convertToDTO(updated);
    }

    // ✅ SUPPRIMER UN DON DE PARRAINAGE
    public void deleteDonParrainage(Long id) {
        if (!donParrainageRepository.existsById(id)) {
            throw new RuntimeException("Don de parrainage introuvable");
        }
        donParrainageRepository.deleteById(id);
    }

    // ✅ CALCULER LE NOMBRE TOTAL DE BÉNÉFICIAIRES D'UN PROJET
    public Integer getTotalBeneficiairesbyProjet(Long projetId) {
        return donParrainageRepository.findByProjetId(projetId)
                .stream()
                .mapToInt(d -> d.getNombreBeneficiaires() != null ? d.getNombreBeneficiaires() : 0)
                .sum();
    }

    // ✅ CONVERTIR EN DTO
    private DonDetailDTO convertToDTO(DonParrainage don) {
        return DonDetailDTO.builder()
                .nom("Parrain/Marraine")
                .date(don.getDateDon())
                .typeDon("PARRAINAGE")
                .typeLien(don.getTypeLien())
                .nombreBeneficiaires(don.getNombreBeneficiaires())
                .details(String.format("Parrainage: %s (%d bénéf.)", don.getTypeLien(), don.getNombreBeneficiaires()))
                .build();
    }
}
