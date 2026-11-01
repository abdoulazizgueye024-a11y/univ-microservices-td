package com.ecommerce.univ.commande.dto;

import java.util.List;

public class CommandeRequestDTO {

    private List<LigneCommandeRequestDTO> lignes;

    public CommandeRequestDTO() {}

    public CommandeRequestDTO(List<LigneCommandeRequestDTO> lignes) {
        this.lignes = lignes;
    }

    public List<LigneCommandeRequestDTO> getLignes() { return lignes; }
    public void setLignes(List<LigneCommandeRequestDTO> lignes) { this.lignes = lignes; }
}
