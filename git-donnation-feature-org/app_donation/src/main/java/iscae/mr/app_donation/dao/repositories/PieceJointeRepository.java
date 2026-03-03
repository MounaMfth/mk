package iscae.mr.app_donation.dao.repositories;

import iscae.mr.app_donation.dao.entities.PieceJointe;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PieceJointeRepository extends JpaRepository<PieceJointe, String> {
    List<PieceJointe> findByOrganisationIdAndTypeFichier(String organisationId, String typeFichier);

    Optional<PieceJointe> findFirstByOrganisationIdAndTypeFichier(String organisationId, String typeFichier);

}
