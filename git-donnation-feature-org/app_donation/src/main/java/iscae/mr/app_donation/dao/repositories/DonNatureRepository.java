package iscae.mr.app_donation.dao.repositories;

import iscae.mr.app_donation.dao.entities.DonNature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DonNatureRepository extends JpaRepository<DonNature, Long> {
    List<DonNature> findByProjetId(Long projetId);

    List<DonNature> findByActiviteId(String activiteId);
}