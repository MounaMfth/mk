package iscae.mr.app_donation.dao.repositories;

import iscae.mr.app_donation.dao.entities.Activite;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActiviteRepository extends JpaRepository<Activite, String> {

    // ✅ Récupérer les activités par organisation
    List<Activite> findByOrganisation_Id(String organisationId);

    // ✅ Toutes les activités avec organisation (évite N+1)
    @Query("""
        select a from Activite a
        join fetch a.organisation o
        order by a.dateDebut desc, a.titre asc
    """)
    List<Activite> findAllWithOrganisation();
    
    // ❌ SUPPRIMER - Activite n'a pas de relation 'user'
    // @Query("SELECT a FROM Activite a WHERE a.user.id = :userId")
    // List<Activite> findByUserId(@Param("userId") Long userId);
}