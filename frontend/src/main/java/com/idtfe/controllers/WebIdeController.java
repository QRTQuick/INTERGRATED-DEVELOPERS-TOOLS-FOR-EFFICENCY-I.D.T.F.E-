package com.idtfe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import com.idtfe.services.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class WebIdeController implements Initializable {
    
    @FXML private Button openFileButton;
    @FXML private Button openFolderButton;
    @FXML private Button saveFileButton;
    @FXML private Button formatButton;
    @FXML private Button lintButton;
    @FXML private Button previewButton;
    @FXML private ComboBox<String> fileTypeCombo;
    @FXML private TextArea codeEditor;
    @FXML private WebView previewWebView;
    @FXML private TextArea lintOutput;
    @FXML private Label statusLabel;
    @FXML private Label filePathLabel;
    @FXML private TreeView<String> fileTreeView;
    @FXML private CheckBox autoDetectResourcesCheckBox;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private File currentFile;
    private File currentDirectory;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize file type combo
        fileTypeCombo.getItems().addAll("html", "css", "javascript", "markdown", "json", "xml", "text");
        fileTypeCombo.setValue("html");
        
        // Set up button actions
        openFileButton.setOnAction(e -> openFile());
        openFolderButton.setOnAction(e -> openFolder());
        saveFileButton.setOnAction(e -> saveFile());
        formatButton.setOnAction(e -> formatCode());
        lintButton.setOnAction(e -> lintCode());
        previewButton.setOnAction(e -> previewCode());
        
        // Auto-detect resources checkbox
        autoDetectResourcesCheckBox.setSelected(true);
        autoDetectResourcesCheckBox.setText("Auto-detect CSS/JS files");
        
        // File tree selection handler
        fileTreeView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.isLeaf()) {
                loadFileFromTree(newSelection.getValue());
            }
        });
        
        // Auto-detect file type when content changes
        codeEditor.textProperty().addListener((obs, oldText, newText) -> {
            if (currentFile != null) {
                String fileName = currentFile.getName().toLowerCase();
                if (fileName.endsWith(".html")) fileTypeCombo.setValue("html");
                else if (fileName.endsWith(".css")) fileTypeCombo.setValue("css");
                else if (fileName.endsWith(".js")) fileTypeCombo.setValue("javascript");
                else if (fileName.endsWith(".md")) fileTypeCombo.setValue("markdown");
                else if (fileName.endsWith(".json")) fileTypeCombo.setValue("json");
                else if (fileName.endsWith(".xml")) fileTypeCombo.setValue("xml");
            }
        });
        
        statusLabel.setText("Ready - Open a file or folder to start editing");
    }
    
    private void openFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("HTML Files", "*.html", "*.htm"),
            new FileChooser.ExtensionFilter("CSS Files", "*.css"),
            new FileChooser.ExtensionFilter("JavaScript Files", "*.js"),
            new FileChooser.ExtensionFilter("Markdown Files", "*.md", "*.markdown"),
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );
        
        File file = fileChooser.showOpenDialog(openFileButton.getScene().getWindow());
        if (file != null) {
            loadFile(file);
            // Set current directory to file's parent
            currentDirectory = file.getParentFile();
            refreshFileTree();
        }
    }
    
    private void openFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Project Folder");
        
        File directory = directoryChooser.showDialog(openFolderButton.getScene().getWindow());
        if (directory != null) {
            currentDirectory = directory;
            refreshFileTree();
            statusLabel.setText("Opened folder: " + directory.getName());
        }
    }
    
    private void refreshFileTree() {
        if (currentDirectory == null) return;
        
        TreeItem<String> rootItem = new TreeItem<>(currentDirectory.getName());
        rootItem.setExpanded(true);
        
        try {
            buildFileTree(currentDirectory.toPath(), rootItem);
            fileTreeView.setRoot(rootItem);
        } catch (IOException e) {
            statusLabel.setText("Error reading directory: " + e.getMessage());
        }
    }
    
    private void buildFileTree(Path directory, TreeItem<String> parentItem) throws IOException {
        List<Path> files = Files.list(directory)
            .filter(path -> !path.getFileName().toString().startsWith("."))
            .sorted((a, b) -> {
                // Directories first, then files
                if (Files.isDirectory(a) && !Files.isDirectory(b)) return -1;
                if (!Files.isDirectory(a) && Files.isDirectory(b)) return 1;
                return a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString());
            })
            .collect(Collectors.toList());
        
        for (Path file : files) {
            TreeItem<String> item = new TreeItem<>(file.getFileName().toString());
            parentItem.getChildren().add(item);
            
            if (Files.isDirectory(file)) {
                item.setExpanded(false);
                // Add a placeholder to make it expandable
                item.getChildren().add(new TreeItem<>("Loading..."));
                
                // Lazy loading for directories
                item.expandedProperty().addListener((obs, wasExpanded, isExpanded) -> {
                    if (isExpanded && item.getChildren().size() == 1 && 
                        "Loading...".equals(item.getChildren().get(0).getValue())) {
                        item.getChildren().clear();
                        try {
                            buildFileTree(file, item);
                        } catch (IOException e) {
                            item.getChildren().add(new TreeItem<>("Error loading"));
                        }
                    }
                });
            }
        }
    }
    
    private void loadFileFromTree(String fileName) {
        if (currentDirectory == null) return;
        
        // Find the file in the current directory or subdirectories
        try {
            Path filePath = findFileInDirectory(currentDirectory.toPath(), fileName);
            if (filePath != null) {
                loadFile(filePath.toFile());
            }
        } catch (IOException e) {
            statusLabel.setText("Error loading file: " + e.getMessage());
        }
    }
    
    private Path findFileInDirectory(Path directory, String fileName) throws IOException {
        return Files.walk(directory)
            .filter(path -> path.getFileName().toString().equals(fileName))
            .findFirst()
            .orElse(null);
    }
    
    private void loadFile(File file) {
        try {
            String content = Files.readString(file.toPath());
            codeEditor.setText(content);
            currentFile = file;
            filePathLabel.setText(file.getAbsolutePath());
            statusLabel.setText("Loaded: " + file.getName());
        } catch (IOException e) {
            statusLabel.setText("Error loading file: " + e.getMessage());
        }
    }
    
    private void saveFile() {
        if (currentFile != null) {
            try {
                Files.writeString(currentFile.toPath(), codeEditor.getText());
                statusLabel.setText("Saved: " + currentFile.getName());
            } catch (IOException e) {
                statusLabel.setText("Error saving file: " + e.getMessage());
            }
        } else {
            saveAsFile();
        }
    }
    
    private void saveAsFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        
        File file = fileChooser.showSaveDialog(saveFileButton.getScene().getWindow());
        if (file != null) {
            try {
                Files.writeString(file.toPath(), codeEditor.getText());
                currentFile = file;
                filePathLabel.setText(file.getAbsolutePath());
                statusLabel.setText("Saved: " + file.getName());
                
                // Update current directory and refresh tree
                currentDirectory = file.getParentFile();
                refreshFileTree();
            } catch (IOException e) {
                statusLabel.setText("Error saving file: " + e.getMessage());
            }
        }
    }
    
    private void formatCode() {
        String content = codeEditor.getText();
        if (content.trim().isEmpty()) {
            statusLabel.setText("No content to format");
            return;
        }
        
        statusLabel.setText("Formatting code...");
        
        new Thread(() -> {
            try {
                Map<String, String> requestData = new HashMap<>();
                requestData.put("action", "format");
                requestData.put("content", content);
                requestData.put("file_type", fileTypeCombo.getValue());
                
                String response = ApiClient.getInstance().post("/api/v1/tools/ide/action", requestData);
                Map<String, Object> result = objectMapper.readValue(response, Map.class);
                
                javafx.application.Platform.runLater(() -> {
                    if ((Boolean) result.get("success")) {
                        codeEditor.setText((String) result.get("content"));
                        statusLabel.setText("Code formatted successfully");
                    } else {
                        statusLabel.setText("Format failed");
                    }
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> 
                    statusLabel.setText("Format error: " + e.getMessage()));
            }
        }).start();
    }
    
    private void lintCode() {
        String content = codeEditor.getText();
        if (content.trim().isEmpty()) {
            lintOutput.setText("No content to lint");
            return;
        }
        
        statusLabel.setText("Linting code...");
        
        new Thread(() -> {
            try {
                Map<String, String> requestData = new HashMap<>();
                requestData.put("action", "lint");
                requestData.put("content", content);
                requestData.put("file_type", fileTypeCombo.getValue());
                
                String response = ApiClient.getInstance().post("/api/v1/tools/ide/action", requestData);
                Map<String, Object> result = objectMapper.readValue(response, Map.class);
                
                javafx.application.Platform.runLater(() -> {
                    if ((Boolean) result.get("success")) {
                        java.util.List<Map<String, Object>> issues = 
                            (java.util.List<Map<String, Object>>) result.get("issues");
                        
                        if (issues.isEmpty()) {
                            lintOutput.setText("✓ No issues found");
                        } else {
                            StringBuilder sb = new StringBuilder();
                            for (Map<String, Object> issue : issues) {
                                sb.append("Line ").append(issue.get("line"))
                                  .append(": ").append(issue.get("message")).append("\n");
                            }
                            lintOutput.setText(sb.toString());
                        }
                        statusLabel.setText("Linting completed");
                    } else {
                        lintOutput.setText("Linting failed");
                        statusLabel.setText("Lint failed");
                    }
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    lintOutput.setText("Lint error: " + e.getMessage());
                    statusLabel.setText("Lint error");
                });
            }
        }).start();
    }
    
    private void previewCode() {
        String content = codeEditor.getText();
        if (content.trim().isEmpty()) {
            previewWebView.getEngine().loadContent("");
            statusLabel.setText("No content to preview");
            return;
        }
        
        statusLabel.setText("Generating preview...");
        
        new Thread(() -> {
            try {
                Map<String, Object> requestData = new HashMap<>();
                requestData.put("action", "preview");
                requestData.put("content", content);
                requestData.put("file_type", fileTypeCombo.getValue());
                requestData.put("auto_detect_resources", autoDetectResourcesCheckBox.isSelected());
                
                // Add directory information for resource detection
                if (currentDirectory != null && autoDetectResourcesCheckBox.isSelected()) {
                    requestData.put("directory_path", currentDirectory.getAbsolutePath());
                    requestData.put("current_file", currentFile != null ? currentFile.getName() : "");
                }
                
                String response = ApiClient.getInstance().post("/api/v1/tools/ide/action", requestData);
                Map<String, Object> result = objectMapper.readValue(response, Map.class);
                
                javafx.application.Platform.runLater(() -> {
                    if ((Boolean) result.get("success")) {
                        String preview = (String) result.get("preview");
                        String type = (String) result.get("type");
                        
                        if ("html".equals(type)) {
                            previewWebView.getEngine().loadContent(preview);
                        } else {
                            // For non-HTML content, wrap in pre tags
                            String wrappedContent = "<html><body><pre style='font-family: monospace; white-space: pre-wrap;'>" 
                                + preview.replace("<", "&lt;").replace(">", "&gt;") + "</pre></body></html>";
                            previewWebView.getEngine().loadContent(wrappedContent);
                        }
                        
                        // Show resource detection info if available
                        if (result.containsKey("detected_resources")) {
                            List<String> resources = (List<String>) result.get("detected_resources");
                            if (!resources.isEmpty()) {
                                statusLabel.setText("Preview updated with " + resources.size() + " detected resources");
                            } else {
                                statusLabel.setText("Preview updated");
                            }
                        } else {
                            statusLabel.setText("Preview updated");
                        }
                    } else {
                        statusLabel.setText("Preview failed");
                    }
                });
                
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> 
                    statusLabel.setText("Preview error: " + e.getMessage()));
            }
        }).start();
    }
}