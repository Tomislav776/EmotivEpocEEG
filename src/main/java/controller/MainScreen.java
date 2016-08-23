package controller;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import Iedk.Edk;
import Iedk.EdkErrorCode;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
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

	public double threshold, barChartValue, baseline = 0, divider = 1, timePlaying = 0;

	
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

		startEmotivInfo();

		buttonOpenUserWindow.setOnAction(e -> {
			//UserScreen.display(this);
			displayUserWindow();
		});

		buttonChooseSong.setOnAction(e -> {
			//UserScreen.display(this);
			chooseSong();
		});

		buttonMainStart.setOnAction(e -> {
			if (baseline == 0 && !choiceBoxTraining.getValue().toString().equals("Baseline")){
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
			//https://www.youtube.com/watch?v=wOtGPJBUAVs
			backgorundThread = new Service<Void>() {


				@Override
				protected Task<Void> createTask() {
					return new Task<Void>() {
						@Override
						protected Void call() throws Exception {

							//epocData(isCancelled());
							emotivData.testData();  //Starts a stream of data
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
		});

		buttonMainFinish.setOnAction(e -> {
			buttonMainFinish.setDisable(true);
			buttonMainStart.setDisable(false);

			if (choiceBoxStimulation.getValue().equals("Music") && buttonOpenUserWindow.isDisabled() && !labelSong.getText().equals(""))
				childUserScreen.mediaPlayer.stop();

			stopTimer();
			enableItems();

			if (choiceBoxTraining.getValue().toString().equals("Baseline")){
				baseline = baseline / divider;
				labelBaseline.setText(String.valueOf(round(baseline, 2)));
			}

			killEpocThread = true;
			startButton = false;

		});

		choiceBoxTraining.setOnAction(e -> {
			if (baseline != 0){
				setThreshold();
			}
		});

		choiceBoxDifficulty.setOnAction(e -> {
			if (baseline != 0){
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
				threshold = baseline * 1.1;
				else if (choiceBoxDifficulty.getValue().toString().equals("Medium"))
					threshold = baseline * 1.2;
				else
					threshold = baseline * 1.3;
				break;
			case "Theta/LowBeta":
				if (choiceBoxDifficulty.getValue().toString().equals("Easy"))
					threshold = baseline * 0.9;
				else if (choiceBoxDifficulty.getValue().toString().equals("Medium"))
					threshold = baseline * 0.8;
				else
					threshold = baseline * 0.7;
				break;
			default:

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

	public void startEmotivInfo(){

		backgorundThread2 = new Service<Void>() {

			@Override
			protected Task<Void> createTask() {
				return new Task<Void>() {
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

		initializeHeadsetInfo();

	}

	public  void initializeHeadsetInfo(){

		if (emotivData.batteryStrength == 3) {
			String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "battery1.png";
			File img = new File(path);
			imageBattery.setImage(new Image(img.toURI().toString()));
		}
		else if (emotivData.batteryStrength == 2){
			String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "battery2.png";
			File img = new File(path);
			imageBattery.setImage(new Image(img.toURI().toString()));
		}
		else if (emotivData.batteryStrength == 1){
			String path = System.getProperty("user.dir") + File.separator + "IndicatorPhotos" + File.separator + "battery3.png";
			File img = new File(path);
			imageBattery.setImage(new Image(img.toURI().toString()));
		}
		else {
			String path = System.getProperty("user.dir")  + File.separator + "IndicatorPhotos" + File.separator + "battery4.png";
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
						"Baseline", "Alpha/Theta", "Theta/LowBeta", "Frontal Asymmetry")
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
			}
		});
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
					double value = getMeasuringBarValue();

						data.setYValue(value);
						series1.getData().set(0, data);

						if (startButton) {
							series.getData().add(new XYChart.Data(++xSeriesData, value));
						}

						if (buttonOpenUserWindow.isDisabled()) {
							childUserScreen.data.setYValue(value);
						}

					if (choiceBoxStimulation.getValue().toString().equals("Music") && baseline != 0 && !choiceBoxTraining.getValue().toString().equals("Baseline") && threshold != 0 && buttonOpenUserWindow.isDisabled() && buttonMainStart.isDisabled()){
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

					if (choiceBoxStimulation.getValue().toString().equals("Photos") && baseline != 0 && !choiceBoxTraining.getValue().toString().equals("Baseline") && threshold != 0 && buttonOpenUserWindow.isDisabled() && buttonMainStart.isDisabled()){
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

		timeline.setCycleCount(1000);
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

		if (buttonChooseSong.isDisabled())
		switch(choiceBoxTraining.getValue().toString())
		{
			case "Baseline":
				baseline += getMeasuringValue();
				divider++;
				break;
			case "Alpha/Theta":
				getMeasuringValue();
				break;
			case "Theta/LowBeta":
				getMeasuringValue();
				break;
			default:
				//Here is different formula
				getMeasuringValue();
				break;
		}


		value = getMeasuringValue();
		return value;
	}

	public double getMeasuringValue(){
		double theta, lowBeta;


		if (choiceBoxDataChannel.getValue().toString().equals("Total")){
			theta = emotivData.totalTheta;
			lowBeta = emotivData.totalLowBeta;
		}
		else
		{
			theta = emotivData.randomTheta;
			lowBeta = emotivData.randomLowBeta;
		}

		return theta/lowBeta;
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

						series1.getData().set(0, new XYChart.Data("theta", emotivData.randomTheta));
						series1.getData().set(1, new XYChart.Data("alpha", emotivData.randomAlpha));
						series1.getData().set(2, new XYChart.Data("low_beta", emotivData.randomLowBeta));
						series1.getData().set(3, new XYChart.Data("high_beta", emotivData.randomHighBeta));
						series1.getData().set(4, new XYChart.Data("gamma", emotivData.randomGamma));
					}



				}));

		timeline.setCycleCount(1000);
		timeline.setAutoReverse(true);  //!?
		timeline.play();
	}


	public void displayUserWindow () {

		try {
			buttonOpenUserWindow.setDisable(true);

			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(Main.class.getResource("../view/UserDisplay.fxml"));
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
