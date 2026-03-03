package iscae.mr.app_donation.Don.services;

import iscae.mr.app_donation.Don.dtos.DonDTO;
import iscae.mr.app_donation.Don.dtos.DonDetailDTO;
import iscae.mr.app_donation.dao.entities.*;
import iscae.mr.app_donation.dao.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonService {

        private final DonRepository donRepository;
        private final DonFinancierRepository donFinancierRepository;
        private final DonEvenementielRepository donEvenementielRepository;
        private final DonNatureRepository donNatureRepository;
        private final DonParrainageRepository donParrainageRepository;

        // =====================
        // LISTE DE TOUS LES DONS
        // =====================
        public List<DonDTO> getAllDons() {
                return donRepository.findAll()
                                .stream()
                                .map(this::convertToDonBasicDTO)
                                .collect(Collectors.toList());
        }

        // =====================
        // DON PAR ID
        // =====================
        public DonDTO getDonById(Long id) {
                Don don = donRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Don introuvable"));
                return convertToDonBasicDTO(don);
        }

        // =====================
        // DONS PAR PROJET
        // =====================
        public DonDTO getProjetDonById(Long projetId) {
                List<DonDetailDTO> donateurs = new ArrayList<>();

                // Récupérer tous les types de dons pour le projet
                donateurs.addAll(donFinancierRepository.findByProjetId(projetId)
                                .stream().map(this::convertDonFinancierToDTO).collect(Collectors.toList()));
                donateurs.addAll(donEvenementielRepository.findByProjetId(projetId)
                                .stream().map(this::convertDonEvenementielToDTO).collect(Collectors.toList()));
                donateurs.addAll(donNatureRepository.findByProjetId(projetId)
                                .stream().map(this::convertDonNatureToDTO).collect(Collectors.toList()));
                donateurs.addAll(donParrainageRepository.findByProjetId(projetId)
                                .stream().map(this::convertDonParrainageToDTO).collect(Collectors.toList()));

                double montantTotal = donateurs.stream()
                                .filter(d -> d.getMontant() != null)
                                .mapToDouble(DonDetailDTO::getMontant)
                                .sum();

                return DonDTO.builder()
                                .id(String.valueOf(projetId))
                                .titre(donateurs.isEmpty() ? "Aucun don" : "Dons du projet")
                                .description(donateurs.isEmpty() ? "Aucun don pour ce projet" : "Liste des dons")
                                .objectifFinancier(montantTotal > 0 ? montantTotal : 1.0)
                                .montantRecolte(montantTotal)
                                .pourcentage((int) Math.min((montantTotal / 1.0) * 100, 100))
                                .nombreDonateurs(donateurs.size())
                                .donateurs(donateurs)
                                .build();
        }

        // =====================
        // DONS PAR ACTIVITÉ
        // =====================
        public DonDTO getDonsByActiviteId(String activiteId) {

                List<DonDetailDTO> donateurs = new ArrayList<>();

                // 1️⃣ Dons financiers
                List<DonFinancier> donsFinanciers = donFinancierRepository.findByActiviteId(activiteId);
                donateurs.addAll(donsFinanciers.stream()
                                .map(this::convertDonFinancierToDTO)
                                .collect(Collectors.toList()));

                // 2️⃣ Dons événementiels
                donateurs.addAll(donEvenementielRepository.findByActiviteId(activiteId)
                                .stream().map(this::convertDonEvenementielToDTO).collect(Collectors.toList()));

                // 3️⃣ Dons nature
                donateurs.addAll(donNatureRepository.findByActiviteId(activiteId)
                                .stream().map(this::convertDonNatureToDTO).collect(Collectors.toList()));

                // 4️⃣ Dons parrainage
                donateurs.addAll(donParrainageRepository.findByActiviteId(activiteId)
                                .stream().map(this::convertDonParrainageToDTO).collect(Collectors.toList()));

                // Calcul du montant total uniquement sur les dons financiers
                double montantTotal = donsFinanciers.stream()
                                .mapToDouble(d -> d.getMontantFinancier() != null ? d.getMontantFinancier() : 0)
                                .sum();

                // Construction du DTO final
                return DonDTO.builder()
                                .id(activiteId)
                                .titre(donateurs.isEmpty() ? "Aucun don" : "Dons de l'activité")
                                .description(donateurs.isEmpty() ? "Aucun don pour cette activité"
                                                : "Dons liés à cette activité")
                                .objectifFinancier(1.0) // éviter division par zéro
                                .montantRecolte(montantTotal)
                                .pourcentage((int) Math.min((montantTotal / 1.0) * 100, 100))
                                .nombreDonateurs(donateurs.size())
                                .donateurs(donateurs)
                                .build();
        }

        // =====================
        // CONVERTISSEURS
        // =====================
        private DonDTO convertToDonBasicDTO(Don don) {
                return DonDTO.builder()
                                .id(String.valueOf(don.getId()))
                                .titre(don.getTitre())
                                .description(don.getDescription())
                                .dateDon(don.getDateDon())
                                .projetId(don.getProjetId())
                                .activiteId(don.getActiviteId())
                                .build();
        }

        private DonDetailDTO convertDonFinancierToDTO(DonFinancier don) {
                return DonDetailDTO.builder()
                                .nom("Donateur")
                                .date(don.getDateDon())
                                .typeDon("FINANCIER")
                                .montant(don.getMontantFinancier())
                                .details(String.format("%.2f MRU", don.getMontantFinancier()))
                                .build();
        }

        private DonDetailDTO convertDonEvenementielToDTO(DonEvenementiel don) {
                return DonDetailDTO.builder()
                                .nom("Donateur")
                                .date(don.getDateDon())
                                .typeDon("EVENEMENTIEL")
                                .evenement(don.getEvenement())
                                .lieu(don.getLieu())
                                .details(String.format("Événement: %s (%s)", don.getEvenement(), don.getLieu()))
                                .build();
        }

        private DonDetailDTO convertDonNatureToDTO(DonNature don) {
                return DonDetailDTO.builder()
                                .nom("Donateur")
                                .date(don.getDateDon())
                                .typeDon("NATURE")
                                .typeNature(don.getTypeNature())
                                .quantite(don.getQuantite())
                                .details(String.format("%d × %s", don.getQuantite(), don.getTypeNature()))
                                .build();
        }

        private DonDetailDTO convertDonParrainageToDTO(DonParrainage don) {
                return DonDetailDTO.builder()
                                .nom("Donateur")
                                .date(don.getDateDon())
                                .typeDon("PARRAINAGE")
                                .typeLien(don.getTypeLien())
                                .nombreBeneficiaires(don.getNombreBeneficiaires())
                                .details(String.format("Parrainage: %s (%d bénéf.)", don.getTypeLien(),
                                                don.getNombreBeneficiaires()))
                                .build();
        }
}
