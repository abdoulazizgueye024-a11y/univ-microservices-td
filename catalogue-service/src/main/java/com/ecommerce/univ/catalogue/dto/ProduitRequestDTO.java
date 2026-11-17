package com.ecommerce.univ.catalogue.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;


public class ProduitRequestDTO {


    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String description;


    @NotNull(message = "Le prix est obligatoire")
    @Positive(message = "Le prix doit etre strictement positif")
    private Double prix;

    @NotNull(message = "Le stock est obligatoire")
    @PositiveOrZero(message = "Le stock ne peut pas etre negatif")
    private Integer quantiteStock;

    public ProduitRequestDTO() {}

    public ProduitRequestDTO(String nom, String description, Double prix, Integer quantiteStock) {
        this.nom = nom;
        this.description = description;
        this.prix = prix;
        this.quantiteStock = quantiteStock;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrix() { return prix; }
    public void setPrix(Double prix) { this.prix = prix; }
    public Integer getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(Integer quantiteStock) { this.quantiteStock = quantiteStock; }
}
