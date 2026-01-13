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

public class AssistantController implements Initializable {
    @FXML private ListView<String> messagesList;
    @FXML private TextArea inputArea;
    @FXML private Button sendBtn;
    @FXML private Button clearBtn;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiClient api = ApiClient.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sendBtn.setOnAction(e -> sendPrompt());
        clearBtn.setOnAction(e -> messagesList.getItems().clear());
    }

    private void sendPrompt() {
        String prompt = inputArea.getText();
        if (prompt == null || prompt.isBlank()) return;
        messagesList.getItems().add("You: " + prompt);
        inputArea.clear();

        new Thread(() -> {
            try {
                String resp = api.post("/api/v1/tools/assistant", Map.of("prompt", prompt));
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                String reply = (String) data.getOrDefault("reply", "(no reply)");
                javafx.application.Platform.runLater(() -> messagesList.getItems().add("Assistant: " + reply));
            } catch (IOException ex) {
                javafx.application.Platform.runLater(() -> messagesList.getItems().add("Error: " + ex.getMessage()));
            }
        }).start();
    }
}
