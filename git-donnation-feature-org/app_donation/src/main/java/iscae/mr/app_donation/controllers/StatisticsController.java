package iscae.mr.app_donation.controllers;

import iscae.mr.app_donation.dao.repositories.DonRepository;
import iscae.mr.app_donation.dao.repositories.DonFinancierRepository;
import iscae.mr.app_donation.dao.repositories.ProjetRepository;
import iscae.mr.app_donation.dao.repositories.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatisticsController {

    @Autowired
    private ProjetRepository projetRepository;

    @Autowired
    private DonRepository donRepository;

    @Autowired
    private DonFinancierRepository donFinancierRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/impact")
    public ResponseEntity<Map<String, Long>> getImpactStats() {
        Map<String, Long> stats = new HashMap<>();

        // ✅ Projets réalisés : projets dont le budget est atteint
        long realisedProjects = projetRepository.countByBudgetAtteintTrue();

        // ✅ Total de projets actifs (utile si on veut afficher plus tard)
        long activeProjects = projetRepository.findAllActifs().size();

        // ✅ Nombre total de dons enregistrés (tous types confondus)
        long donationsCount = donRepository.count();

        // ✅ Donateurs actifs : nombre d'emails distincts ayant fait au moins un don financier
        long activeDonors = donFinancierRepository.countDistinctByEmailIsNotNull();

        // (Optionnel) Taille de la communauté enregistrée (tous les utilisateurs)
        long usersCount = utilisateurRepository.count();

        // Map existing frontend fields to meaningful metrics
        stats.put("projectsCount", realisedProjects);     // Projets réalisés
        stats.put("donationsCount", donationsCount);      // Nombre total de dons
        stats.put("communityCount", activeDonors);        // Donateurs actifs

        // Estimation simplifiée des vies impactées
        long livesImpacted = (realisedProjects * 50) + (donationsCount * 2);
        stats.put("livesImpacted", livesImpacted);

        return ResponseEntity.ok(stats);
    }
}
