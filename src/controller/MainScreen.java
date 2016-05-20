package controller;


import java.util.Random;

import Iedk.Edk;
import Iedk.EdkErrorCode;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.IntByReference;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;



public class MainScreen extends Main {
	
	private Main main;

	public static DoubleByReference alpha     = new DoubleByReference(0);
	public static DoubleByReference low_beta  = new DoubleByReference(0);
	public static DoubleByReference high_beta = new DoubleByReference(0);
	public static DoubleByReference gamma     = new DoubleByReference(0);
	public static DoubleByReference theta     = new DoubleByReference(0);

	private double totalAlpha, totalLowBeta, totalHighBeta, totalGamma, totalTheta;

	private Service<Void> backgorundThread;
	private boolean killEpocThread = false;

	final CategoryAxis xAxis1 = new CategoryAxis();
    final NumberAxis yAxis1 = new NumberAxis();

	final CategoryAxis xAxis2 = new CategoryAxis();
	final NumberAxis yAxis2 = new NumberAxis();
	
	@FXML
	private BarChart <String, Number> barChartConcentration = new BarChart<>(xAxis2, yAxis2);
	
	@FXML BarChart<String, Number> barChartWaveData = new BarChart<>(xAxis1, yAxis1);

	@FXML
	private Button buttonMainStart;

	@FXML
	private Button buttonMainFinish;
	
	@FXML
	private GridPane gridMainScreen;

	@FXML
	private ChoiceBox  choiceBoxDataChannel;

	
	
    final BarChart<String, Number> barChart1 = new BarChart<>(xAxis1, yAxis1);
	
	public void setMainApp(Main main) {
		System.out.println("Uslo je");
        this.main = main;
    }
	
