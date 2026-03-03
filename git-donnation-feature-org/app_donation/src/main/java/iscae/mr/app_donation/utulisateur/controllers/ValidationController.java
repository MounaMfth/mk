package iscae.mr.app_donation.utulisateur.controllers;

import iscae.mr.app_donation.dao.entities.Organisation;
import iscae.mr.app_donation.dao.entities.Utilisateur;
import iscae.mr.app_donation.dao.entities.ValidationRequest;
import iscae.mr.app_donation.dao.repositories.OrganisationRepository;
import iscae.mr.app_donation.dao.repositories.UtilisateurRepository;
import iscae.mr.app_donation.dao.repositories.ValidationRequestRepository;
import iscae.mr.app_donation.utulisateur.dtos.ValidationRequestDTO;
import iscae.mr.app_donation.utulisateur.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/validation")
@CrossOrigin(origins = { "http://localhost:4200" }, allowedHeaders = { "*" }, methods = { RequestMethod.GET,
        RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
        RequestMethod.OPTIONS }, allowCredentials = "true", maxAge = 3600)
public class ValidationController {

    @Autowired
    private ValidationRequestRepository validationRequestRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private UtilisateurService utilisateurService;

    @Autowired
    private OrganisationRepository organisationRepository;

    /**
     * User requests ORG role validation
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestValidation(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ValidationRequestDTO requestDTO) {
        try {
            if (jwt == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            Long userId = Long.parseLong(jwt.getClaim("userId").toString());

            // Check if user already has a pending request
            Optional<ValidationRequest> existingRequest = validationRequestRepository
                    .findByUtilisateurIdAndStatus(userId, "PENDING");

            if (existingRequest.isPresent()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vous avez déjà une demande en attente"));
            }

            // Check if user already has ORG role
            Utilisateur user = utilisateurRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getRoles() != null && user.getRoles().contains("ORG")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Vous avez déjà le rôle ORGANISATION"));
            }

            // Create validation request with all organisation info
            ValidationRequest validationRequest = new ValidationRequest();
            validationRequest.setUtilisateurId(userId);
            validationRequest.setRequestedRole("ORG");
            validationRequest.setStatus("PENDING");
            validationRequest.setOrganisationNom(requestDTO.getOrganisationNom());
            validationRequest.setOrganisationDescription(requestDTO.getOrganisationDescription());
            validationRequest.setOrganisationLocalisation(requestDTO.getLocalisation());
            validationRequest.setOrganisationEmail(requestDTO.getEmail());
            validationRequest.setOrganisationTelephone(requestDTO.getTelephone());
            validationRequest.setOrganisationSiteWeb(requestDTO.getSiteWeb());
            validationRequest.setOrganisationLogoUrl(requestDTO.getLogoUrl());
            validationRequest.setOrganisationCertificateUrl(requestDTO.getCertificateUrl());

            // Store secteur IDs as comma-separated string
            if (requestDTO.getSecteurIds() != null && !requestDTO.getSecteurIds().isEmpty()) {
                validationRequest.setSecteurIds(String.join(",", requestDTO.getSecteurIds()));
            }

            validationRequest.setCreatedAt(LocalDateTime.now());

            validationRequestRepository.save(validationRequest);

            System.out.println("✅ [ValidationController] Demande créée avec logo: " + requestDTO.getLogoUrl());

            return ResponseEntity.ok(Map.of(
                    "message", "Demande de validation envoyée avec succès",
                    "requestId", validationRequest.getId()));

        } catch (Exception e) {
            System.err.println("❌ [ValidationController] Erreur création demande: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Erreur lors de la création de la demande"));
        }
    }

    /**
     * Get all pending validation requests (ADMIN only)
     */
    @GetMapping("/requests")
    public ResponseEntity<?> getPendingRequests(@AuthenticationPrincipal Jwt jwt) {
        try {
            if (jwt == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            List<ValidationRequest> requests = validationRequestRepository
                    .findByStatusOrderByCreatedAtDesc("PENDING");

            // Enrich with user information
            List<Map<String, Object>> enrichedRequests = new ArrayList<>();
            for (ValidationRequest request : requests) {
                Optional<Utilisateur> userOpt = utilisateurRepository.findById(request.getUtilisateurId());
                if (userOpt.isPresent()) {
                    Utilisateur user = userOpt.get();
                    Map<String, Object> requestMap = new HashMap<>();
                    requestMap.put("id", request.getId());
                    requestMap.put("utilisateurId", request.getUtilisateurId());
                    requestMap.put("username", user.getUsername());
                    requestMap.put("email", user.getEmail());
                    requestMap.put("telephone", user.getTelephone());
                    requestMap.put("requestedRole", request.getRequestedRole());
                    requestMap.put("organisationNom", request.getOrganisationNom());
                    requestMap.put("organisationDescription", request.getOrganisationDescription());
                    requestMap.put("organisationLocalisation", request.getOrganisationLocalisation());
                    requestMap.put("organisationEmail", request.getOrganisationEmail());
                    requestMap.put("organisationTelephone", request.getOrganisationTelephone());
                    requestMap.put("organisationSiteWeb", request.getOrganisationSiteWeb());
                    requestMap.put("organisationLogoUrl", request.getOrganisationLogoUrl());
                    requestMap.put("organisationCertificateUrl", request.getOrganisationCertificateUrl());
                    requestMap.put("secteurIds", request.getSecteurIds());
                    requestMap.put("status", request.getStatus());
                    requestMap.put("createdAt", request.getCreatedAt());
                    enrichedRequests.add(requestMap);
                }
            }

            return ResponseEntity.ok(enrichedRequests);

        } catch (Exception e) {
            System.err.println("❌ [ValidationController] Erreur récupération demandes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Erreur lors de la récupération des demandes"));
        }
    }

