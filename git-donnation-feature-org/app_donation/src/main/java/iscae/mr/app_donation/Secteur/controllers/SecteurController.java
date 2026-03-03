package iscae.mr.app_donation.Secteur.controllers;

import iscae.mr.app_donation.Secteur.dtos.SecteurDTO;
import iscae.mr.app_donation.Secteur.services.SecteurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/secteurs")
public class SecteurController {

    @Autowired
    private SecteurService secteurService;

    @PostMapping
    public SecteurDTO createSecteur(@RequestBody SecteurDTO dto) {
        return secteurService.createSecteur(dto);
    }

    @PutMapping("/{id}")
    public SecteurDTO updateSecteur(@PathVariable String id, @RequestBody SecteurDTO dto) {
        return secteurService.updateSecteur(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteSecteur(@PathVariable String id) {
        secteurService.deleteSecteur(id);
    }

    @GetMapping("/{id}")
    public SecteurDTO getSecteurById(@PathVariable String id) {
        return secteurService.getSecteurById(id);
    }

    @GetMapping
    public List<SecteurDTO> getAllSecteurs() {
        return secteurService.getAllSecteurs();
    }
}
