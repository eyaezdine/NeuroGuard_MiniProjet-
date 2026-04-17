package tn.esprit.ms_livraison.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ms_livraison.entities.Livraison;
import tn.esprit.ms_livraison.services.LivraisonService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/")
public class LivraisonController {

    @Autowired
    private LivraisonService livraisonService;

    @GetMapping("/course/hello")
    public String hello() {
        return "Hello livraison";
    }

    @GetMapping("/livraisons")
    public List<Livraison> getAllLivraisons() {
        return livraisonService.getAllLivraisons();
    }

    @GetMapping("/livraisons/{id}")
    public Optional<Livraison> getLivraisonById(@PathVariable Long id) {
        return livraisonService.getLivraisonById(id);
    }

    @GetMapping("/livraisons/search")
    public List<Livraison> getLivraisonsByAdresse(@RequestParam String adresse) {
        return livraisonService.getLivraisonsByAdresse(adresse);
    }

    @PostMapping("/livraisons")
    public Livraison addLivraison(@RequestBody Livraison livraison) {
        return livraisonService.addLivraison(livraison);
    }

    @PutMapping("/livraisons/{id}")
    public Livraison updateLivraison(@PathVariable Long id, @RequestBody Livraison livraison) {
        return livraisonService.updateLivraison(id, livraison);
    }

    @DeleteMapping("/livraisons/{id}")
    public void deleteLivraison(@PathVariable Long id) {
        livraisonService.deleteLivraison(id);
    }
}
