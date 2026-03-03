package iscae.mr.app_donation.utulisateur;

import io.jsonwebtoken.*;
import iscae.mr.app_donation.dao.entities.Utilisateur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@Component
public class JwtUtil {

    private final JwtConfig jwtConfig;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Autowired
    public JwtUtil(JwtConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    @PostConstruct
    public void init() throws Exception {
        byte[] privateKeyBytes = Files.readAllBytes(Paths.get(jwtConfig.getPrivateKeyPath()));
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get(jwtConfig.getPublicKeyPath()));

        // Supprimer les headers PEM
        String privateKeyPEM = new String(privateKeyBytes)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        String publicKeyPEM = new String(publicKeyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        // Décoder Base64
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyPEM));
        X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyPEM));

        privateKey = keyFactory.generatePrivate(privateSpec);
        publicKey = keyFactory.generatePublic(publicSpec);

        System.out.println("RSA keys loaded successfully.");
    }

    // ---------------------
    // Génération tokens
    // ---------------------
    
    /**
     * Génère un access token SANS rôles (ancien format)
     */
    public String generateAccessToken(Utilisateur user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("profil", user.getProfil())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1h
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * ✅ Génère un access token AVEC rôles, profil et orgId
     */
    public String generateAccessTokenWithClaims(Utilisateur user, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        
        // S'assurer que les claims critiques sont présents
        if (!claims.containsKey("profil")) {
            claims.put("profil", user.getProfil() != null ? user.getProfil() : "");
        }
        if (!claims.containsKey("orgId")) {
            claims.put("orgId", "");
        }

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // 1h
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * Génère un refresh token (valide 7 jours)
     */
    public String generateRefreshToken(Utilisateur user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 7)) // 7 jours
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    // ---------------------
    // Validation token
    // ---------------------
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            System.out.println("Token invalide ou expiré: " + e.getMessage());
            return false;
        }
    }

    public boolean isRefreshTokenValid(String token) {
        return isTokenValid(token);
    }

    // ---------------------
    // Extraction données
    // ---------------------
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractProfil(String token) {
        return (String) Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("profil");
    }

    /**
     * ✅ Extrait les rôles du token
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object rolesClaim = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("roles");
        
        if (rolesClaim instanceof List) {
            return (List<String>) rolesClaim;
        }
        return Collections.emptyList();
    }

    /**
     * ✅ Extrait l'orgId du token
     */
    public String extractOrgId(String token) {
        return (String) Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("orgId");
    }
}   