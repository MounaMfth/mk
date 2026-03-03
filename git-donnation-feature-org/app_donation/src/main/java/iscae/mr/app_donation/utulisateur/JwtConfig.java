package iscae.mr.app_donation.utulisateur;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig implements AuthenticationEntryPoint {

    private String privateKeyPath;
    private String publicKeyPath;
    private String passphrase;

    // ✅ Garde toutes tes méthodes existantes
    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getPublicKeyPath() {
        return publicKeyPath;
    }

    public void setPublicKeyPath(String publicKeyPath) {
        this.publicKeyPath = publicKeyPath;
    }

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    // ✅ Ajoute juste cette méthode pour AuthenticationEntryPoint
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
