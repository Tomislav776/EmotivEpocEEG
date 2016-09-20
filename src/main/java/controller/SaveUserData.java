package controller;


import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;

import javax.imageio.ImageIO;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;

public class SaveUserData {

    private RootLayout main;

    @FXML
    private Button buttonSave;

    @FXML
    public ImageView imageViewProgressChart;

    @FXML
    public Label labelInfo;

    @FXML
    public VBox vBoxImage;

    @FXML
    public  void initialize (){

        buttonSave.setOnAction(e -> {
            saveAsPng();
        });
    }

    public void setMainApp(RootLayout main) {
        System.out.println("Uslo je");
        this.main = main;

        WritableImage image = main.main.childMainScreen.lineChartProgress.snapshot(new SnapshotParameters(), null);
        imageViewProgressChart.setImage(image);

        setInfo();
    }

    public void setInfo(){
        String info="";
        double aboveThreshold;

        System.out.println(main.main.childMainScreen.timer + " " + main.main.childMainScreen.timePlaying);
        if (main.main.childMainScreen.timer != 0 && main.main.childMainScreen.timePlaying != 0)
            aboveThreshold = main.main.childMainScreen.timePlaying / main.main.childMainScreen.timer;
        else
            aboveThreshold = 0;

        info = "Training: " + main.main.childMainScreen.choiceBoxTraining.getValue().toString() + "      ";
        info += "Data channel: " + main.main.childMainScreen.choiceBoxDataChannel.getValue().toString() + "     ";
        info += "Baseline: " + main.main.childMainScreen.labelBaseline.getText() + "     ";
        info += "Threshold: " + main.main.childMainScreen.labelThreshold.getText() + "     ";
        info += "Time above threshold: " + round(aboveThreshold,2) + "%";

        labelInfo.setText(info);
    }

    @FXML
    public void saveAsPng() {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        String timestamp = time.toString(), name="", info="";
        int counter = 0;

        timestamp = timestamp.replace(" ", "_");
        timestamp = timestamp.replace(".", ",");

        for (int i = 0; i< timestamp.length(); i++){
            if (timestamp.charAt(i) == ':'){
                if (counter == 1) {
                    name += 'M';
                    counter++;
                }
                else if(counter == 0) {
                    name += 'H';
                    counter++;
                }
            }
            else
                name += timestamp.charAt(i);
        }

        name+='S';

        String path = System.getProperty("user.dir") + File.separator + "Documents" + File.separator + "Users" + userName() + File.separator + name + ".png";

        WritableImage image = vBoxImage.snapshot(new SnapshotParameters(), null);

        // TODO: probably use a file chooser here
        File file = new File(path);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);

            showAlertSaved();
        } catch (IOException e) {
            // TODO: handle exception here
        }
    }

    public String userName(){
        String name="", fullName = File.separator + main.main.childMainScreen.choiceBoxUser.getValue().toString();

        for (int i = 0; i < fullName.length(); i++) {
            if (fullName.charAt(i) == ' ')
                break;
            name += fullName.charAt(i);
        }
        return name;
    }

    private void showAlertSaved(){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Data");
        alert.setHeaderText(null);
        alert.setContentText("Data is saved!");
        alert.showAndWait();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
