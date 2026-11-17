package com.ecommerce.univ.commande.service;

import com.ecommerce.univ.commande.client.CatalogueClient;
import com.ecommerce.univ.commande.domain.Commande;
import com.ecommerce.univ.commande.dto.*;
import com.ecommerce.univ.commande.exception.ResourceNotFoundException;
import com.ecommerce.univ.commande.repository.CommandeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommandeServiceTest {

    private CommandeRepository repository;
    private CatalogueClient catalogueClient;
    private CommandeService service;

    @BeforeEach
    void setUp() {
        repository = mock(CommandeRepository.class);
        catalogueClient = mock(CatalogueClient.class);

        service = new CommandeService(
                repository,
                new CommandeMapper(),
                catalogueClient
        );
    }

    private Commande commandeExistante() {
        return new Commande(
                1L,
                LocalDateTime.of(2026, 11, 16, 12, 0),
                Commande.StatutCommande.EN_ATTENTE,
                150.0,
                new ArrayList<>()
        );
    }

    @Test
    void getAllCommandes_listeVide() {
        when(repository.findAll()).thenReturn(List.of());

        List<CommandeResponseDTO> resultat =
                service.getAllCommandes();

        assertTrue(resultat.isEmpty());
        verify(repository).findAll();
    }

    @Test
    void getAllCommandes_listeExistante() {
        when(repository.findAll())
                .thenReturn(List.of(commandeExistante()));

        List<CommandeResponseDTO> resultat =
                service.getAllCommandes();

        assertEquals(1, resultat.size());
        assertEquals(1L, resultat.get(0).getId());
        assertEquals("EN_ATTENTE", resultat.get(0).getStatut());
        assertEquals(150.0, resultat.get(0).getMontantTotal(), 0.001);
    }

    @Test
    void getCommandeById_existante() {
        when(repository.findById(1L))
                .thenReturn(Optional.of(commandeExistante()));

        CommandeResponseDTO resultat =
                service.getCommandeById(1L);

        assertEquals(1L, resultat.getId());
        assertEquals("EN_ATTENTE", resultat.getStatut());
    }

    @Test
    void getCommandeById_inexistante() {
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.getCommandeById(999L)
        );
    }

    @Test
    void createCommande_uneLigne() {
        when(catalogueClient.getProduitById(10L))
                .thenReturn(new ProduitDTO(
                        10L, "Clavier", "Test", 25.0, 100
                ));

        when(repository.save(any(Commande.class)))
                .thenAnswer(invocation -> {
                    Commande commande = invocation.getArgument(0);
                    commande.setId(7L);
                    return commande;
                });

        CommandeRequestDTO requete = new CommandeRequestDTO(
                List.of(new LigneCommandeRequestDTO(10L, 4))
        );

        CommandeResponseDTO resultat =
                service.createCommande(requete);

        assertEquals(7L, resultat.getId());
        assertEquals("EN_ATTENTE", resultat.getStatut());
        assertEquals(100.0, resultat.getMontantTotal(), 0.001);
        assertEquals(1, resultat.getLignes().size());
        assertEquals(4, resultat.getLignes().get(0).getQuantite());
        assertEquals(25.0,
                resultat.getLignes().get(0).getPrixUnitaire(), 0.001);

        verify(catalogueClient).getProduitById(10L);
        verify(repository).save(any(Commande.class));
    }

    @Test
    void createCommande_deuxProduits_calculTotal() {
        when(catalogueClient.getProduitById(10L))
                .thenReturn(new ProduitDTO(
                        10L, "Clavier", "Test", 100.0, 50
                ));

        when(catalogueClient.getProduitById(20L))
                .thenReturn(new ProduitDTO(
                        20L, "Souris", "Test", 25.0, 50
                ));

        when(repository.save(any(Commande.class)))
                .thenAnswer(invocation -> {
                    Commande commande = invocation.getArgument(0);
                    commande.setId(8L);
                    return commande;
                });

        CommandeRequestDTO requete = new CommandeRequestDTO(
                List.of(
                        new LigneCommandeRequestDTO(10L, 2),
                        new LigneCommandeRequestDTO(20L, 3)
                )
        );

        CommandeResponseDTO resultat =
                service.createCommande(requete);

        assertEquals(275.0, resultat.getMontantTotal(), 0.001);
        assertEquals("EN_ATTENTE", resultat.getStatut());
        assertEquals(2, resultat.getLignes().size());
        assertNotNull(resultat.getDateCommande());

        ArgumentCaptor<Commande> capture =
                ArgumentCaptor.forClass(Commande.class);

        verify(repository).save(capture.capture());

        Commande sauvegardee = capture.getValue();

        assertEquals(275.0,
                sauvegardee.getMontantTotal(), 0.001);
        assertEquals(2, sauvegardee.getLignes().size());

        for (var ligne : sauvegardee.getLignes()) {
            assertSame(sauvegardee, ligne.getCommande());
        }

        verify(catalogueClient).getProduitById(10L);
        verify(catalogueClient).getProduitById(20L);
        verifyNoMoreInteractions(catalogueClient);
    }

    @Test
    void updateStatut_commandeExistante() {
        Commande commande = commandeExistante();

        when(repository.findById(1L))
                .thenReturn(Optional.of(commande));

        when(repository.save(any(Commande.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CommandeResponseDTO resultat =
                service.updateStatut(1L, "CONFIRMEE");

        assertEquals("CONFIRMEE", resultat.getStatut());
        assertEquals(
                Commande.StatutCommande.CONFIRMEE,
                commande.getStatut()
        );

        verify(repository).save(commande);
    }

    @Test
    void updateStatut_commandeInexistante() {
        when(repository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.updateStatut(999L, "CONFIRMEE")
        );

        verify(repository, never()).save(any(Commande.class));
    }

    @Test
    void deleteCommande_existante() {
        when(repository.existsById(1L)).thenReturn(true);

        service.deleteCommande(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void deleteCommande_inexistante() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.deleteCommande(999L)
        );

        verify(repository, never()).deleteById(anyLong());
    }
}