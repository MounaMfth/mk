package iscae.mr.app_donation.dao.repositories;

import iscae.mr.app_donation.dao.entities.DonEvenementiel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonEvenementielRepository extends JpaRepository<DonEvenementiel, Long> {
    List<DonEvenementiel> findByProjetId(Long projetId);

    List<DonEvenementiel> findByActiviteId(String activiteId);
}