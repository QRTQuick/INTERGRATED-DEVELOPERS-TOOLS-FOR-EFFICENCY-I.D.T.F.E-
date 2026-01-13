package com.idtfe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import com.idtfe.services.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ApiTesterController implements Initializable {
    
    @FXML private TextField urlField;
    @FXML private ComboBox<String> methodCombo;
    @FXML private TextArea headersArea;
    @FXML private TextArea bodyArea;
    @FXML private Button sendButton;
    @FXML private TextArea responseArea;
    @FXML private Label statusLabel;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        methodCombo.getItems().addAll("GET", "POST", "PUT", "DELETE");
        methodCombo.setValue("GET");
        
        sendButton.setOnAction(e -> sendRequest());
    }
    
    private void sendRequest() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            statusLabel.setText("Please enter a URL");
            return;
        }
        
        sendButton.setDisable(true);
        statusLabel.setText("Sending request...");
        
        new Thread(() -> {
            try {
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("url", url);
                requestData.put("method", methodCombo.getValue());
                requestData.put("headers", parseHeaders());
                requestData.put("body", bodyArea.getText());
                
                String response = ApiClient.getInstance().post("/api/v1/tools/api-tester", requestData);
                Map<String, Object> result = objectMapper.readValue(response, Map.class);
                
                javafx.application.Platform.runLater(() -> {
                    displayResponse(result);
                    sendButton.setDisable(false);
                    statusLabel.setText("Request completed");
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    responseArea.setText("Error: " + e.getMessage());
                    sendButton.setDisable(false);
                    statusLabel.setText("Request failed");
                });
            }
        }).start();
    }
    
    private Map<String, String> parseHeaders() {
        Map<String, String> headers = new HashMap<>();
        String[] lines = headersArea.getText().split("\n");
        
        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                headers.put(parts[0].trim(), parts[1].trim());
            }
        }
        
        return headers;
    }
    
    private void displayResponse(Map<String, Object> result) {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(result.get("status_code")).append("\n");
        sb.append("Duration: ").append(result.get("duration")).append("s\n\n");
        sb.append("Headers:\n");
        
        Map<String, Object> headers = (Map<String, Object>) result.get("headers");
        for (Map.Entry<String, Object> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        
        sb.append("\nBody:\n");
        Object body = result.get("body");
        if (body instanceof Map || body instanceof java.util.List) {
            try {
                sb.append(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(body));
            } catch (Exception e) {
                sb.append(body.toString());
            }
        } else {
            sb.append(body.toString());
        }
        
        responseArea.setText(sb.toString());
    }
}