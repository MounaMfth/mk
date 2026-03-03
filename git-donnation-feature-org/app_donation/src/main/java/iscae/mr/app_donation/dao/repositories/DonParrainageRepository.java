package iscae.mr.app_donation.dao.repositories;

import iscae.mr.app_donation.dao.entities.DonParrainage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DonParrainageRepository extends JpaRepository<DonParrainage, Long> {
    List<DonParrainage> findByProjetId(Long projetId);

    List<DonParrainage> findByActiviteId(String activiteId);
}