	@FXML
    public void initialize() {

		initalizeChoiceBox();

		buttonMainStart.setOnAction(e -> {
			killEpocThread = false;
			//https://www.youtube.com/watch?v=wOtGPJBUAVs
			backgorundThread = new Service<Void>() {


				@Override
				protected Task<Void> createTask() {
					return new Task<Void>() {
						@Override
						protected Void call() throws Exception {

							epocData(isCancelled());
							System.out.println("Zavrsilo je.");
							return null;
						}
					};
				}
			};

			backgorundThread.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
				@Override
				public void handle(WorkerStateEvent event) {
					System.out.println("Zavrsilo je.");
				}
			});

			//barChartMain10.barGapProperty().bind(backgorundThread.);

			backgorundThread.restart();
		});

		buttonMainFinish.setOnAction(e -> {
			killEpocThread = true;
		});

		initializeBarChartWaveData();

		initializeBarChartConcentrationData();

	 
	    }


	public void initalizeChoiceBox(){
		choiceBoxDataChannel.setItems(FXCollections.observableArrayList(
						"AF3", "F7", "F3", "FC5", "T7", "P7", "Pz", "O1", "O2", "P8", "T8", "FC6", "F4", "F8", "AF4", "Total")
		);

		choiceBoxDataChannel.getSelectionModel().selectFirst();
	}

	public void initializeBarChartConcentrationData(){
		XYChart.Series series1 = new XYChart.Series();
		series1.getData().add(new XYChart.Data("Concentration", 0));



		barChartConcentration.getData().addAll(series1);
		//Label labelAnimated1 = new Label();
		barChartConcentration.setAnimated(false);

		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.millis(500), (ActionEvent actionEvent) -> {

					if (choiceBoxDataChannel.getValue().toString().equals("Total")){
						series1.getData().set(0, new XYChart.Data("Concentration", totalTheta/totalLowBeta));

					}
					else{
						series1.getData().set(0, new XYChart.Data("Concentration", theta.getValue()/low_beta.getValue()));
					}
				}));

		timeline.setCycleCount(1000);
		timeline.setAutoReverse(true);  //!?
		timeline.play();
	}

	public void initializeBarChartWaveData(){
		XYChart.Series series1 = new XYChart.Series();
		series1.getData().add(new XYChart.Data("theta", 0));
		series1.getData().add(new XYChart.Data("alpha", 0));
		series1.getData().add(new XYChart.Data("low_beta", 0));
		series1.getData().add(new XYChart.Data("high_beta",0));
		series1.getData().add(new XYChart.Data("gamma", 0));


		barChartWaveData.getData().addAll(series1);
		//Label labelAnimated1 = new Label();
		barChartWaveData.setAnimated(false);

		Timeline timeline = new Timeline();
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.millis(500), (ActionEvent actionEvent) -> {

					if (choiceBoxDataChannel.getValue().toString().equals("Total")){
						series1.getData().set(0, new XYChart.Data("theta", totalTheta));
						series1.getData().set(1, new XYChart.Data("alpha",totalAlpha ));
						series1.getData().set(2, new XYChart.Data("low_beta", totalLowBeta));
						series1.getData().set(3, new XYChart.Data("high_beta", totalHighBeta));
						series1.getData().set(4, new XYChart.Data("gamma", totalGamma));
					}
					else{
						series1.getData().set(0, new XYChart.Data("theta", theta.getValue()));
						series1.getData().set(1, new XYChart.Data("alpha", alpha.getValue()));
						series1.getData().set(2, new XYChart.Data("low_beta", low_beta.getValue()));
						series1.getData().set(3, new XYChart.Data("high_beta", high_beta.getValue()));
						series1.getData().set(4, new XYChart.Data("gamma", gamma.getValue()));
					}


				}));

		timeline.setCycleCount(1000);
		timeline.setAutoReverse(true);  //!?
		timeline.play();
	}

	public void epocData(boolean cancel) {
		Pointer eEvent = Edk.INSTANCE.IEE_EmoEngineEventCreate();
		Pointer eState = Edk.INSTANCE.IEE_EmoStateCreate();

		IntByReference userID = null;
		boolean ready = false;
		int state = 0;

		Edk.IEE_DataChannels_t dataChannel;

		userID = new IntByReference(0);

		if (Edk.INSTANCE.IEE_EngineConnect("Emotiv Systems-5") != EdkErrorCode.EDK_OK
				.ToInt()) {
			System.out.println("Emotiv Engine start up failed.");
			return;
		}

		System.out.println("Start receiving Data!");
		System.out.println("Theta, Alpha, Low_beta, High_beta, Gamma");

		while (true) {
			if (killEpocThread) {
				break;
			}

			state = Edk.INSTANCE.IEE_EngineGetNextEvent(eEvent);

			// New event needs to be handled
			if (state == EdkErrorCode.EDK_OK.ToInt()) {
				int eventType = Edk.INSTANCE.IEE_EmoEngineEventGetType(eEvent);
				Edk.INSTANCE.IEE_EmoEngineEventGetUserId(eEvent, userID);

				// Log the EmoState if it has been updated
				if (eventType == Edk.IEE_Event_t.IEE_UserAdded.ToInt())
					if (userID != null) {
						System.out.println("User added");
						ready = true;
					}
			} else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
				System.out.println("Internal error in Emotiv Engine!");
				break;
			}

			if (ready) {

				int i = 0;
				String choice = choiceBoxDataChannel.getValue().toString();

				// 01 = Pz
				switch (choice) {
					case "AF3" :  i = 3; break;
					case "F7" :  i = 4; break;
					case "F3" :  i = 5; break;
					case "FC5" :  i = 6; break;
					case "T7" :  i = 7; break;
					case "P7" :  i = 8; break;
					case "Pz" :  i = 9; break;
					case "O1" :  i = 9; break;
					case "O2" :  i = 10; break;
					case "P8" :  i = 11; break;
					case "T8" :  i = 12; break;
					case "FC6" :  i = 13; break;
					case "F4" :  i = 14; break;
					case "F8" :  i = 15; break;
					case "AF4" :  i = 16; break;
					case "Total" :  i = 0; break;
					default: break;
				}

				if (i != 0){
					totalAlpha = totalLowBeta = totalHighBeta = totalGamma = totalTheta = 0;
					for(int j = 3 ; j < 17 ; j++)
					{
						int result = Edk.INSTANCE.IEE_GetAverageBandPowers(userID.getValue(), j, theta, alpha, low_beta, high_beta, gamma);
						if(result == EdkErrorCode.EDK_OK.ToInt()){
							totalAlpha += alpha.getValue();
							totalLowBeta += low_beta.getValue();
							totalHighBeta += high_beta.getValue();
							totalGamma += gamma.getValue();
							totalTheta += theta.getValue();
						}
					}
					totalAlpha /= 16;
					totalLowBeta /= 16;
					totalHighBeta /= 16;
					totalGamma /= 16;
					totalTheta /= 16;
				}
				else {
					int result = Edk.INSTANCE.IEE_GetAverageBandPowers(userID.getValue(), i, theta, alpha, low_beta, high_beta, gamma);
					if(result == EdkErrorCode.EDK_OK.ToInt()){
						//If data is returned
					}
				}


			}
		}

		Edk.INSTANCE.IEE_EngineDisconnect();
		Edk.INSTANCE.IEE_EmoStateFree(eState);
		Edk.INSTANCE.IEE_EmoEngineEventFree(eEvent);
		System.out.println("Disconnected!");
	}
	

}
