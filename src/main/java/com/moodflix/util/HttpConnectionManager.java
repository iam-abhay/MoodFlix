package com.moodflix.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Optimized HTTP connection manager with connection pooling and request optimization
 */
public class HttpConnectionManager {
    
    // Connection pool settings
    private static final int MAX_CONNECTIONS = 20;
    private static final int CONNECT_TIMEOUT = 3000; // 3 seconds
    private static final int READ_TIMEOUT = 5000; // 5 seconds
    
    // Connection pool
    private static final Map<String, HttpURLConnection> connectionPool = new ConcurrentHashMap<>();
    private static final AtomicInteger activeConnections = new AtomicInteger(0);
    
    // Request optimization
    private static final Map<String, CompletableFuture<?>> pendingRequests = new ConcurrentHashMap<>();
    private static final ExecutorService httpExecutor = Executors.newFixedThreadPool(8);
    
    // Performance metrics
    private static final AtomicInteger totalRequests = new AtomicInteger(0);
    private static final AtomicInteger successfulRequests = new AtomicInteger(0);
    private static final AtomicInteger failedRequests = new AtomicInteger(0);
    
    /**
     * Make an optimized HTTP GET request
     */
    public static CompletableFuture<String> getAsync(String urlString) {
        return batchRequest(urlString, () -> {
            try {
                return CompletableFuture.completedFuture(makeGetRequest(urlString));
            } catch (Exception e) {
                System.err.println("GET request failed for " + urlString + ": " + e.getMessage());
                return CompletableFuture.failedFuture(e);
            }
        });
    }
    
    /**
     * Make an optimized HTTP POST request
     */
    public static CompletableFuture<String> postAsync(String urlString, String data) {
        String requestKey = urlString + ":" + data.hashCode();
        return batchRequest(requestKey, () -> {
            try {
                return CompletableFuture.completedFuture(makePostRequest(urlString, data));
            } catch (Exception e) {
                System.err.println("POST request failed for " + urlString + ": " + e.getMessage());
                return CompletableFuture.failedFuture(e);
            }
        });
    }
    
    /**
     * Batch requests to avoid duplicate calls
     */
    @SuppressWarnings("unchecked")
    private static <T> CompletableFuture<T> batchRequest(String key, Supplier<CompletableFuture<T>> supplier) {
        CompletableFuture<?> existing = pendingRequests.get(key);
        if (existing != null && !existing.isDone()) {
            return (CompletableFuture<T>) existing;
        }
        
        CompletableFuture<T> future = supplier.get();
        pendingRequests.put(key, future);
        
        future.whenComplete((result, throwable) -> {
            pendingRequests.remove(key);
        });
        
        return future;
    }
    
    /**
     * Make an optimized GET request
     */
    private static String makeGetRequest(String urlString) throws Exception {
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();
        
        try {
            HttpURLConnection conn = getConnection(urlString);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "MoodFlix/1.0");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Connection", "keep-alive");
            
            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);
            
            long duration = System.currentTimeMillis() - startTime;
            successfulRequests.incrementAndGet();
            
            System.out.println("[HTTP-OPT] GET " + urlString + " - " + responseCode + " (" + duration + "ms)");
            
            return response;
            
        } catch (Exception e) {
            failedRequests.incrementAndGet();
            throw e;
        }
    }
    
    /**
     * Make an optimized POST request
     */
    private static String makePostRequest(String urlString, String data) throws Exception {
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();
        
        try {
            HttpURLConnection conn = getConnection(urlString);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "MoodFlix/1.0");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setDoOutput(true);
            
            // Write request data
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = data.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            String response = readResponse(conn);
            
            long duration = System.currentTimeMillis() - startTime;
            successfulRequests.incrementAndGet();
            
            System.out.println("[HTTP-OPT] POST " + urlString + " - " + responseCode + " (" + duration + "ms)");
            
            return response;
            
        } catch (Exception e) {
            failedRequests.incrementAndGet();
            throw e;
        }
    }
    
    /**
     * Get or create connection with pooling
     */
    private static HttpURLConnection getConnection(String urlString) throws Exception {
        // Check if we have a reusable connection
        HttpURLConnection existing = connectionPool.get(urlString);
        if (existing != null && activeConnections.get() < MAX_CONNECTIONS) {
            try {
                // Test if connection is still valid
                existing.getResponseCode();
                return existing;
            } catch (Exception e) {
                // Connection is stale, remove it
                connectionPool.remove(urlString);
            }
        }
        
        // Create new connection
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        // Optimize connection settings
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setUseCaches(true);
        conn.setDefaultUseCaches(true);
        
        // Add to pool if we have space
        if (activeConnections.get() < MAX_CONNECTIONS) {
            connectionPool.put(urlString, conn);
            activeConnections.incrementAndGet();
        }
        
        return conn;
    }
    
    /**
     * Read response from connection
     */
    private static String readResponse(HttpURLConnection conn) throws IOException {
        InputStream inputStream;
        if (conn.getResponseCode() >= 400) {
            inputStream = conn.getErrorStream();
        } else {
            inputStream = conn.getInputStream();
        }
        
        if (inputStream == null) {
            return "";
        }
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
    
    /**
     * Get HTTP performance statistics
     */
    public static String getHttpStats() {
        int total = totalRequests.get();
        int successful = successfulRequests.get();
        int failed = failedRequests.get();
        double successRate = total > 0 ? (double) successful / total * 100 : 0;
        
        return String.format(
            "HTTP Performance Stats:\n" +
            "- Total Requests: %d\n" +
            "- Successful: %d\n" +
            "- Failed: %d\n" +
            "- Success Rate: %.1f%%\n" +
            "- Active Connections: %d\n" +
            "- Pooled Connections: %d",
            total,
            successful,
            failed,
            successRate,
            activeConnections.get(),
            connectionPool.size()
        );
    }
    
    /**
     * Clear connection pool
     */
    public static void clearConnectionPool() {
        connectionPool.clear();
        activeConnections.set(0);
        System.out.println("[HTTP-OPT] Connection pool cleared");
    }
    
    /**
     * Shutdown HTTP executor
     */
    public static void shutdown() {
        httpExecutor.shutdown();
        try {
            if (!httpExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                httpExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            httpExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 