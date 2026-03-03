package iscae.mr.app_donation.Piecejointe.controllers;

import iscae.mr.app_donation.Piecejointe.dtos.PiecejointeDTO;
import iscae.mr.app_donation.Piecejointe.services.PiecejointeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/piecesjointes")
@CrossOrigin(origins = "http://localhost:4200")
@Slf4j
public class PiecejointeController {

    @Autowired
    private PiecejointeService piecejointeService;

    // ✅ CREATE (version JSON existante)
    @PostMapping
    public PiecejointeDTO create(@RequestBody PiecejointeDTO dto) {
        return piecejointeService.createPieceJointe(dto);
    }

    // ✅ NOUVELLE ROUTE : Upload de fichier multipart
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("typeFichier") String typeFichier,
            @RequestParam(value = "donId", required = false) String donId,
            @RequestParam(value = "activiteId", required = false) String activiteId,
            @RequestParam(value = "utilisateurId", required = false) String utilisateurId,
            @RequestParam(value = "organisationId", required = false) String organisationId) {
        
        try {
            PiecejointeDTO result = piecejointeService.uploadPieceJointe(
                file, typeFichier, donId, activiteId, utilisateurId, organisationId
            );
            return ResponseEntity.ok(result);
            
        } catch (IOException e) {
            log.error("Erreur lors de l'upload: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Erreur lors de l'upload: " + e.getMessage()));
        }
    }

    // ✅ ROUTE SPÉCIFIQUE : Upload logo d'organisation
    @PostMapping(value = "/logo-organisation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadLogoOrganisation(
            @RequestParam("file") MultipartFile file,
            @RequestParam("organisationId") String organisationId,
            @RequestParam(value = "utilisateurId", required = false) String utilisateurId) {
        
        try {
            PiecejointeDTO result = piecejointeService.uploadLogoOrganisation(
                file, organisationId, utilisateurId
            );
            return ResponseEntity.ok(result);
            
        } catch (IOException e) {
            log.error("Erreur lors de l'upload du logo: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ErrorResponse("Erreur lors de l'upload du logo: " + e.getMessage()));
        }
    }

    // ✅ ROUTE : Récupérer le logo d'une organisation
    @GetMapping("/logo-organisation/{organisationId}")
    public ResponseEntity<PiecejointeDTO> getLogoOrganisation(@PathVariable String organisationId) {
        PiecejointeDTO logo = piecejointeService.getLogoOrganisation(organisationId);
        
        if (logo != null) {
            return ResponseEntity.ok(logo);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ ROUTE : Télécharger/Afficher un fichier
    @GetMapping("/fichier/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String id) {
        try {
            PiecejointeDTO pieceJointe = piecejointeService.getPieceJointeById(id);
            
            if (pieceJointe == null) {
                return ResponseEntity.notFound().build();
            }

            // Construire le chemin du fichier à partir de l'URL
            String url = pieceJointe.getUrl();
            String fileName = url.substring(url.lastIndexOf("/") + 1);
            
            // Déterminer le sous-répertoire basé sur le type
            String sousRepertoire = getSousRepertoireFromType(pieceJointe.getTypeFichier());
            Path filePath = Paths.get("uploads", sousRepertoire, fileName);
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                // Déterminer le type de contenu
                String contentType = determineContentType(pieceJointe.getNomFichier());
                
                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "inline; filename=\"" + pieceJointe.getNomFichier() + "\"")
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("Erreur lors du téléchargement: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ ROUTE : Récupérer par type
    @GetMapping("/type/{typeFichier}")
    public List<PiecejointeDTO> getByType(@PathVariable String typeFichier) {
        return piecejointeService.getPieceJointesByType(typeFichier);
    }

    // ✅ ROUTE : Récupérer par utilisateur
    @GetMapping("/utilisateur/{utilisateurId}")
    public List<PiecejointeDTO> getByUtilisateur(@PathVariable String utilisateurId) {
        return piecejointeService.getPieceJointesByUtilisateur(utilisateurId);
    }

    // ✅ UPDATE (version existante)
    @PutMapping("/{id}")
    public PiecejointeDTO update(@PathVariable String id, @RequestBody PiecejointeDTO dto) {
        return piecejointeService.updatePieceJointe(id, dto);
    }

    // ✅ DELETE (version existante améliorée)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        try {
            piecejointeService.deletePieceJointe(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Erreur lors de la suppression: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ GET BY ID (version existante)
    @GetMapping("/{id}")
    public PiecejointeDTO getById(@PathVariable String id) {
        return piecejointeService.getPieceJointeById(id);
    }

    // ✅ GET ALL (version existante)
    @GetMapping
    public List<PiecejointeDTO> getAll() {
        return piecejointeService.getAllPieceJointes();
    }

    // ===== MÉTHODES UTILITAIRES =====

    private String getSousRepertoireFromType(String typeFichier) {
        switch (typeFichier.toUpperCase()) {
            case "LOGO": return "logos";
            case "PHOTO": return "photos";
            case "IMAGE": return "images";
            case "DOCUMENT": return "documents";
            case "PDF": return "documents";
            default: return "autres";
        }
    }

    private String determineContentType(String fileName) {
        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.toLowerCase().endsWith(".png")) {
            return "image/png";
        } else if (fileName.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else if (fileName.toLowerCase().endsWith(".pdf")) {
            return "application/pdf";
        } else {
            return "application/octet-stream";
        }
    }

    // ===== CLASSES DE RÉPONSE =====

    public static class ErrorResponse {
        public final String message;
        public final long timestamp;

        public ErrorResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters pour Jackson
        public String getMessage() {
            return message;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }

    public static class SuccessResponse {
        public final String message;
        public final Object data;

        public SuccessResponse(String message, Object data) {
            this.message = message;
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public Object getData() {
            return data;
        }
    }
}