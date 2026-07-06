package com.moodflix.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Performance monitoring utility for tracking response times and identifying bottlenecks
 */
public class PerformanceMonitor {
    
    private static final Map<String, OperationStats> operationStats = new ConcurrentHashMap<>();
    private static final Map<String, Long> activeOperations = new ConcurrentHashMap<>();
    private static final AtomicLong totalOperations = new AtomicLong(0);
    private static final AtomicLong totalErrors = new AtomicLong(0);
    
    /**
     * Operation statistics
     */
    public static class OperationStats {
        private final AtomicLong count = new AtomicLong(0);
        private final AtomicLong totalTime = new AtomicLong(0);
        private final AtomicLong minTime = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxTime = new AtomicLong(0);
        private final AtomicLong errors = new AtomicLong(0);
        
        public void record(long duration, boolean success) {
            count.incrementAndGet();
            totalTime.addAndGet(duration);
            
            // Update min time
            long currentMin = minTime.get();
            while (duration < currentMin && !minTime.compareAndSet(currentMin, duration)) {
                currentMin = minTime.get();
            }
            
            // Update max time
            long currentMax = maxTime.get();
            while (duration > currentMax && !maxTime.compareAndSet(currentMax, duration)) {
                currentMax = maxTime.get();
            }
            
            if (!success) {
                errors.incrementAndGet();
            }
        }
        
        public long getCount() { return count.get(); }
        public long getTotalTime() { return totalTime.get(); }
        public long getMinTime() { return minTime.get(); }
        public long getMaxTime() { return maxTime.get(); }
        public long getErrors() { return errors.get(); }
        public double getAverageTime() { 
            long count = this.count.get();
            return count > 0 ? (double) totalTime.get() / count : 0;
        }
        public double getErrorRate() {
            long count = this.count.get();
            return count > 0 ? (double) errors.get() / count * 100 : 0;
        }
    }
    
    /**
     * Start timing an operation
     */
    public static String startOperation(String operationName) {
        String operationId = operationName + "_" + System.currentTimeMillis() + "_" + Thread.currentThread().getId();
        activeOperations.put(operationId, System.currentTimeMillis());
        return operationId;
    }
    
