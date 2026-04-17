export interface Livraison {
  id?: number;
  dateLivraison?: string | Date;
  adresse?: string;
  codePostal?: string;
  phone?: string;
  statut?: string;
  commandeId?: number;
  montantCOD?: number;
}
