package iscae.mr.app_donation.DonFinancier.services;

import iscae.mr.app_donation.Don.dtos.DonDetailDTO;
import iscae.mr.app_donation.dao.entities.DonFinancier;
import iscae.mr.app_donation.dao.entities.Projet;
import iscae.mr.app_donation.dao.entities.Activite;
import iscae.mr.app_donation.dao.repositories.DonFinancierRepository;
import iscae.mr.app_donation.dao.repositories.ProjetRepository;
import iscae.mr.app_donation.dao.repositories.ActiviteRepository;
import iscae.mr.app_donation.Notification.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonFinancierService {

    private final DonFinancierRepository donFinancierRepository;
    private final ProjetRepository projetRepository;
    private final ActiviteRepository activiteRepository;
    private final iscae.mr.app_donation.dao.repositories.OrganisationRepository organisationRepository;
    private final NotificationService notificationService;

    // ✅ CRÉER UN DON FINANCIER
    @Transactional
    public DonDetailDTO createDonFinancier(DonFinancier donFinancier) {
        if (donFinancier.getProjetId() != null) {
            projetRepository.findById(donFinancier.getProjetId())
                    .orElseThrow(() -> new RuntimeException("Projet introuvable"));
        } else if (donFinancier.getActiviteId() != null) {
            activiteRepository.findById(donFinancier.getActiviteId())
                    .orElseThrow(() -> new RuntimeException("Activité introuvable"));
        }

        // Set dateCreation and dateModification
        donFinancier.setDateCreation(LocalDate.now());
        donFinancier.setDateModification(LocalDate.now());

        // If dateDon is not set, use current date
        if (donFinancier.getDateDon() == null) {
            donFinancier.setDateDon(LocalDate.now());
        }

        // Log donation details for debugging
        System.out.println("💾 [DonFinancierService] Creating donation:");
        System.out.println("   - Nom: " + donFinancier.getNom());
        System.out.println("   - Prénom: " + donFinancier.getPrenom());
        System.out.println("   - Email: " + donFinancier.getEmail());
        System.out.println("   - Téléphone: " + donFinancier.getTelephone());
        System.out.println("   - Montant: " + donFinancier.getMontantFinancier());
        System.out.println("   - Mode paiement: " + donFinancier.getModePaiement());
        System.out.println("   - Date don: " + donFinancier.getDateDon());
        System.out.println("   - Projet ID: " + donFinancier.getProjetId());
        System.out.println("   - Activité ID: " + donFinancier.getActiviteId());
        System.out.println("   - Organisation ID: " + donFinancier.getOrganisationId());

        DonFinancier saved = donFinancierRepository.save(donFinancier);
        System.out.println("✅ [DonFinancierService] Donation saved with ID: " + saved.getId());
        System.out.println("✅ [DonFinancierService] Saved donation projetId: " + saved.getProjetId());
        System.out.println("✅ [DonFinancierService] Saved donation activiteId: " + saved.getActiviteId());

        // Trigger notification checks for budget and progress
        if (saved.getProjetId() != null) {
            System.out.println(
                    "🔔 [DonFinancierService] Triggering notification check for project: " + saved.getProjetId());
            notificationService.checkBudgetAndNotify(saved.getProjetId(), null);
            notificationService.notifyDonorsAboutProgress(saved.getProjetId(), null);
        }
        if (saved.getActiviteId() != null) {
            System.out.println(
                    "🔔 [DonFinancierService] Triggering notification check for activity: " + saved.getActiviteId());
            notificationService.checkBudgetAndNotify(null, saved.getActiviteId());
            notificationService.notifyDonorsAboutProgress(null, saved.getActiviteId());
        }

        // Notify organization about the new donation
        System.out.println("🔔 [DonFinancierService] Notifying organization about new donation");
        notificationService.notifyOrgAboutDonation(saved);

        return convertToDTO(saved);
    }

    // ✅ OBTENIR TOUS LES DONS FINANCIERS
    public List<DonDetailDTO> getAllDonsFinanciers() {
        return donFinancierRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ OBTENIR UN DON FINANCIER PAR ID
    public DonDetailDTO getDonFinancierById(Long id) {
        DonFinancier don = donFinancierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don financier introuvable"));
        return convertToDTO(don);
    }

    // ✅ OBTENIR LES DONS FINANCIERS D'UN PROJET
    public List<DonDetailDTO> getDonsFinanciersByProjet(Long projetId) {
        return donFinancierRepository.findByProjetId(projetId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ OBTENIR LES DONS FINANCIERS D'UNE ACTIVITÉ
    public List<DonDetailDTO> getDonsFinanciersByActivite(String activiteId) {
        return donFinancierRepository.findByActiviteId(activiteId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ OBTENIR LES DONS FINANCIERS D'UNE ORGANISATION
    public List<DonDetailDTO> getDonsFinanciersByOrganisation(String organisationId) {
        return donFinancierRepository.findByOrganisationId(organisationId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ OBTENIR LES DONS D'UN UTILISATEUR PAR EMAIL
    public List<DonDetailDTO> getDonsByEmail(String email) {
        return donFinancierRepository.findByEmail(email)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // ✅ METTRE À JOUR UN DON FINANCIER
    public DonDetailDTO updateDonFinancier(Long id, DonFinancier donUpdate) {
        DonFinancier don = donFinancierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Don financier introuvable"));

        don.setTitre(donUpdate.getTitre());
        don.setDescription(donUpdate.getDescription());
        don.setMontantFinancier(donUpdate.getMontantFinancier());
        don.setModePaiement(donUpdate.getModePaiement());
        don.setDateModification(LocalDate.now());

        DonFinancier updated = donFinancierRepository.save(don);
        return convertToDTO(updated);
    }

    // ✅ SUPPRIMER UN DON FINANCIER
    public void deleteDonFinancier(Long id) {
        if (!donFinancierRepository.existsById(id)) {
            throw new RuntimeException("Don financier introuvable");
        }
        donFinancierRepository.deleteById(id);
    }

    // ✅ CALCULER LE MONTANT TOTAL DES DONS FINANCIERS D'UN PROJET
    public Double getTotalMontantByProjet(Long projetId) {
        return donFinancierRepository.findByProjetId(projetId)
                .stream()
                .mapToDouble(d -> d.getMontantFinancier() != null ? d.getMontantFinancier() : 0)
                .sum();
    }

    // ✅ OBTENIR TOUS LES DONS FINANCIERS POUR UNE ORGANISATION
    // Inclut: dons directs à l'organisation + dons aux projets + dons aux activités
    public List<DonDetailDTO> getAllDonsForOrganisation(String organisationId) {
        List<DonDetailDTO> allDons = new ArrayList<>();

        // 1. Dons directs à l'organisation
        allDons.addAll(getDonsFinanciersByOrganisation(organisationId));

        // 2. Dons aux projets de l'organisation
        List<Projet> projets = projetRepository.findByOrganisation_Id(organisationId);
        for (Projet projet : projets) {
            if (projet.getId() != null) {
                allDons.addAll(getDonsFinanciersByProjet(projet.getId()));
            }
        }

        // 3. Dons aux activités de l'organisation
        List<Activite> activites = activiteRepository.findByOrganisation_Id(organisationId);
        for (Activite activite : activites) {
            if (activite.getId() != null) {
                allDons.addAll(getDonsFinanciersByActivite(activite.getId()));
            }
        }

        // Retourner la liste triée par date (plus récent en premier)
        return allDons.stream()
                .sorted((d1, d2) -> {
                    if (d1.getDate() == null && d2.getDate() == null)
                        return 0;
                    if (d1.getDate() == null)
                        return 1;
                    if (d2.getDate() == null)
                        return -1;
                    return d2.getDate().compareTo(d1.getDate());
                })
                .collect(Collectors.toList());
    }

    // ✅ CALCULER LE MONTANT TOTAL DES DONS POUR UNE ORGANISATION
    public Double getTotalMontantForOrganisation(String organisationId) {
        return getAllDonsForOrganisation(organisationId)
                .stream()
                .mapToDouble(d -> d.getMontant() != null ? d.getMontant() : 0)
                .sum();
    }

    // ✅ CONVERTIR EN DTO
    private DonDetailDTO convertToDTO(DonFinancier don) {
        // Déterminer la destination du don
        String destination = "Organisation";
        String projetTitre = null;
        String activiteTitre = null;
        Double objectifFinancier = null;
        Double montantCollecte = null;
        String projetImage = null;
        String activiteImage = null;
        String organisationImage = null;

        if (don.getProjetId() != null) {
            destination = "Projet";
            Projet projet = projetRepository.findById(don.getProjetId()).orElse(null);
            if (projet != null) {
                projetTitre = projet.getTitre();
                objectifFinancier = projet.getObjectifFinancier();
                projetImage = projet.getImage();
                montantCollecte = getTotalMontantByProjet(don.getProjetId());
            }
        } else if (don.getActiviteId() != null) {
            destination = "Activité";
            Activite activite = activiteRepository.findById(don.getActiviteId()).orElse(null);
            if (activite != null) {
                activiteTitre = activite.getTitre();
                objectifFinancier = activite.getObjectifFinancier();
                activiteImage = activite.getImage();
                // Calculer le total pour l'activité
                montantCollecte = donFinancierRepository.findByActiviteId(don.getActiviteId())
                        .stream()
                        .mapToDouble(d -> d.getMontantFinancier() != null ? d.getMontantFinancier() : 0)
                        .sum();
            }
        } else if (don.getOrganisationId() != null) {
            destination = "Organisation";
            iscae.mr.app_donation.dao.entities.Organisation organisation = organisationRepository
                    .findById(don.getOrganisationId()).orElse(null);
            if (organisation != null) {
                organisationImage = organisation.getLogo();
            }
        }

        return DonDetailDTO.builder()
                .nom(don.getNom() != null ? don.getNom() : "Donateur")
                .prenom(don.getPrenom())
                .email(don.getEmail())
                .telephone(don.getTelephone())
                .date(don.getDateDon())
                .typeDon("FINANCIER")
                .montant(don.getMontantFinancier())
                .details(String.format("%.2f MRU - Mode: %s - Destination: %s",
                        don.getMontantFinancier(),
                        don.getModePaiement() != null ? don.getModePaiement() : "Screenshot",
                        destination))
                .preuvePaiement(don.getPreuvePaiement())
                // Nouveaux champs pour le dashboard
                .projetTitre(projetTitre)
                .activiteTitre(activiteTitre)
                .objectifFinancier(objectifFinancier)
                .montantCollecte(montantCollecte)
                .projetImage(projetImage)
                .activiteImage(activiteImage)
                .organisationImage(organisationImage)
                .build();
    }
}