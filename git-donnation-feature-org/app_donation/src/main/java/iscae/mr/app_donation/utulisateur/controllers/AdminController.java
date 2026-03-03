package iscae.mr.app_donation.utulisateur.controllers;

import iscae.mr.app_donation.dao.entities.Organisation;
import iscae.mr.app_donation.dao.entities.Utilisateur;
import iscae.mr.app_donation.utulisateur.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Utilisateur> users = utilisateurService.getAllUsers();
        List<Map<String, Object>> response = users.stream().map(user -> Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "profil", user.getProfil() != null ? user.getProfil() : "",
                "roles", user.getRoles(),
                "organisation", user.getOrganisation() != null ? user.getOrganisation().getNom() : ""))
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/organisations")
    public ResponseEntity<List<Organisation>> getAllOrganisations() {
        return ResponseEntity.ok(utilisateurService.getAllOrganisations());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        utilisateurService.deleteUser(id);
        return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès"));
    }

    @DeleteMapping("/organisations/{id}")
    public ResponseEntity<?> deleteOrganisation(@PathVariable String id) {
        try {
            utilisateurService.deleteOrganisation(id);
            return ResponseEntity.ok(Map.of("message", "Organisation supprimée avec succès"));
        } catch (Exception e) {
            System.err.println("❌ [AdminController] Erreur suppression organisation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Erreur lors de la suppression de l'organisation", 
                                 "message", e.getMessage()));
        }
    }

    @PutMapping("/organisations/{id}/status")
    public ResponseEntity<?> updateOrganisationStatus(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        try {
            Boolean valide = body.get("valide");
            if (valide == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Le champ 'valide' est requis"));
            }
            
            Organisation updatedOrg = utilisateurService.updateOrganisationStatus(id, valide);
            return ResponseEntity.ok(Map.of(
                    "message", "Statut de l'organisation mis à jour avec succès",
                    "organisation", updatedOrg));
        } catch (RuntimeException e) {
            System.err.println("❌ [AdminController] Erreur mise à jour statut: " + e.getMessage());
            return ResponseEntity.status(404)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            System.err.println("❌ [AdminController] Erreur mise à jour statut: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Erreur lors de la mise à jour du statut"));
        }
    }
}
