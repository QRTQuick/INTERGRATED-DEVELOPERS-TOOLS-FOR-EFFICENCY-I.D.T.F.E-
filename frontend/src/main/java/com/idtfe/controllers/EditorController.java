package com.idtfe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import com.idtfe.services.EditorService;

import java.net.URL;
import java.util.ResourceBundle;

public class EditorController implements Initializable {
    @FXML private TextArea editorArea;
    @FXML private Button openButton;
    @FXML private Button saveButton;

    private final EditorService editorService = new EditorService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        openButton.setOnAction(e -> openFile());
        saveButton.setOnAction(e -> saveFile());
    }

    private void openFile() {
        // Simple demo: load a placeholder file if available
        String content = editorService.loadFile("README.md");
        if (content != null) editorArea.setText(content);
    }

    private void saveFile() {
        String content = editorArea.getText();
        editorService.saveFile("unsaved.txt", content);
    }
}
