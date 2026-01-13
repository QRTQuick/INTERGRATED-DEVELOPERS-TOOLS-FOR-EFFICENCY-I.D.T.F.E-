package com.idtfe.controllers;

import com.idtfe.services.ApiClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ProjectSettingsController implements Initializable {
    @FXML private Button checkStatusBtn;
    @FXML private Button syncReposBtn;
    @FXML private ListView<Map<String, Object>> reposList;
    @FXML private ListView<Map<String, Object>> filesList;
    @FXML private Button openFileBtn;
    @FXML private TextArea fileContentArea;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkStatusBtn.setOnAction(e -> checkStatus());
        syncReposBtn.setOnAction(e -> syncRepos());
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
            fileContentArea.clear();
            if (newV != null) {
                // auto-load root contents for selected repo
                String fullName = (String) newV.get("full_name");
                String[] parts = fullName.split("/");
                if (parts.length == 2) {
                    fetchRepoContents(parts[0], parts[1], "");
                }
            }
        });

        openFileBtn.setOnAction(e -> openSelectedFile());
        filesList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) openSelectedFile();
        });
    }

    private void fetchRepoContents(String owner, String repoName, String path) {
        new Thread(() -> {
            try {
                String url = String.format("/api/v1/tools/github/repos/%s/%s/contents?path=%s", owner, repoName, java.net.URLEncoder.encode(path, "UTF-8"));
                String resp = ApiClient.getInstance().get(url);
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                if ((Boolean) data.getOrDefault("success", false)) {
                    Object contentsObj = data.get("contents");
                    if (contentsObj instanceof List) {
                        List<Map<String, Object>> contents = (List<Map<String, Object>>) contentsObj;
                        javafx.application.Platform.runLater(() -> {
                            filesList.getItems().clear();
                            filesList.getItems().addAll(contents);
                        });
                    }
                }
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to list files: " + ex.getMessage());
                    a.showAndWait();
                });
            }
        }).start();
    }

    private void checkStatus() {
        new Thread(() -> {
            try {
                String resp = ApiClient.getInstance().get("/api/v1/tools/github/status");
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                javafx.application.Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("GitHub Status");
                    a.setHeaderText(null);
                    a.setContentText(data.toString());
                    a.showAndWait();
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to check status: " + ex.getMessage());
                    a.showAndWait();
                });
            }
        }).start();
    }

    private void syncRepos() {
        new Thread(() -> {
            try {
                String resp = ApiClient.getInstance().get("/api/v1/tools/github/repos");
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                if ((Boolean) data.getOrDefault("success", false)) {
                    List<Map<String, Object>> repos = (List<Map<String, Object>>) data.get("repos");
                    javafx.application.Platform.runLater(() -> {
                        reposList.getItems().clear();
                        reposList.getItems().addAll(repos);
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        Alert a = new Alert(Alert.AlertType.ERROR, "Failed to sync: " + data.get("message"));
                        a.showAndWait();
                    });
                }
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to sync: " + ex.getMessage());
                    a.showAndWait();
                });
            }
        }).start();
    }

    private void openSelectedFile() {
        Map<String, Object> repo = reposList.getSelectionModel().getSelectedItem();
        Map<String, Object> file = filesList.getSelectionModel().getSelectedItem();
        if (repo == null || file == null) return;
        String fullName = (String) repo.get("full_name");
        String[] parts = fullName.split("/");
        if (parts.length != 2) return;
        String owner = parts[0];
        String repoName = parts[1];
        String path = (String) file.get("path");

        new Thread(() -> {
            try {
                String url = String.format("/api/v1/tools/github/repos/%s/%s/contents?path=%s", owner, repoName, java.net.URLEncoder.encode(path, "UTF-8"));
                String resp = ApiClient.getInstance().get(url);
                Map<String, Object> data = objectMapper.readValue(resp, Map.class);
                if ((Boolean) data.getOrDefault("success", false)) {
                    List<Map<String, Object>> contents = (List<Map<String, Object>>) data.get("contents");
                    // If the selected item is a file, GitHub returns an object; handle both
                    if (contents != null && !contents.isEmpty()) {
                        Map<String, Object> item = contents.get(0);
                        String type = (String) item.get("type");
                        if ("file".equals(type)) {
                            String downloadUrl = (String) item.get("download_url");
                            String fileText = ApiClient.getInstance().rawGet(downloadUrl);
                            javafx.application.Platform.runLater(() -> fileContentArea.setText(fileText));
                        } else {
                            javafx.application.Platform.runLater(() -> {
                                filesList.getItems().clear();
                                filesList.getItems().addAll(contents);
                            });
                        }
                    } else {
                        // If response contained a single object
                        Object contentsObj = data.get("contents");
                        if (contentsObj instanceof Map) {
                            Map<String, Object> item = (Map<String, Object>) contentsObj;
                            if ("file".equals(item.get("type"))) {
                                String downloadUrl = (String) item.get("download_url");
                                String fileText = ApiClient.getInstance().rawGet(downloadUrl);
                                javafx.application.Platform.runLater(() -> fileContentArea.setText(fileText));
                            }
                        }
                    }
                } else {
                    javafx.application.Platform.runLater(() -> {
                        Alert a = new Alert(Alert.AlertType.ERROR, "Failed to fetch file: " + data.get("message"));
                        a.showAndWait();
                    });
                }
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Failed to fetch file: " + ex.getMessage());
                    a.showAndWait();
                });
            }
        }).start();
    }
}
