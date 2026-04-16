package tn.esprit.ms_livraison.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Date;

@Entity
public class Livraison {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date dateLivraison;
    private String adresse;
    private String codePostal;
    private String phone;
    private String statut;
    private Long commandeId; 
    private Double montantCOD;

    public Livraison() {
    }

    public Livraison(Date dateLivraison, String adresse, String codePostal, String phone, String statut, Long commandeId, Double montantCOD) {
        this.dateLivraison = dateLivraison;
        this.adresse = adresse;
        this.codePostal = codePostal;
        this.phone = phone;
        this.statut = statut;
        this.commandeId = commandeId;
        this.montantCOD = montantCOD;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateLivraison() {
        return dateLivraison;
    }

    public void setDateLivraison(Date dateLivraison) {
        this.dateLivraison = dateLivraison;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Long getCommandeId() {
        return commandeId;
    }

    public void setCommandeId(Long commandeId) {
        this.commandeId = commandeId;
    }

    public Double getMontantCOD() {
        return montantCOD;
    }

    public void setMontantCOD(Double montantCOD) {
        this.montantCOD = montantCOD;
    }
}
