package iscae.mr.app_donation.Organisation.services;

import iscae.mr.app_donation.Piecejointe.dtos.PiecejointeDTO;
import iscae.mr.app_donation.Piecejointe.services.PiecejointeService;
import iscae.mr.app_donation.dao.entities.Organisation;
import iscae.mr.app_donation.dao.repositories.OrganisationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OrganisationService {

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private PiecejointeService piecejointeService;

    // --- CRUD basiques ---
    public List<Organisation> getAllOrganisations() {
        return organisationRepository.findAll();
    }

    public Optional<Organisation> getOrganisationById(String id) {
        return organisationRepository.findById(id);
    }

    public Organisation createOrganisation(Organisation organisation) {
        organisation.setCreatedAt(LocalDateTime.now());
        organisation.setUpdatedAt(LocalDateTime.now());
        return organisationRepository.save(organisation);
    }

    public Organisation updateOrganisation(String id, Organisation organisationDetails) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        organisation.setNom(organisationDetails.getNom());
        organisation.setEmail(organisationDetails.getEmail());
        organisation.setDescription(organisationDetails.getDescription());
        organisation.setLocalisation(organisationDetails.getLocalisation());
        organisation.setValide(organisationDetails.getValide());
        organisation.setUpdatedAt(LocalDateTime.now());

        return organisationRepository.save(organisation);
    }

    public void deleteOrganisation(String id) {
        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        try {
            piecejointeService.supprimerLogosOrganisation(id.toString());
        } catch (Exception e) {
            log.warn("Erreur lors de la suppression du logo: {}", e.getMessage());
        }

        organisationRepository.delete(organisation);
    }

    // --- Créer avec logo ---
    public Organisation createOrganisationAvecLogo(
            String nom,
            String email,
            String description,
            String localisation,
            Boolean valide,
            MultipartFile logoFile,
            String utilisateurId) throws IOException {

        Organisation organisation = new Organisation();
        organisation.setNom(nom);
        organisation.setEmail(email);
        organisation.setDescription(description);
        organisation.setLocalisation(localisation);
        organisation.setValide(valide != null ? valide : false);
        organisation.setCreatedAt(LocalDateTime.now());
        organisation.setUpdatedAt(LocalDateTime.now());

        Organisation savedOrganisation = organisationRepository.save(organisation);

        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                PiecejointeDTO logo = piecejointeService.uploadLogoOrganisation(
                        logoFile,
                        String.valueOf(savedOrganisation.getId()),
                        utilisateurId
                );
                savedOrganisation.setLogo(logo.getUrl()); // ⚠️ Vérifie bien que le champ existe
                savedOrganisation = organisationRepository.save(savedOrganisation);

                log.info("Logo uploadé avec succès pour l'organisation {}: {}",
                        savedOrganisation.getId(), logo.getUrl());
            } catch (Exception e) {
                log.error("Erreur lors de l'upload du logo: {}", e.getMessage());
            }
        }

        return savedOrganisation;
    }

    // --- Update avec logo ---
    public Organisation updateOrganisationAvecLogo(
            String id,
            String nom,
            String email,
            String description,
            String localisation,
            Boolean valide,
            MultipartFile logoFile,
            String utilisateurId) throws IOException {

        Organisation organisation = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée"));

        organisation.setNom(nom);
        organisation.setEmail(email);
        organisation.setDescription(description);
        organisation.setLocalisation(localisation);
        organisation.setValide(valide != null ? valide : organisation.getValide());
        organisation.setUpdatedAt(LocalDateTime.now());

        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                PiecejointeDTO logo = piecejointeService.uploadLogoOrganisation(
                        logoFile,
                        id.toString(),
                        utilisateurId
                );
                organisation.setLogo(logo.getUrl());
                log.info("Nouveau logo uploadé pour organisation {}: {}", id, logo.getUrl());
            } catch (Exception e) {
                log.error("Erreur lors du nouvel upload de logo: {}", e.getMessage());
            }
        }

        return organisationRepository.save(organisation);
    }
}
