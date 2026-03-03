package iscae.mr.app_donation.DonNature.services;

import iscae.mr.app_donation.Don.dtos.DonDetailDTO;
import iscae.mr.app_donation.dao.entities.DonNature;
import iscae.mr.app_donation.dao.repositories.DonNatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonNatureService {

    private final DonNatureRepository donNatureRepository;

    // ✅ CRÉER UN DON EN NATURE
    public DonDetailDTO createDonNature(DonNature donNature) {
        donNature.setDateCreation(LocalDate.now());
        DonNature saved = donNatureRepository.save(donNature);
        return convertToDTO(saved);
    }

    // ✅ OBTENIR TOUS LES DONS EN NATURE
    public List<DonDetailDTO> getAllDonsNature() {
        return donNatureRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ OBTENIR UN DON EN NATURE PAR ID
    public DonDetailDTO getDonNatureById(Long id) {
        DonNature don = donNatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don en nature introuvable"));
        return convertToDTO(don);
    }

    // ✅ OBTENIR LES DONS EN NATURE D'UN PROJET
    public List<DonDetailDTO> getDonsNatureByProjet(Long projetId) {
        return donNatureRepository.findByProjetId(projetId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ METTRE À JOUR UN DON EN NATURE
    public DonDetailDTO updateDonNature(Long id, DonNature donUpdate) {
        DonNature don = donNatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don en nature introuvable"));

        don.setTitre(donUpdate.getTitre());
        don.setDescription(donUpdate.getDescription());
        don.setTypeNature(donUpdate.getTypeNature());
        don.setQuantite(donUpdate.getQuantite());
        don.setDateModification(LocalDate.now());

        DonNature updated = donNatureRepository.save(don);
        return convertToDTO(updated);
    }

    // ✅ SUPPRIMER UN DON EN NATURE
    public void deleteDonNature(Long id) {
        if (!donNatureRepository.existsById(id)) {
            throw new RuntimeException("Don en nature introuvable");
        }
        donNatureRepository.deleteById(id);
    }

    // ✅ CALCULER LA QUANTITÉ TOTALE D'UN PROJET
    public Integer getTotalQuantiteByProjet(Long projetId) {
        return donNatureRepository.findByProjetId(projetId)
                .stream()
                .mapToInt(d -> d.getQuantite() != null ? d.getQuantite() : 0)
                .sum();
    }

    // ✅ CONVERTIR EN DTO
    private DonDetailDTO convertToDTO(DonNature don) {
        return DonDetailDTO.builder()
                .nom("Donateur Nature")
                .date(don.getDateDon())
                .typeDon("NATURE")
                .typeNature(don.getTypeNature())
                .quantite(don.getQuantite())
                .details(String.format("%d × %s", don.getQuantite(), don.getTypeNature()))
                .build();
    }
}
