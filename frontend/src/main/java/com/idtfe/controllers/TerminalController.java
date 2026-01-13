package com.idtfe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import com.idtfe.services.TerminalService;

import java.net.URL;
import java.util.ResourceBundle;

public class TerminalController implements Initializable {
    @FXML private TextArea outputArea;
    @FXML private TextField inputField;
    @FXML private Button sendButton;
    @FXML private Button clearButton;

    private final TerminalService terminalService = new TerminalService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sendButton.setOnAction(e -> runCommand());
        clearButton.setOnAction(e -> outputArea.clear());
    }

    private void runCommand() {
        String cmd = inputField.getText();
        if (cmd == null || cmd.isBlank()) return;
        outputArea.appendText("$ " + cmd + "\n");
        new Thread(() -> {
            String out = terminalService.runCommand(cmd);
            javafx.application.Platform.runLater(() -> outputArea.appendText(out + "\n"));
        }).start();
        inputField.clear();
    }
}
