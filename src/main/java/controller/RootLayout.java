package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;


public class RootLayout{

    @FXML
    private MenuItem menuItemUser;


    public Main main;

    public RootLayout() {
    }


    public void setMainApp(Main main) {
        this.main = main;
    }

    @FXML
    private void handleUser() {
        displayUserOptions();
    }

    @FXML
    private void handleSave() {
        displaySave();
    }

    @FXML
    private void handleClose() {
        System.exit(0);
    }

    public void displayUserOptions(){
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("../view/UserOptions.fxml"));
            AnchorPane anchor = (AnchorPane) loader.load();

            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("User Options");

            UserOptions controller = loader.getController();
            controller.setMainApp(this);

            Scene scene = new Scene(anchor);
            window.setScene(scene);
            window.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displaySave(){
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("../view/SaveUserData.fxml"));
            AnchorPane anchor = (AnchorPane) loader.load();

            Stage window = new Stage();
            window.initModality(Modality.APPLICATION_MODAL);
            window.setTitle("User Options");

            SaveUserData controller = loader.getController();
            controller.setMainApp(this);

            Scene scene = new Scene(anchor);
            window.setScene(scene);
            window.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
