package com.idtfe.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class ProjectExplorerController implements Initializable {
    @FXML private TreeView<String> projectTree;
    @FXML private Button refreshBtn;
    @FXML private Button openBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshBtn.setOnAction(e -> refreshTree());
        openBtn.setOnAction(e -> openSelected());
        refreshTree();
    }

    private void refreshTree() {
        File cwd = new File(System.getProperty("user.dir"));
        TreeItem<String> root = buildTree(cwd);
        projectTree.setRoot(root);
        root.setExpanded(true);
    }

    private TreeItem<String> buildTree(File file) {
        TreeItem<String> node = new TreeItem<>(file.getName());
        if (file.isDirectory()) {
            File[] kids = file.listFiles();
            if (kids != null) {
                for (File k : kids) {
                    node.getChildren().add(buildTree(k));
                }
            }
        }
        return node;
    }

    private void openSelected() {
        TreeItem<String> sel = projectTree.getSelectionModel().getSelectedItem();
        if (sel != null) {
            // For now show selection in console / future: open in editor
            System.out.println("Selected: " + sel.getValue());
        }
    }
}
