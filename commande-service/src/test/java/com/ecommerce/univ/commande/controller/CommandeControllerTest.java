package com.ecommerce.univ.commande.controller;

import com.ecommerce.univ.commande.dto.CommandeRequestDTO;
import com.ecommerce.univ.commande.dto.CommandeResponseDTO;
import com.ecommerce.univ.commande.exception.GlobalExceptionHandlerCommande;
import com.ecommerce.univ.commande.exception.ResourceNotFoundException;
import com.ecommerce.univ.commande.service.CommandeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommandeControllerTest {

    private MockMvc mvc;
    private CommandeService service;

    @BeforeEach
    void setUp() {
        service = mock(CommandeService.class);

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        mvc = MockMvcBuilders
                .standaloneSetup(new CommandeController(service))
                .setControllerAdvice(new GlobalExceptionHandlerCommande())
                .setValidator(validator)
                .build();
    }

    private CommandeResponseDTO reponseValide() {
        return new CommandeResponseDTO(
                7L,
                LocalDateTime.of(2026, 11, 16, 12, 0),
                "EN_ATTENTE",
                50.0,
                List.of()
        );
    }

    @Test
    void postCommandeValide_retourne201() throws Exception {
        when(service.createCommande(any(CommandeRequestDTO.class)))
                .thenReturn(reponseValide());

        mvc.perform(post("/api/v1/commandes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"lignes":[{"produitId":10,"quantite":2}]}
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.montantTotal").value(50.0));

        verify(service).createCommande(any(CommandeRequestDTO.class));
    }

    @Test
    void postListeVide_retourne400() throws Exception {
        mvc.perform(post("/api/v1/commandes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"lignes":[]}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"))
                .andExpect(jsonPath("$.errors.lignes").exists());

        verifyNoInteractions(service);
    }

    @Test
    void postListeAbsente_retourne400() throws Exception {
        mvc.perform(post("/api/v1/commandes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verifyNoInteractions(service);
    }

    @Test
    void postQuantiteZero_retourne400() throws Exception {
        mvc.perform(post("/api/v1/commandes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"lignes":[{"produitId":10,"quantite":0}]}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verifyNoInteractions(service);
    }

    @Test
    void postQuantiteNegative_retourne400() throws Exception {
        mvc.perform(post("/api/v1/commandes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"lignes":[{"produitId":10,"quantite":-2}]}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verifyNoInteractions(service);
    }

    @Test
    void postQuantiteAbsente_retourne400() throws Exception {
        mvc.perform(post("/api/v1/commandes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"lignes":[{"produitId":10}]}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verifyNoInteractions(service);
    }

    @Test
    void postProduitAbsent_retourne400() throws Exception {
        mvc.perform(post("/api/v1/commandes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"lignes":[{"quantite":2}]}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verifyNoInteractions(service);
    }

    @Test
    void postProduitIdNegatif_retourne400() throws Exception {
        mvc.perform(post("/api/v1/commandes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"lignes":[{"produitId":-1,"quantite":2}]}
                    """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));

        verifyNoInteractions(service);
    }

    @Test
    void getCommandeInexistante_retourne404() throws Exception {
        when(service.getCommandeById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Commande non trouvee avec l'id: 999"
                ));

        mvc.perform(get("/api/v1/commandes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void patchStatutValide_retourne200() throws Exception {
        CommandeResponseDTO reponse = reponseValide();
        reponse.setStatut("CONFIRMEE");

        when(service.updateStatut(7L, "CONFIRMEE"))
                .thenReturn(reponse);

        mvc.perform(patch("/api/v1/commandes/7/statut")
                .param("statut", "CONFIRMEE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("CONFIRMEE"));

        verify(service).updateStatut(7L, "CONFIRMEE");
    }

    @Test
    void deleteCommandeExistante_retourne204() throws Exception {
        mvc.perform(delete("/api/v1/commandes/7"))
                .andExpect(status().isNoContent());

        verify(service).deleteCommande(7L);
    }
}