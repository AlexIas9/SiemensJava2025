package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllItems() throws Exception {
        List<Item> items = List.of(new Item(1L, "A", "desc", "NEW", "a@test.com"));
        Mockito.when(itemService.findAll()).thenReturn(items);

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("A"));
    }

    @Test
    void testCreateItemValid() throws Exception {
        Item item = new Item(null, "Valid", "desc", "NEW", "v@test.com");
        Mockito.when(itemService.save(any())).thenReturn(item);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateItemInvalidEmail() throws Exception {
        Item invalidItem = new Item(null, "Test Item", "Description", "NEW", "invalid-email");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email should be valid"));
    }




    @Test
    void testGetItemById() throws Exception {
        Item item = new Item(1L, "X", "desc", "NEW", "x@test.com");
        Mockito.when(itemService.findById(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("X"));
    }

    @Test
    void testProcessItemsAsync() throws Exception {
        // Răspunsul procesării asincrone
        List<Item> processed = List.of(new Item(1L, "Proc", "desc", "PROCESSED", "p@test.com"));
        Mockito.when(itemService.processItemsAsync())
                .thenReturn(CompletableFuture.completedFuture(processed));

        // Testarea procesării asincrone
        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk())  // Verifică dacă statusul este 200 OK
                .andExpect(jsonPath("$[0].status").value("PROCESSED"))  // Verifică statusul item-ului procesat
                .andExpect(jsonPath("$[0].name").value("Proc"))  // Verifică numele item-ului procesat
                .andExpect(jsonPath("$[0].email").value("p@test.com"));
    }
}
