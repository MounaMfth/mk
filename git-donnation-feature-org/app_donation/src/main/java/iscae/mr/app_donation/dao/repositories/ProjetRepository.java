package iscae.mr.app_donation.dao.repositories;

import iscae.mr.app_donation.dao.entities.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjetRepository extends JpaRepository<Projet, Long> {

    @Query("SELECT p FROM Projet p WHERE p.statut = 'ACTIF'")
    List<Projet> findAllActifs();

    List<Projet> findByTitreContainingIgnoreCase(String titre);

    // ✅ Find projects by organization ID
    List<Projet> findByOrganisation_Id(String organisationId);

    // ✅ Count projects whose budget objective has been reached
    long countByBudgetAtteintTrue();
}