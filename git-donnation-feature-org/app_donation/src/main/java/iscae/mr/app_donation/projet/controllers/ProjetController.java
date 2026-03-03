// ============= CONTROLLER PROJET =============
package iscae.mr.app_donation.projet.controllers;

import iscae.mr.app_donation.projet.dtos.ProjetDTO;
import iscae.mr.app_donation.projet.Service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjetController {

    private final ProjetService projetService;
    private final java.nio.file.Path uploadRoot = java.nio.file.Paths.get("uploads", "projets");

    // ✅ CRÉER UN PROJET
    @PostMapping(consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjetDTO> createProjet(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String titre,
            @RequestParam String description,
            @RequestParam Double objectifFinancier,
            @RequestParam String dateDebut,
            @RequestParam String dateFin,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile image) {
        try {
            ProjetDTO dto = ProjetDTO.builder()
                    .titre(titre)
                    .description(description)
                    .objectifFinancier(objectifFinancier)
                    .dateDebut(java.time.LocalDate.parse(dateDebut))
                    .dateFin(java.time.LocalDate.parse(dateFin))
                    .statut(statut)
                    .build();

            if (image != null && !image.isEmpty()) {
                dto.setImage(saveImage(image));
            }

            // Extract organization ID from JWT for ORG users
            String organisationId = extractOrgIdFromJwt(jwt);
            ProjetDTO created = projetService.createProjet(dto, organisationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR TOUS LES PROJETS
    // For ORG users: returns only their organization's projects
    // For public/non-ORG: returns all projects
    @GetMapping
    public ResponseEntity<List<ProjetDTO>> getAllProjets(@AuthenticationPrincipal Jwt jwt) {
        try {
            // If authenticated with ORG role, return only their projects
            if (jwt != null) {
                String orgId = extractOrgIdFromJwt(jwt);
                if (orgId != null && !orgId.isEmpty()) {
                    // Check if user has ORG role
                    Object roles = jwt.getClaims().get("roles");
                    if (roles instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<String> roleList = (java.util.List<String>) roles;
                        if (roleList.contains("ORG") || roleList.contains("ROLE_ORG") || 
                            roleList.contains("ORGANISATEUR") || roleList.contains("ROLE_ORGANISATEUR")) {
                            // Return filtered projects for ORG users
                            List<ProjetDTO> projets = projetService.getProjetsByOrganisationId(orgId);
                            return ResponseEntity.ok(projets);
                        }
                    }
                }
            }
            // For non-ORG users or public access, return all projects
            List<ProjetDTO> projets = projetService.getAllProjets();
            return ResponseEntity.ok(projets);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ Extract organization ID from JWT
    private String extractOrgIdFromJwt(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        Object claim = jwt.getClaims().get("orgId");
        if (claim == null) {
            return null;
        }
        String orgId = claim.toString();
        return (orgId != null && !orgId.isEmpty()) ? orgId : null;
    }

    // ✅ OBTENIR LES PROJETS ACTIFS
    @GetMapping("/actifs")
    public ResponseEntity<List<ProjetDTO>> getProjetsActifs() {
        try {
            System.out.println("📋 [ProjetController] GET /api/projets/actifs - Public endpoint called");
            List<ProjetDTO> projets = projetService.getProjetsActifs();
            System.out.println("✅ [ProjetController] Returning " + projets.size() + " active projects");
            projets.forEach(p -> System.out.println("   - Projet: " + p.getTitre() + " (ID: " + p.getId() + ")"));
            return ResponseEntity.ok(projets);
        } catch (Exception e) {
            System.err.println("❌ [ProjetController] Error getting active projects: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR UN PROJET PAR ID
    @GetMapping("/{id}")
    public ResponseEntity<ProjetDTO> getProjetById(@PathVariable String id) {
        try {
            ProjetDTO projet = projetService.getProjetById(id);
            return ResponseEntity.ok(projet);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ METTRE À JOUR UN PROJET
    @PutMapping(path = "/{id}", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProjetDTO> updateProjet(
            @PathVariable String id,
            @RequestParam String titre,
            @RequestParam String description,
            @RequestParam Double objectifFinancier,
            @RequestParam String dateDebut,
            @RequestParam String dateFin,
            @RequestParam(required = false) String statut,
            @RequestParam(required = false) org.springframework.web.multipart.MultipartFile image) {
        try {
            ProjetDTO dto = ProjetDTO.builder()
                    .titre(titre)
                    .description(description)
                    .objectifFinancier(objectifFinancier)
                    .dateDebut(java.time.LocalDate.parse(dateDebut))
                    .dateFin(java.time.LocalDate.parse(dateFin))
                    .statut(statut)
                    .build();

            if (image != null && !image.isEmpty()) {
                dto.setImage(saveImage(image));
            }

            ProjetDTO updated = projetService.updateProjet(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String saveImage(org.springframework.web.multipart.MultipartFile file) throws Exception {
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new IllegalArgumentException("Type de fichier non supporté: " + ct);
        }

        String ext = switch (ct) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            case "image/svg+xml" -> "svg";
            default -> "img";
        };

        java.nio.file.Files.createDirectories(uploadRoot);
        String filename = java.util.UUID.randomUUID().toString().replace("-", "") + "." + ext;
        java.nio.file.Path target = uploadRoot.resolve(filename);
        java.nio.file.Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    // ✅ SUPPRIMER UN PROJET
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjet(@PathVariable String id) {
        try {
            projetService.deleteProjet(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ RECHERCHER DES PROJETS
    @GetMapping("/search")
    public ResponseEntity<List<ProjetDTO>> searchProjet(@RequestParam String titre) {
        try {
            List<ProjetDTO> projets = projetService.searchProjet(titre);
            return ResponseEntity.ok(projets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}