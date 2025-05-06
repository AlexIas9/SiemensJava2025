package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    @InjectMocks
    private ItemService itemService;

    @Mock
    private ItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        List<Item> items = List.of(new Item(1L, "A", "desc", "NEW", "a@test.com"));
        when(itemRepository.findAll()).thenReturn(items);

        List<Item> result = itemService.findAll();

        assertEquals(1, result.size());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        Item item = new Item(1L, "Test", "desc", "NEW", "x@test.com");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        Optional<Item> result = itemService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test", result.get().getName());
    }

    @Test
    void testSave() {
        Item item = new Item(null, "Save Test", "desc", "NEW", "s@test.com");
        when(itemRepository.save(item)).thenReturn(item);

        Item saved = itemService.save(item);

        assertEquals("Save Test", saved.getName());
    }

    @Test
    void testDeleteById() {
        itemService.deleteById(1L);
        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    void testProcessItemsAsync() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        when(itemRepository.findAllIds()).thenReturn(ids);

        for (Long id : ids) {
            Item item = new Item(id, "Item" + id, "", "NEW", "i" + id + "@test.com");
            when(itemRepository.findById(id)).thenReturn(Optional.of(item));
            when(itemRepository.save(any())).thenReturn(item);
        }

        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> result = future.get(); // wait for async to complete

        assertEquals(2, result.size());
        for (Item item : result) {
            assertEquals("PROCESSED", item.getStatus());
        }
    }
}
