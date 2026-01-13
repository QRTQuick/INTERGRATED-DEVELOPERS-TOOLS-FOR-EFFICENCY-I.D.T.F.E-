package com.idtfe;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.idtfe.services.ApiClient;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize API client
        ApiClient.getInstance().initialize();
        
        // Load main FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 800);
        
        primaryStage.setTitle("I.D.T.F.E - Integrated Developer Tools for Efficiency | Quick Red Tech");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Check backend health on startup
        ApiClient.getInstance().checkHealth();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}