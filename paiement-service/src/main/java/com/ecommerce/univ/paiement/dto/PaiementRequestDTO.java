package com.ecommerce.univ.paiement.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public class PaiementRequestDTO {


    @NotNull(message = "La commande est obligatoire")
    @Positive(message = "L'identifiant de commande doit etre positif")
    private Long commandeId;

    @NotBlank(message = "Le mode de paiement est obligatoire")
    @Pattern(regexp = "CARTE_BANCAIRE|VIREMENT|PAYPAL|ESPECES", message = "Mode de paiement invalide")
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
