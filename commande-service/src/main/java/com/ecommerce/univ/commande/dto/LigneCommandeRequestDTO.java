package com.ecommerce.univ.commande.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public class LigneCommandeRequestDTO {

    @NotNull(message = "Le produit est obligatoire")
    @Positive(message = "L identifiant du produit doit etre positif")
    private Long produitId;

    @NotNull(message = "La quantite est obligatoire")
    @Positive(message = "La quantite doit etre strictement positive")
    private Integer quantite;

    public LigneCommandeRequestDTO() {}

    public LigneCommandeRequestDTO(Long produitId, Integer quantite) {
        this.produitId = produitId;
        this.quantite = quantite;
    }

    public Long getProduitId() { return produitId; }
    public void setProduitId(Long produitId) { this.produitId = produitId; }
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }
}
