package iscae.mr.app_donation.mappers;

import iscae.mr.app_donation.Don.dtos.DonDTO;
import iscae.mr.app_donation.Don.dtos.DonDetailDTO;
import iscae.mr.app_donation.dao.entities.*;

public class DonMapper {

    // ========================================
    // CONVERTIR ENTITY EN DTO GÉNÉRIQUE
    // ========================================
    public static DonDTO toDTO(Don don) {
        if (don == null) return null;

        return DonDTO.builder()
                .id(String.valueOf(don.getId()))
                .titre(don.getTitre())
                .description(don.getDescription())
                .dateDon(don.getDateDon())
                .projetId(don.getProjetId())
                .activiteId(don.getActiviteId())
                .build();
    }

    // ========================================
    // CONVERTIR DONFINANCIER EN DETAILDTO
    // ========================================
    public static DonDetailDTO toDonFinancierDetailDTO(DonFinancier don) {
        if (don == null) return null;

        return DonDetailDTO.builder()
                .nom("Donateur Financier")
                .date(don.getDateDon())
                .typeDon("FINANCIER")
                .montant(don.getMontantFinancier())
                .details(String.format("%.2f€ - Mode: %s", 
                    don.getMontantFinancier(), 
                    don.getModePaiement() != null ? don.getModePaiement() : "Non spécifié"))
                .build();
    }

    // ========================================
    // CONVERTIR DONNATURE EN DETAILDTO
    // ========================================
    public static DonDetailDTO toDonNatureDetailDTO(DonNature don) {
        if (don == null) return null;

        return DonDetailDTO.builder()
                .nom("Donateur Nature")
                .date(don.getDateDon())
                .typeDon("NATURE")
                .typeNature(don.getTypeNature())
                .quantite(don.getQuantite())
                .details(String.format("%d × %s", 
                    don.getQuantite() != null ? don.getQuantite() : 0, 
                    don.getTypeNature()))
                .build();
    }

    // ========================================
    // CONVERTIR DONEVENEMENTIEL EN DETAILDTO
    // ========================================
    public static DonDetailDTO toDonEvenementielDetailDTO(DonEvenementiel don) {
        if (don == null) return null;

        return DonDetailDTO.builder()
                .nom("Donateur Événement")
                .date(don.getDateDon())
                .typeDon("EVENEMENTIEL")
                .evenement(don.getEvenement())
                .lieu(don.getLieu())
                .details(String.format("Événement: %s - Lieu: %s", 
                    don.getEvenement(), 
                    don.getLieu()))
                .build();
    }

    // ========================================
    // CONVERTIR DONPARRAINAGE EN DETAILDTO
    // ========================================
    public static DonDetailDTO toDonParrainageDetailDTO(DonParrainage don) {
        if (don == null) return null;

        return DonDetailDTO.builder()
                .nom("Parrain/Marraine")
                .date(don.getDateDon())
                .typeDon("PARRAINAGE")
                .typeLien(don.getTypeLien())
                .nombreBeneficiaires(don.getNombreBeneficiaires())
                .details(String.format("Parrainage: %s (%d bénéf.)", 
                    don.getTypeLien(), 
                    don.getNombreBeneficiaires() != null ? don.getNombreBeneficiaires() : 0))
                .build();
    }

    // ========================================
    // CONVERTIR DTO EN DONFINANCIER ENTITY
    // ========================================
    public static DonFinancier toEntityFinancier(DonDetailDTO dto) {
        if (dto == null) return null;

        return DonFinancier.builder()
                .titre(dto.getNom())
                .montantFinancier(dto.getMontant())
                .modePaiement(dto.getDetails())
                .build();
    }

    // ========================================
    // CONVERTIR DTO EN DONNATURE ENTITY
    // ========================================
    public static DonNature toEntityNature(DonDetailDTO dto) {
        if (dto == null) return null;

        return DonNature.builder()
                .titre(dto.getNom())
                .typeNature(dto.getTypeNature())
                .quantite(dto.getQuantite())
                .build();
    }

    // ========================================
    // CONVERTIR DTO EN DONEVENEMENTIEL ENTITY
    // ========================================
    public static DonEvenementiel toEntityEvenementiel(DonDetailDTO dto) {
        if (dto == null) return null;

        return DonEvenementiel.builder()
                .titre(dto.getNom())
                .evenement(dto.getEvenement())
                .lieu(dto.getLieu())
                .build();
    }

    // ========================================
    // CONVERTIR DTO EN DONPARRAINAGE ENTITY
    // ========================================
    public static DonParrainage toEntityParrainage(DonDetailDTO dto) {
        if (dto == null) return null;

        return DonParrainage.builder()
                .titre(dto.getNom())
                .typeLien(dto.getTypeLien())
                .nombreBeneficiaires(dto.getNombreBeneficiaires())
                .build();
    }
}
