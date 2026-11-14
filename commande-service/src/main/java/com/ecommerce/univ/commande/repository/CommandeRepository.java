package com.ecommerce.univ.commande.repository;

import com.ecommerce.univ.commande.domain.Commande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {
}
