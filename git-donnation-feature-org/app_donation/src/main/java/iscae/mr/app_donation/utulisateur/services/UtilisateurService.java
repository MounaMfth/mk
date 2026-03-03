package iscae.mr.app_donation.utulisateur.services;

import iscae.mr.app_donation.dao.entities.Utilisateur;
import iscae.mr.app_donation.dao.entities.Organisation;
import iscae.mr.app_donation.dao.repositories.UtilisateurRepository;
import iscae.mr.app_donation.dao.repositories.OrganisationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========================================
    // 👤 UTILISATEUR
    // ========================================

    public Utilisateur registerUser(String username, String password, String email,
            String adresse, String telephone, String profil, String prenom, String nom) {
        Utilisateur user = new Utilisateur();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setAdresse(adresse);
        user.setTelephone(telephone);
        user.setProfil(profil);
        user.setPrenom(prenom);
        user.setNom(nom);

        return utilisateurRepository.save(user);
    }

    public Utilisateur getUserByUsername(String username) {
        return utilisateurRepository.findByUsername(username).orElse(null);
    }

    public void updateUser(Utilisateur user) {
        utilisateurRepository.save(user);
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // ========================================
    // 🏢 ORGANISATION
    // ========================================

    /**
     * Créer une nouvelle organisation (simple)
     */
    public Organisation createOrganisation(String nom, String description) {
        Organisation org = new Organisation();
        org.setNom(nom);
        org.setDescription(description);
        org.setCreatedAt(LocalDateTime.now());
        org.setUpdatedAt(LocalDateTime.now());

        System.out.println("📝 [UtilisateurService] Création organisation: " + nom);
        return organisationRepository.save(org);
    }

    /**
     * Créer une organisation complète avec toutes les infos
     */
    public Organisation createOrganisationComplete(String nom, String description, String localisation,
            String email, String telephone, String siteWeb, String logoUrl) {
        Organisation org = new Organisation();
        org.setNom(nom);
        org.setDescription(description);
        org.setLocalisation(localisation);
        org.setEmail(email);
        org.setTelephone(telephone);
        org.setSiteWeb(siteWeb);
        org.setLogo(logoUrl);
        org.setValide(false); // Pending validation
        org.setCreatedAt(LocalDateTime.now());
        org.setUpdatedAt(LocalDateTime.now());

        System.out.println("📝 [UtilisateurService] Création organisation complète: " + nom);
        System.out.println("   - Logo: " + logoUrl);
        return organisationRepository.save(org);
    }

    /**
     * Assigner un utilisateur à une organisation
     */
    public void assignUserToOrganisation(Long userId, String organisationId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé (ID: " + userId + ")"));

        Organisation org = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée (ID: " + organisationId + ")"));

        user.setOrganisation(org);
        utilisateurRepository.save(user);

        System.out.println("✅ [UtilisateurService] Utilisateur " + user.getUsername() +
                " assigné à organisation " + org.getNom());
    }

    /**
     * Récupérer l'organisation d'un utilisateur
     */
    public Organisation getUserOrganisation(Long userId) {
        Utilisateur user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return user.getOrganisation();
    }

    // ========================================
    // 🛠️ ADMIN ACTIONS
    // ========================================

    public java.util.List<Utilisateur> getAllUsers() {
        return utilisateurRepository.findAll();
    }

    public java.util.List<Organisation> getAllOrganisations() {
        return organisationRepository.findAll();
    }

    public void deleteUser(Long id) {
        utilisateurRepository.deleteById(id);
    }

    @Transactional
    public void deleteOrganisation(String id) {
        System.out.println("🗑️ [UtilisateurService] Début suppression organisation " + id);

        // First, find all users linked to this organisation and set their organisation
        // to null
        List<Utilisateur> linkedUsers = utilisateurRepository.findAll().stream()
                .filter(u -> u.getOrganisation() != null && u.getOrganisation().getId().equals(id))
                .toList();

        System.out.println("   - Utilisateurs liés trouvés: " + linkedUsers.size());

        // Unlink users from organisation
        for (Utilisateur user : linkedUsers) {
            System.out.println("   - Traitement utilisateur: " + user.getUsername());

            user.setOrganisation(null);

            // Also remove ORG role if they have it
            if (user.getRoles() != null && user.getRoles().contains("ORG")) {
                user.getRoles().remove("ORG");
                if (!user.getRoles().contains("USER")) {
                    user.getRoles().add("USER"); // Revert to USER role
                }
                user.setProfil("DONATEUR");
                System.out.println("     - Rôle changé de ORG à USER");
            }

            utilisateurRepository.save(user);
            System.out.println("     - Utilisateur sauvegardé et délié");
        }

        // Flush to ensure all user updates are committed before deleting organisation
        utilisateurRepository.flush();

        // Now delete the organisation
        System.out.println("   - Suppression de l'organisation...");
        organisationRepository.deleteById(id);
        System.out.println("✅ [UtilisateurService] Organisation supprimée avec succès");
    }

    /**
     * Update organization status (valide field)
     */
    @Transactional
    public Organisation updateOrganisationStatus(String id, Boolean valide) {
        System.out.println("🔄 [UtilisateurService] Mise à jour statut organisation " + id + " -> " + valide);

        Organisation org = organisationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Organisation non trouvée (ID: " + id + ")"));

        org.setValide(valide);
        org.setUpdatedAt(LocalDateTime.now());

        System.out.println("✅ [UtilisateurService] Statut organisation mis à jour: " + org.getNom() + " -> " + valide);
        return organisationRepository.save(org);
    }
}