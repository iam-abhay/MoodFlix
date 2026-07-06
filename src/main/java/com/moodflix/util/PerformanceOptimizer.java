package com.moodflix.util;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import javafx.application.Platform;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Advanced performance optimization utilities for the Moodflix application
 */
public class PerformanceOptimizer {
    
    // Enhanced thread pool configuration
    private static final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(
        Math.max(4, Runtime.getRuntime().availableProcessors() * 2)
    );
    
    // Connection pool for HTTP requests
    private static final ExecutorService httpExecutor = Executors.newFixedThreadPool(8);
    
    // Advanced caching system
    private static final Map<String, CacheEntry> dataCache = new ConcurrentHashMap<>();
    private static final Map<String, CacheEntry> userCache = new ConcurrentHashMap<>();
    private static final Map<String, CacheEntry> contentCache = new ConcurrentHashMap<>();
    private static final long DEFAULT_CACHE_TTL = 5 * 60 * 1000; // 5 minutes
    private static final long USER_CACHE_TTL = 10 * 60 * 1000; // 10 minutes
    private static final long CONTENT_CACHE_TTL = 15 * 60 * 1000; // 15 minutes
    
    // Request batching and deduplication
    private static final Map<String, CompletableFuture<?>> pendingRequests = new ConcurrentHashMap<>();
    private static final Map<String, Long> requestTimestamps = new ConcurrentHashMap<>();
    
    // Performance metrics and monitoring
    private static final AtomicInteger cacheHits = new AtomicInteger(0);
    private static final AtomicInteger cacheMisses = new AtomicInteger(0);
    private static final AtomicLong totalResponseTime = new AtomicLong(0);
    private static final AtomicInteger totalRequests = new AtomicInteger(0);
    
    // Memory management
    private static final int MAX_CACHE_SIZE = 1000;
    private static final long MEMORY_CLEANUP_INTERVAL = 5 * 60 * 1000; // 5 minutes
    
    // Background cleanup task
    static {
        scheduleCleanup();
    }
    
    /**
     * Enhanced cache entry with memory management
     */
    private static class CacheEntry {
        final Object data;
        final long timestamp;
        final long ttl;
        final int size; // Estimated memory size
        
        CacheEntry(Object data, long ttl) {
            this.data = data;
            this.timestamp = System.currentTimeMillis();
            this.ttl = ttl;
            this.size = estimateSize(data);
        }
        
        boolean isExpired() {
            return (System.currentTimeMillis() - timestamp) > ttl;
        }
        
        private int estimateSize(Object obj) {
            if (obj == null) return 0;
            if (obj instanceof String) return ((String) obj).length() * 2;
            if (obj instanceof java.util.List) return ((java.util.List<?>) obj).size() * 100;
            if (obj instanceof java.util.Map) return ((java.util.Map<?, ?>) obj).size() * 200;
            return 100; // Default estimate
        }
    }
    
    /**
     * Execute a task in background with performance monitoring
     */
    public static <T> CompletableFuture<T> runAsync(Supplier<T> task) {
        long startTime = System.currentTimeMillis();
        return CompletableFuture.supplyAsync(() -> {
            try {
                T result = task.get();
                long duration = System.currentTimeMillis() - startTime;
                totalResponseTime.addAndGet(duration);
                totalRequests.incrementAndGet();
                return result;
            } catch (Exception e) {
                System.err.println("Background task failed: " + e.getMessage());
                throw e;
            }
        }, backgroundExecutor);
    }
    
    /**
     * Execute HTTP requests with connection pooling
     */
    public static <T> CompletableFuture<T> runHttpAsync(Supplier<T> task) {
        return CompletableFuture.supplyAsync(task, httpExecutor);
    }
    
