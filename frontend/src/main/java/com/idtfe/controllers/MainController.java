package com.idtfe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.BorderPane;
import javafx.application.Platform;
import javafx.stage.Stage;
import com.idtfe.services.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.awt.Desktop;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class MainController implements Initializable {
    
    @FXML private VBox moduleList;
    @FXML private TabPane contentTabs;
    @FXML private Label statusLabel;
    @FXML private Label datetimeLabel;
    @FXML private HBox statusBarContainer;
    @FXML private javafx.scene.layout.VBox leftContainer;
    
    // Menu items
    @FXML private MenuItem exitMenuItem;
    @FXML private MenuItem apiTesterMenuItem;
    @FXML private MenuItem readmePreviewerMenuItem;
    @FXML private MenuItem webIdeMenuItem;
    @FXML private MenuItem browserMenuItem;
    @FXML private MenuItem terminalMenuItem;
    @FXML private MenuItem codeEditorMenuItem;
    @FXML private MenuItem projectExplorerMenuItem;
    @FXML private MenuItem projectSettingsMenuItem;
    @FXML private MenuItem codeYourGitRepoMenuItem;
    @FXML private MenuItem refreshMenuItem;
    @FXML private CheckMenuItem statusBarMenuItem;
    @FXML private CheckMenuItem darkModeMenuItem;
    @FXML private MenuItem helpMenuItem;
    @FXML private MenuItem githubMenuItem;
    @FXML private MenuItem aboutMenuItem;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMenuActions();
        loadModules();
        statusLabel.setText("Ready");
        // Start datetime updater
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), ev -> {
                if (datetimeLabel != null) {
                    datetimeLabel.setText(LocalDateTime.now().format(dtf));
                }
            }));
            clock.setCycleCount(Timeline.INDEFINITE);
            clock.play();
        } catch (Exception ex) {
            // ignore if animation fails
        }
        // Apply theme after scene is ready
        javafx.application.Platform.runLater(() -> {
            try {
                if (darkModeMenuItem == null) return;
                Scene scene = statusLabel.getScene();
                if (scene == null) return;
                String css = getClass().getResource("/css/dark-theme.css").toExternalForm();
                if (darkModeMenuItem.isSelected()) {
                    if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
                } else {
                    scene.getStylesheets().remove(css);
                }
                // wire toggle
                darkModeMenuItem.setOnAction(e -> {
                    if (darkModeMenuItem.isSelected()) {
                        if (!scene.getStylesheets().contains(css)) scene.getStylesheets().add(css);
                    } else {
                        scene.getStylesheets().remove(css);
                    }
                });
            } catch (Exception ex) {
                // ignore
            }
        });
    }
    
    private void setupMenuActions() {
        // File menu
        exitMenuItem.setOnAction(e -> Platform.exit());
        
        // Tools menu
        apiTesterMenuItem.setOnAction(e -> openModule("api-tester", "API Tester"));
        readmePreviewerMenuItem.setOnAction(e -> openModule("readme-previewer", "README Previewer"));
        webIdeMenuItem.setOnAction(e -> openModule("web-ide", "Web IDE"));
        browserMenuItem.setOnAction(e -> openModule("browser", "Browser"));
        // Developer tools
        terminalMenuItem.setOnAction(e -> openModule("terminal", "Terminal"));
        codeEditorMenuItem.setOnAction(e -> openModule("code-editor", "Code Editor"));
        projectExplorerMenuItem.setOnAction(e -> openModule("project-explorer", "Project Explorer"));
        projectSettingsMenuItem.setOnAction(e -> openModule("project-settings", "Project Settings"));
        codeYourGitRepoMenuItem.setOnAction(e -> openModule("code-your-git-repo", "Code: Your Git Repo"));
        
        // View menu
        refreshMenuItem.setOnAction(e -> loadModules());
        statusBarMenuItem.setOnAction(e -> {
            statusBarContainer.setVisible(statusBarMenuItem.isSelected());
            statusBarContainer.setManaged(statusBarMenuItem.isSelected());
        });
        
        // Help menu
        helpMenuItem.setOnAction(e -> showHelp());
        githubMenuItem.setOnAction(e -> openGitHub());
        aboutMenuItem.setOnAction(e -> showAbout());
    }
    
    private void loadModules() {
        new Thread(() -> {
            try {
                String response = ApiClient.getInstance().get("/api/v1/meta");
                Map<String, Object> metadata = objectMapper.readValue(response, Map.class);
                List<Map<String, Object>> modules = (List<Map<String, Object>>) metadata.get("modules");
                
                javafx.application.Platform.runLater(() -> {
                    moduleList.getChildren().clear(); // Clear existing modules
                    for (Map<String, Object> module : modules) {
                        String name = (String) module.get("name");
                        String id = (String) module.get("id");
                        Boolean enabled = (Boolean) module.get("enabled");
                        
                        if (enabled) {
                            Button moduleButton = new Button(name);
                            moduleButton.setMaxWidth(Double.MAX_VALUE);
                            moduleButton.setOnAction(e -> openModule(id, name));
                            moduleList.getChildren().add(moduleButton);
                        }
                    }
                });
                
                // Load version
                String versionResponse = ApiClient.getInstance().get("/api/v1/version");
                Map<String, Object> versionData = objectMapper.readValue(versionResponse, Map.class);
                String version = (String) versionData.get("version");
                
                javafx.application.Platform.runLater(() -> 
                    statusLabel.setText("Version " + version + " - Ready"));
                
            } catch (IOException e) {
                javafx.application.Platform.runLater(() -> 
                    statusLabel.setText("Failed to load modules: " + e.getMessage()));
            }
        }).start();
    }
    
    private void openModule(String moduleId, String moduleName) {
        try {
            String fxmlPath = "/fxml/" + moduleId + ".fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            
            if (getClass().getResource(fxmlPath) != null) {
                Tab tab = new Tab(moduleName);
                tab.setContent(loader.load());
                contentTabs.getTabs().add(tab);
                contentTabs.getSelectionModel().select(tab);
                statusLabel.setText("Opened " + moduleName);
            } else {
                // Create a simple placeholder tab for missing modules
                Tab tab = new Tab(moduleName);
                VBox placeholder = new VBox();
                placeholder.setAlignment(javafx.geometry.Pos.CENTER);
                placeholder.setSpacing(20);
                
                Label titleLabel = new Label("Module: " + moduleName);
                titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                
                Label messageLabel = new Label("This module is being implemented...");
                messageLabel.setStyle("-fx-text-fill: #666;");
                
                Button closeButton = new Button("Close Tab");
                closeButton.setOnAction(e -> contentTabs.getTabs().remove(tab));
                
                placeholder.getChildren().addAll(titleLabel, messageLabel, closeButton);
                tab.setContent(placeholder);
                contentTabs.getTabs().add(tab);
                contentTabs.getSelectionModel().select(tab);
                statusLabel.setText("Opened placeholder for " + moduleName);
            }
            
        } catch (IOException e) {
            statusLabel.setText("Failed to load module: " + e.getMessage());
        }
    }
    
    private void showHelp() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("I.D.T.F.E Help");
        alert.setHeaderText("Integrated Developer Tools for Efficiency");
        
        String helpContent = "Welcome to I.D.T.F.E - Your comprehensive developer toolkit!\n\n" +
            "🔧 API TESTER\n" +
            "• Test REST APIs with GET, POST, PUT, DELETE methods\n" +
            "• Add custom headers and request bodies\n" +
            "• View formatted responses with timing information\n\n" +
            "📖 README PREVIEWER\n" +
            "• Load and preview Markdown files\n" +
            "• Real-time HTML rendering with syntax highlighting\n" +
            "• Support for GitHub-flavored Markdown\n\n" +
            "💻 WEB IDE\n" +
            "• Edit HTML, CSS, JavaScript, JSON, and Markdown files\n" +
            "• Code formatting and syntax validation\n" +
            "• Live preview for supported file types\n\n" +
            "🌐 BROWSER\n" +
            "• Built-in web browser with navigation controls\n" +
            "• JSON formatting for API responses\n" +
            "• Integrated with other tools for seamless workflow\n\n" +
            "📋 GETTING STARTED\n" +
            "1. Select a tool from the sidebar or Tools menu\n" +
            "2. Each tool opens in a new tab for easy switching\n" +
            "3. Use the status bar to monitor operations\n\n" +
            "For more information, visit our GitHub repository!";
        
        alert.setContentText(helpContent);
        alert.getDialogPane().setPrefWidth(600);
        alert.getDialogPane().setPrefHeight(500);
        alert.showAndWait();
    }
    
    private void openGitHub() {
        // Prefer opening the user's linked GitHub account if available
        new Thread(() -> {
            try {
                String resp = ApiClient.getInstance().get("/api/v1/tools/github/status");
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                String targetUrl = "https://github.com/QRTQuick"; // fallback
                if ((Boolean) data.getOrDefault("success", false)) {
                    String projectRepo = (String) data.getOrDefault("project_repo", null);
                    if (projectRepo != null && !projectRepo.isBlank()) {
                        targetUrl = "https://github.com/" + projectRepo;
                    } else {
                        String username = (String) data.getOrDefault("username", null);
                        if (username != null && !username.isBlank()) {
                            targetUrl = "https://github.com/" + username;
                        }
                    }
                }

                final String openUrl = targetUrl;
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(openUrl));
                } else {
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("GitHub Repository");
                        alert.setHeaderText("Visit GitHub");
                        alert.setContentText("GitHub Repository: " + openUrl + "\n\nCopy this URL to your browser to view the source code.");
                        alert.showAndWait();
                    });
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> statusLabel.setText("Could not open GitHub link"));
            }
        }).start();
    }
    
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About I.D.T.F.E");
        alert.setHeaderText("Integrated Developer Tools for Efficiency");
        
        String aboutContent = "I.D.T.F.E v1.0.0\n\n" +
            "🏢 COMPANY\n" +
            "Quick Red Tech\n" +
            "Building innovative developer tools and solutions\n\n" +
            "👨‍💻 DEVELOPER\n" +
            "Chisom Life Eke\n" +
            "Lead Developer & Architect\n\n" +
            "📋 PROJECT DETAILS\n" +
            "• Open Source Software\n" +
            "• Built with JavaFX & Python Flask\n" +
            "• Modular, extensible architecture\n" +
            "• Cross-platform compatibility\n\n" +
            "🔗 LINKS\n" +
            "• GitHub: QRTQuick\n" +
            "• License: Open Source\n" +
            "• Support: GitHub Issues\n\n" +
            "📅 COPYRIGHT\n" +
            "© 2026 Quick Red Tech. All rights reserved.\n\n" +
            "This software is open source and available under\n" +
            "the terms specified in the project repository.\n\n" +
            "Thank you for using I.D.T.F.E!";
        
        alert.setContentText(aboutContent);
        alert.getDialogPane().setPrefWidth(500);
        alert.getDialogPane().setPrefHeight(450);
        alert.showAndWait();
    }
}