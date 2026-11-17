package com.ecommerce.univ.commande.controller;

import com.ecommerce.univ.commande.client.CatalogueClient;
import com.ecommerce.univ.commande.domain.Commande;
import com.ecommerce.univ.commande.dto.CommandeMapper;
import com.ecommerce.univ.commande.exception.GlobalExceptionHandlerCommande;
import com.ecommerce.univ.commande.repository.CommandeRepository;
import com.ecommerce.univ.commande.service.CommandeService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommandeStatutInvalideTest {

    @Test
    void patchStatutInconnu_retourne400_sansSauvegarde() throws Exception {
        CommandeRepository repository = mock(CommandeRepository.class);
        CatalogueClient catalogueClient = mock(CatalogueClient.class);
        CommandeService service = new CommandeService(
                repository, new CommandeMapper(), catalogueClient);

        Commande commande = new Commande(
                1L,
                LocalDateTime.of(2026, 11, 16, 12, 0),
                Commande.StatutCommande.EN_ATTENTE,
                100.0,
                new ArrayList<>());

        when(repository.findById(1L)).thenReturn(Optional.of(commande));

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(new CommandeController(service))
                .setControllerAdvice(new GlobalExceptionHandlerCommande())
                .build();

        mvc.perform(patch("/api/v1/commandes/1/statut")
                .param("statut", "INCONNU"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(repository).findById(1L);
        verify(repository, never()).save(any(Commande.class));
        verifyNoInteractions(catalogueClient);
    }
}
