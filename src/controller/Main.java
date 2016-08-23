package controller;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;


public class Main extends Application {
	
	private Stage primaryStage;
	private BorderPane rootLayout;

    public MainScreen childMainScreen;
    
	
	@Override
	public void start(Stage primaryStage) {
		
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Emotiv EEG");

        initRootLayout();

        showMainContent();
	}
	

	
	public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("../view/RootLayout.fxml"));
            rootLayout = (BorderPane) loader.load();

            
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);

            RootLayout controller = loader.getController();
            controller.setMainApp(this);

            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
	public void showMainContent() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("../view/MainScreen.fxml"));
            AnchorPane personOverview = (AnchorPane) loader.load();

            rootLayout.setCenter(personOverview);

            // Give the controller access to the main app.
            MainScreen controller = loader.getController();
            controller.setMainAppMain(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
	/**
     * Returns the main stage.
     * @return
     */
	public Stage getPrimaryStage() {
        return primaryStage;
    }
	
	public static void main(String[] args) {
		launch(args);
	}

    public void setSubScreen(MainScreen sub) {
        this.childMainScreen = sub;
    }

	
}
