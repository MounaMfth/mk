package iscae.mr.app_donation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class SecurityConfig {

  // === JWT -> Authorities: supporte "roles": ["ORGANISATION", ...] et "profil":
  // "ORGANISATION"
  @Bean
  public JwtAuthenticationConverter jwtAuthConverter() {
    var jac = new JwtAuthenticationConverter();
    jac.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
    return jac;
  }

  private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
    List<GrantedAuthority> out = new ArrayList<>();

    // 1) roles array (ex: ["ADMIN","ORGANISATION"])
    Object rolesClaim = jwt.getClaim("roles");
    if (rolesClaim instanceof Collection<?> coll) {
      out.addAll(coll.stream()
          .filter(Objects::nonNull)
          .map(Object::toString)
          .filter(s -> !s.isBlank())
          .map(s -> s.startsWith("ROLE_") ? s : "ROLE_" + s)
          .map(SimpleGrantedAuthority::new)
          .collect(Collectors.toList()));
    }

    // 2) profil string (ex: "ORGANISATION")
    Object profil = jwt.getClaim("profil");
    if (profil instanceof String s && !s.isBlank()) {
      String role = s.startsWith("ROLE_") ? s : "ROLE_" + s;
      out.add(new SimpleGrantedAuthority(role));
    }

    // 3) scopes (optionnel) — si jamais tu ajoutes "scope": "read write"
    Object scope = jwt.getClaim("scope");
    if (scope instanceof String scopes && !scopes.isBlank()) {
      for (String sc : scopes.split("\\s+")) {
        out.add(new SimpleGrantedAuthority("SCOPE_" + sc));
      }
    }

    return out;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationConverter jac) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .cors(c -> c.configurationSource(corsSource()))
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // ✅ OPTIONS et UPLOADS toujours autorisés
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            .requestMatchers("/uploads/**").permitAll()

            // ✅ Login et Register PUBLICS (changé de "/auth" à "/auth/**")
            .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
            .requestMatchers(HttpMethod.POST, "/auth/inscription").permitAll()

            // ✅ Projets et Activités PUBLICS en lecture
            .requestMatchers(HttpMethod.GET, "/api/projets/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/activites/public").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/organisations").permitAll()
            // ✅ Statistiques d'impact publiques
            .requestMatchers(HttpMethod.GET, "/api/stats/**").permitAll()
            // ✅ Secteurs PUBLIC (requis pour le formulaire d'inscription)
            .requestMatchers(HttpMethod.GET, "/api/secteurs").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/secteurs/**").permitAll()
            // ✅ AI assistant accessible to all authenticated roles (USER, ORG, ADMIN)
            .requestMatchers("/api/ai/**").hasAnyRole("USER", "ORG", "ADMIN")

            // ✅ Notifications - authentifiées
            .requestMatchers("/api/notifications/**").hasAnyRole("ADMIN", "ORG", "USER")

            // 🔐 Lecture des activités par organisation - authentifiée
            .requestMatchers(HttpMethod.GET, "/api/activites/**")
            .hasAnyRole("ADMIN", "ORG", "USER")

            // ✏️ Création/MAJ/Suppression d'activités - réservé aux ADMIN et ORGANISATION
            .requestMatchers(HttpMethod.POST, "/api/activites/**")
            .hasAnyRole("ADMIN", "ORG")
            .requestMatchers(HttpMethod.PUT, "/api/activites/**")
            .hasAnyRole("ADMIN", "ORG")
            .requestMatchers(HttpMethod.DELETE, "/api/activites/**")
            .hasAnyRole("ADMIN", "ORG")

            // 🔐 Validation endpoints
            .requestMatchers(HttpMethod.POST, "/api/validation/request")
            .hasAnyRole("USER", "ORG", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/validation/my-requests")
            .hasAnyRole("USER", "ORG", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/validation/requests")
            .hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/validation/approve/**")
            .hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/validation/reject/**")
            .hasRole("ADMIN")

            // 🛠️ Admin Management
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // 💰 Dons - ORG et ADMIN peuvent voir leurs dons
            .requestMatchers(HttpMethod.GET, "/api/dons-financiers/organisation/**")
            .hasAnyRole("ORG", "ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/dons-financiers")
            .hasAnyRole("USER", "ORG", "ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/dons-financiers/**")
            .hasAnyRole("USER", "ORG", "ADMIN")

            // 🔒 Tous les autres endpoints requièrent authentification
            .anyRequest().authenticated())
        // ✅ Utiliser le converter pour mapper claim "roles" → ROLE_*
        .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(jac)));

    return http.build();
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("/uploads/**");
  }

  @Bean
  public CorsConfigurationSource corsSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of("http://localhost:4200"));
    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    cfg.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-ORGANISATION-ID"));
    cfg.setExposedHeaders(List.of("Authorization"));
    cfg.setAllowCredentials(true);

    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }

  @Bean
  JwtDecoder jwtDecoder(
      @org.springframework.beans.factory.annotation.Value("${jwt.public-key-path}") String publicKeyPath) {
    try {
      String pem = Files.readString(Path.of(publicKeyPath));
      String b64 = pem.replace("-----BEGIN PUBLIC KEY-----", "")
          .replace("-----END PUBLIC KEY-----", "")
          .replaceAll("\\s", "");
      byte[] der = Base64.getDecoder().decode(b64);
      var kf = KeyFactory.getInstance("RSA");
      var pub = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(der));
      return NimbusJwtDecoder.withPublicKey(pub).build();
    } catch (Exception e) {
      throw new IllegalStateException("Impossible de charger la clé publique: " + publicKeyPath, e);
    }
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}