// src/main/java/iscae/mr/app_donation/Organisation/controllers/OrganisationController.java
package iscae.mr.app_donation.Organisation.controllers;

import iscae.mr.app_donation.dao.entities.Organisation;
import iscae.mr.app_donation.dao.repositories.OrganisationRepository;
import iscae.mr.app_donation.utulisateur.services.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class OrganisationController {

  private final OrganisationRepository organisationRepository;
  private final UtilisateurService utilisateurService;

  @Value("${app.upload.base-dir:uploads}") // configurable via application.properties
  private String uploadBaseDir;

  /* -------- Utils fichiers -------- */

  /** Dossier absolu: <base>/logos/ (créé si absent) */
  private Path logosRoot() throws IOException {
    Path root = Paths.get(uploadBaseDir, "logos").toAbsolutePath().normalize();
    Files.createDirectories(root);
    return root;
  }

  /** Retire tout chemin (C:\... ou /home/...) et ne garde que le nom */
  private String basename(String original) {
    if (original == null) return "logo";
    String s = original.replace("\\", "/");
    int i = s.lastIndexOf('/');
    return (i >= 0 ? s.substring(i + 1) : s);
  }

  /** Sauvegarde le logo si fourni, renvoie le nom de fichier stocké (ou null) */
  private String saveLogoFile(MultipartFile logo) throws IOException {
    if (logo == null || logo.isEmpty()) return null;

    String base = basename(logo.getOriginalFilename());
    String ext = "";
    int dot = base.lastIndexOf('.');
    if (dot >= 0) ext = base.substring(dot).replaceAll("[^a-zA-Z0-9.]", "");

    String unique = UUID.randomUUID().toString().replace("-", "") + ext;
    Path dest = logosRoot().resolve(unique);

    // Copie via NIO (évite ApplicationPart.write et les problèmes de chemins)
    Files.copy(logo.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
    return unique; // on stocke uniquement le nom
  }

  /* -------- Endpoints -------- */

  @GetMapping
  public List<Organisation> getAll(@AuthenticationPrincipal Jwt jwt) {
    // If authenticated with ORG role, return only their organization
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
            // Return only their organization for ORG users
            return organisationRepository.findById(orgId)
                .map(java.util.Collections::singletonList)
                .orElse(java.util.Collections.emptyList());
          }
        }
      }
    }
    // For public/non-ORG users, return all organizations
    return organisationRepository.findAll();
  }

  /**
   * Extract organization ID from JWT
   */
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

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Organisation> createOrganisation(
      @RequestParam String nom,
      @RequestParam String description,
      @RequestParam String localisation,
      @RequestParam Boolean statutVerifie,
      @RequestParam(required = false) String telephone,
      @RequestParam(required = false) String siteWeb,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) MultipartFile logo
  ) throws IOException {

    Organisation org = new Organisation();
    org.setNom(nom);
    org.setDescription(description);
    org.setLocalisation(localisation);
    org.setStatutVerifie(Boolean.TRUE.equals(statutVerifie));
    org.setTelephone(telephone);
    org.setSiteWeb(siteWeb);
    org.setEmail(email);

    String saved = saveLogoFile(logo);
    if (saved != null) org.setLogo(saved);

    return ResponseEntity.ok(organisationRepository.save(org));
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Organisation> updateOrganisation(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable String id,
      @RequestParam String nom,
      @RequestParam String description,
      @RequestParam String localisation,
      @RequestParam(required = false) String telephone,
      @RequestParam(required = false) String siteWeb,
      @RequestParam(required = false) String email,
      @RequestParam Boolean statutVerifie,
      @RequestParam(required = false) MultipartFile logo
  ) throws IOException {

    Optional<Organisation> opt = organisationRepository.findById(id);
    if (opt.isEmpty()) return ResponseEntity.notFound().build();

    // For ORG users, ensure they can only update their own organization
    String userOrgId = extractOrgIdFromJwt(jwt);
    if (jwt != null && userOrgId != null && !userOrgId.isEmpty()) {
      Object roles = jwt.getClaims().get("roles");
      if (roles instanceof java.util.List) {
        @SuppressWarnings("unchecked")
        java.util.List<String> roleList = (java.util.List<String>) roles;
        if ((roleList.contains("ORG") || roleList.contains("ROLE_ORG") || 
             roleList.contains("ORGANISATEUR") || roleList.contains("ROLE_ORGANISATEUR")) &&
            !userOrgId.equals(id)) {
          // ORG user trying to update another organization - forbidden
          return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN).build();
        }
      }
    }

    Organisation org = opt.get();
    org.setNom(nom);
    org.setDescription(description);
    org.setLocalisation(localisation);
    org.setTelephone(telephone);
    org.setSiteWeb(siteWeb);
    org.setEmail(email);
    org.setStatutVerifie(Boolean.TRUE.equals(statutVerifie));

    // si un nouveau logo est fourni, on le remplace
    if (logo != null && !logo.isEmpty()) {
      String saved = saveLogoFile(logo);
      if (saved != null) org.setLogo(saved);
    }

    org.setUpdatedAt(LocalDateTime.now());
    return ResponseEntity.ok(organisationRepository.save(org));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> deleteOrganisation(@PathVariable String id) {
    if (!organisationRepository.existsById(id)) return ResponseEntity.notFound().build();
    organisationRepository.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get current user's organization (for ORG users)
   */
  @GetMapping("/me")
  @PreAuthorize("hasAnyRole('ORG', 'ADMIN')")
  public ResponseEntity<Organisation> getMyOrganisation(
      @AuthenticationPrincipal Jwt jwt) {
    if (jwt == null) {
      return ResponseEntity.status(401).build();
    }

    String orgId = (String) jwt.getClaims().getOrDefault("orgId", "");
    if (orgId == null || orgId.isEmpty()) {
      return ResponseEntity.status(404).build();
    }

    return organisationRepository.findById(orgId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Update current user's organization (for ORG users only - they can only update their own org)
   */
  @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasAnyRole('ORG', 'ADMIN')")
  public ResponseEntity<Organisation> updateMyOrganisation(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(required = false) String nom,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String localisation,
      @RequestParam(required = false) String telephone,
      @RequestParam(required = false) String siteWeb,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) MultipartFile logo
  ) throws IOException {
    if (jwt == null) {
      return ResponseEntity.status(401).build();
    }

    String orgId = (String) jwt.getClaims().getOrDefault("orgId", "");
    if (orgId == null || orgId.isEmpty()) {
      return ResponseEntity.status(404).build();
    }

    Optional<Organisation> opt = organisationRepository.findById(orgId);
    if (opt.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    Organisation org = opt.get();
    
    // Update only provided fields
    if (nom != null && !nom.isEmpty()) org.setNom(nom);
    if (description != null) org.setDescription(description);
    if (localisation != null) org.setLocalisation(localisation);
    if (telephone != null) org.setTelephone(telephone);
    if (siteWeb != null) org.setSiteWeb(siteWeb);
    if (email != null) org.setEmail(email);

    // Update logo if provided
    if (logo != null && !logo.isEmpty()) {
      String saved = saveLogoFile(logo);
      if (saved != null) org.setLogo(saved);
    }

    org.setUpdatedAt(LocalDateTime.now());
    return ResponseEntity.ok(organisationRepository.save(org));
  }
}
