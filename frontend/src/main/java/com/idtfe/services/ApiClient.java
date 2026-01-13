package com.idtfe.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.io.IOException;
import java.util.Map;

public class ApiClient {
    private static ApiClient instance;
    
    // Backend URL - Change this to your Render deployment URL
    private final String baseUrl = "https://intergrated-developers-tools-for.onrender.com";
    // For Render deployment, use: "https://your-service-name.onrender.com"
    
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    private ApiClient() {
        this.httpClient = HttpClients.createDefault();
        this.objectMapper = new ObjectMapper();
    }
    
    public static ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }
    
    public void initialize() {
        // Initialization logic
        System.out.println("API Client initialized with base URL: " + baseUrl);
    }
    
    public void checkHealth() {
        new Thread(() -> {
            try {
                HttpGet request = new HttpGet(baseUrl + "/api/v1/health");
                var response = httpClient.execute(request);
                String responseBody = EntityUtils.toString(response.getEntity());
                
                if (response.getStatusLine().getStatusCode() != 200) {
                    Platform.runLater(() -> showError("Backend connection failed"));
                } else {
                    System.out.println("Backend health check successful: " + responseBody);
                }
            } catch (IOException e) {
                Platform.runLater(() -> showError("Cannot connect to backend: " + e.getMessage()));
            }
        }).start();
    }
    
    public String get(String endpoint) throws IOException {
        HttpGet request = new HttpGet(baseUrl + endpoint);
        var response = httpClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    public String rawGet(String fullUrl) throws IOException {
        HttpGet request = new HttpGet(fullUrl);
        var response = httpClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }
    
    public String post(String endpoint, Object data) throws IOException {
        HttpPost request = new HttpPost(baseUrl + endpoint);
        request.setHeader("Content-Type", "application/json");
        
        String json = objectMapper.writeValueAsString(data);
        request.setEntity(new StringEntity(json));
        
        var response = httpClient.execute(request);
        return EntityUtils.toString(response.getEntity());
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Connection Error");
        alert.setHeaderText("Backend Connection Failed");
        alert.setContentText(message + "\n\nBackend URL: " + baseUrl);
        alert.showAndWait();
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
}