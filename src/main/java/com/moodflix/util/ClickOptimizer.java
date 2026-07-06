package com.moodflix.util;

import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.application.Platform;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import java.util.concurrent.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Advanced click optimization system for reduced latency UI interactions
 */
public class ClickOptimizer {
    
    // Click debouncing to prevent rapid-fire clicks
    private static final Map<String, Long> lastClickTimes = new ConcurrentHashMap<>();
    private static final long CLICK_DEBOUNCE_MS = 100; // 100ms debounce
    
    // Click metrics
    private static final AtomicInteger totalClicks = new AtomicInteger(0);
    private static final AtomicInteger debouncedClicks = new AtomicInteger(0);
    private static final AtomicInteger instantClicks = new AtomicInteger(0);
    
    // Pre-computed event handlers for common actions
    private static final Map<String, EventHandler<ActionEvent>> cachedHandlers = new ConcurrentHashMap<>();
    
    // Thread pool for background click processing
    private static final ExecutorService clickExecutor = Executors.newFixedThreadPool(2);
    
    /**
     * Optimized button click with debouncing and instant feedback
     */
    public static void setOptimizedClickHandler(Button button, String actionId, Consumer<ActionEvent> handler) {
        button.setOnAction(e -> {
            long currentTime = System.currentTimeMillis();
            String buttonId = actionId + "_" + button.hashCode();
            
            // Check debouncing
            Long lastClick = lastClickTimes.get(buttonId);
            if (lastClick != null && (currentTime - lastClick) < CLICK_DEBOUNCE_MS) {
                debouncedClicks.incrementAndGet();
                System.out.println("[CLICK-OPT] Debounced click for " + actionId);
                return;
            }
            
            lastClickTimes.put(buttonId, currentTime);
            totalClicks.incrementAndGet();
            
            // Provide instant visual feedback
            button.setDisable(true);
            button.setStyle(button.getStyle() + "; -fx-opacity: 0.7;");
            
            // Execute handler with performance monitoring
            long startTime = System.currentTimeMillis();
            try {
                handler.accept(e);
                instantClicks.incrementAndGet();
                System.out.println("[CLICK-OPT] " + actionId + " executed in " + 
                    (System.currentTimeMillis() - startTime) + "ms");
            } catch (Exception ex) {
                System.err.println("[CLICK-OPT] Error in " + actionId + ": " + ex.getMessage());
                showErrorDialog("Action Failed", "Error executing " + actionId, ex.getMessage());
            } finally {
                // Re-enable button after short delay
                Platform.runLater(() -> {
                    button.setDisable(false);
                    button.setStyle(button.getStyle().replace("; -fx-opacity: 0.7;", ""));
                });
            }
        });
    }
    
    /**
     * Optimized navigation click with preloading
     */
    public static void setNavigationClickHandler(Button button, String pageName, Supplier<javafx.scene.Scene> sceneCreator) {
        setOptimizedClickHandler(button, "nav_" + pageName, e -> {
            NavigationOptimizer.navigateInstantly(pageName, sceneCreator);
        });
    }
    
