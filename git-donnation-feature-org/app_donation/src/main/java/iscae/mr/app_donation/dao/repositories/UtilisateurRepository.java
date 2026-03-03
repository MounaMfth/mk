package iscae.mr.app_donation.dao.repositories;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import iscae.mr.app_donation.dao.entities.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
     Optional<Utilisateur> findByUsername(String username);
     Optional<Utilisateur> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    java.util.List<Utilisateur> findByOrganisation_Id(String organisationId);
}
