package iscae.mr.app_donation.projet.Service;

import iscae.mr.app_donation.dao.entities.Projet;
import iscae.mr.app_donation.dao.entities.Organisation;
import iscae.mr.app_donation.dao.repositories.ProjetRepository;
import iscae.mr.app_donation.dao.repositories.OrganisationRepository;
import iscae.mr.app_donation.dao.repositories.DonFinancierRepository;
import iscae.mr.app_donation.projet.dtos.ProjetDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjetService {

    private final ProjetRepository projetRepository;
    private final OrganisationRepository organisationRepository;
    private final DonFinancierRepository donFinancierRepository;

    // ✅ OBTENIR TOUS LES PROJETS
    public List<ProjetDTO> getAllProjets() {
        return projetRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ OBTENIR LES PROJETS ACTIFS
    public List<ProjetDTO> getProjetsActifs() {
        return projetRepository.findAllActifs()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ OBTENIR LES PROJETS PAR ORGANISATION
    public List<ProjetDTO> getProjetsByOrganisationId(String organisationId) {
        return projetRepository.findByOrganisation_Id(organisationId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ OBTENIR UN PROJET PAR ID
    public ProjetDTO getProjetById(String id) {
        Long projetId = Long.parseLong(id);
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));
        return convertToDTO(projet);
    }

    // ✅ CRÉER UN PROJET
    public ProjetDTO createProjet(ProjetDTO dto, String organisationId) {
        Projet.ProjetBuilder builder = Projet.builder()
                .titre(dto.getTitre())
                .description(dto.getDescription())
                .objectifFinancier(dto.getObjectifFinancier())
                .statut(dto.getStatut() != null ? dto.getStatut() : "EN_ATTENTE")
                .image(dto.getImage())
                .dateCreation(LocalDate.now())
                .dateModification(LocalDate.now())
                .montantRecolte(0.0) // Initialize to 0
                .pourcentageProgress(0) // Initialize to 0
                .budgetAtteint(false) // Initialize to false
                .notificationEnvoyee(false); // Initialize to false

        // Set organization if provided
        if (organisationId != null && !organisationId.isEmpty()) {
            Organisation org = organisationRepository.findById(organisationId)
                    .orElse(null);
            if (org != null) {
                builder.organisation(org);
            }
        }

        Projet projet = builder.build();
        System.out.println("💾 [ProjetService] Creating project with objective: " + projet.getObjectifFinancier());
        Projet saved = projetRepository.save(projet);
        System.out.println("✅ [ProjetService] Project saved with ID: " + saved.getId() + " and objective: "
                + saved.getObjectifFinancier());
        return convertToDTO(saved);
    }

    // ✅ METTRE À JOUR UN PROJET
    public ProjetDTO updateProjet(String id, ProjetDTO dto) {
        Long projetId = Long.parseLong(id);
        Projet projet = projetRepository.findById(projetId)
                .orElseThrow(() -> new RuntimeException("Projet introuvable"));

        projet.setTitre(dto.getTitre());
        projet.setDescription(dto.getDescription());
        projet.setObjectifFinancier(dto.getObjectifFinancier());
        projet.setStatut(dto.getStatut());
        projet.setImage(dto.getImage());
        projet.setDateModification(LocalDate.now());

        // Objective is already updated above via setObjectifFinancier

        Projet updated = projetRepository.save(projet);
        return convertToDTO(updated);
    }

    // ✅ SUPPRIMER UN PROJET
    public void deleteProjet(String id) {
        Long projetId = Long.parseLong(id);
        if (!projetRepository.existsById(projetId)) {
            throw new RuntimeException("Projet introuvable");
        }
        projetRepository.deleteById(projetId);
    }

    // ✅ RECHERCHER DES PROJETS PAR TITRE
    public List<ProjetDTO> searchProjet(String titre) {
        return projetRepository.findByTitreContainingIgnoreCase(titre)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ CONVERTIR ENTITY EN DTO
    private ProjetDTO convertToDTO(Projet projet) {
        // Calculate montantRecolte from donations
        Double montantRecolte = 0.0;
        if (projet.getId() != null) {
            montantRecolte = donFinancierRepository.findByProjetId(projet.getId())
                    .stream()
                    .mapToDouble(d -> d.getMontantFinancier() != null ? d.getMontantFinancier() : 0)
                    .sum();
        }

        // Calculate progress percentage
        Integer pourcentageProgress = 0;
        if (projet.getObjectifFinancier() != null && projet.getObjectifFinancier() > 0) {
            pourcentageProgress = (int) Math.min((montantRecolte / projet.getObjectifFinancier()) * 100, 100);
        }

        ProjetDTO.ProjetDTOBuilder builder = ProjetDTO.builder()
                .id(String.valueOf(projet.getId()))
                .titre(projet.getTitre())
                .description(projet.getDescription())
                .objectifFinancier(projet.getObjectifFinancier())
                .statut(projet.getStatut())
                .image(projet.getImage())
                .dateCreation(projet.getDateCreation())
                .dateModification(projet.getDateModification())
                .montantRecolte(montantRecolte)
                .pourcentageProgress(pourcentageProgress)
                .budgetAtteint(projet.getBudgetAtteint() != null ? projet.getBudgetAtteint() : false);

        // Add organisation ID if present
        if (projet.getOrganisation() != null) {
            builder.organisationId(projet.getOrganisation().getId());
        }

        return builder.build();
    }
}