package controller;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import Iedk.Edk;
import Iedk.EdkErrorCode;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import model.EmotivData;

import javax.imageio.ImageIO;


public class MainScreen extends Main {

	private Main mainApp;
	private UserScreen childUserScreen;

	public static DoubleByReference alpha     = new DoubleByReference(0);
	public static DoubleByReference low_beta  = new DoubleByReference(0);
	public static DoubleByReference high_beta = new DoubleByReference(0);
	public static DoubleByReference gamma     = new DoubleByReference(0);
	public static DoubleByReference theta     = new DoubleByReference(0);

	private double totalAlpha, totalLowBeta, totalHighBeta, totalGamma, totalTheta;

	UserScreen userScreen;

	private Service<Void> backgorundThread, backgorundThread2, backgorundThreadTimer;
	public static boolean killEpocThread = false;
	public static boolean startButton = false;

	final CategoryAxis xAxis1 = new CategoryAxis();
	final NumberAxis yAxis1 = new NumberAxis();

	final CategoryAxis xAxis2 = new CategoryAxis();
	final NumberAxis yAxis2 = new NumberAxis();

	public NumberAxis xAxisLine = new NumberAxis();
	public NumberAxis yAxisLine = new NumberAxis();

	public double threshold, baselineValue, divider = 1, timePlaying = 0;
    public double[] baseline = {0, 0, 0, 0, 0};
	boolean resetBase = true;

    public static String choiceEmotivData;


	@FXML
	public BarChart <String, Number> barChartConcentration = new BarChart<>(xAxis2, yAxis2);

	@FXML
	public BarChart<String, Number> barChartWaveData = new BarChart<>(xAxis1, yAxis1);

	@FXML
	public LineChart<Number, Number> lineChartProgress = new LineChart<>(xAxisLine, yAxisLine);

	@FXML
	public Button buttonMainStart, buttonMainFinish, buttonOpenUserWindow, buttonChooseSong;


	@FXML
	private GridPane gridMainScreen;

	@FXML
	public ChoiceBox choiceBoxDataChannel, choiceBoxUser, choiceBoxTraining, choiceBoxStimulation, choiceBoxDifficulty;

	@FXML
	public Label labelTimer, labelThreshold, labelSong, labelBaseline;

	@FXML
	private Circle circleIndicatorAF3, circleIndicatorAF4, circleIndicatorF7, circleIndicatorF3, circleIndicatorF4, circleIndicatorF8, circleIndicatorFC5, circleIndicatorFC6, circleIndicatorT7,
			circleIndicatorT8, circleIndicatorP7, circleIndicatorP8, circleIndicatorO1, circleIndicatorO2, circleIndicatorCMS, circleIndicatorDRL;

	@FXML
	public ImageView imageBattery, imageWireless;

	public static String dataChannelChoice = "AF3";

	public static EmotivData emotivData = new EmotivData();

	public List<String> users = new ArrayList<String>();

	public int TimerMax = 120, timer = 0;
	private Timeline time = new Timeline();

	public String pathMusicTrack;

	public XYChart.Series series = new XYChart.Series();


	public void setMainAppMain(Main mainApp) {
		this.mainApp = mainApp;
		mainApp.setSubScreen(this);
	}

	public void setSubScreen(UserScreen sub) {
		this.childUserScreen = sub;
	}

