package iscae.mr.app_donation.dao.repositories;

import iscae.mr.app_donation.dao.entities.Don;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DonRepository extends JpaRepository<Don, Long> {
    List<Don> findByActiviteId(String activiteId);

    List<Don> findByProjetId(Long projetId);
}