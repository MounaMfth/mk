package iscae.mr.app_donation.Activite.services;

import iscae.mr.app_donation.Activite.dtos.ActiviteDTO;
import iscae.mr.app_donation.dao.entities.Activite;
import iscae.mr.app_donation.dao.entities.Organisation;
import iscae.mr.app_donation.dao.repositories.ActiviteRepository;
import iscae.mr.app_donation.dao.repositories.OrganisationRepository;
import iscae.mr.app_donation.dao.repositories.DonFinancierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ActiviteService {

  private final ActiviteRepository activiteRepository;
  private final OrganisationRepository organisationRepository;
  private final DonFinancierRepository donFinancierRepository;

  public ActiviteService(ActiviteRepository activiteRepository,
      OrganisationRepository organisationRepository,
      DonFinancierRepository donFinancierRepository) {
    this.activiteRepository = activiteRepository;
    this.organisationRepository = organisationRepository;
    this.donFinancierRepository = donFinancierRepository;
  }

  /*
   * ============================================================
   * MAPPER : Entity → DTO
   * ============================================================
   */
  private ActiviteDTO toDTO(Activite a) {
    String orgId = (a.getOrganisation() != null) ? a.getOrganisation().getId() : null;
    String orgNom = (a.getOrganisation() != null) ? a.getOrganisation().getNom() : null;

    // Calculate montantRecolte from donations
    Double montantRecolte = 0.0;
    if (a.getId() != null) {
      montantRecolte = donFinancierRepository.findByActiviteId(a.getId())
          .stream()
          .mapToDouble(d -> d.getMontantFinancier() != null ? d.getMontantFinancier() : 0)
          .sum();
    }

    // Calculate progress percentage
    Integer pourcentageProgress = 0;
    if (a.getObjectifFinancier() != null && a.getObjectifFinancier() > 0) {
      pourcentageProgress = (int) Math.min((montantRecolte / a.getObjectifFinancier()) * 100, 100);
    }

    return ActiviteDTO.builder()
        .id(a.getId())
        .titre(a.getTitre())
        .description(a.getDescription())
        .latitude(a.getLatitude())
        .longitude(a.getLongitude())
        .image(a.getImage())
        .organisationId(orgId)
        .organisationNom(orgNom)
        .objectifFinancier(a.getObjectifFinancier())
        .montantRecolte(montantRecolte)
        .pourcentageProgress(pourcentageProgress)
        .budgetAtteint(a.getBudgetAtteint() != null ? a.getBudgetAtteint() : false)
        .build();
  }

  /*
   * ============================================================
   * READ - Liste publique (toutes les activités)
   * ============================================================
   */
  @Transactional(readOnly = true)
  public List<ActiviteDTO> listPublic() {
    return activiteRepository.findAll().stream()
        .map(this::toDTO)
        .toList();
  }

  /*
   * ============================================================
   * READ - Liste par organisation (FILTRÉ)
   * ============================================================
   */
  @Transactional(readOnly = true)
  public List<ActiviteDTO> listByOrganisationId(String organisationId) {
    System.out.println("🔍 [ActiviteService] Récupération des activités pour orgId: " + organisationId);

    List<ActiviteDTO> activites = activiteRepository.findByOrganisation_Id(organisationId)
        .stream()
        .map(this::toDTO)
        .toList();

    System.out.println("✅ [ActiviteService] " + activites.size() + " activité(s) trouvée(s)");
    return activites;
  }

  /*
   * ============================================================
   * READ - Toutes les activités avec organisation (évite N+1)
   * ============================================================
   */
  @Transactional(readOnly = true)
  public List<ActiviteDTO> listAllWithOrganisation() {
    return activiteRepository.findAllWithOrganisation()
        .stream()
        .map(this::toDTO)
        .toList();
  }

  /*
   * ============================================================
   * READ - Une activité spécifique (avec vérification organisation)
   * ============================================================
   */
  @Transactional(readOnly = true)
  public Optional<ActiviteDTO> findOneForOrganisation(String id, String organisationId) {
    return activiteRepository.findById(id)
        .filter(a -> a.getOrganisation() != null &&
            organisationId.equals(a.getOrganisation().getId()))
        .map(this::toDTO);
  }

  /*
   * ============================================================
   * CREATE - Créer une activité
   * ============================================================
   */
  @Transactional
  public ActiviteDTO createActivite(Activite a, String organisationId) {
    System.out.println("📝 [ActiviteService] Création activité pour orgId: " + organisationId);

    // ✅ Vérifier que l'organisation existe
    Organisation org = organisationRepository.findById(organisationId)
        .orElseThrow(() -> new IllegalArgumentException("Organisation inconnue: " + organisationId));

    System.out.println("✅ [ActiviteService] Organisation trouvée: " + org.getNom());

    // ✅ Associer l'organisation à l'activité
    a.setOrganisation(org);

    // ✅ Sauvegarder
    Activite saved = activiteRepository.save(a);
    System.out.println("✅ [ActiviteService] Activité créée avec ID: " + saved.getId());

    return toDTO(saved);
  }

  /*
   * ============================================================
   * UPDATE - Modifier une activité
   * ============================================================
   */
  @Transactional
  public ActiviteDTO updateActivite(String id, Activite updated, String organisationId) {
    System.out.println("📝 [ActiviteService] Mise à jour activité ID: " + id);

    return activiteRepository.findById(id).map(existing -> {
      // ✅ Vérifier que l'activité appartient à cette organisation
      if (existing.getOrganisation() == null ||
          !organisationId.equals(existing.getOrganisation().getId())) {
        throw new IllegalStateException("Accès refusé : activité d'une autre organisation");
      }

      // ✅ Mettre à jour les champs
      existing.setTitre(updated.getTitre());
      existing.setDescription(updated.getDescription());
      existing.setLatitude(updated.getLatitude());
      existing.setLongitude(updated.getLongitude());

      if (updated.getImage() != null) {
        existing.setImage(updated.getImage());
      }

      Activite saved = activiteRepository.save(existing);
      System.out.println("✅ [ActiviteService] Activité mise à jour: " + saved.getId());

      return toDTO(saved);

    }).orElseThrow(() -> new IllegalArgumentException("Activité non trouvée: " + id));
  }

  /*
   * ============================================================
   * DELETE - Supprimer une activité
   * ============================================================
   */
  @Transactional
  public void deleteActivite(String id, String organisationId) {
    System.out.println("🗑️ [ActiviteService] Suppression activité ID: " + id);

    activiteRepository.findById(id).ifPresentOrElse(a -> {
      // ✅ Vérifier que l'activité appartient à cette organisation
      if (a.getOrganisation() == null ||
          !organisationId.equals(a.getOrganisation().getId())) {
        throw new IllegalStateException("Accès refusé : activité d'une autre organisation");
      }

      activiteRepository.delete(a);
      System.out.println("✅ [ActiviteService] Activité supprimée: " + id);

    }, () -> {
      throw new IllegalArgumentException("Activité non trouvée: " + id);
    });
  }
}