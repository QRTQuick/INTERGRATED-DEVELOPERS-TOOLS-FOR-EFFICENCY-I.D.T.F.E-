package com.idtfe.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idtfe.services.ApiClient;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class AiSettingsController implements Initializable {
    @FXML private TextField grocUrlField;
    @FXML private PasswordField aiKeyField;
    @FXML private Button saveBtn;
    @FXML private Button testBtn;
    @FXML private TextArea outputArea;

    private final ApiClient api = ApiClient.getInstance();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        saveBtn.setOnAction(e -> saveConfig());
        testBtn.setOnAction(e -> testConfig());
        loadCurrent();
    }

    private void loadCurrent() {
        // attempt to read current status from backend
        new Thread(() -> {
            try {
                String resp = api.get("/api/v1/tools/github/status");
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                javafx.application.Platform.runLater(() -> outputArea.setText("GitHub status: " + data.toString()));
            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> outputArea.setText("Failed to load status: " + e.getMessage()));
            }
        }).start();
    }

    private void saveConfig() {
        String url = grocUrlField.getText();
        String key = aiKeyField.getText();
        new Thread(() -> {
            try {
                var payload = Map.of("ai_key", key, "groc_url", url);
                String resp = api.post("/api/v1/tools/ai/config", payload);
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                javafx.application.Platform.runLater(() -> outputArea.setText("Saved: " + data.toString()));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> outputArea.setText("Save failed: " + e.getMessage()));
            }
        }).start();
    }

    private void testConfig() {
        String prompt = "Test message from I.D.T.F.E";
        new Thread(() -> {
            try {
                String resp = api.post("/api/v1/tools/assistant", Map.of("prompt", prompt));
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                javafx.application.Platform.runLater(() -> outputArea.setText("AI response: " + data.get("reply")));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> outputArea.setText("Test failed: " + e.getMessage()));
            }
        }).start();
    }
}
