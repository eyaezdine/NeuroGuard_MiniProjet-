package tn.esprit.ms_livraison.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ms_livraison.entities.Livraison;

import java.util.List;

public interface LivraisonRepository extends JpaRepository<Livraison, Long> {
    
    // Example for searching by name/address if user wants "afficher par son nom" (using adresse or statut or commandeId)
    // The instructions say "Afficher un livraison par son id ou son nom", 
    // we'll implement a findByAdresse since 'nom' isn't a direct field, maybe address or we can add 'nom' if needed
    // However, looking at the entity fields: id, dateLivraison, adresse, codePostal, phone, statut, commandeId, montantCOD
    // I will add a findByAdresse method as a fallback for 'nom' or we add a field 'nom'.
    // Wait, the screenshot has fields, no 'nom'. Let's search by Adresse.
    List<Livraison> findByAdresseContainingIgnoreCase(String adresse);
}
