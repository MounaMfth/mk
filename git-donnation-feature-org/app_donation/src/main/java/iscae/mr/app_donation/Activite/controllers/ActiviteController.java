package iscae.mr.app_donation.Activite.controllers;

import iscae.mr.app_donation.Activite.dtos.ActiviteDTO;
import iscae.mr.app_donation.Activite.services.ActiviteService;
import iscae.mr.app_donation.Don.services.DonService;
import iscae.mr.app_donation.dao.entities.Activite;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/activites")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class ActiviteController {

    private final ActiviteService activiteService;
    private final DonService donService;
    private final Path uploadRoot = Paths.get("uploads", "activites");

    /*
     * ============================================================
     * GET /api/activites
     * ============================================================
     */
    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal Jwt jwt) {
        try {
            String orgId = requireOrgId(jwt);
            return ResponseEntity.ok(activiteService.listByOrganisationId(orgId));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    /*
     * ============================================================
     * GET /api/activites/public
     * ============================================================
     */
    @GetMapping("/public")
    public ResponseEntity<?> listPublic() {
        try {
            System.out.println("📋 [ActiviteController] GET /api/activites/public - Public endpoint called");
            List<ActiviteDTO> activites = activiteService.listPublic();
            System.out.println("✅ [ActiviteController] Returning " + activites.size() + " public activities");
            activites.forEach(a -> System.out.println("   - Activité: " + a.getTitre() + " (ID: " + a.getId() + ")"));
            return ResponseEntity.ok(activites);
        } catch (Exception e) {
            System.err.println("❌ [ActiviteController] Error getting public activities: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erreur serveur");
        }
    }

    /*
     * ============================================================
     * POST /api/activites
     * ============================================================
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String titre,
            @RequestParam(required = false) String description,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double objectifFinancier,
            @RequestParam(required = false) MultipartFile image) {
        try {
            String orgId = requireOrgId(jwt);

            Activite a = new Activite();
            a.setTitre(titre);
            a.setDescription(description);
            a.setLatitude(latitude);
            a.setLongitude(longitude);
            a.setObjectifFinancier(objectifFinancier);

            if (image != null && !image.isEmpty()) {
                a.setImage(saveImage(image));
            }

            ActiviteDTO out = activiteService.createActivite(a, orgId);
            return ResponseEntity.status(HttpStatus.CREATED).body(out);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erreur serveur");
        }
    }

    /*
     * ============================================================
     * PUT /api/activites/{id}
     * ============================================================
     */
    @PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String titre,
            @RequestParam(required = false) String description,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false) Double objectifFinancier,
            @RequestParam(required = false) MultipartFile image) {
        try {
            String orgId = requireOrgId(jwt);

            Activite a = new Activite();
            a.setTitre(titre);
            a.setDescription(description);
            a.setLatitude(latitude);
            a.setLongitude(longitude);
            a.setObjectifFinancier(objectifFinancier);

            if (image != null && !image.isEmpty()) {
                a.setImage(saveImage(image));
            }

            ActiviteDTO out = activiteService.updateActivite(id, a, orgId);
            return ResponseEntity.ok(out);

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Erreur serveur");
        }
    }

    /*
     * ============================================================
     * DELETE /api/activites/{id}
     * ============================================================
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            String orgId = requireOrgId(jwt);
            activiteService.deleteActivite(id, orgId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    /*
     * ============================================================
     * GET /api/activites/{activiteId}/dons
     * ============================================================
     */
    @GetMapping("/{activiteId}/dons")
    public ResponseEntity<?> getDonsParActivite(@PathVariable String activiteId) {
        return ResponseEntity.ok(donService.getDonsByActiviteId(activiteId));
    }

    /*
     * ============================================================
     * UTILS
     * ============================================================
     */
    private String requireOrgId(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalStateException("Non authentifié");
        }
        Object claim = jwt.getClaims().get("orgId");
        if (claim == null || claim.toString().isBlank()) {
            throw new IllegalStateException("Organisation absente du token");
        }
        return claim.toString();
    }

    private String saveImage(MultipartFile file) throws Exception {
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

        Files.createDirectories(uploadRoot);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = uploadRoot.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }
}
