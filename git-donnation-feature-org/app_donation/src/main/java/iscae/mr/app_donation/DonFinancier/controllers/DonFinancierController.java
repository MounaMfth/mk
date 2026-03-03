package iscae.mr.app_donation.DonFinancier.controllers;

import iscae.mr.app_donation.Don.dtos.DonDetailDTO;
import iscae.mr.app_donation.DonFinancier.services.DonFinancierService;
import iscae.mr.app_donation.dao.entities.DonFinancier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.UUID;

import java.util.List;

@RestController
@RequestMapping("/api/dons-financiers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DonFinancierController {

    private final DonFinancierService donFinancierService;

    private final Path uploadRoot = Paths.get("uploads", "dons");

    // ✅ CRÉER UN DON FINANCIER
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DonDetailDTO> createDonFinancier(
            @RequestParam("montantFinancier") Double montantFinancier,
            @RequestParam(value = "modePaiement", required = false) String modePaiement,
            @RequestParam(value = "projetId", required = false) Long projetId,
            @RequestParam(value = "activiteId", required = false) String activiteId,
            @RequestParam(value = "organisationId", required = false) String organisationId,
            @RequestParam("nom") String nom,
            @RequestParam("prenom") String prenom,
            @RequestParam("email") String email,
            @RequestParam("telephone") String telephone,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            System.out.println("📥 [DonFinancierController] Received multipart donation:");
            System.out.println("   - Montant: " + montantFinancier);
            System.out.println("   - Email: " + email);

            DonFinancier donFinancier = DonFinancier.builder()
                    .montantFinancier(montantFinancier)
                    .modePaiement(modePaiement)
                    .projetId(projetId)
                    .activiteId(activiteId)
                    .organisationId(organisationId)
                    .nom(nom)
                    .prenom(prenom)
                    .email(email)
                    .telephone(telephone)
                    .dateDon(LocalDate.now())
                    .build();

            if (image != null && !image.isEmpty()) {
                donFinancier.setPreuvePaiement(saveImage(image));
            }

            DonDetailDTO created = donFinancierService.createDonFinancier(donFinancier);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            System.err.println("❌ [DonFinancierController] Error creating donation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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
            default -> "img";
        };

        Files.createDirectories(uploadRoot);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        Path target = uploadRoot.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    // ✅ OBTENIR TOUS LES DONS FINANCIERS
    @GetMapping
    public ResponseEntity<List<DonDetailDTO>> getAllDonsFinanciers() {
        try {
            List<DonDetailDTO> dons = donFinancierService.getAllDonsFinanciers();
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR UN DON FINANCIER PAR ID
    @GetMapping("/{id}")
    public ResponseEntity<DonDetailDTO> getDonFinancierById(@PathVariable Long id) {
        try {
            DonDetailDTO don = donFinancierService.getDonFinancierById(id);
            return ResponseEntity.ok(don);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR LES DONS FINANCIERS D'UN PROJET
    @GetMapping("/projet/{projetId}")
    public ResponseEntity<List<DonDetailDTO>> getDonsFinanciersByProjet(@PathVariable Long projetId) {
        try {
            List<DonDetailDTO> dons = donFinancierService.getDonsFinanciersByProjet(projetId);
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR LES DONS FINANCIERS D'UNE ACTIVITÉ
    @GetMapping("/activite/{activiteId}")
    public ResponseEntity<List<DonDetailDTO>> getDonsFinanciersByActivite(@PathVariable String activiteId) {
        try {
            List<DonDetailDTO> dons = donFinancierService.getDonsFinanciersByActivite(activiteId);
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR LES DONS FINANCIERS D'UNE ORGANISATION
    @GetMapping("/organisation/{organisationId}")
    public ResponseEntity<List<DonDetailDTO>> getDonsFinanciersByOrganisation(@PathVariable String organisationId) {
        try {
            List<DonDetailDTO> dons = donFinancierService.getDonsFinanciersByOrganisation(organisationId);
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR LES DONS D'UN UTILISATEUR PAR EMAIL
    @GetMapping("/email/{email}")
    public ResponseEntity<List<DonDetailDTO>> getDonsByEmail(@PathVariable String email) {
        try {
            List<DonDetailDTO> dons = donFinancierService.getDonsByEmail(email);
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ METTRE À JOUR UN DON FINANCIER
    @PutMapping("/{id}")
    public ResponseEntity<DonDetailDTO> updateDonFinancier(@PathVariable Long id,
            @RequestBody DonFinancier donUpdate) {
        try {
            DonDetailDTO updated = donFinancierService.updateDonFinancier(id, donUpdate);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ SUPPRIMER UN DON FINANCIER
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDonFinancier(@PathVariable Long id) {
        try {
            donFinancierService.deleteDonFinancier(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR LE MONTANT TOTAL D'UN PROJET
    @GetMapping("/total/{projetId}")
    public ResponseEntity<Double> getTotalMontantByProjet(@PathVariable Long projetId) {
        try {
            Double total = donFinancierService.getTotalMontantByProjet(projetId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR TOUS LES DONS POUR UNE ORGANISATION (projets + activités + directs)
    @GetMapping("/organisation/{organisationId}/all")
    public ResponseEntity<List<DonDetailDTO>> getAllDonsForOrganisation(
            @PathVariable String organisationId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            // Vérifier que l'utilisateur a le droit de voir les dons de cette organisation
            if (jwt != null) {
                String userOrgId = (String) jwt.getClaims().getOrDefault("orgId", "");
                Object roles = jwt.getClaims().get("roles");

                // Si l'utilisateur n'est pas ADMIN et n'appartient pas à cette organisation,
                // refuser
                if (roles instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> roleList = (java.util.List<String>) roles;
                    boolean isAdmin = roleList.contains("ADMIN") || roleList.contains("ROLE_ADMIN");
                    boolean isOrgOwner = !userOrgId.isEmpty() && userOrgId.equals(organisationId);

                    if (!isAdmin && !isOrgOwner) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
            }

            List<DonDetailDTO> dons = donFinancierService.getAllDonsForOrganisation(organisationId);
            return ResponseEntity.ok(dons);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ✅ OBTENIR LE MONTANT TOTAL DES DONS POUR UNE ORGANISATION
    @GetMapping("/organisation/{organisationId}/total")
    public ResponseEntity<Double> getTotalMontantForOrganisation(
            @PathVariable String organisationId,
            @AuthenticationPrincipal Jwt jwt) {
        try {
            // Vérifier que l'utilisateur a le droit de voir les dons de cette organisation
            if (jwt != null) {
                String userOrgId = (String) jwt.getClaims().getOrDefault("orgId", "");
                Object roles = jwt.getClaims().get("roles");

                if (roles instanceof java.util.List) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> roleList = (java.util.List<String>) roles;
                    boolean isAdmin = roleList.contains("ADMIN") || roleList.contains("ROLE_ADMIN");
                    boolean isOrgOwner = !userOrgId.isEmpty() && userOrgId.equals(organisationId);

                    if (!isAdmin && !isOrgOwner) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
            }

            Double total = donFinancierService.getTotalMontantForOrganisation(organisationId);
            return ResponseEntity.ok(total);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}