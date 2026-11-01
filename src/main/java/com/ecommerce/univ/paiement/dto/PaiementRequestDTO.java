package com.ecommerce.univ.paiement.dto;


public class PaiementRequestDTO {


    private Long commandeId;

    private String modePaiement;

    public PaiementRequestDTO() {}

    public PaiementRequestDTO(Long commandeId, String modePaiement) {
        this.commandeId = commandeId;
        this.modePaiement = modePaiement;
    }

    public Long getCommandeId() { return commandeId; }
    public void setCommandeId(Long commandeId) { this.commandeId = commandeId; }
    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }
}
