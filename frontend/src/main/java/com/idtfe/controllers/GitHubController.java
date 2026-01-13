package com.idtfe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import com.idtfe.services.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class GitHubController implements Initializable {
    @FXML private TextField tokenField;
    @FXML private Button linkButton;
    @FXML private Label statusLabel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        linkButton.setOnAction(e -> linkAccount());
        // Check status
        new Thread(() -> {
            try {
                String resp = ApiClient.getInstance().get("/api/v1/tools/github/status");
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                javafx.application.Platform.runLater(() -> {
                    if ((Boolean) data.getOrDefault("success", false)) {
                        statusLabel.setText("Linked: " + data.getOrDefault("username", ""));
                    } else {
                        statusLabel.setText((String) data.getOrDefault("message", "Not linked"));
                    }
                });
            } catch (IOException ex) {
                javafx.application.Platform.runLater(() -> statusLabel.setText("Status check failed"));
            }
        }).start();
    }

    private void linkAccount() {
        String token = tokenField.getText().trim();
        if (token.isEmpty()) {
            statusLabel.setText("Enter a token first");
            return;
        }

        statusLabel.setText("Linking...");
        new Thread(() -> {
            try {
                String resp = ApiClient.getInstance().post("/api/v1/tools/github/link", Map.of("token", token));
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                javafx.application.Platform.runLater(() -> {
                    if ((Boolean) data.getOrDefault("success", false)) {
                        statusLabel.setText("Linked: " + data.getOrDefault("username", ""));
                    } else {
                        statusLabel.setText((String) data.getOrDefault("message", "Link failed"));
                    }
                });
            } catch (IOException ex) {
                javafx.application.Platform.runLater(() -> statusLabel.setText("Link request failed"));
            }
        }).start();
    }
}
