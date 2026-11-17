package com.ecommerce.univ.paiement.service;

import com.ecommerce.univ.paiement.client.CommandeClient;
import com.ecommerce.univ.paiement.domain.Paiement;
import com.ecommerce.univ.paiement.dto.CommandeDTO;
import com.ecommerce.univ.paiement.dto.PaiementMapper;
import com.ecommerce.univ.paiement.dto.PaiementRequestDTO;
import com.ecommerce.univ.paiement.dto.PaiementResponseDTO;
import com.ecommerce.univ.paiement.exception.ResourceNotFoundException;
import com.ecommerce.univ.paiement.repository.PaiementRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaiementServiceTest {

    private final PaiementRepository repository = mock(PaiementRepository.class);
    private final CommandeClient commandeClient = mock(CommandeClient.class);
    private final PaiementService service = new PaiementService(
            repository, new PaiementMapper(), commandeClient);

    private Paiement creerPaiement() {
        return new Paiement(1L, 10L, 1799.98,
                Paiement.ModePaiement.CARTE_BANCAIRE,
                Paiement.StatutPaiement.ACCEPTE,
                LocalDateTime.of(2026, 11, 16, 12, 0));
    }

    @Test
    void listeVide() {
        when(repository.findAll()).thenReturn(List.of());
        assertTrue(service.getAllPaiements().isEmpty());
    }

    @Test
    void listeAvecUnPaiement() {
        when(repository.findAll()).thenReturn(List.of(creerPaiement()));
        List<PaiementResponseDTO> resultat = service.getAllPaiements();
        assertEquals(1, resultat.size());
        assertEquals(10L, resultat.get(0).getCommandeId());
    }

    @Test
    void recherchePaiementExistant() {
        when(repository.findById(1L)).thenReturn(Optional.of(creerPaiement()));
        PaiementResponseDTO resultat = service.getPaiementById(1L);
        assertEquals(1L, resultat.getId());
        assertEquals("ACCEPTE", resultat.getStatut());
    }

    @Test
    void recherchePaiementInexistant() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.getPaiementById(999L));
    }

    @Test
    void rechercheParCommande() {
        when(repository.findByCommandeId(10L))
                .thenReturn(List.of(creerPaiement()));
        List<PaiementResponseDTO> resultat = service.getPaiementsByCommandeId(10L);
        assertEquals(1, resultat.size());
        assertEquals(10L, resultat.get(0).getCommandeId());
    }

    @Test
    void rechercheParCommandeSansPaiement() {
        when(repository.findByCommandeId(999L)).thenReturn(List.of());
        assertTrue(service.getPaiementsByCommandeId(999L).isEmpty());
    }

    @Test
    void creationAvecMontantRecupereParFeign() {
        CommandeDTO commande = new CommandeDTO(10L, null, "EN_ATTENTE", 1799.98);
        when(commandeClient.getCommandeById(10L)).thenReturn(commande);
        when(repository.save(any(Paiement.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaiementResponseDTO resultat = service.createPaiement(
                new PaiementRequestDTO(10L, "CARTE_BANCAIRE"));

        assertEquals(10L, resultat.getCommandeId());
        assertEquals(1799.98, resultat.getMontant());
        assertEquals("ACCEPTE", resultat.getStatut());
        assertEquals("CARTE_BANCAIRE", resultat.getModePaiement());
        assertNotNull(resultat.getDatePaiement());

        ArgumentCaptor<Paiement> capture = ArgumentCaptor.forClass(Paiement.class);
        verify(repository).save(capture.capture());
        assertEquals(1799.98, capture.getValue().getMontant());
        verify(commandeClient).getCommandeById(10L);
    }

    @Test
    void modePaiementInconnu() {
        CommandeDTO commande = new CommandeDTO(10L, null, "EN_ATTENTE", 100.0);
        when(commandeClient.getCommandeById(10L)).thenReturn(commande);
        assertThrows(IllegalArgumentException.class,
                () -> service.createPaiement(new PaiementRequestDTO(10L, "INCONNU")));
        verify(repository, never()).save(any(Paiement.class));
    }

    @Test
    void suppressionPaiementExistant() {
        when(repository.existsById(1L)).thenReturn(true);
        service.deletePaiement(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void suppressionPaiementInexistant() {
        when(repository.existsById(999L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class,
                () -> service.deletePaiement(999L));
        verify(repository, never()).deleteById(999L);
    }
}
