package com.ecommerce.univ.catalogue.service;

import com.ecommerce.univ.catalogue.domain.Produit;
import com.ecommerce.univ.catalogue.dto.ProduitMapper;
import com.ecommerce.univ.catalogue.dto.ProduitRequestDTO;
import com.ecommerce.univ.catalogue.dto.ProduitResponseDTO;
import com.ecommerce.univ.catalogue.exception.ResourceNotFoundException;
import com.ecommerce.univ.catalogue.repository.ProduitRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class ProduitServiceTest {

    private ProduitRepository repository;
    private ProduitService service;

    @BeforeEach
    void setUp() {
        repository = mock(ProduitRepository.class);
        service = new ProduitService(repository, new ProduitMapper());
    }

    private Produit produit(Long id, String nom) {
        Produit p = new Produit();
        p.setId(id);
        p.setNom(nom);
        p.setDescription("Description de test");
        p.setPrix(99.99);
        p.setQuantiteStock(10);
        return p;
    }

    private ProduitRequestDTO requete(String nom) {
        return new ProduitRequestDTO(
                nom,
                "Description de test",
                99.99,
                10
        );
    }

    @Test
    void getAllProduits_retourneLaListe() {
        when(repository.findAll()).thenReturn(
                List.of(produit(1L, "Produit A"), produit(2L, "Produit B"))
        );

        List<ProduitResponseDTO> resultat = service.getAllProduits();

        assertEquals(2, resultat.size());
        assertEquals("Produit A", resultat.get(0).getNom());
        assertEquals("Produit B", resultat.get(1).getNom());
        verify(repository).findAll();
    }

    @Test
    void getProduitById_existant_retourneLeProduit() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(produit(1L, "Ordinateur")));

        ProduitResponseDTO resultat = service.getProduitById(1L);

        assertEquals(1L, resultat.getId());
        assertEquals("Ordinateur", resultat.getNom());
        assertEquals(99.99, resultat.getPrix());
    }

    @Test
    void getProduitById_inexistant_leveUneException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getProduitById(999L)
        );
    }

    @Test
    void createProduit_enregistreEtRetourneLeProduit() {
        when(repository.save(any(Produit.class))).thenAnswer(invocation -> {
            Produit p = invocation.getArgument(0);
            p.setId(7L);
            return p;
        });

        ProduitResponseDTO resultat =
                service.createProduit(requete("Clavier"));

        assertEquals(7L, resultat.getId());
        assertEquals("Clavier", resultat.getNom());
        assertEquals(10, resultat.getQuantiteStock());
        verify(repository).save(any(Produit.class));
    }

    @Test
    void updateProduit_existant_modifieLeProduit() {
        Produit existant = produit(1L, "Ancien nom");

        when(repository.findById(1L))
                .thenReturn(Optional.of(existant));
        when(repository.save(any(Produit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProduitResponseDTO resultat =
                service.updateProduit(1L, requete("Nouveau nom"));

        assertEquals(1L, resultat.getId());
        assertEquals("Nouveau nom", resultat.getNom());
        verify(repository).save(any(Produit.class));
    }

    @Test
    void updateProduit_inexistant_leveUneException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateProduit(999L, requete("Test"))
        );

        verify(repository, never()).save(any(Produit.class));
    }

    @Test
    void deleteProduit_existant_supprimeLeProduit() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteProduit(1L);

        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void deleteProduit_inexistant_leveUneException() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.deleteProduit(999L)
        );

        verify(repository, never()).deleteById(anyLong());
    }
}