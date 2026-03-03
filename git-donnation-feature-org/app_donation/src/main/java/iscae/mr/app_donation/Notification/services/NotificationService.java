package iscae.mr.app_donation.Notification.services;

import iscae.mr.app_donation.dao.entities.*;
import iscae.mr.app_donation.dao.repositories.*;
import iscae.mr.app_donation.Notification.dtos.NotificationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final DonFinancierRepository donFinancierRepository;
    private final ProjetRepository projetRepository;
    private final ActiviteRepository activiteRepository;
    private final UtilisateurRepository utilisateurRepository;

    /**
     * Notify organization users about a new donation
     */
    @Transactional
    public void notifyOrgAboutDonation(DonFinancier don) {
        String orgId = don.getOrganisationId();
        String destination = "l'organisation";
        String targetId = don.getOrganisationId();
        String targetType = "ORGANISATION";

        if (don.getProjetId() != null) {
            Projet p = projetRepository.findById(don.getProjetId()).orElse(null);
            if (p != null) {
                destination = String.format("le projet '%s'", p.getTitre());
                targetId = String.valueOf(p.getId());
                targetType = "PROJET";
                if (orgId == null && p.getOrganisation() != null) {
                    orgId = p.getOrganisation().getId();
                }
            }
        } else if (don.getActiviteId() != null) {
            Activite a = activiteRepository.findById(don.getActiviteId()).orElse(null);
            if (a != null) {
                destination = String.format("l'activité '%s'", a.getTitre());
                targetId = a.getId();
                targetType = "ACTIVITE";
                if (orgId == null && a.getOrganisation() != null) {
                    orgId = a.getOrganisation().getId();
                }
            }
        }
        if (orgId != null) {
            List<Utilisateur> orgUsers = utilisateurRepository.findByOrganisation_Id(orgId);
            for (Utilisateur user : orgUsers) {
                createNotification(
                        user.getId(),
                        "NEW_DONATION",
                        String.format("Nouveau don de %.2f MRU reçu pour %s de la part de %s %s.",
                                don.getMontantFinancier(), destination, don.getPrenom(), don.getNom()),
                        targetType,
                        targetId);
            }
        }
    }

    /**
     * Check if budget limit is reached and send notification to organization
     */
    @Transactional
    public void checkBudgetAndNotify(Long projetId, String activiteId) {
        if (projetId != null) {
            checkProjetBudget(projetId);
        }
        if (activiteId != null) {
            checkActiviteBudget(activiteId);
        }
    }

    private void checkProjetBudget(Long projetId) {
        System.out.println("🔔 [NotificationService] Checking budget for project ID: " + projetId);

        Projet projet = projetRepository.findById(projetId).orElse(null);
        if (projet == null) {
            System.out.println("❌ [NotificationService] Project not found with ID: " + projetId);
            return;
        }

        if (projet.getObjectifFinancier() == null) {
            System.out.println(
                    "⚠️ [NotificationService] Project has no objective set. Objective: "
                            + projet.getObjectifFinancier());
            return;
        }

        System.out.println("📊 [NotificationService] Project details:");
        System.out.println("   - Titre: " + projet.getTitre());
        System.out.println("   - Objectif: " + projet.getObjectifFinancier());
        System.out.println("   - Objectif déjà atteint: " + projet.getBudgetAtteint());

        // Calculate total donations for this project
        List<DonFinancier> donations = donFinancierRepository.findByProjetId(projetId);
        System.out.println("💰 [NotificationService] Found " + donations.size() + " donations for this project");

        Double totalDonations = donations.stream()
                .mapToDouble(d -> {
                    Double montant = d.getMontantFinancier() != null ? d.getMontantFinancier() : 0;
                    System.out.println("   - Donation: " + montant + " MRU from " + d.getEmail());
                    return montant;
                })
                .sum();

        System.out.println("💰 [NotificationService] Total donations: " + totalDonations + " MRU");
        System.out.println("💰 [NotificationService] Objectif: " + projet.getObjectifFinancier() + " MRU");

        projet.setMontantRecolte(totalDonations);

        // Calculate progress percentage
        if (projet.getObjectifFinancier() != null && projet.getObjectifFinancier() > 0) {
            int progress = (int) Math.min((totalDonations / projet.getObjectifFinancier()) * 100, 100);
            projet.setPourcentageProgress(progress);
        }

        // Check if objective is reached
        boolean budgetReached = totalDonations >= projet.getObjectifFinancier();
        boolean notAlreadyNotified = (projet.getBudgetAtteint() == null || !projet.getBudgetAtteint());

        System.out.println("🔔 [NotificationService] Budget check:");
        System.out.println("   - Budget reached: " + budgetReached);
        System.out.println("   - Not already notified: " + notAlreadyNotified);

        if (budgetReached && notAlreadyNotified) {
            System.out.println("✅ [NotificationService] Budget limit reached! Creating notifications...");

            projet.setBudgetAtteint(true);
            projetRepository.save(projet);

            // Send notification to organization users
            if (projet.getOrganisation() != null) {
                String organisationId = projet.getOrganisation().getId();
                System.out.println("👥 [NotificationService] Finding users for organisation: " + organisationId);

                List<Utilisateur> orgUsers = utilisateurRepository
                        .findByOrganisation_Id(organisationId);

                System.out.println("👥 [NotificationService] Found " + orgUsers.size() + " users in organisation");

                if (orgUsers.isEmpty()) {
                    System.out.println("⚠️ [NotificationService] No users found for organisation: " + organisationId);
                }

                for (Utilisateur user : orgUsers) {
                    System.out.println("📨 [NotificationService] Creating notification for user: " + user.getUsername()
                            + " (ID: " + user.getId() + ")");
                    Notification notification = createNotification(
                            user.getId(),
                            "BUDGET_REACHED",
                            String.format(
                                    "L'objectif (%.2f MRU) a été atteint pour le projet '%s'. Vous pouvez maintenant démarrer le projet!",
                                    projet.getObjectifFinancier(), projet.getTitre()),
                            "PROJET",
                            String.valueOf(projetId));
                    System.out.println("✅ [NotificationService] Notification created with ID: " + notification.getId());
                }
            } else {
                System.out.println("❌ [NotificationService] Project has no organisation associated!");
            }
        } else {
            if (!budgetReached) {
                System.out.println("⚠️ [NotificationService] Objectif not yet reached (" + totalDonations + " < "
                        + projet.getObjectifFinancier() + ")");
            }
            if (!notAlreadyNotified) {
                System.out.println("⚠️ [NotificationService] Budget already notified");
            }
        }
    }

    private void checkActiviteBudget(String activiteId) {
        Activite activite = activiteRepository.findById(activiteId).orElse(null);
        if (activite == null || activite.getObjectifFinancier() == null) {
            return;
        }

        // Calculate total donations for this activity
        Double totalDonations = donFinancierRepository.findByActiviteId(activiteId)
                .stream()
                .mapToDouble(d -> d.getMontantFinancier() != null ? d.getMontantFinancier() : 0)
                .sum();

        activite.setMontantRecolte(totalDonations);

        // Calculate progress percentage (using objectifFinancier as target)
        if (activite.getObjectifFinancier() > 0) {
            int progress = (int) Math.min((totalDonations / activite.getObjectifFinancier()) * 100, 100);
            activite.setPourcentageProgress(progress);
        }

        // Check if objective is reached
        if (totalDonations >= activite.getObjectifFinancier() &&
                (activite.getBudgetAtteint() == null || !activite.getBudgetAtteint())) {

            activite.setBudgetAtteint(true);
            activiteRepository.save(activite);

            // Send notification to organization users
            if (activite.getOrganisation() != null) {
                List<Utilisateur> orgUsers = utilisateurRepository
                        .findByOrganisation_Id(activite.getOrganisation().getId());

                for (Utilisateur user : orgUsers) {
                    createNotification(
                            user.getId(),
                            "BUDGET_REACHED",
                            String.format(
                                    "L'objectif (%.2f MRU) a été atteint pour l'activité '%s'. Vous pouvez maintenant démarrer l'activité!",
                                    activite.getObjectifFinancier(), activite.getTitre()),
                            "ACTIVITE",
                            activiteId);
                }
            }
        }
    }

    /**
     * Notify donors about progress updates for projects/activities they donated to
     */
    @Transactional
    public void notifyDonorsAboutProgress(Long projetId, String activiteId) {
        if (projetId != null) {
            notifyDonorsAboutProjetProgress(projetId);
        }
        if (activiteId != null) {
            notifyDonorsAboutActiviteProgress(activiteId);
        }
    }

    private void notifyDonorsAboutProjetProgress(Long projetId) {
        Projet projet = projetRepository.findById(projetId).orElse(null);
        if (projet == null)
            return;

        // Get all donors for this project
        List<DonFinancier> donations = donFinancierRepository.findByProjetId(projetId);

        final Double totalDonations = donations.stream()
                .mapToDouble(d -> d.getMontantFinancier() != null ? d.getMontantFinancier() : 0)
                .sum();

        final int progress;
        if (projet.getObjectifFinancier() != null && projet.getObjectifFinancier() > 0) {
            progress = (int) Math.min((totalDonations / projet.getObjectifFinancier()) * 100, 100);
        } else {
            progress = 0;
        }

        // Make final copies for use in lambda
        final Projet finalProjet = projet;
        final Double finalObjectif = projet.getObjectifFinancier();

        // Notify each donor
        for (DonFinancier don : donations) {
            if (don.getEmail() != null) {
                // Find user by email
                utilisateurRepository.findByEmail(don.getEmail())
                        .ifPresent(user -> {
                            createNotification(
                                    user.getId(),
                                    "PROGRESS_UPDATE",
                                    String.format("Le projet '%s' a atteint %d%% de son objectif (%.2f MRU / %.2f MRU)",
                                            finalProjet.getTitre(), progress, totalDonations,
                                            finalObjectif != null ? finalObjectif : 0),
                                    "PROJET",
                                    String.valueOf(projetId));
                        });
            }
        }
    }

    private void notifyDonorsAboutActiviteProgress(String activiteId) {
        Activite activite = activiteRepository.findById(activiteId).orElse(null);
        if (activite == null)
            return;

        // Get all donors for this activity
        List<DonFinancier> donations = donFinancierRepository.findByActiviteId(activiteId);

        final Double totalDonations = donations.stream()
                .mapToDouble(d -> d.getMontantFinancier() != null ? d.getMontantFinancier() : 0)
                .sum();

        final int progress;
        if (activite.getObjectifFinancier() != null && activite.getObjectifFinancier() > 0) {
            progress = (int) Math.min((totalDonations / activite.getObjectifFinancier()) * 100, 100);
        } else {
            progress = 0;
        }

        // Make final copies for use in lambda
        final Activite finalActivite = activite;
        final Double finalObjectif = activite.getObjectifFinancier();
        final String finalActiviteId = activiteId;

        // Notify each donor
        for (DonFinancier don : donations) {
            if (don.getEmail() != null) {
                utilisateurRepository.findByEmail(don.getEmail())
                        .ifPresent(user -> {
                            createNotification(
                                    user.getId(),
                                    "PROGRESS_UPDATE",
                                    String.format(
                                            "L'activité '%s' a atteint %d%% de son objectif (%.2f MRU / %.2f MRU)",
                                            finalActivite.getTitre(), progress, totalDonations,
                                            finalObjectif != null ? finalObjectif : 0),
                                    "ACTIVITE",
                                    finalActiviteId);
                        });
            }
        }
    }

    /**
     * Create a notification
     */
    public Notification createNotification(Long userId, String type, String message,
            String relatedEntityType, String relatedEntityId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * Get all notifications for a user
     */
    public List<NotificationDTO> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notifications for a user
     */
    public List<NotificationDTO> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get unread notification count
     */
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .message(notification.getMessage())
                .relatedEntityType(notification.getRelatedEntityType())
                .relatedEntityId(notification.getRelatedEntityId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
