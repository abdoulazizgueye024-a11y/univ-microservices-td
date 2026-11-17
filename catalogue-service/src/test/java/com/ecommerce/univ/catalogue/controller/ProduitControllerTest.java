package com.ecommerce.univ.catalogue.controller;

import com.ecommerce.univ.catalogue.dto.ProduitRequestDTO;
import com.ecommerce.univ.catalogue.dto.ProduitResponseDTO;
import com.ecommerce.univ.catalogue.exception.GlobalExceptionHandler;
import com.ecommerce.univ.catalogue.exception.ResourceNotFoundException;
import com.ecommerce.univ.catalogue.service.ProduitService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProduitControllerTest {

    private ProduitService service;
    private MockMvc mvc;
    private LocalValidatorFactoryBean validator;

    private static final String VALID_JSON =
            "{\"nom\":\"Clavier\",\"description\":\"Test\","
            + "\"prix\":99.99,\"quantiteStock\":10}";

    @BeforeEach
    void setUp() {
        service = mock(ProduitService.class);

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mvc = MockMvcBuilders
                .standaloneSetup(new ProduitController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void postNomVide_retourne400() throws Exception {
        mvc.perform(post("/api/v1/produits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"  \",\"prix\":10,\"quantiteStock\":2}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.errors.nom").exists());

        verifyNoInteractions(service);
    }

    @Test
    void postPrixNegatif_retourne400() throws Exception {
        mvc.perform(post("/api/v1/produits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"Clavier\",\"prix\":-10,\"quantiteStock\":2}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.prix").exists());

        verifyNoInteractions(service);
    }

    @Test
    void postStockNegatif_retourne400() throws Exception {
        mvc.perform(post("/api/v1/produits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"Clavier\",\"prix\":10,\"quantiteStock\":-2}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.quantiteStock").exists());

        verifyNoInteractions(service);
    }

    @Test
    void postPrixAbsent_retourne400() throws Exception {
        mvc.perform(post("/api/v1/produits")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"Clavier\",\"quantiteStock\":2}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.prix").exists());

        verifyNoInteractions(service);
    }

    @Test
    void putDonneesInvalides_retourne400() throws Exception {
        mvc.perform(put("/api/v1/produits/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nom\":\"\",\"prix\":0,\"quantiteStock\":-1}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.nom").exists())
            .andExpect(jsonPath("$.errors.prix").exists())
            .andExpect(jsonPath("$.errors.quantiteStock").exists());

        verifyNoInteractions(service);
    }

    @Test
    void postValide_retourne201() throws Exception {
        when(service.createProduit(any(ProduitRequestDTO.class)))
                .thenReturn(new ProduitResponseDTO(
                        7L, "Clavier", "Test", 99.99, 10
                ));

        mvc.perform(post("/api/v1/produits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_JSON))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.nom").value("Clavier"));

        verify(service).createProduit(any(ProduitRequestDTO.class));
    }

    @Test
    void putValide_retourne200() throws Exception {
        when(service.updateProduit(eq(1L), any(ProduitRequestDTO.class)))
                .thenReturn(new ProduitResponseDTO(
                        1L, "Clavier", "Test", 99.99, 10
                ));

        mvc.perform(put("/api/v1/produits/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.nom").value("Clavier"));

        verify(service).updateProduit(
                eq(1L), any(ProduitRequestDTO.class)
        );
    }

    @Test
    void getProduitInexistant_retourne404() throws Exception {
        when(service.getProduitById(999L))
                .thenThrow(new ResourceNotFoundException(
                        "Produit non trouve avec l'id: 999"
                ));

        mvc.perform(get("/api/v1/produits/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"));
    }
}