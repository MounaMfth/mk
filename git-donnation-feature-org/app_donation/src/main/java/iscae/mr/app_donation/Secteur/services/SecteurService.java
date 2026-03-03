package iscae.mr.app_donation.Secteur.services;


import iscae.mr.app_donation.Secteur.dtos.SecteurDTO;
import iscae.mr.app_donation.dao.entities.Secteur;
import iscae.mr.app_donation.dao.repositories.SecteurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SecteurService {

    @Autowired
    private SecteurRepository secteurRepository;

    // ✅ CREATE
    public SecteurDTO createSecteur(SecteurDTO dto) {
        Secteur secteur = new Secteur();
        secteur.setNom(dto.getNom());
        secteur.setDescription(dto.getDescription());
        secteur.setStatut(dto.getStatut());
        Secteur saved = secteurRepository.save(secteur);

        return mapToDTO(saved);
    }

    // ✅ UPDATE
    public SecteurDTO updateSecteur(String id, SecteurDTO dto) {
        Secteur secteur = secteurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Secteur introuvable"));
        secteur.setNom(dto.getNom());
        secteur.setDescription(dto.getDescription());
        secteur.setStatut(dto.getStatut());
        Secteur updated = secteurRepository.save(secteur);

        return mapToDTO(updated);
    }

    // ✅ DELETE
    public void deleteSecteur(String id) {
        secteurRepository.deleteById(id);
    }

    // ✅ GET BY ID
    public SecteurDTO getSecteurById(String id) {
        Secteur secteur = secteurRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Secteur introuvable"));
        return mapToDTO(secteur);
    }

    // ✅ GET ALL
    public List<SecteurDTO> getAllSecteurs() {
        return secteurRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ✅ Mapper
    private SecteurDTO mapToDTO(Secteur secteur) {
        return new SecteurDTO(
                secteur.getId(),
                secteur.getNom(),
                secteur.getDescription(),
                secteur.getStatut()
        );
    }
}

