package iscae.mr.app_donation.dao.repositories;

import iscae.mr.app_donation.dao.entities.DonFinancier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DonFinancierRepository extends JpaRepository<DonFinancier, Long> {
    List<DonFinancier> findByProjetId(Long projetId);

    List<DonFinancier> findByActiviteId(String activiteId);

    List<DonFinancier> findByOrganisationId(String organisationId);

    List<DonFinancier> findByEmail(String email);

    // ✅ Count distinct donors (by email) who have made at least one financial
    // donation
    long countDistinctByEmailIsNotNull();
}