    /**
     * Approve validation request (ADMIN only)
     */
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            if (jwt == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            Long adminId = Long.parseLong(jwt.getClaim("userId").toString());

            ValidationRequest request = validationRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

            if (!"PENDING".equals(request.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Cette demande a déjà été traitée"));
            }

            // Update user role - replace USER with ORG
            Utilisateur user = utilisateurRepository.findById(request.getUtilisateurId())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            System.out.println("🔍 [ValidationController] Avant modification:");
            System.out.println("   - User ID: " + user.getId());
            System.out.println("   - Username: " + user.getUsername());
            System.out.println("   - Profil actuel: " + user.getProfil());
            System.out.println("   - Rôles actuels: " + user.getRoles());

            // Set profil to ORGANISATEUR (not ORG) - the setProfil method will
            // automatically set roles
            user.setProfil("ORGANISATEUR");

            System.out.println("✅ [ValidationController] Après modification:");
            System.out.println("   - Profil nouveau: " + user.getProfil());
            System.out.println("   - Rôles nouveaux: " + user.getRoles());

            // Create organization for the user with all info from the request
            if (request.getOrganisationNom() != null && !request.getOrganisationNom().isEmpty()) {
                System.out.println("📋 [ValidationController] Données de la demande:");
                System.out.println("   - Nom: " + request.getOrganisationNom());
                System.out.println("   - Description: " + request.getOrganisationDescription());
                System.out.println("   - Localisation: " + request.getOrganisationLocalisation());
                System.out.println("   - Email: " + request.getOrganisationEmail());
                System.out.println("   - Téléphone: " + request.getOrganisationTelephone());
                System.out.println("   - Site Web: " + request.getOrganisationSiteWeb());
                System.out.println("   - Logo URL: " + request.getOrganisationLogoUrl());

                Organisation organisation = utilisateurService.createOrganisationComplete(
                        request.getOrganisationNom(),
                        request.getOrganisationDescription(),
                        request.getOrganisationLocalisation(),
                        request.getOrganisationEmail(),
                        request.getOrganisationTelephone(),
                        request.getOrganisationSiteWeb(),
                        request.getOrganisationLogoUrl());

                // Set organization directly on our user object before saving
                user.setOrganisation(organisation);
                System.out.println("🏢 [ValidationController] Organisation créée avec toutes les infos:");
                System.out.println("   - Nom: " + organisation.getNom());
                System.out.println("   - Logo: " + organisation.getLogo());
                System.out.println("   - Localisation: " + organisation.getLocalisation());
            }

            utilisateurRepository.save(user);
            System.out.println("💾 [ValidationController] Utilisateur sauvegardé");

            // Update validation request
            request.setStatus("APPROVED");
            request.setProcessedAt(LocalDateTime.now());
            request.setProcessedBy(adminId);
            validationRequestRepository.save(request);

            return ResponseEntity.ok(Map.of(
                    "message", "Demande approuvée avec succès",
                    "userId", user.getId()));

        } catch (Exception e) {
            System.err.println("❌ [ValidationController] Erreur approbation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Erreur lors de l'approbation"));
        }
    }

    /**
     * Reject validation request (ADMIN only)
     */
    @PostMapping("/reject/{id}")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            if (jwt == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            Long adminId = Long.parseLong(jwt.getClaim("userId").toString());

            ValidationRequest request = validationRequestRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

            if (!"PENDING".equals(request.getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Cette demande a déjà été traitée"));
            }

            // Update validation request
            request.setStatus("REJECTED");
            request.setProcessedAt(LocalDateTime.now());
            request.setProcessedBy(adminId);

            if (body != null && body.containsKey("reason")) {
                request.setRejectionReason(body.get("reason"));
            }

            validationRequestRepository.save(request);

            return ResponseEntity.ok(Map.of(
                    "message", "Demande rejetée",
                    "requestId", id));

        } catch (Exception e) {
            System.err.println("❌ [ValidationController] Erreur rejet: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Erreur lors du rejet"));
        }
    }

    /**
     * Get user's own validation requests
     */
    @GetMapping("/my-requests")
    public ResponseEntity<?> getMyRequests(@AuthenticationPrincipal Jwt jwt) {
        try {
            if (jwt == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            Long userId = Long.parseLong(jwt.getClaim("userId").toString());
            List<ValidationRequest> requests = validationRequestRepository.findByUtilisateurId(userId);

            return ResponseEntity.ok(requests);

        } catch (Exception e) {
            System.err.println("❌ [ValidationController] Erreur récupération demandes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Erreur lors de la récupération des demandes"));
        }
    }

    /**
     * Get dashboard statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@AuthenticationPrincipal Jwt jwt) {
        try {
            if (jwt == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            long pendingCount = validationRequestRepository.countByStatus("PENDING");
            long approvedCount = validationRequestRepository.countByStatus("APPROVED");
            long rejectedCount = validationRequestRepository.countByStatus("REJECTED");
            long totalOrganisations = organisationRepository.count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("pendingCount", pendingCount);
            stats.put("processedCount", approvedCount + rejectedCount);
            stats.put("totalOrganisations", totalOrganisations);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("❌ [ValidationController] Erreur statistiques: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors du calcul des statistiques"));
        }
    }
}