	public void setMainApp(MainScreen mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	public void initialize() {

		buttonMainFinish.setDisable(true);

		createUsersDirectory();

		initalizeChoiceBox();

		initalizeChoiceBoxUser();

		initalizeChoiceTraining();

		initalizeChoiceStimulation();

		initalizeChoiceDifficulty();

		initializeBarChartWaveData();

		initializeBarChartConcentrationData();

        //startEmotivData();

        startEmotivInfo();
		initializeHeadsetInfo();

		buttonOpenUserWindow.setOnAction(e -> {
			//UserScreen.display(this);
			displayUserWindow();
		});

		buttonChooseSong.setOnAction(e -> {
			//UserScreen.display(this);
			chooseSong();
		});

		buttonMainStart.setOnAction(e -> {
			if (baseline[0] == 0 && !choiceBoxTraining.getValue().toString().equals("Baseline")){
				showAlertBaseline();
				return;
			}

			buttonMainFinish.setDisable(false);
			buttonMainStart.setDisable(true);

			resetLineChart();

			disableItems();
			startStimulation();
			startTimer();

            startButton = true;
            killEpocThread = false;

            choiceEmotivData = choiceBoxDataChannel.getValue().toString();

		});

		buttonMainFinish.setOnAction(e -> {
			buttonMainFinish.setDisable(true);
			buttonMainStart.setDisable(false);

			if (choiceBoxStimulation.getValue().equals("Music") && buttonOpenUserWindow.isDisabled() && !labelSong.getText().equals(""))
				childUserScreen.mediaPlayer.stop();

			stopTimer();
			enableItems();

			if (choiceBoxTraining.getValue().toString().equals("Baseline")){
				baseline[0] = baseline[0] / divider;
                baseline[1] = baseline[1] / divider;
                baseline[2] = baseline[2] / divider;
                baseline[3] = baseline[3] / divider;
                baseline[4] = baseline[4] / divider;

				baselineValue = (baseline[0] + baseline[1] + baseline[2] + baseline[3] + baseline[4]) / 5;

				System.out.println("Base " +  baseline[0] + " " +  baseline[1] + " " +  baseline[2] + " " +  baseline[3] + " " +  baseline[4] + " "  +  divider + " " + baselineValue);

				labelBaseline.setText(String.valueOf(round(baselineValue , 2)));
			}

            killEpocThread = true;
			startButton = false;

		});

		choiceBoxTraining.setOnAction(e -> {
			if (baselineValue != 0){
				setThreshold();
			}
		});

		choiceBoxDifficulty.setOnAction(e -> {
			if (baselineValue != 0){
				setThreshold();
			}
		});



	}

	public void resetLineChart(){
		series.getData().clear();
		series.setName(choiceBoxTraining.getValue().toString());
		xSeriesData=0;
	}

	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public void setThreshold(){

		switch(choiceBoxTraining.getValue().toString())
		{
			case "Alpha/Theta":
				if (choiceBoxDifficulty.getValue().toString().equals("Easy"))
					threshold = (baseline[2]/baseline[0]) * 1.1;
				else if (choiceBoxDifficulty.getValue().toString().equals("Medium"))
					threshold = (baseline[2]/baseline[0]) * 1.2;
				else
					threshold = (baseline[2]/baseline[0]) * 1.3;
				labelBaseline.setText(String.valueOf(round(baseline[2]/baseline[0] , 2)));
				break;
			case "Theta/LowBeta":
				if (choiceBoxDifficulty.getValue().toString().equals("Easy"))
					threshold = (baseline[0]/baseline[1]) * 0.9;
				else if (choiceBoxDifficulty.getValue().toString().equals("Medium"))
					threshold = (baseline[0]/baseline[1]) * 0.8;
				else
					threshold = (baseline[0]/baseline[1]) * 0.7;
				labelBaseline.setText(String.valueOf(round(baseline[0]/baseline[1] , 2)));
				break;
			default:
				labelBaseline.setText(String.valueOf(round(baselineValue , 2)));
				break;
		}

		labelThreshold.setText(String.valueOf(round(threshold,2)));
	}

	public void disableItems(){
		choiceBoxStimulation.setDisable(true);
		choiceBoxTraining.setDisable(true);
		choiceBoxDataChannel.setDisable(true);
		choiceBoxUser.setDisable(true);
		choiceBoxDifficulty.setDisable(true);

		buttonChooseSong.setDisable(true);
	}

	public void enableItems(){
		choiceBoxStimulation.setDisable(false);
		choiceBoxTraining.setDisable(false);
		choiceBoxDataChannel.setDisable(false);
		choiceBoxUser.setDisable(false);
		choiceBoxDifficulty.setDisable(false);

		buttonChooseSong.setDisable(false);
	}

	public void startStimulation(){

		if(buttonOpenUserWindow.isDisabled()) {
			if (choiceBoxStimulation.getValue().toString().equals("Music") && !labelSong.getText().equals("")) {
				childUserScreen.initializeMusic();
			} else if (choiceBoxStimulation.getValue().toString().equals("Photos")) {
				childUserScreen.initialzeSlideShow();
			}
		}

	}

	public void chooseSong(){
		String pathMusic = System.getProperty("user.dir") + File.separator + "Documents" + File.separator + "Music" + File.separator;

		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("MP3", "*.mp3"),
				new FileChooser.ExtensionFilter("All Files", "*.*")
		);

		fileChooser.setInitialDirectory(new File(pathMusic));

		File file;

		if ((file = fileChooser.showOpenDialog(this.getPrimaryStage())) != null) {
			pathMusicTrack = file.getAbsolutePath();
			pathMusicTrack = pathMusicTrack.replace("\\", "/");

			String text = "";
			for (int i = pathMusicTrack.length() - 1; i>0;i--){
				if (pathMusicTrack.charAt(i)=='/')
					break;
				text = pathMusicTrack.charAt(i) + text;
			}
			labelSong.setText(text);
		}
	}

