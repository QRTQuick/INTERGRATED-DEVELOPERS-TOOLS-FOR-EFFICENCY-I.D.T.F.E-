package com.idtfe.controllers;

import com.idtfe.services.ApiClient;
import com.idtfe.services.EditorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class CodeYourGitRepoController implements Initializable {
    @FXML private ListView<Map<String, Object>> reposList;
    @FXML private ListView<Map<String, Object>> filesList;
    @FXML private TextArea editorArea;
    @FXML private Button refreshBtn;
    @FXML private Button openInEditorBtn;
    @FXML private Button saveLocalBtn;
    @FXML private Button pushToGitHubBtn;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiClient api = ApiClient.getInstance();
    private final EditorService editorService = new EditorService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshBtn.setOnAction(e -> loadRepos());
        reposList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText((String) item.getOrDefault("full_name", item.get("name")));
            }
        });

        filesList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Map<String, Object> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText((String) item.getOrDefault("path", item.get("name")));
            }
        });

        reposList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            filesList.getItems().clear();
            editorArea.clear();
            if (newV != null) {
                String full = (String) newV.get("full_name");
                String[] parts = full.split("/");
                if (parts.length == 2) fetchRepoContents(parts[0], parts[1], "");
            }
        });

        filesList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) loadFileContent(newV);
        });

        openInEditorBtn.setOnAction(e -> {
            Map<String, Object> file = filesList.getSelectionModel().getSelectedItem();
            if (file != null) loadFileContent(file);
        });

        saveLocalBtn.setOnAction(e -> {
            String text = editorArea.getText();
            Map<String, Object> file = filesList.getSelectionModel().getSelectedItem();
            if (file != null) {
                String path = (String) file.getOrDefault("path", file.get("name"));
                // Save locally using EditorService
                boolean ok = editorService.saveFile(path.replaceAll("[\\/:]", "_"), text);
                Alert a = new Alert(ok ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR, ok ? "Saved locally" : "Failed to save");
                a.showAndWait();
            }
        });

        pushToGitHubBtn.setOnAction(e -> pushToGitHub());

        // initial load
        loadRepos();
    }

    private void pushToGitHub() {
        Map<String, Object> repo = reposList.getSelectionModel().getSelectedItem();
        Map<String, Object> file = filesList.getSelectionModel().getSelectedItem();
        if (repo == null || file == null) {
            javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.WARNING, "Select a repository and file first").showAndWait());
            return;
        }
        String full = (String) repo.get("full_name");
        String[] parts = full.split("/");
        if (parts.length != 2) return;
        String owner = parts[0];
        String repoName = parts[1];
        String path = (String) file.getOrDefault("path", file.get("name"));
        String content = editorArea.getText();
        new Thread(() -> {
            try {
                var payload = Map.of("path", path, "content", content, "message", "Update from I.D.T.F.E");
                String url = String.format("/api/v1/tools/github/repos/%s/%s/file", owner, repoName);
                String resp = api.post(url, payload);
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                if ((Boolean) data.getOrDefault("success", false)) {
                    javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "Pushed to GitHub").showAndWait());
                } else {
                    javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Failed: " + data.get("message")).showAndWait());
                }
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait());
            }
        }).start();
    }

    private void loadRepos() {
        new Thread(() -> {
            try {
                String resp = api.get("/api/v1/tools/github/repos");
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                if ((Boolean) data.getOrDefault("success", false)) {
                    List<Map<String, Object>> repos = (List<Map<String, Object>>) data.get("repos");
                    javafx.application.Platform.runLater(() -> {
                        reposList.getItems().clear();
                        reposList.getItems().addAll(repos);
                    });
                } else {
                    javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Failed to load repos: " + data.get("message")).showAndWait());
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Error: " + e.getMessage()).showAndWait());
            }
        }).start();
    }

    private void fetchRepoContents(String owner, String repoName, String path) {
        new Thread(() -> {
            try {
                String url = String.format("/api/v1/tools/github/repos/%s/%s/contents?path=%s", owner, repoName, java.net.URLEncoder.encode(path, "UTF-8"));
                String resp = api.get(url);
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                if ((Boolean) data.getOrDefault("success", false)) {
                    Object obj = data.get("contents");
                    if (obj instanceof List) {
                        List<Map<String, Object>> contents = (List<Map<String, Object>>) obj;
                        javafx.application.Platform.runLater(() -> {
                            filesList.getItems().clear();
                            filesList.getItems().addAll(contents);
                        });
                    }
                }
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Failed to list contents: " + e.getMessage()).showAndWait());
            }
        }).start();
    }

    private void loadFileContent(Map<String, Object> file) {
        new Thread(() -> {
            try {
                String downloadUrl = (String) file.get("download_url");
                if (downloadUrl == null) {
                    javafx.application.Platform.runLater(() -> editorArea.setText("(No download URL for selected item)"));
                    return;
                }
                String text = api.rawGet(downloadUrl);
                javafx.application.Platform.runLater(() -> editorArea.setText(text));
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "Failed to load file: " + e.getMessage()).showAndWait());
            }
        }).start();
    }
}
