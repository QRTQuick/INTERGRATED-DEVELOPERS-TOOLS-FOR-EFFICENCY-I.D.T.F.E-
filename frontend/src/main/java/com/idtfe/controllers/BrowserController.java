package com.idtfe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import com.idtfe.services.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class BrowserController implements Initializable {
    
    @FXML private TextField urlField;
    @FXML private Button goButton;
    @FXML private Button backButton;
    @FXML private Button forwardButton;
    @FXML private Button refreshButton;
    @FXML private Button homeButton;
    @FXML private Button formatJsonButton;
    @FXML private WebView webView;
    @FXML private ProgressBar loadingProgress;
    @FXML private Label statusLabel;
    
    private WebEngine webEngine;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String HOME_URL = "https://www.google.com";
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        webEngine = webView.getEngine();
        
        // Set up button actions
        goButton.setOnAction(e -> navigateToUrl());
        backButton.setOnAction(e -> webEngine.getHistory().go(-1));
        forwardButton.setOnAction(e -> webEngine.getHistory().go(1));
        refreshButton.setOnAction(e -> webEngine.reload());
        homeButton.setOnAction(e -> navigateToHome());
        formatJsonButton.setOnAction(e -> formatCurrentPageAsJson());
        
        // Handle Enter key in URL field
        urlField.setOnAction(e -> navigateToUrl());
        
        // Update URL field when page changes
        webEngine.locationProperty().addListener((obs, oldLocation, newLocation) -> {
            if (newLocation != null) {
                urlField.setText(newLocation);
            }
        });
        
        // Update loading progress
        webEngine.getLoadWorker().progressProperty().addListener((obs, oldProgress, newProgress) -> {
            loadingProgress.setProgress(newProgress.doubleValue());
            if (newProgress.doubleValue() >= 1.0) {
                loadingProgress.setVisible(false);
                statusLabel.setText("Page loaded");
            } else {
                loadingProgress.setVisible(true);
                statusLabel.setText("Loading... " + Math.round(newProgress.doubleValue() * 100) + "%");
            }
        });
        
        // Update navigation buttons
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                backButton.setDisable(webEngine.getHistory().getCurrentIndex() <= 0);
                forwardButton.setDisable(webEngine.getHistory().getCurrentIndex() >= 
                    webEngine.getHistory().getEntries().size() - 1);
            }
        });
        
        // Handle page title changes
        webEngine.titleProperty().addListener((obs, oldTitle, newTitle) -> {
            if (newTitle != null && !newTitle.isEmpty()) {
                statusLabel.setText("Loaded: " + newTitle);
            }
        });
        
        // Initialize with home page
        urlField.setText(HOME_URL);
        loadingProgress.setVisible(false);
        statusLabel.setText("Ready - Enter a URL to browse");
        
        // Disable navigation buttons initially
        backButton.setDisable(true);
        forwardButton.setDisable(true);
    }
    
    private void navigateToUrl() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            statusLabel.setText("Please enter a URL");
            return;
        }
        
        // Add protocol if missing
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
            urlField.setText(url);
        }
        
        try {
            webEngine.load(url);
            statusLabel.setText("Loading " + url + "...");
        } catch (Exception e) {
            statusLabel.setText("Error loading URL: " + e.getMessage());
        }
    }
    
    private void navigateToHome() {
        urlField.setText(HOME_URL);
        navigateToUrl();
    }
    
    private void formatCurrentPageAsJson() {
        String currentUrl = webEngine.getLocation();
        if (currentUrl == null || currentUrl.isEmpty()) {
            statusLabel.setText("No page loaded");
            return;
        }
        
        // Check if the current page might be JSON/API content
        String content = (String) webEngine.executeScript("document.body.innerText");
        if (content == null || content.trim().isEmpty()) {
            statusLabel.setText("No content to format");
            return;
        }
        
        // Try to detect if it's JSON
        String trimmedContent = content.trim();
        if ((trimmedContent.startsWith("{") && trimmedContent.endsWith("}")) ||
            (trimmedContent.startsWith("[") && trimmedContent.endsWith("]"))) {
            
            formatJsonContent(trimmedContent);
        } else {
            statusLabel.setText("Current page doesn't appear to be JSON");
        }
    }
    
    private void formatJsonContent(String jsonContent) {
        statusLabel.setText("Formatting JSON...");
        
        new Thread(() -> {
            try {
                // Parse and pretty-print JSON
                Object jsonObject = objectMapper.readValue(jsonContent, Object.class);
                String formattedJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(jsonObject);
                
                javafx.application.Platform.runLater(() -> {
                    // Create a formatted HTML page
                    String htmlContent = "<html><head><title>Formatted JSON</title>" +
                        "<style>body { font-family: 'Consolas', 'Monaco', monospace; " +
                        "background-color: #f8f8f8; padding: 20px; } " +
                        "pre { background-color: white; padding: 15px; border-radius: 5px; " +
                        "border: 1px solid #ddd; overflow-x: auto; }</style></head>" +
                        "<body><h2>Formatted JSON</h2><pre>" + 
                        formattedJson.replace("<", "&lt;").replace(">", "&gt;") + 
                        "</pre></body></html>";
                    
                    webEngine.loadContent(htmlContent);
                    statusLabel.setText("JSON formatted successfully");
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> 
                    statusLabel.setText("JSON format error: " + e.getMessage()));
            }
        }).start();
    }
    
    public void loadUrl(String url) {
        urlField.setText(url);
        navigateToUrl();
    }
}