    /**
     * Update UI on JavaFX thread with error handling
     */
    public static void runOnUIThread(Runnable task) {
        if (Platform.isFxApplicationThread()) {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("UI task failed: " + e.getMessage());
            }
        } else {
            Platform.runLater(() -> {
                try {
                    task.run();
                } catch (Exception e) {
                    System.err.println("UI task failed: " + e.getMessage());
                }
            });
        }
    }
    
    /**
     * Get cached data or compute if not available with memory management
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCachedOrCompute(String key, Supplier<T> supplier) {
        return getCachedOrCompute(key, supplier, DEFAULT_CACHE_TTL);
    }
    
    /**
     * Get cached data or compute if not available with custom TTL
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCachedOrCompute(String key, Supplier<T> supplier, long ttl) {
        CacheEntry entry = dataCache.get(key);
        if (entry != null && !entry.isExpired()) {
            cacheHits.incrementAndGet();
            return (T) entry.data;
        }
        
        cacheMisses.incrementAndGet();
        T result = supplier.get();
        if (result != null) {
            // Check cache size before adding
            if (dataCache.size() >= MAX_CACHE_SIZE) {
                cleanupExpiredEntries(dataCache);
            }
            dataCache.put(key, new CacheEntry(result, ttl));
        }
        return result;
    }
    
    /**
     * User-specific caching with longer TTL
     */
    @SuppressWarnings("unchecked")
    public static <T> T getUserCachedOrCompute(String userEmail, String key, Supplier<T> supplier) {
        String cacheKey = userEmail + ":" + key;
        CacheEntry entry = userCache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            cacheHits.incrementAndGet();
            return (T) entry.data;
        }
        
        cacheMisses.incrementAndGet();
        T result = supplier.get();
        if (result != null) {
            if (userCache.size() >= MAX_CACHE_SIZE) {
                cleanupExpiredEntries(userCache);
            }
            userCache.put(cacheKey, new CacheEntry(result, USER_CACHE_TTL));
        }
        return result;
    }
    
    /**
     * Content-specific caching with longer TTL
     */
    @SuppressWarnings("unchecked")
    public static <T> T getContentCachedOrCompute(String key, Supplier<T> supplier) {
        CacheEntry entry = contentCache.get(key);
        if (entry != null && !entry.isExpired()) {
            cacheHits.incrementAndGet();
            return (T) entry.data;
        }
        
        cacheMisses.incrementAndGet();
        T result = supplier.get();
        if (result != null) {
            if (contentCache.size() >= MAX_CACHE_SIZE) {
                cleanupExpiredEntries(contentCache);
            }
            contentCache.put(key, new CacheEntry(result, CONTENT_CACHE_TTL));
        }
        return result;
    }
    
    /**
     * Batch multiple requests to avoid duplicate calls with timeout
     */
    @SuppressWarnings("unchecked")
    public static <T> CompletableFuture<T> batchRequest(String key, Supplier<CompletableFuture<T>> supplier) {
        return batchRequest(key, supplier, 5000); // 5 second timeout
    }
    
    /**
     * Batch multiple requests with custom timeout
     */
    @SuppressWarnings("unchecked")
    public static <T> CompletableFuture<T> batchRequest(String key, Supplier<CompletableFuture<T>> supplier, long timeoutMs) {
        CompletableFuture<T> existing = (CompletableFuture<T>) pendingRequests.get(key);
        if (existing != null && !existing.isDone()) {
            return existing;
        }
        
        // Check if request is too recent (debouncing)
        Long lastRequest = requestTimestamps.get(key);
        if (lastRequest != null && (System.currentTimeMillis() - lastRequest) < 100) {
            return existing != null ? existing : CompletableFuture.completedFuture(null);
        }
        
        requestTimestamps.put(key, System.currentTimeMillis());
        CompletableFuture<T> future = supplier.get();
        pendingRequests.put(key, future);
        
        // Add timeout
        CompletableFuture<T> timeoutFuture = future.orTimeout(timeoutMs, TimeUnit.MILLISECONDS);
        
        timeoutFuture.whenComplete((result, throwable) -> {
            pendingRequests.remove(key);
            requestTimestamps.remove(key);
        });
        
        return timeoutFuture;
    }
    
    /**
     * Clean up expired entries from cache
     */
    private static void cleanupExpiredEntries(Map<String, CacheEntry> cache) {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    /**
     * Schedule periodic cleanup
     */
    private static void scheduleCleanup() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                cleanupExpiredEntries(dataCache);
                cleanupExpiredEntries(userCache);
                cleanupExpiredEntries(contentCache);
                
                // Clean up old request timestamps
                long cutoff = System.currentTimeMillis() - 60000; // 1 minute
                requestTimestamps.entrySet().removeIf(entry -> entry.getValue() < cutoff);
                
                System.out.println("[PERF-OPT] Cache cleanup completed");
            } catch (Exception e) {
                System.err.println("[PERF-OPT] Cleanup failed: " + e.getMessage());
            }
        }, MEMORY_CLEANUP_INTERVAL, MEMORY_CLEANUP_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Get performance statistics
     */
    public static String getPerformanceStats() {
        int totalRequests = PerformanceOptimizer.totalRequests.get();
        long avgResponseTime = totalRequests > 0 ? PerformanceOptimizer.totalResponseTime.get() / totalRequests : 0;
        
        return String.format(
            "Performance Stats:\n" +
            "- Cache Hits: %d\n" +
            "- Cache Misses: %d\n" +
            "- Hit Rate: %.1f%%\n" +
            "- Total Requests: %d\n" +
            "- Avg Response Time: %dms\n" +
            "- Data Cache Size: %d\n" +
            "- User Cache Size: %d\n" +
            "- Content Cache Size: %d",
            cacheHits.get(),
            cacheMisses.get(),
            totalRequests > 0 ? (double) cacheHits.get() / (cacheHits.get() + cacheMisses.get()) * 100 : 0,
            totalRequests,
            avgResponseTime,
            dataCache.size(),
            userCache.size(),
            contentCache.size()
        );
    }
    
    /**
     * Clear all caches
     */
    public static void clearAllCaches() {
        dataCache.clear();
        userCache.clear();
        contentCache.clear();
        pendingRequests.clear();
        requestTimestamps.clear();
        System.out.println("[PERF-OPT] All caches cleared");
    }
    
    /**
     * Clear cache for specific user
     */
    public static void clearUserCache(String userEmail) {
        userCache.entrySet().removeIf(entry -> entry.getKey().startsWith(userEmail + ":"));
        System.out.println("[PERF-OPT] Cache cleared for user: " + userEmail);
    }
    
    /**
     * Shutdown all executors
     */
    public static void shutdown() {
        backgroundExecutor.shutdown();
        httpExecutor.shutdown();
        try {
            if (!backgroundExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                backgroundExecutor.shutdownNow();
            }
            if (!httpExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                httpExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            backgroundExecutor.shutdownNow();
            httpExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 