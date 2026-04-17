package tn.esprit.ms_livraison.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tn.esprit.ms_livraison.entities.Livraison;
import tn.esprit.ms_livraison.repositories.LivraisonRepository;

import java.util.List;
import java.util.Optional;

@Service
public class LivraisonService {

    @Autowired
    private LivraisonRepository livraisonRepository;

    public List<Livraison> getAllLivraisons() {
        return livraisonRepository.findAll();
    }

    public Optional<Livraison> getLivraisonById(Long id) {
        return livraisonRepository.findById(id);
    }

    public List<Livraison> getLivraisonsByAdresse(String adresse) {
        return livraisonRepository.findByAdresseContainingIgnoreCase(adresse);
    }

    public Livraison addLivraison(Livraison livraison) {
        return livraisonRepository.save(livraison);
    }

    public Livraison updateLivraison(Long id, Livraison newLivraison) {
        return livraisonRepository.findById(id)
                .map(livraison -> {
                    livraison.setDateLivraison(newLivraison.getDateLivraison());
                    livraison.setAdresse(newLivraison.getAdresse());
                    livraison.setCodePostal(newLivraison.getCodePostal());
                    livraison.setPhone(newLivraison.getPhone());
                    livraison.setStatut(newLivraison.getStatut());
                    livraison.setCommandeId(newLivraison.getCommandeId());
                    livraison.setMontantCOD(newLivraison.getMontantCOD());
                    return livraisonRepository.save(livraison);
                })
                .orElseGet(() -> {
                    newLivraison.setId(id);
                    return livraisonRepository.save(newLivraison);
                });
    }

    public void deleteLivraison(Long id) {
        livraisonRepository.deleteById(id);
    }
}
