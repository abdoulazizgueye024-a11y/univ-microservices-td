package com.ecommerce.univ.catalogue.repository;


import com.ecommerce.univ.catalogue.domain.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
}
