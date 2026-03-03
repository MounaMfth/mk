package iscae.mr.app_donation.dao.repositories;

import iscae.mr.app_donation.dao.entities.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganisationRepository extends JpaRepository<Organisation, String> {
    List<Organisation> findByNom(String nom);
    List<Organisation> findByLocalisation(String localisation);
}
