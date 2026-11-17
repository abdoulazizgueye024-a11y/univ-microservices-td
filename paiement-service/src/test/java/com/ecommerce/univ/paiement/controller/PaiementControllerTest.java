package com.ecommerce.univ.paiement.controller;

import com.ecommerce.univ.paiement.dto.PaiementResponseDTO;
import com.ecommerce.univ.paiement.exception.GlobalExceptionHandlerPaiment;
import com.ecommerce.univ.paiement.exception.ResourceNotFoundException;
import com.ecommerce.univ.paiement.service.PaiementService;
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

class PaiementControllerTest {

    private PaiementService service;
    private MockMvc mvc;

    @BeforeEach
    void preparer() {
        service = mock(PaiementService.class);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mvc = MockMvcBuilders.standaloneSetup(new PaiementController(service))
                .setControllerAdvice(new GlobalExceptionHandlerPaiment())
                .setValidator(validator)
                .build();
    }

    private PaiementResponseDTO paiement() {
        return new PaiementResponseDTO(1L, 10L, 100.0,
                "CARTE_BANCAIRE", "ACCEPTE",
                LocalDateTime.of(2026, 11, 16, 12, 0));
    }

    @Test
    void creationSansCommandeRetourne400() throws Exception {
        mvc.perform(post("/api/v1/paiements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"modePaiement\":\"VIREMENT\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void creationCommandeNegativeRetourne400() throws Exception {
        mvc.perform(post("/api/v1/paiements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commandeId\":-1,\"modePaiement\":\"VIREMENT\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void creationSansModeRetourne400() throws Exception {
        mvc.perform(post("/api/v1/paiements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commandeId\":10}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void creationModeInconnuRetourne400() throws Exception {
        mvc.perform(post("/api/v1/paiements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commandeId\":10,\"modePaiement\":\"INCONNU\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void creationValideRetourne201() throws Exception {
        when(service.createPaiement(any())).thenReturn(paiement());
        mvc.perform(post("/api/v1/paiements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"commandeId\":10,\"modePaiement\":\"CARTE_BANCAIRE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commandeId").value(10))
                .andExpect(jsonPath("$.statut").value("ACCEPTE"));
        verify(service).createPaiement(any());
    }

    @Test
    void consultationPaiementRetourne200() throws Exception {
        when(service.getPaiementById(1L)).thenReturn(paiement());
        mvc.perform(get("/api/v1/paiements/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void consultationPaiementInexistantRetourne404() throws Exception {
        when(service.getPaiementById(999L))
                .thenThrow(new ResourceNotFoundException("Paiement introuvable"));
        mvc.perform(get("/api/v1/paiements/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void consultationParCommandeRetourne200() throws Exception {
        when(service.getPaiementsByCommandeId(10L))
                .thenReturn(List.of(paiement()));
        mvc.perform(get("/api/v1/paiements/commande/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].commandeId").value(10));
    }

    @Test
    void suppressionRetourne204() throws Exception {
        mvc.perform(delete("/api/v1/paiements/1"))
                .andExpect(status().isNoContent());
        verify(service).deletePaiement(1L);
    }
}
