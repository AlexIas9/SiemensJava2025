package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class ItemService {

    @Autowired
    private ItemRepository itemRepository;

    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * Asynchronously processes all items:
     * - fetches IDs
     * - updates each item status
     * - saves changes to DB
     * - returns all successfully processed items
     */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();
        List<Item> processedItems = new CopyOnWriteArrayList<>();

        List<CompletableFuture<Void>> futures = itemIds.stream().map(id ->
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(100); // simulate work

                        itemRepository.findById(id).ifPresent(item -> {
                            item.setStatus("PROCESSED");
                            itemRepository.save(item);
                            processedItems.add(item);
                        });

                    } catch (Exception e) {
                        System.err.println("Failed to process item " + id + ": " + e.getMessage());
                    }
                }, executor)
        ).collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> processedItems);
    }
}
