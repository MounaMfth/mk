package iscae.mr.app_donation.Piecejointe.services;

import iscae.mr.app_donation.Piecejointe.dtos.PiecejointeDTO;
import iscae.mr.app_donation.dao.entities.PieceJointe;
import iscae.mr.app_donation.dao.repositories.PieceJointeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PiecejointeService {

    @Autowired
    private PieceJointeRepository pieceJointeRepository;

    @Value("${app.upload.dir:uploads}")
    private String baseUploadDir;

    @Value("${app.base.url:http://localhost:8080}")
    private String baseUrl;

    // Types de fichiers autorisés
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = Arrays.asList(
        "application/pdf", "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    // Tailles maximales
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final long MAX_DOCUMENT_SIZE = 10 * 1024 * 1024; // 10MB

    // ✅ CREATE (version existante)
    public PiecejointeDTO createPieceJointe(PiecejointeDTO dto) {
        PieceJointe pieceJointe = new PieceJointe();
        pieceJointe.setNomFichier(dto.getNomFichier());
        pieceJointe.setTypeFichier(dto.getTypeFichier());
        pieceJointe.setUrl(dto.getUrl());
        pieceJointe.setDateAjout(dto.getDateAjout());
        pieceJointe.setDonId(dto.getDonId());
        pieceJointe.setActiviteId(dto.getActiviteId());
        pieceJointe.setUtilisateurId(dto.getUtilisateurId());

        PieceJointe saved = pieceJointeRepository.save(pieceJointe);
        return mapToDTO(saved);
    }

    // ✅ NOUVELLE MÉTHODE : Upload avec fichier MultipartFile
    public PiecejointeDTO uploadPieceJointe(
            MultipartFile file,
            String typeFichier,
            String donId,
            String activiteId,
            String utilisateurId,
            String organisationId) throws IOException {

        // Validation du fichier
        validerFichier(file, typeFichier);

        // Créer le répertoire spécifique au type
        String sousRepertoire = getSousRepertoire(typeFichier);
        Path uploadPath = Paths.get(baseUploadDir, sousRepertoire);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Générer un nom de fichier unique
        String nomFichierUnique = genererNomFichierUnique(file.getOriginalFilename());
        Path cheminFichier = uploadPath.resolve(nomFichierUnique);

        // Sauvegarder le fichier physiquement
        Files.copy(file.getInputStream(), cheminFichier, StandardCopyOption.REPLACE_EXISTING);
        
        // Créer l'URL d'accès
        String urlAcces = construireUrl(sousRepertoire, nomFichierUnique);

        // Créer l'entité PieceJointe
        PieceJointe pieceJointe = new PieceJointe();
        pieceJointe.setNomFichier(file.getOriginalFilename());
        pieceJointe.setTypeFichier(typeFichier);
        pieceJointe.setUrl(urlAcces);
        pieceJointe.setDateAjout(LocalDateTime.now());
        pieceJointe.setDonId(donId);
        pieceJointe.setActiviteId(activiteId);
        pieceJointe.setUtilisateurId(utilisateurId);
        
        // Ajouter le champ organisation si nécessaire (vous devrez ajouter ce champ à votre entité)
        // pieceJointe.setOrganisationId(organisationId);

        PieceJointe saved = pieceJointeRepository.save(pieceJointe);
        return mapToDTO(saved);
    }


    // ✅ UPDATE (version existante)
    public PiecejointeDTO updatePieceJointe(String id, PiecejointeDTO dto) {
        PieceJointe pieceJointe = pieceJointeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pièce jointe introuvable"));

        pieceJointe.setNomFichier(dto.getNomFichier());
        pieceJointe.setTypeFichier(dto.getTypeFichier());
        pieceJointe.setUrl(dto.getUrl());
        pieceJointe.setDateAjout(dto.getDateAjout());
        pieceJointe.setDonId(dto.getDonId());
        pieceJointe.setActiviteId(dto.getActiviteId());
        pieceJointe.setUtilisateurId(dto.getUtilisateurId());

        PieceJointe updated = pieceJointeRepository.save(pieceJointe);
        return mapToDTO(updated);
    }

    // ✅ DELETE amélioré avec suppression du fichier physique
    public void deletePieceJointe(String id) {
        PieceJointe pieceJointe = pieceJointeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pièce jointe introuvable"));
        
        // Supprimer le fichier physique
        supprimerFichierPhysique(pieceJointe.getUrl());
        
        // Supprimer de la base
        pieceJointeRepository.deleteById(id);
    }

    // ✅ GET BY ID (version existante)
    public PiecejointeDTO getPieceJointeById(String id) {
        PieceJointe pieceJointe = pieceJointeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pièce jointe introuvable"));
        return mapToDTO(pieceJointe);
    }

    // ✅ GET ALL (version existante)
    public List<PiecejointeDTO> getAllPieceJointes() {
        return pieceJointeRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ✅ NOUVELLES MÉTHODES utilitaires

    public List<PiecejointeDTO> getPieceJointesByType(String typeFichier) {
        return pieceJointeRepository.findAll().stream()
            .filter(pj -> typeFichier.equals(pj.getTypeFichier()))
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    public List<PiecejointeDTO> getPieceJointesByUtilisateur(String utilisateurId) {
        return pieceJointeRepository.findAll().stream()
            .filter(pj -> utilisateurId.equals(pj.getUtilisateurId()))
            .map(this::mapToDTO)
            .collect(Collectors.toList());
    }

    // ===== MÉTHODES PRIVÉES =====

    private void validerFichier(MultipartFile file, String typeFichier) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("Le fichier est vide");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IOException("Type de contenu indéterminé");
        }

        // Validation selon le type
        switch (typeFichier.toUpperCase()) {
            case "LOGO":
            case "PHOTO":
            case "IMAGE":
                if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
                    throw new IOException("Type d'image non autorisé: " + contentType);
                }
                if (file.getSize() > MAX_IMAGE_SIZE) {
                    throw new IOException("Image trop volumineuse (max 5MB)");
                }
                break;
                
            case "DOCUMENT":
            case "PDF":
                if (!ALLOWED_IMAGE_TYPES.contains(contentType) && !ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
                    throw new IOException("Type de document non autorisé: " + contentType);
                }
                if (file.getSize() > MAX_DOCUMENT_SIZE) {
                    throw new IOException("Document trop volumineux (max 10MB)");
                }
                break;
        }
    }

    private String getSousRepertoire(String typeFichier) {
        switch (typeFichier.toUpperCase()) {
            case "LOGO": return "logos";
            case "PHOTO": return "photos";
            case "IMAGE": return "images";
            case "DOCUMENT": return "documents";
            case "PDF": return "documents";
            default: return "autres";
        }
    }

    private String genererNomFichierUnique(String nomOriginal) {
        String extension = "";
        if (nomOriginal != null && nomOriginal.contains(".")) {
            extension = nomOriginal.substring(nomOriginal.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }

    private String construireUrl(String sousRepertoire, String nomFichier) {
        return baseUrl + "/uploads/" + sousRepertoire + "/" + nomFichier;
    }

    private void supprimerFichierPhysique(String url) {
        try {
            if (url != null && url.startsWith(baseUrl)) {
                // Extraire le chemin relatif de l'URL
                String cheminRelatif = url.substring((baseUrl + "/uploads/").length());
                Path cheminFichier = Paths.get(baseUploadDir, cheminRelatif);
                Files.deleteIfExists(cheminFichier);
                log.info("Fichier supprimé: {}", cheminFichier);
            }
        } catch (IOException e) {
            log.error("Erreur lors de la suppression du fichier: {}", e.getMessage());
        }
    }

    // ✅ Mapper (version existante)
    private PiecejointeDTO mapToDTO(PieceJointe pieceJointe) {
        return new PiecejointeDTO(
                pieceJointe.getId(),
                pieceJointe.getNomFichier(),
                pieceJointe.getTypeFichier(),
                pieceJointe.getUrl(),
                pieceJointe.getDateAjout(),
                pieceJointe.getDonId(),
                pieceJointe.getActiviteId(),
                pieceJointe.getUtilisateurId()
        );
    }
    // ✅ Upload d’un logo pour une organisation
public PiecejointeDTO uploadLogoOrganisation(MultipartFile file, String organisationId, String utilisateurId) throws IOException {
    // Supprimer l’ancien logo de cette organisation
    supprimerLogosOrganisation(organisationId);

    // Upload du nouveau logo
    return uploadPieceJointe(file, "LOGO", null, null, utilisateurId, organisationId);
}

// ✅ Récupérer le logo d’une organisation
public PiecejointeDTO getLogoOrganisation(String organisationId) {
    return pieceJointeRepository.findFirstByOrganisationIdAndTypeFichier(organisationId, "LOGO")
            .map(this::mapToDTO)
            .orElse(null);
}

// ✅ Supprimer tous les logos d’une organisation
public void supprimerLogosOrganisation(String organisationId) {
    List<PieceJointe> logos = pieceJointeRepository.findByOrganisationIdAndTypeFichier(organisationId, "LOGO");
    for (PieceJointe logo : logos) {
        try {
            // Supprimer le fichier physique associé
            supprimerFichierPhysique(logo.getUrl());

            // Supprimer en base
            pieceJointeRepository.delete(logo);
        } catch (Exception e) {
            log.error("Erreur lors de la suppression du logo [{}]: {}", logo.getId(), e.getMessage());
        }
    }
}

}