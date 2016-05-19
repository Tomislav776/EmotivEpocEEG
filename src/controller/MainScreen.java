package controller;

import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class MainScreen extends Main {
	
	private Main main;
	
	int group[] = new int[10];
	final CategoryAxis xAxis1 = new CategoryAxis();
    final NumberAxis yAxis1 = new NumberAxis();
	
	@FXML
	private BarChart barChartMain11;
	
	@FXML
	private BarChart<String, Number> barChartMain10 = new BarChart<>(xAxis1, yAxis1);
	
	@FXML
	private GridPane gridMainScreen;
	
	
    final BarChart<String, Number> barChart1 = new BarChart<>(xAxis1, yAxis1);
	
	public void setMainApp(Main main) {
		System.out.println("Uslo je");
        this.main = main;
    }
	
	@FXML
    public void initialize() {
		
		System.out.println("Uslo je");
		
		 XYChart.Series series1 = new XYChart.Series();
	        series1.getData().add(new XYChart.Data("0-10", group[0]));
	        series1.getData().add(new XYChart.Data("10-20", group[1]));
	        series1.getData().add(new XYChart.Data("20-30", group[2]));
	        series1.getData().add(new XYChart.Data("30-40", group[3]));
	        series1.getData().add(new XYChart.Data("40-50", group[4]));
	        series1.getData().add(new XYChart.Data("50-60", group[5]));
	        series1.getData().add(new XYChart.Data("60-70", group[6]));
	        series1.getData().add(new XYChart.Data("70-80", group[7]));
	        series1.getData().add(new XYChart.Data("80-90", group[8]));
	        series1.getData().add(new XYChart.Data("90-100", group[9]));

	        barChartMain10.getData().addAll(series1);
	        //Label labelAnimated1 = new Label();
	        barChartMain10.setAnimated(false);
	        //vBoxBarChart1.getChildren().addAll(barChart1, labelAnimated1);
	        //- End of barChart1
	 
	        //PieChart2 with setAnimated(false)
	        //VBox vBoxPieChart2 = new VBox();
	 
	 
	        Random random = new Random();
	 
	        //Apply Animating Data in Charts
	        //ref: http://docs.oracle.com/javafx/2/charts/bar-chart.htm
	        //"Animating Data in Charts" section
	        Timeline timeline = new Timeline();
	        timeline.getKeyFrames().add(
	                new KeyFrame(Duration.millis(200), (ActionEvent actionEvent) -> {
	 
	                    int data = random.nextInt(100);
	                    /*
	                    final int mean = 50;
	                    final int standardDeviation = 10;
	                    int data = mean + (int) (random.nextGaussian() * standardDeviation);
	                    */
	 
	                    if (data <= 10) {
	                        group[0]++;
	                        series1.getData().set(0, new XYChart.Data("0-10", group[0]));

	                    } else if (data <= 20) {
	                        group[1]++;
	                        series1.getData().set(1, new XYChart.Data("10-20", group[1]));

	                    } else if (data <= 30) {
	                        group[2]++;
	                        series1.getData().set(2, new XYChart.Data("20-30", group[2]));
;
	                    } else if (data <= 40) {
	                        group[3]++;
	                        series1.getData().set(3, new XYChart.Data("30-40", group[3]));
	              
	                    } else if (data <= 50) {
	                        group[4]++;
	                        series1.getData().set(4, new XYChart.Data("40-50", group[4]));
	          
	                    } else if (data <= 60) {
	                        group[5]++;
	                        series1.getData().set(5, new XYChart.Data("50-60", group[5]));
	              
	                    } else if (data <= 70) {
	                        group[6]++;
	                        series1.getData().set(6, new XYChart.Data("60-70", group[6]));
	             
	                    } else if (data <= 80) {
	                        group[7]++;
	                        series1.getData().set(7, new XYChart.Data("70-80", group[7]));
	           
	                    } else if (data <= 90) {
	                        group[8]++;
	                        series1.getData().set(8, new XYChart.Data("80-90", group[8]));
	        
	                    } else if (data <= 100) {
	                        group[9]++;
	                        series1.getData().set(9, new XYChart.Data("10-100", group[9]));

	                    }
	 
	                    /*labelAnimated1.setText(
	                            "barChart1.getAnimated() = " + barChart1.getAnimated());*/

	 
	                    String s = "";
	                    for (int i = 0; i < 10; i++) {
	                        s += " " + group[i];
	                    }
	                   // labelCnt.setText(s);
	                }));
	 
	        timeline.setCycleCount(1000);
	        timeline.setAutoReverse(true);  //!?
	        timeline.play();
	 
	    }
	 
	 
	    //generate dummy random data
	    private void prepareData() {
	        for (int i = 0; i < 10; i++) {
	            group[i] = 0;
	        }
	    }
	

}