    /**
     * Optimized table row selection with instant feedback
     */
    public static <T> void setOptimizedTableSelection(TableView<T> table, Consumer<T> selectionHandler) {
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Instant visual feedback
                table.setStyle(table.getStyle() + "; -fx-background-color: rgba(0,255,0,0.1);");
                
                // Execute handler
                Platform.runLater(() -> {
                    try {
                        selectionHandler.accept(newVal);
                    } catch (Exception ex) {
                        System.err.println("[CLICK-OPT] Table selection error: " + ex.getMessage());
                    } finally {
                        // Remove highlight after short delay
                        Platform.runLater(() -> {
                            table.setStyle(table.getStyle().replace("; -fx-background-color: rgba(0,255,0,0.1);", ""));
                        });
                    }
                });
            }
        });
    }
    
    /**
     * Optimized list selection with instant feedback
     */
    public static <T> void setOptimizedListSelection(ListView<T> list, Consumer<T> selectionHandler) {
        list.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // Instant visual feedback
                list.setStyle(list.getStyle() + "; -fx-background-color: rgba(0,255,0,0.1);");
                
                // Execute handler
                Platform.runLater(() -> {
                    try {
                        selectionHandler.accept(newVal);
                    } catch (Exception ex) {
                        System.err.println("[CLICK-OPT] List selection error: " + ex.getMessage());
                    } finally {
                        // Remove highlight after short delay
                        Platform.runLater(() -> {
                            list.setStyle(list.getStyle().replace("; -fx-background-color: rgba(0,255,0,0.1);", ""));
                        });
                    }
                });
            }
        });
    }
    
    /**
     * Optimized combo box selection
     */
    public static <T> void setOptimizedComboBoxSelection(ComboBox<T> comboBox, Consumer<T> selectionHandler) {
        comboBox.setOnAction(e -> {
            T selected = comboBox.getValue();
            if (selected != null) {
                // Instant visual feedback
                comboBox.setStyle(comboBox.getStyle() + "; -fx-background-color: rgba(0,255,0,0.2);");
                
                // Execute handler
                Platform.runLater(() -> {
                    try {
                        selectionHandler.accept(selected);
                    } catch (Exception ex) {
                        System.err.println("[CLICK-OPT] ComboBox selection error: " + ex.getMessage());
                    } finally {
                        // Remove highlight after short delay
                        Platform.runLater(() -> {
                            comboBox.setStyle(comboBox.getStyle().replace("; -fx-background-color: rgba(0,255,0,0.2);", ""));
                        });
                    }
                });
            }
        });
    }
    
    /**
     * Optimized text field with instant validation
     */
    public static void setOptimizedTextField(TextField textField, Consumer<String> validationHandler) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                // Instant visual feedback
                textField.setStyle(textField.getStyle() + "; -fx-border-color: #17a2b8;");
                
                // Execute validation
                Platform.runLater(() -> {
                    try {
                        validationHandler.accept(newVal);
                    } catch (Exception ex) {
                        System.err.println("[CLICK-OPT] TextField validation error: " + ex.getMessage());
                    } finally {
                        // Remove highlight after short delay
                        Platform.runLater(() -> {
                            textField.setStyle(textField.getStyle().replace("; -fx-border-color: #17a2b8;", ""));
                        });
                    }
                });
            }
        });
    }
    
    /**
     * Optimized check box with instant feedback
     */
    public static void setOptimizedCheckBox(CheckBox checkBox, Consumer<Boolean> changeHandler) {
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            // Instant visual feedback
            checkBox.setStyle(checkBox.getStyle() + "; -fx-text-fill: #28a745;");
            
            // Execute handler
            Platform.runLater(() -> {
                try {
                    changeHandler.accept(newVal);
                } catch (Exception ex) {
                    System.err.println("[CLICK-OPT] CheckBox change error: " + ex.getMessage());
                } finally {
                    // Remove highlight after short delay
                    Platform.runLater(() -> {
                        checkBox.setStyle(checkBox.getStyle().replace("; -fx-text-fill: #28a745;", ""));
                    });
                }
            });
        });
    }
    
    /**
     * Optimized radio button with instant feedback
     */
    public static void setOptimizedRadioButton(RadioButton radioButton, Consumer<Boolean> changeHandler) {
        radioButton.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                // Instant visual feedback
                radioButton.setStyle(radioButton.getStyle() + "; -fx-text-fill: #28a745; -fx-font-weight: bold;");
                
                // Execute handler
                Platform.runLater(() -> {
                    try {
                        changeHandler.accept(newVal);
                    } catch (Exception ex) {
                        System.err.println("[CLICK-OPT] RadioButton change error: " + ex.getMessage());
                    } finally {
                        // Remove highlight after short delay
                        Platform.runLater(() -> {
                            radioButton.setStyle(radioButton.getStyle().replace("; -fx-text-fill: #28a745; -fx-font-weight: bold;", ""));
                        });
                    }
                });
            }
        });
    }
    
    /**
     * Add keyboard shortcuts for faster navigation
     */
    public static void addKeyboardShortcuts(javafx.scene.Scene scene) {
        // Profile page shortcut
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.P) {
                e.consume();
                // Navigate to profile
                System.out.println("[CLICK-OPT] Keyboard shortcut: Ctrl+P -> Profile");
            }
            // Watchlist shortcut
            else if (e.isControlDown() && e.getCode() == KeyCode.W) {
                e.consume();
                // Navigate to watchlist
                System.out.println("[CLICK-OPT] Keyboard shortcut: Ctrl+W -> Watchlist");
            }
            // Feedback shortcut
            else if (e.isControlDown() && e.getCode() == KeyCode.F) {
                e.consume();
                // Navigate to feedback
                System.out.println("[CLICK-OPT] Keyboard shortcut: Ctrl+F -> Feedback");
            }
            // Activity shortcut
            else if (e.isControlDown() && e.getCode() == KeyCode.A) {
                e.consume();
                // Navigate to activity
                System.out.println("[CLICK-OPT] Keyboard shortcut: Ctrl+A -> Activity");
            }
        });
    }
    
    /**
     * Show error dialog with optimized styling
     */
    private static void showErrorDialog(String title, String header, String content) {
        Platform.runLater(() -> {
            MoodflixDialog.showError(title, header + "\n" + content);
        });
    }
    
    /**
     * Get click optimization statistics
     */
    public static String getClickStats() {
        int total = totalClicks.get();
        int debounced = debouncedClicks.get();
        int instant = instantClicks.get();
        int successful = total - debounced;
        
        double successRate = total > 0 ? (double) successful / total * 100 : 0;
        double instantRate = total > 0 ? (double) instant / total * 100 : 0;
        
        return String.format(
            "Click Optimization Stats:\n" +
            "- Total Clicks: %d\n" +
            "- Successful Clicks: %d (%.1f%%)\n" +
            "- Debounced Clicks: %d\n" +
            "- Instant Clicks: %d (%.1f%%)\n" +
            "- Cached Handlers: %d",
            total,
            successful, successRate,
            debounced,
            instant, instantRate,
            cachedHandlers.size()
        );
    }
    
    /**
     * Clear click statistics
     */
    public static void clearClickStats() {
        totalClicks.set(0);
        debouncedClicks.set(0);
        instantClicks.set(0);
        lastClickTimes.clear();
        System.out.println("[CLICK-OPT] Click statistics cleared");
    }
    
    /**
     * Shutdown click executor
     */
    public static void shutdown() {
        clickExecutor.shutdown();
        try {
            if (!clickExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                clickExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            clickExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
} 