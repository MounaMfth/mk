// src/main/java/iscae/mr/app_donation/utulisateur/controllers/UtilisateurController.java
package iscae.mr.app_donation.utulisateur.controllers;

import iscae.mr.app_donation.dao.entities.Utilisateur;
import iscae.mr.app_donation.utulisateur.JwtUtil;
import iscae.mr.app_donation.utulisateur.dtos.LoginRequestDTO;
import iscae.mr.app_donation.utulisateur.dtos.RegisterRequest;
import iscae.mr.app_donation.utulisateur.dtos.UtilisateurDTO;
import iscae.mr.app_donation.utulisateur.services.UtilisateurService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth") // <-- aligne avec le front (environment.apiUrl + '/auth')
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class UtilisateurController {

  // private final JwtUtil jwtUtil;
  // private final UtilisateurService utilisateurService;

  // // ----------------------------
  // // Register
  // // ----------------------------
  // @PostMapping("/inscription")
  // public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
  //   try {
  //     // Map vers le DTO métier
  //     UtilisateurDTO dto = req.toUtilisateurDTO();
  //     UtilisateurDTO saved = utilisateurService.inscrire(dto);

  //     Map<String, Object> body = new HashMap<>();
  //     body.put("id", saved.getId());
  //     body.put("username", saved.getUsername());
  //     body.put("role", saved.getProfil());

  //     return ResponseEntity.status(HttpStatus.CREATED).body(body);
  //   } catch (Exception e) {
  //     return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
  //   }
  // }

  // // ----------------------------
  // // Login  (retourne { token, username, roles })
  // // ----------------------------
  // @PostMapping("/login")
  // public ResponseEntity<?> login(@RequestBody LoginRequestDTO req) {
  //   try {
  //     Map<String, String> tokens = utilisateurService.authenticateUser(
  //         req.getUsername(), req.getPassword()
  //     );

  //     Utilisateur user = utilisateurService.getUserByUsername(req.getUsername());

  //     Map<String, Object> body = new HashMap<>();
  //     // ⚠️ le front attend "token"
  //     body.put("token", tokens.getOrDefault("access_token", ""));
  //     body.put("refreshToken", tokens.getOrDefault("refresh_token", ""));
  //     body.put("username", user != null ? user.getUsername() : req.getUsername());
  //     body.put("roles",   user != null ? user.getProfil()   : "ORGANISATION");

  //     return ResponseEntity.ok(body);
  //   } catch (RuntimeException ex) {
  //     return ResponseEntity.status(401).body(Map.of("error", ex.getMessage()));
  //   }
  // }

  // // ----------------------------
  // // Me
  // // ----------------------------
  // @GetMapping("/me")
  // public ResponseEntity<?> me(@RequestHeader(name = "Authorization", required = false) String authHeader) {
  //   if (authHeader == null || !authHeader.startsWith("Bearer ")) {
  //     return ResponseEntity.status(401).body(Map.of("error", "Token manquant ou invalide"));
  //   }
  //   String token = authHeader.substring(7);
  //   if (!jwtUtil.isTokenValid(token)) {
  //     return ResponseEntity.status(401).body(Map.of("error", "Token expiré ou invalide"));
  //   }

  //   String username = jwtUtil.extractUsername(token);
  //   Utilisateur user = utilisateurService.getUserByUsername(username);
  //   if (user == null) {
  //     return ResponseEntity.status(404).body(Map.of("error", "Utilisateur non trouvé"));
  //   }

  //   return ResponseEntity.ok(Map.of(
  //       "username",  user.getUsername(),
  //       "roles",     user.getProfil(),
  //       "email",     user.getEmail(),
  //       "telephone", user.getTelephone(),
  //       "adresse",   user.getAdresse()
  //   ));
  // }
}