	public void startTimer(){
		timer = 0;
		timePlaying = 0;

		KeyFrame update = new KeyFrame(Duration.seconds(1), ae -> {
			updateLabel();
		});
		time = new Timeline(update);
		time.setCycleCount(Timeline.INDEFINITE);
		time.play();

	}

	public void updateLabel(){
		timer++;
		labelTimer.setText("" + timer + " sec");

		if (timer == 120){
			stopTimer();
			buttonMainFinish.fire();
		}

	}

	public void stopTimer(){
		time.stop();
	}

	public  void createUsersDirectory() {
		String path = System.getProperty("user.dir") + File.separator;

		File fileUsersTxt = new File(path+"Users.txt");
		if (!fileUsersTxt.exists()) {
			try {
				BufferedWriter bw = new BufferedWriter(new PrintWriter(path + "Users.txt"));
			} catch (IOException e) {
				e.printStackTrace();
			}

			File file = new File(path + "Documents");
			if (!file.exists()) {
				if (file.mkdir()) {
					System.out.println("Directory is created!");
				} else {
					System.out.println("Failed to create directory!");
				}
			}
		}

		path = System.getProperty("user.dir") + File.separator + "Documents" + File.separator;

		File fileUsers = new File(path+"Users");
		if (!fileUsers.exists()) {
			if (fileUsers.mkdir()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}

		File fileMusic = new File(path+"Music");
		if (!fileMusic.exists()) {
			if (fileMusic.mkdir()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}

		File filePhotos = new File(path+"Photos");
		if (!filePhotos.exists()) {
			if (filePhotos.mkdir()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}
	}


	/*public void startEmotivData(){
        backgorundThread = new Service<Void>() {


            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {

                        emotivData.epocData();  //Starts a stream of data
                        return null;
                    }
                };
            }
        };

        backgorundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {

            }
        });

        //barChartMain10.barGapProperty().bind(backgorundThread.);

        backgorundThread.restart();
    }*/


    public void startEmotivInfo(){

        backgorundThread2 = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>(){
                    @Override
                    protected Void call() throws Exception {

                        emotivData.epocHeadsetInfo();  //Starts a stream of data
                        return null;
                    }
                };
            }
        };

        backgorundThread2.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
            }
        });

        //barChartMain10.barGapProperty().bind(backgorundThread.);

        backgorundThread2.restart();

        //initializeHeadsetInfo();
    }


	public  void initializeHeadsetInfo(){


		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.millis(1000), (ActionEvent actionEvent) -> {

		if (emotivData.wirelessStrength != 0) {
            if (emotivData.batteryStrength == 5) {
                String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "battery1.png";
                File img = new File(path);
                imageBattery.setImage(new Image(img.toURI().toString()));
            } else if (emotivData.batteryStrength == 4) {
                String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "battery2.png";
                File img = new File(path);
                imageBattery.setImage(new Image(img.toURI().toString()));
            } else if (emotivData.batteryStrength == 3) {
                String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "battery3.png";
                File img = new File(path);
                imageBattery.setImage(new Image(img.toURI().toString()));
            } else if (emotivData.batteryStrength == 2) {
                String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "battery4.png";
                File img = new File(path);
                imageBattery.setImage(new Image(img.toURI().toString()));
            } else if (emotivData.batteryStrength == 1) {
                String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "battery5.png";
                File img = new File(path);
                imageBattery.setImage(new Image(img.toURI().toString()));
            } else {
                String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "battery6.png";
                File img = new File(path);
                imageBattery.setImage(new Image(img.toURI().toString()));
            }
        }
        else{
            String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "battery6.png";
            File img = new File(path);
            imageBattery.setImage(new Image(img.toURI().toString()));
        }


		if (emotivData.wirelessStrength == 2) {
			String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "wireless3.png";
			File img = new File(path);
			imageWireless.setImage(new Image(img.toURI().toString()));
		}
		else if (emotivData.wirelessStrength == 1){
			String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "wireless2.png";
			File img = new File(path);
			imageWireless.setImage(new Image(img.toURI().toString()));
		}
		else {
			String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "wireless1.png";
			File img = new File(path);
			imageWireless.setImage(new Image(img.toURI().toString()));
		}


                    if (emotivData.wirelessStrength != 0) {
                        circleIndicatorAF3.setFill(color(emotivData.signalAF3));
                        circleIndicatorAF4.setFill(color(emotivData.signalAF4));
                        circleIndicatorF7.setFill(color(emotivData.signalF7));
                        circleIndicatorF3.setFill(color(emotivData.signalF3));
                        circleIndicatorF4.setFill(color(emotivData.signalF4));
                        circleIndicatorF8.setFill(color(emotivData.signalF8));
                        circleIndicatorFC5.setFill(color(emotivData.signalFC5));
                        circleIndicatorFC6.setFill(color(emotivData.signalFC6));
                        circleIndicatorT7.setFill(color(emotivData.signalT7));
                        circleIndicatorT8.setFill(color(emotivData.signalT8));
                        circleIndicatorCMS.setFill(color(emotivData.signalCMS));
                        circleIndicatorDRL.setFill(color(emotivData.signalDRL));
                        circleIndicatorP7.setFill(color(emotivData.signalP7));
                        circleIndicatorP8.setFill(color(emotivData.signalP8));
                        circleIndicatorO1.setFill(color(emotivData.signalO1));
                        circleIndicatorO2.setFill(color(emotivData.signalO2));
                    }
                    else{
                        circleIndicatorAF3.setFill(color(0));
                        circleIndicatorAF4.setFill(color(0));
                        circleIndicatorF7.setFill(color(0));
                        circleIndicatorF3.setFill(color(0));
                        circleIndicatorF4.setFill(color(0));
                        circleIndicatorF8.setFill(color(0));
                        circleIndicatorFC5.setFill(color(0));
                        circleIndicatorFC6.setFill(color(0));
                        circleIndicatorT7.setFill(color(0));
                        circleIndicatorT8.setFill(color(0));
                        circleIndicatorCMS.setFill(color(0));
                        circleIndicatorDRL.setFill(color(0));
                        circleIndicatorP7.setFill(color(0));
                        circleIndicatorP8.setFill(color(0));
                        circleIndicatorO1.setFill(color(0));
                        circleIndicatorO2.setFill(color(0));
                    }
				}));

		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.setAutoReverse(true);
		timeline.play();


	}

	private Color color (int strength){
		Color value = Color.BLACK;

		switch (strength) {
			case 0 :  value = Color.BLACK; break;
			case 1 :  value = Color.RED; break;
			case 2 :  value = Color.ORANGE; break;
			case 3 :  value = Color.YELLOW; break;
			case 4 :  value = Color.GREEN; break;
			default: break;
		}


		return value;
	}

	public void initalizeChoiceDifficulty(){
		choiceBoxDifficulty.setItems(FXCollections.observableArrayList(
						"Easy", "Medium", "Hard")
		);
		choiceBoxDifficulty.getSelectionModel().selectFirst();
	}

	public void initalizeChoiceTraining(){
		choiceBoxTraining.setItems(FXCollections.observableArrayList(
						"Baseline", "Alpha/Theta", "Theta/LowBeta")
		);
		choiceBoxTraining.getSelectionModel().selectFirst();
	}

	public void initalizeChoiceStimulation(){
		choiceBoxStimulation.setItems(FXCollections.observableArrayList(
						"Music", "Photos")
		);
		choiceBoxStimulation.getSelectionModel().selectFirst();
	}

	public void initalizeChoiceBox(){
		choiceBoxDataChannel.setItems(FXCollections.observableArrayList(
						"AF3", "F7", "F3", "FC5", "T7", "P7", "Pz", "O1", "O2", "P8", "T8", "FC6", "F4", "F8", "AF4", "Total")
		);
		choiceBoxDataChannel.getSelectionModel().selectFirst();

		choiceBoxDataChannel.valueProperty().addListener(new ChangeListener() {
			@Override
			public void changed(ObservableValue observable, Object oldValue, Object newValue) {
				dataChannelChoice = newValue.toString();
                choiceEmotivData = choiceBoxDataChannel.getValue().toString();
			}
		});

        choiceEmotivData = choiceBoxDataChannel.getValue().toString();
	}

	public void initalizeChoiceBoxUser(){

		choiceBoxUser.getItems().clear();
		users.clear();

		BufferedReader reader = null;

		try {
			File file = new File("Users.txt");
			reader = new BufferedReader(new FileReader(file));

			String line;
			while ((line = reader.readLine()) != null) {
				users.add(line);
			}


		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (int i =0;i<users.size();i++){
			choiceBoxUser.setItems(FXCollections.observableArrayList(users));
		}

		choiceBoxUser.getSelectionModel().selectFirst();

	}

	static int xSeriesData = 0;

	public void initializeBarChartConcentrationData(){

		XYChart.Series series1 = new XYChart.Series();
		final XYChart.Data<String, Number> data = new XYChart.Data(choiceBoxTraining.getValue().toString(), 0);
		series1.getData().add(data);

		barChartConcentration.getData().removeAll(series1);
		barChartConcentration.getData().addAll(series1);
		barChartConcentration.setAnimated(false);

		//ProgressChart
		xSeriesData = 0;
		//defining a series
		series = new XYChart.Series();
		series.setName(choiceBoxTraining.getValue().toString());

		lineChartProgress.getData().remove(series.getData());
		lineChartProgress.getData().add(series);

		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.millis(500), (ActionEvent actionEvent) -> {


					if (startButton && resetBase) {
						baseline[0] = baseline[1] = baseline[2] = baseline[3] = baseline[4] = 0;
						System.out.println("Uslo je jej");
						resetBase = false;
					} else if (buttonMainFinish.isDisable() && !resetBase){
						resetBase = true;
					}

					double value = getMeasuringBarValue();

					data.setYValue(value);
					series1.getData().set(0, data);

					if (startButton) {
						series.getData().add(new XYChart.Data(++xSeriesData, value));
					}

					if (buttonOpenUserWindow.isDisabled()) {
						childUserScreen.data.setYValue(value);
					}

					if (choiceBoxStimulation.getValue().toString().equals("Music") && baseline[0] != 0 && !choiceBoxTraining.getValue().toString().equals("Baseline") && threshold != 0 && buttonOpenUserWindow.isDisabled() && buttonMainStart.isDisabled()){
						if (choiceBoxTraining.getValue().toString().equals("Alpha/Theta") && value < threshold){
							childUserScreen.mediaPlayer.pause();
						} else if (choiceBoxTraining.getValue().toString().equals("Alpha/Theta") && value >= threshold) {
							childUserScreen.mediaPlayer.play();
							timePlaying++;
						}

						if (choiceBoxTraining.getValue().toString().equals("Theta/LowBeta") && value > threshold){
							childUserScreen.mediaPlayer.pause();
						}else if (choiceBoxTraining.getValue().toString().equals("Theta/LowBeta") && value <= threshold){
							childUserScreen.mediaPlayer.play();
							timePlaying++;
						}
					}

					if (choiceBoxStimulation.getValue().toString().equals("Photos") && baseline[0] != 0 && !choiceBoxTraining.getValue().toString().equals("Baseline") && threshold != 0 && buttonOpenUserWindow.isDisabled() && buttonMainStart.isDisabled()){
						if (choiceBoxTraining.getValue().toString().equals("Alpha/Theta") && value < threshold){
							childUserScreen.timeline.pause();
						} else if (choiceBoxTraining.getValue().toString().equals("Alpha/Theta") && value >= threshold) {
							childUserScreen.timeline.play();
							timePlaying++;
						}

						if (choiceBoxTraining.getValue().toString().equals("Theta/LowBeta") && value > threshold){
							childUserScreen.timeline.pause();
						}else if (choiceBoxTraining.getValue().toString().equals("Theta/LowBeta") && value <= threshold){
							childUserScreen.timeline.play();
							timePlaying++;
						}
					}

				}));

		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.setAutoReverse(true);
		timeline.play();
	}

	private void showAlertBaseline(){
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle("Baseline");
		alert.setHeaderText(null);
		alert.setContentText("Baseline is not calculated and set. To do proper training you need to set baseline first!");
		alert.showAndWait();
	}

	public double getMeasuringBarValue(){
		double theta, lowBeta, value;

			switch(choiceBoxTraining.getValue().toString())
			{
				case "Baseline":
					if (baseline[0] == 0) divider = 0;
				    value = getMeasuringValue("Base");
					divider++;
					break;
				case "Alpha/Theta":
					value = getMeasuringValue("AT");
					break;
				case "Theta/LowBeta":
					value = getMeasuringValue("TB");
					break;
				default:
					//Here is different formula
					value = getMeasuringValue("Base");
					break;
			}
		return value;
	}

	public double getMeasuringValue(String choice){
		double theta=0, lowBeta=0, alpha = 0, gamma = 0, highBeta = 0, value;


        if (choice.equals("Base") && startButton) {
            if (choiceBoxDataChannel.getValue().toString().equals("Total")) {
                value = (emotivData.totalTheta + emotivData.totalLowBeta + emotivData.totalAlpha + emotivData.totalHighBeta + emotivData.totalGamma) / 5;
                baseline[0] += emotivData.totalTheta;
                baseline[1] += emotivData.totalLowBeta;
                baseline[2] += emotivData.totalAlpha;
                baseline[3] += emotivData.totalHighBeta;
                baseline[4] += emotivData.totalGamma;

            } else {
                value = (emotivData.singleTheta + emotivData.singleLowBeta + emotivData.singleAlpha + emotivData.singleHighBeta + emotivData.singleGamma) / 5;
                baseline[0] += emotivData.singleTheta;
                baseline[1] += emotivData.singleLowBeta;
                baseline[2] += emotivData.singleAlpha;
                baseline[3] += emotivData.singleHighBeta;
                baseline[4] += emotivData.singleGamma;
            }
            System.out.println("POstavljanje: " + baseline[0] + " " + emotivData.singleTheta + " 1 "  + baseline[1] + " " + emotivData.singleLowBeta + " 2 "  + baseline[2]
					+ " " + emotivData.singleAlpha + " 3 "  + baseline[3] + " " + emotivData.singleHighBeta + " 4 "  + baseline[4] + " " + emotivData.singleGamma + " D " + divider);
        }
        else if (choice.equals("AT")){
            if (choiceBoxDataChannel.getValue().toString().equals("Total")) {
                value = emotivData.totalAlpha / emotivData.totalTheta;
            } else {
                value = emotivData.singleAlpha / emotivData.singleTheta;
            }
        }
        else{
            if (choiceBoxDataChannel.getValue().toString().equals("Total")) {
                value = emotivData.totalTheta / emotivData.totalLowBeta;
            } else {
                value = emotivData.singleTheta / emotivData.singleLowBeta;
            }
        }

		return value;
	}


	public void initializeBarChartWaveData(){
		XYChart.Series series1 = new XYChart.Series();
		series1.getData().add(new XYChart.Data("theta", 0));
		series1.getData().add(new XYChart.Data("alpha", 0));
		series1.getData().add(new XYChart.Data("low_beta", 0));
		series1.getData().add(new XYChart.Data("high_beta",0));
		series1.getData().add(new XYChart.Data("gamma", 0));

		barChartWaveData.getData().addAll(series1);
		barChartWaveData.setAnimated(false);



		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.millis(500), (ActionEvent actionEvent) -> {

					if (choiceBoxDataChannel.getValue().toString().equals("Total")){
						series1.getData().set(0, new XYChart.Data("theta", emotivData.totalTheta));
						series1.getData().set(1, new XYChart.Data("alpha",emotivData.totalAlpha ));
						series1.getData().set(2, new XYChart.Data("low_beta", emotivData.totalLowBeta));
						series1.getData().set(3, new XYChart.Data("high_beta", emotivData.totalHighBeta));
						series1.getData().set(4, new XYChart.Data("gamma", emotivData.totalGamma));
					}
					else{
						/*series1.getData().set(0, new XYChart.Data("theta", theta.getValue()));
						series1.getData().set(1, new XYChart.Data("alpha", alpha.getValue()));
						series1.getData().set(2, new XYChart.Data("low_beta", low_beta.getValue()));
						series1.getData().set(3, new XYChart.Data("high_beta", high_beta.getValue()));
						series1.getData().set(4, new XYChart.Data("gamma", gamma.getValue()));*/

						series1.getData().set(0, new XYChart.Data("theta", emotivData.singleTheta));
						series1.getData().set(1, new XYChart.Data("alpha", emotivData.singleAlpha));
						series1.getData().set(2, new XYChart.Data("low_beta", emotivData.singleLowBeta));
						series1.getData().set(3, new XYChart.Data("high_beta", emotivData.singleHighBeta));
						series1.getData().set(4, new XYChart.Data("gamma", emotivData.singleGamma));
					}



				}));

		timeline.setCycleCount(Animation.INDEFINITE);
		timeline.setAutoReverse(true);  //!?
		timeline.play();
	}


	public void displayUserWindow () {

		try {
			buttonOpenUserWindow.setDisable(true);

			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getClassLoader().getResource("view/UserDisplay.fxml"));
			AnchorPane anchor = (AnchorPane) loader.load();

			Stage window = new Stage();
			window.initModality(Modality.WINDOW_MODAL);
			window.setTitle("Emotiv EEG");

			window.setOnCloseRequest(new EventHandler<WindowEvent>() {
				public void handle(WindowEvent we) {
					buttonOpenUserWindow.setDisable(false);
				}
			});

			// Give the controller access to the main app.
			UserScreen controller = loader.getController();
			controller.setMainApp(this);

			Scene scene = new Scene(anchor);
			window.setScene(scene);
			window.showAndWait();
			//userScreen = new UserScreen(this);


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
