package iscae.mr.app_donation.dao.repositories;

import iscae.mr.app_donation.dao.entities.ValidationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ValidationRequestRepository extends JpaRepository<ValidationRequest, Long> {

    List<ValidationRequest> findByStatus(String status);

    List<ValidationRequest> findByUtilisateurId(Long utilisateurId);

    Optional<ValidationRequest> findByUtilisateurIdAndStatus(Long utilisateurId, String status);

    List<ValidationRequest> findByStatusOrderByCreatedAtDesc(String status);

    long countByStatus(String status);
}
