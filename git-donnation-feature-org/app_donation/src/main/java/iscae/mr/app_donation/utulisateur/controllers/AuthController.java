package iscae.mr.app_donation.utulisateur.controllers;

import iscae.mr.app_donation.utulisateur.JwtUtil;
import iscae.mr.app_donation.dao.entities.Utilisateur;
import iscae.mr.app_donation.utulisateur.services.UtilisateurService;
import iscae.mr.app_donation.utulisateur.dtos.AuthRequestDTO;
import iscae.mr.app_donation.utulisateur.dtos.UpdateProfileDTO;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.*;
import java.nio.file.*;
import java.io.IOException;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "http://localhost:4200" }, allowedHeaders = { "*" }, methods = { RequestMethod.GET,
    RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE,
    RequestMethod.OPTIONS }, allowCredentials = "true", maxAge = 3600)
public class AuthController {

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UtilisateurService utilisateurService;

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody AuthRequestDTO request,
      HttpServletResponse response) {
    Map<String, Object> responseMap = new HashMap<>();
    try {
      System.out.println("📝 [AuthController] Tentative de login: " + request.getUsername());

      Utilisateur user = utilisateurService.getUserByUsername(request.getUsername());
      if (user == null) {
        System.err.println("❌ [AuthController] Utilisateur non trouvé");
        return ResponseEntity.status(401).body(Map.of("error", "Utilisateur ou mot de passe incorrect"));
      }

      System.out
          .println("✅ [AuthController] Utilisateur trouvé: " + user.getUsername() + " (ID: " + user.getId() + ")");

      boolean passwordValid = utilisateurService.verifyPassword(request.getPassword(), user.getPassword());
      if (!passwordValid) {
        System.err.println("❌ [AuthController] Mot de passe incorrect");
        return ResponseEntity.status(401).body(Map.of("error", "Utilisateur ou mot de passe incorrect"));
      }

      System.out.println("✅ [AuthController] Mot de passe valide");

      List<String> roles = new ArrayList<>(user.getRoles() != null ? user.getRoles() : new HashSet<>());
      System.out.println("🔑 [AuthController] Rôles: " + roles);

      // ✅ PARTIE CRITIQUE : RÉCUPÉRER L'ORGANISATION
      String orgId = null;
      String orgNom = null;

      try {
        System.out.println("🔍 [AuthController] Vérification de l'organisation...");

        // ✅ Charger explicitement l'organisation pour éviter
        // LazyInitializationException
        if (user.getOrganisation() != null) {
          // Accéder aux propriétés dans le même contexte de transaction
          orgId = user.getOrganisation().getId();
          orgNom = user.getOrganisation().getNom();

          System.out.println("✅ [AuthController] Organisation trouvée:");
          System.out.println("   - ID: " + orgId);
          System.out.println("   - Nom: " + orgNom);
        } else {
          System.out.println("⚠️ [AuthController] PAS D'ORGANISATION pour " + user.getUsername());
        }
      } catch (Exception orgError) {
        System.err.println("⚠️ [AuthController] Erreur accès organisation: " + orgError.getMessage());
        orgError.printStackTrace();
        // Continuer sans organisation
      }

      // ✅ CRÉER LES CLAIMS
      Map<String, Object> claims = new HashMap<>();
      claims.put("roles", roles);
      claims.put("profil", user.getProfil() != null ? user.getProfil() : "");
      claims.put("orgId", orgId != null ? orgId : "");
      claims.put("orgNom", orgNom != null ? orgNom : "");
      claims.put("userId", user.getId());

      System.out.println("🔑 [AuthController] Claims générés:");
      System.out.println("   - orgId: " + (orgId != null ? orgId : "NULL"));
      System.out.println("   - orgNom: " + (orgNom != null ? orgNom : "NULL"));
      System.out.println("   - userId: " + user.getId());

      try {
        String accessToken = jwtUtil.generateAccessTokenWithClaims(user, claims);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        System.out.println("✅ [AuthController] Tokens générés avec succès");

        addCookie(response, "access_token", accessToken);
        addCookie(response, "refresh_token", refreshToken);

        responseMap.put("token", accessToken);
        responseMap.put("refreshToken", refreshToken);
        responseMap.put("username", request.getUsername());
        responseMap.put("roles", roles);
        responseMap.put("profil", user.getProfil());
        responseMap.put("email", user.getEmail());
        responseMap.put("prenom", user.getPrenom());
        responseMap.put("nom", user.getNom());
        responseMap.put("telephone", user.getTelephone());
        responseMap.put("adresse", user.getAdresse());

        if (orgId != null && !orgId.isEmpty()) {
          responseMap.put("organisationId", orgId);
          responseMap.put("organisationNom", orgNom != null ? orgNom : "");
        } else {
          responseMap.put("organisationId", null);
          responseMap.put("organisationNom", null);
        }

        System.out.println("✅ [AuthController] Login réussi pour: " + request.getUsername());
        return ResponseEntity.ok(responseMap);

      } catch (Exception tokenError) {
        System.err.println("❌ [AuthController] Erreur génération token: " + tokenError.getMessage());
        tokenError.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", "Erreur token: " + tokenError.getMessage()));
      }

    } catch (Exception e) {
      System.err.println("❌ [AuthController] Erreur login: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(500).body(Map.of("error", "Erreur: " + e.getMessage()));
    }
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<Map<String, String>> refreshToken(
      @RequestHeader(value = "Authorization", required = false) String authHeader,
      @CookieValue(value = "refresh_token", required = false) String cookieRefresh,
      HttpServletResponse response) {
    String rt = (cookieRefresh != null && !cookieRefresh.isBlank())
        ? cookieRefresh
        : (authHeader != null && authHeader.toLowerCase().startsWith("bearer ") ? authHeader.substring(7).trim()
            : null);

    if (rt == null || !jwtUtil.isRefreshTokenValid(rt)) {
      return ResponseEntity.status(403).body(Map.of("error", "Invalid or expired refresh token"));
    }

    String username = jwtUtil.extractUsername(rt);
    Utilisateur user = utilisateurService.getUserByUsername(username);

    List<String> roles = new ArrayList<>(user.getRoles() != null ? user.getRoles() : new HashSet<>());

    // ✅ CORRECTION: Même logique pour le refresh
    String orgId = user.getOrganisation() != null ? user.getOrganisation().getId() : null;
    String orgNom = user.getOrganisation() != null ? user.getOrganisation().getNom() : null;

    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", roles);
    claims.put("profil", user.getProfil() != null ? user.getProfil() : "");
    claims.put("orgId", orgId);
    claims.put("orgNom", orgNom);
    claims.put("userId", user.getId());

    String newAccessToken = jwtUtil.generateAccessTokenWithClaims(user, claims);

    ResponseCookie cookie = ResponseCookie.from("refresh_token", rt)
        .httpOnly(true)
        .secure(false)
        .sameSite("Lax")
        .path("/")
        .maxAge(60 * 60 * 24 * 7)
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

    return ResponseEntity.ok(Map.of("access_token", newAccessToken));
  }

  @PostMapping(value = { "/register", "/inscription" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> registerUser(
      @ModelAttribute AuthRequestDTO request,
      @RequestParam(value = "logoFile", required = false) MultipartFile logoFile) {
    try {
      System.out.println("📝 [AuthController] Demande d'inscription (Multipart):");
      System.out.println("  - Username: " + request.getUsername());
      System.out.println("  - Profil: " + request.getProfil());

      // ✅ Vérifier les champs obligatoires (Personal)
      if (request.getUsername() == null || request.getPassword() == null ||
          request.getEmail() == null || request.getAdresse() == null ||
          request.getTelephone() == null) {
        return ResponseEntity.badRequest().body(Map.of("error", "Tous les champs personnels sont obligatoires"));
      }

      // ✅ SAVING LOGO IF PROVIDED
      String logoUrl = null;
      if (logoFile != null && !logoFile.isEmpty()) {
        try {
          Path root = Paths.get("uploads/orgs").toAbsolutePath().normalize();
          Files.createDirectories(root);
          String ext = "";
          String original = logoFile.getOriginalFilename();
          if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
          }
          String filename = "org-" + System.currentTimeMillis() + ext;
          Files.copy(logoFile.getInputStream(), root.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
          logoUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
              .path("/uploads/orgs/")
              .path(filename)
              .toUriString();
          System.out.println("✅ [AuthController] Logo sauvegardé: " + logoUrl);
        } catch (IOException e) {
          System.err.println("⚠️ [AuthController] Erreur logo: " + e.getMessage());
        }
      }

      // ✅ CAPTURER LE PROFIL
      String profil = request.getProfil() != null ? request.getProfil() : "DONATEUR";

      // ✅ CRÉER L'UTILISATEUR
      Utilisateur user = utilisateurService.registerUser(
          request.getUsername(),
          request.getPassword(),
          request.getEmail(),
          request.getAdresse(),
          request.getTelephone(),
          profil,
          request.getPrenom(),
          request.getNom());

      System.out.println("✅ [AuthController] Utilisateur créé avec profil " + profil + ": " +
          user.getUsername() + " (ID: " + user.getId() + ")");

      // ✅ SI C'EST UN ORGANISATEUR, CRÉER L'ORGANISATION
      if ("ORGANISATEUR".equalsIgnoreCase(profil) || "ORG".equalsIgnoreCase(profil)) {
        if (request.getOrganisationNom() != null && !request.getOrganisationNom().isBlank()) {
          System.out.println("🏢 [AuthController] Création de l'organisation: " + request.getOrganisationNom());

          iscae.mr.app_donation.dao.entities.Organisation org = utilisateurService.createOrganisationComplete(
              request.getOrganisationNom(),
              request.getDescription(),
              request.getLocalisation(),
              request.getOrgEmail(),
              request.getOrgTelephone(),
              request.getSiteWeb(),
              logoUrl);

          utilisateurService.assignUserToOrganisation(user.getId(), org.getId());
          System.out.println("✅ [AuthController] Organisation liée à l'utilisateur");
        }
      }

      return ResponseEntity.ok(Map.of(
          "message", "Utilisateur enregistré avec succès",
          "id", user.getId(),
          "username", user.getUsername(),
          "profil", profil));

    } catch (Exception e) {
      System.err.println("❌ [AuthController] Erreur inscription: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.status(400)
          .body(Map.of("error", e.getMessage()));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
    deleteCookie(response, "access_token");
    deleteCookie(response, "refresh_token");
    return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
  }

  @GetMapping("/me")
  public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal Jwt jwt) {
    if (jwt == null)
      return ResponseEntity.status(401).build();

    String username = (String) jwt.getClaims().getOrDefault("sub", "");
    Utilisateur user = utilisateurService.getUserByUsername(username);

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("username", username);
    out.put("orgId", jwt.getClaims().get("orgId"));
    out.put("orgNom", jwt.getClaims().get("orgNom"));
    out.put("userId", jwt.getClaims().get("userId"));
    out.put("roles", jwt.getClaims().get("roles"));

    if (user != null) {
      out.put("email", user.getEmail());
      out.put("adresse", user.getAdresse());
      out.put("telephone", user.getTelephone());
      out.put("profil", user.getProfil());
      out.put("prenom", user.getPrenom());
      out.put("nom", user.getNom());
    }

    return ResponseEntity.ok(out);
  }

  @PutMapping("/me")
  public ResponseEntity<Map<String, Object>> updateMe(
      @AuthenticationPrincipal Jwt jwt,
      @RequestBody UpdateProfileDTO req) {
    if (jwt == null)
      return ResponseEntity.status(401).build();

    String username = (String) jwt.getClaims().getOrDefault("sub", "");
    Utilisateur user = utilisateurService.getUserByUsername(username);
    if (user == null)
      return ResponseEntity.status(404).body(Map.of("error", "Utilisateur introuvable"));

    if (req.getEmail() != null)
      user.setEmail(req.getEmail().trim().isEmpty() ? null : req.getEmail().trim());
    if (req.getAdresse() != null)
      user.setAdresse(req.getAdresse().trim().isEmpty() ? null : req.getAdresse().trim());
    if (req.getTelephone() != null)
      user.setTelephone(req.getTelephone().trim().isEmpty() ? null : req.getTelephone().trim());
    if (req.getProfil() != null)
      user.setProfil(req.getProfil().trim().isEmpty() ? null : req.getProfil().trim());
    if (req.getPrenom() != null)
      user.setPrenom(req.getPrenom().trim().isEmpty() ? null : req.getPrenom().trim());
    if (req.getNom() != null)
      user.setNom(req.getNom().trim().isEmpty() ? null : req.getNom().trim());

    utilisateurService.updateUser(user);

    Map<String, Object> out = new LinkedHashMap<>();
    out.put("username", username);
    out.put("orgId", jwt.getClaims().get("orgId"));
    out.put("orgNom", jwt.getClaims().get("orgNom"));
    out.put("userId", jwt.getClaims().get("userId"));
    out.put("roles", jwt.getClaims().get("roles"));
    out.put("email", user.getEmail());
    out.put("adresse", user.getAdresse());
    out.put("telephone", user.getTelephone());
    out.put("profil", user.getProfil());
    out.put("prenom", user.getPrenom());
    out.put("nom", user.getNom());
    return ResponseEntity.ok(out);
  }

  @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, Object>> uploadPhoto(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam("file") MultipartFile file) {
    if (jwt == null)
      return ResponseEntity.status(401).build();

    try {
      if (file == null || file.isEmpty()) {
        return ResponseEntity.badRequest().body(Map.of("error", "Fichier manquant"));
      }
      String ct = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
      if (!ct.startsWith("image/")) {
        return ResponseEntity.badRequest().body(Map.of("error", "Format non supporté"));
      }
      long MAX = 5L * 1024 * 1024;
      if (file.getSize() > MAX) {
        return ResponseEntity.badRequest().body(Map.of("error", "Image trop volumineuse (>5Mo)"));
      }

      String username = (String) jwt.getClaims().getOrDefault("sub", "");
      Utilisateur user = utilisateurService.getUserByUsername(username);
      if (user == null)
        return ResponseEntity.status(404).body(Map.of("error", "Utilisateur introuvable"));

      Path root = Paths.get("uploads/users").toAbsolutePath().normalize();
      Files.createDirectories(root);

      String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar";
      String ext = "";
      int dot = original.lastIndexOf('.');
      if (dot >= 0)
        ext = original.substring(dot).replaceAll("[^a-zA-Z0-9.]", "");
      if (ext.length() > 10)
        ext = ext.substring(ext.length() - 10);

      String cleanUser = username.replaceAll("[^a-zA-Z0-9_-]", "_");
      String filename = cleanUser + "-" + System.currentTimeMillis() + ext;

      Path dest = root.resolve(filename);
      Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

      String publicUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
          .path("/uploads/users/")
          .path(filename)
          .toUriString();

      user.setProfil(publicUrl);
      utilisateurService.updateUser(user);

      Map<String, Object> out = new LinkedHashMap<>();
      out.put("username", username);
      out.put("orgId", jwt.getClaims().get("orgId"));
      out.put("orgNom", jwt.getClaims().get("orgNom"));
      out.put("userId", jwt.getClaims().get("userId"));
      out.put("roles", jwt.getClaims().get("roles"));
      out.put("email", user.getEmail());
      out.put("adresse", user.getAdresse());
      out.put("telephone", user.getTelephone());
      out.put("profil", user.getProfil());

      return ResponseEntity.ok(out);

    } catch (IOException ex) {
      return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de l'enregistrement du fichier"));
    }
  }

  private void addCookie(HttpServletResponse response, String name, String value) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(60 * 60 * 24);
    response.addCookie(cookie);
  }

  private void deleteCookie(HttpServletResponse response, String name) {
    Cookie cookie = new Cookie(name, null);
    cookie.setHttpOnly(true);
    cookie.setSecure(false);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }
}