    /**
     * End timing an operation
     */
    public static void endOperation(String operationId, boolean success) {
        Long startTime = activeOperations.remove(operationId);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            String operationName = operationId.split("_")[0];
            
            OperationStats stats = operationStats.computeIfAbsent(operationName, k -> new OperationStats());
            stats.record(duration, success);
            
            totalOperations.incrementAndGet();
            if (!success) {
                totalErrors.incrementAndGet();
            }
        }
    }
    
    /**
     * Record a completed operation
     */
    public static void recordOperation(String operationName, long duration, boolean success) {
        OperationStats stats = operationStats.computeIfAbsent(operationName, k -> new OperationStats());
        stats.record(duration, success);
        
        totalOperations.incrementAndGet();
        if (!success) {
            totalErrors.incrementAndGet();
        }
    }
    
    /**
     * Get statistics for a specific operation
     */
    public static OperationStats getStats(String operationName) {
        return operationStats.get(operationName);
    }
    
    /**
     * Get all operation statistics
     */
    public static Map<String, OperationStats> getAllStats() {
        return new ConcurrentHashMap<>(operationStats);
    }
    
    /**
     * Get performance summary
     */
    public static String getPerformanceSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("=== Performance Summary ===\n");
        summary.append("Total Operations: ").append(totalOperations.get()).append("\n");
        summary.append("Total Errors: ").append(totalErrors.get()).append("\n");
        summary.append("Overall Error Rate: ").append(String.format("%.2f%%", 
            totalOperations.get() > 0 ? (double) totalErrors.get() / totalOperations.get() * 100 : 0)).append("\n\n");
        
        // Sort operations by average time (slowest first)
        List<Map.Entry<String, OperationStats>> sortedStats = new ArrayList<>(operationStats.entrySet());
        sortedStats.sort((a, b) -> Double.compare(b.getValue().getAverageTime(), a.getValue().getAverageTime()));
        
        for (Map.Entry<String, OperationStats> entry : sortedStats) {
            String operationName = entry.getKey();
            OperationStats stats = entry.getValue();
            
            summary.append("Operation: ").append(operationName).append("\n");
            summary.append("  Count: ").append(stats.getCount()).append("\n");
            summary.append("  Avg Time: ").append(String.format("%.2fms", stats.getAverageTime())).append("\n");
            summary.append("  Min Time: ").append(stats.getMinTime()).append("ms\n");
            summary.append("  Max Time: ").append(stats.getMaxTime()).append("ms\n");
            summary.append("  Error Rate: ").append(String.format("%.2f%%", stats.getErrorRate())).append("\n");
            summary.append("  Total Time: ").append(stats.getTotalTime()).append("ms\n\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Get slowest operations
     */
    public static List<String> getSlowestOperations(int count) {
        List<Map.Entry<String, OperationStats>> sortedStats = new ArrayList<>(operationStats.entrySet());
        sortedStats.sort((a, b) -> Double.compare(b.getValue().getAverageTime(), a.getValue().getAverageTime()));
        
        List<String> slowest = new ArrayList<>();
        for (int i = 0; i < Math.min(count, sortedStats.size()); i++) {
            slowest.add(sortedStats.get(i).getKey());
        }
        return slowest;
    }
    
    /**
     * Get operations with highest error rates
     */
    public static List<String> getOperationsWithHighestErrorRate(int count) {
        List<Map.Entry<String, OperationStats>> sortedStats = new ArrayList<>(operationStats.entrySet());
        sortedStats.sort((a, b) -> Double.compare(b.getValue().getErrorRate(), a.getValue().getErrorRate()));
        
        List<String> highestErrorRate = new ArrayList<>();
        for (int i = 0; i < Math.min(count, sortedStats.size()); i++) {
            highestErrorRate.add(sortedStats.get(i).getKey());
        }
        return highestErrorRate;
    }
    
    /**
     * Clear all statistics
     */
    public static void clearStats() {
        operationStats.clear();
        activeOperations.clear();
        totalOperations.set(0);
        totalErrors.set(0);
    }
    
    /**
     * Check if an operation is taking too long
     */
    public static boolean isOperationSlow(String operationName, long thresholdMs) {
        OperationStats stats = operationStats.get(operationName);
        return stats != null && stats.getAverageTime() > thresholdMs;
    }
    
    /**
     * Get recommendations for performance improvements
     */
    public static String getPerformanceRecommendations() {
        StringBuilder recommendations = new StringBuilder();
        recommendations.append("=== Performance Recommendations ===\n\n");
        
        // Check for slow operations
        List<String> slowestOperations = getSlowestOperations(5);
        if (!slowestOperations.isEmpty()) {
            recommendations.append("Slow Operations (consider optimization):\n");
            for (String operation : slowestOperations) {
                OperationStats stats = operationStats.get(operation);
                if (stats.getAverageTime() > 1000) { // More than 1 second
                    recommendations.append("  - ").append(operation)
                                 .append(" (avg: ").append(String.format("%.2fms", stats.getAverageTime()))
                                 .append(")\n");
                }
            }
            recommendations.append("\n");
        }
        
        // Check for high error rates
        List<String> highErrorOperations = getOperationsWithHighestErrorRate(5);
        if (!highErrorOperations.isEmpty()) {
            recommendations.append("Operations with High Error Rates:\n");
            for (String operation : highErrorOperations) {
                OperationStats stats = operationStats.get(operation);
                if (stats.getErrorRate() > 5) { // More than 5% error rate
                    recommendations.append("  - ").append(operation)
                                 .append(" (error rate: ").append(String.format("%.2f%%", stats.getErrorRate()))
                                 .append(")\n");
                }
            }
            recommendations.append("\n");
        }
        
        // General recommendations
        recommendations.append("General Recommendations:\n");
        recommendations.append("  - Use caching for frequently accessed data\n");
        recommendations.append("  - Implement connection pooling for HTTP requests\n");
        recommendations.append("  - Use async operations for non-blocking UI\n");
        recommendations.append("  - Consider implementing request batching\n");
        recommendations.append("  - Monitor and optimize database queries\n");
        
        return recommendations.toString();
    }
} 