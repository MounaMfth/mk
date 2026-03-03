package iscae.mr.app_donation.ai.config;

import iscae.mr.app_donation.Activite.dtos.ActiviteDTO;
import iscae.mr.app_donation.Activite.services.ActiviteService;
import iscae.mr.app_donation.Organisation.services.OrganisationService;
import iscae.mr.app_donation.dao.entities.Organisation;
import iscae.mr.app_donation.projet.Service.ProjetService;
import iscae.mr.app_donation.projet.dtos.ProjetDTO;
import iscae.mr.app_donation.dao.entities.DonFinancier;
import iscae.mr.app_donation.dao.repositories.DonFinancierRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.function.Function;

@Configuration
public class AiToolsConfig {

    public record NoArgs() {
    }

    @Bean
    @Description("Obtenir la liste de tous les projets de donation actifs")
    public Function<NoArgs, List<ProjetDTO>> getProjetsActifs(ProjetService projetService) {
        return request -> projetService.getProjetsActifs();
    }

    @Bean
    @Description("Obtenir la liste de toutes les organisations caritatives")
    public Function<NoArgs, List<Organisation>> getOrganisations(OrganisationService organisationService) {
        return request -> organisationService.getAllOrganisations();
    }

    @Bean
    @Description("Obtenir la liste des activités récentes et événements")
    public Function<NoArgs, List<ActiviteDTO>> getActivites(ActiviteService activiteService) {
        return request -> activiteService.listPublic();
    }

    @Bean
    @Description("Obtenir la liste des dons effectués par l'utilisateur actuellement connecté")
    public Function<NoArgs, List<DonFinancier>> getMesDons(DonFinancierRepository donFinancierRepository) {
        return request -> {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            return donFinancierRepository.findByEmail(email);
        };
    }

    public record ProjetIdArgs(String id) {
    }

    @Bean
    @Description("Obtenir les détails et la progression d'un projet spécifique par son ID")
    public Function<ProjetIdArgs, ProjetDTO> getProjetDetail(ProjetService projetService) {
        return request -> projetService.getProjetById(request.id());
    }
}
