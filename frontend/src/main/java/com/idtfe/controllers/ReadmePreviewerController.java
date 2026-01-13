package com.idtfe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import com.idtfe.services.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ReadmePreviewerController implements Initializable {
    
    @FXML private Button loadFileButton;
    @FXML private TextArea markdownArea;
    @FXML private WebView previewWebView;
    @FXML private Label statusLabel;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFileButton.setOnAction(e -> loadMarkdownFile());
        markdownArea.textProperty().addListener((obs, oldText, newText) -> renderPreview());
    }
    
    private void loadMarkdownFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Markdown File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Markdown Files", "*.md", "*.markdown")
        );
        
        File file = fileChooser.showOpenDialog(loadFileButton.getScene().getWindow());
        if (file != null) {
            try {
                String content = Files.readString(file.toPath());
                markdownArea.setText(content);
                statusLabel.setText("Loaded: " + file.getName());
            } catch (IOException e) {
                statusLabel.setText("Error loading file: " + e.getMessage());
            }
        }
    }
    
    private void renderPreview() {
        String markdown = markdownArea.getText();
        if (markdown.trim().isEmpty()) {
            previewWebView.getEngine().loadContent("");
            return;
        }
        
        new Thread(() -> {
            try {
                Map<String, String> requestData = new HashMap<>();
                requestData.put("content", markdown);
                
                String response = ApiClient.getInstance().post("/api/v1/tools/markdown/render", requestData);
                Map<String, Object> result = objectMapper.readValue(response, Map.class);
                String html = (String) result.get("html");
                
                javafx.application.Platform.runLater(() -> {
                    String styledHtml = "<html><head><style>" +
                        "body { font-family: Arial, sans-serif; margin: 20px; }" +
                        "code { background-color: #f4f4f4; padding: 2px 4px; border-radius: 3px; }" +
                        "pre { background-color: #f4f4f4; padding: 10px; border-radius: 5px; overflow-x: auto; }" +
                        "</style></head><body>" + html + "</body></html>";
                    
                    previewWebView.getEngine().loadContent(styledHtml);
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> 
                    statusLabel.setText("Preview error: " + e.getMessage()));
            }
        }).start();
    }
}