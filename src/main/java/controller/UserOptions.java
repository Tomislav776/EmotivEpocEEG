package controller;


import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;

import java.io.*;


public class UserOptions {
    private RootLayout main;

    @FXML
    private TextField textFieldFirstName;

    @FXML
    private TextField textFieldLastName;

    @FXML
    private TextField textFieldDateOfBirth;

    @FXML
    private TextField textFieldAddress;

    @FXML
    private TextField textFieldPhoneNumber;

    @FXML
    private Button buttonAddUser;

    private String userPath;


    @FXML
    public  void initialize (){
        buttonAddUser.setOnAction(e -> {
            if (textFieldFirstName.getText().isEmpty() || textFieldLastName.getText().isEmpty() || textFieldDateOfBirth.getText().isEmpty() || textFieldPhoneNumber.getText().isEmpty() || textFieldAddress.getText().isEmpty())
                showAlertNotFilled();
            else {
                addUser();
                main.main.childMainScreen.initalizeChoiceBoxUser();
            }
        });
    }

    public void setMainApp(RootLayout main) {
        this.main = main;
    }

    private void addUser(){

        createUserDirectory();

        saveUserInfo();
        addUserToTheList();

        showAlert();

        textFieldFirstName.setText("");
        textFieldLastName.setText("");
        textFieldDateOfBirth.setText("");
        textFieldAddress.setText("");
        textFieldPhoneNumber.setText("");
    }


    public  void createUserDirectory() {
        String path = System.getProperty("user.dir");
        userPath = path + File.separator + "Documents" + File.separator + "Users" + File.separator + textFieldFirstName.getText() + File.separator;

        File file = new File(path + File.separator + "Documents" + File.separator + "Users" + File.separator + textFieldFirstName.getText());
        if (!file.exists()) {
            if (file.mkdir()) {
                System.out.println("Directory is created!");
            } else {
                System.out.println("Failed to create directory!");
            }
        }
    }


    private void showAlert(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("User");
        alert.setHeaderText(null);
        alert.setContentText("User added!");
        alert.showAndWait();
    }

    private void showAlertNotFilled(){
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("User");
        alert.setHeaderText(null);
        alert.setContentText("All fields must be filled!");
        alert.showAndWait();
    }

    public void saveUserInfo(){

            try (BufferedWriter bw = new BufferedWriter(new PrintWriter(userPath + "UserInfo.txt"))) {

                bw.write("First Name");
                bw.newLine();
                bw.write(textFieldFirstName.getText());
                bw.newLine();

                bw.write("Last Name");
                bw.newLine();
                bw.write(textFieldLastName.getText());
                bw.newLine();

                bw.write("Date of Birth");
                bw.newLine();
                bw.write(textFieldDateOfBirth.getText());
                bw.newLine();

                bw.write("Address");
                bw.newLine();
                bw.write(textFieldAddress.getText());
                bw.newLine();

                bw.write("Phone Number");
                bw.newLine();
                bw.write(textFieldPhoneNumber.getText());
                bw.newLine();

                bw.newLine();bw.newLine();
                bw.write("Data");
                bw.newLine();


            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    private void addUserToTheList(){
            try (BufferedWriter output = new BufferedWriter(new FileWriter("Users.txt", true))) {
                output.write(textFieldFirstName.getText() + " " + textFieldLastName.getText());
                output.newLine();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

    }


}
