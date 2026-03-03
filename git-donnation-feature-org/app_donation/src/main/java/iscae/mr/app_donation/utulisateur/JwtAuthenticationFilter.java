package iscae.mr.app_donation.utulisateur;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import iscae.mr.app_donation.dao.entities.Utilisateur;
import iscae.mr.app_donation.dao.repositories.UtilisateurRepository;  // Repository des utilisateurs

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;  // Utilitaire pour extraire les informations du JWT

    @Autowired
    private UtilisateurRepository userRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenFromRequest(request);

        if (token != null && jwtUtil.isTokenValid(token)) {
            String username = jwtUtil.extractUsername(token);

            // ⚡ Partie corrigée avec Optional
Optional<Utilisateur> utilisateurOpt = userRepository.findByUsername(username);            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(utilisateur, null, utilisateur.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();  // Retourner le token d'accès
                }
            }
        }
        return null;
    }
}
