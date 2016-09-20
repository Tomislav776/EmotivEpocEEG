package controller;


import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import model.ImageHandler;


import java.io.File;
import java.io.IOException;

public class UserScreen {

    private  MainScreen mainApp;
    public static int i = 0;

    public XYChart.Series series1 = new XYChart.Series();
    public final XYChart.Data<String, Number> data = new XYChart.Data("Training", 0);

    final CategoryAxis xAxis = new CategoryAxis();
    final NumberAxis yAxis = new NumberAxis();

    @FXML
    public BarChart<String, Number> barChartConcentrationUser = new BarChart<>(xAxis, yAxis);

    @FXML
    public ImageView imageViewSlideShow;

    @FXML
    public MediaView mediaViewPlayer;

    public Service<Void> backgorundThread;
    public MediaPlayer mediaPlayer;
    public Timeline timeline = new Timeline();

    public void setMainApp(MainScreen mainApp) {
        this.mainApp = mainApp;
        mainApp.setSubScreen(this);
    }

    public UserScreen () {
    }


    @FXML
    public void initialize (){
        initializeBarChartConcentrationData();
    }

    public void initializeMusic() {
        Media media = new Media(new File(mainApp.pathMusicTrack).toURI().toString());

        String pathMusic = System.getProperty("user.dir") + File.separator + "Documents" + File.separator + "Music" + File.separator + "MusicPicture.png";

        File MusicPic = new File(pathMusic);
        imageViewSlideShow.setImage(new Image(MusicPic.toURI().toString(), 900, 700, true, true));

        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.stop();
        mediaPlayer.setAutoPlay(true);
        mediaViewPlayer.setMediaPlayer(mediaPlayer);

    }

    public void initialzeSlideShow() {
        File[] fileList;
        ImageHandler nesto = new ImageHandler();
        fileList = nesto.photo();
        imageViewSlideShow.setImage(new Image(fileList[0].toURI().toString(), 900, 700, true, true));
        i=0;

        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(2000), (ActionEvent actionEvent) -> {

                    if (MainScreen.startButton && (i < fileList.length)){
                        imageViewSlideShow.setImage(new Image(fileList[i].toURI().toString(), 900, 700, true, true));
                        i++;
                    }
                }));

        timeline.setCycleCount(1000);
        timeline.setAutoReverse(true);  //!?
        timeline.play();
    }


    public void initializeBarChartConcentrationData(){
        series1.getData().add(data);


        barChartConcentrationUser.getData().addAll(series1);
        barChartConcentrationUser.setAnimated(false);

        /*Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(500), (ActionEvent actionEvent) -> {

                    if (MainScreen.dataChannelChoice.equals("Total")){
                        //series1.getData().set(0, new XYChart.Data("Concentration", totalTheta/totalLowBeta));
                        data.setYValue(MainScreen.emotivData.totalTheta/MainScreen.emotivData.totalLowBeta);
                        series1.getData().set(0, data);
                    }
                    else{
                        //series1.getData().set(0, new XYChart.Data("Concentration", theta.getValue()/low_beta.getValue()));
                        data.setYValue(MainScreen.emotivData.randomTheta/MainScreen.emotivData.randomLowBeta);
                        series1.getData().set(0, data);
                    }
                }));

        timeline.setCycleCount(1000);
        timeline.setAutoReverse(true);  //!?
        timeline.play();*/
    }


}
