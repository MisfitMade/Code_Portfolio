
/*Michael Nafe | nafem@unm.edu*/

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

public class Display {

    private final Scene SCENE;
    private final GraphicsContext CANVAS_GRAPHICS;
    private int multiplyBy;
    private long rate;//controlled by the < and > buttons
    //indicates if the points number has changed, to keep from re-drawing the points over and over
    private int circlePoints;
    private Visualizer visualizer;
    private boolean cyclingVisuals;
    private boolean unpaused;//will control the pause and play button
    private final Slider INCREMENT_BY;
    private boolean sliderChanged;
    private ComboBox<Integer> timesTableSelector;

    public Display(){
        //initial values for times table visuals
        circlePoints = 36;
        multiplyBy = 82;
        rate = 5;//5 is slowest, 1 is fastest
        unpaused = true;//initially want to be playing the visualization
        //controls the visualizations seen if the user clicks the cycle visuls button
        cyclingVisuals = false;

        //fancy shadow effect
        DropShadow shadow = new DropShadow();shadow.setOffsetX(5.0);
        shadow.setOffsetY(3.0);
        shadow.setOffsetX(3.0);
        shadow.setColor(Color.WHITE);//rgb(0,0,0,0.45));
        //a title for the scene: formatting, context and effect
        Label sceneTitle = new Label("Times Table Visualization");
        sceneTitle.setTextFill(Color.rgb(255, 0, 0,0.9));
        sceneTitle.setFont(Font.font(null, FontWeight.BOLD, 52));
        sceneTitle.setEffect(shadow);
        sceneTitle.setPadding(new Insets(0,35,0,35));
        sceneTitle.setBackground(new Background(new BackgroundFill(Color.BLACK,
                new CornerRadii(12), new Insets(5,15,5,15))));

        //a canvas for drawing the lines on. put a circle right away
        Canvas canvas = new Canvas(700,700);
        CANVAS_GRAPHICS = canvas.getGraphicsContext2D();


        //put the circle and canvas in a stack pane
        StackPane circleWithLines = new StackPane();
        circleWithLines.setAlignment(Pos.CENTER);
        circleWithLines.getChildren().addAll(canvas);


        /*now the buttons themselves
        background for the buttons */
        final int LABEL_AND_BUTTON_WIDTH = 175;
        Background buttonBack = new Background(new BackgroundFill(Color.RED,
                new CornerRadii(50), new Insets(5)));

        //the pause button and its label
        Label pauseLabel = new Label("Play/Pause");
        pauseLabel.setFont(Font.font(null, FontWeight.BOLD, 22));
        pauseLabel.setTextFill(Color.WHITE);
        pauseLabel.setMinSize(LABEL_AND_BUTTON_WIDTH,65);
        pauseLabel.setAlignment(Pos.CENTER);
        //pause button
        Button pause = new Button();
        pause.setEffect(shadow);
        //a square and triangle shape for the play and pause symbols
        final Rectangle pauseSquare = new Rectangle(25,25);
        pauseSquare.setFill(Color.BLACK);
        final Polygon playTriangle = new Polygon(67.0,20.0,92.0,32.5,67.0,45.0);
        playTriangle.setFill(Color.BLACK);
        pause.setGraphic(pauseSquare);
        pause.setBackground(buttonBack);
        pause.setMinSize(LABEL_AND_BUTTON_WIDTH,65);
        //pause button functionality
        pause.setOnAction(e -> {
            //if the button's graphic is the pause square, then the user wants to pause
            if(unpaused){
                pause.setGraphic(playTriangle);//switch symbol
                unpaused = false;//switch to pause
            }
            //else the user has clicked the button to play the visualization
            else{
                pause.setGraphic(pauseSquare);//switch symbol
                unpaused = true;
            }
        });
        //the FPS buttons, will be a < and a > in an HBox and their label
        Label speed = new Label("Rate");
        speed.setFont(Font.font(null, FontWeight.BOLD, 22));
        speed.setMinSize(LABEL_AND_BUTTON_WIDTH,65);
        speed.setTextFill(Color.WHITE);
        speed.setAlignment(Pos.CENTER);
        /*using this to show the user that the rate has been changed.*/
        PauseTransition rateButtonEffect = new PauseTransition();
        //decrease speed button
        Button slowDown = new Button("<");
        slowDown.setTextFill(Color.BLACK);
        slowDown.setFont(Font.font(null, FontWeight.BOLD, 22));
        slowDown.setBackground(new Background(new BackgroundFill(Color.RED,
                new CornerRadii(50,0,0,50,false), Insets.EMPTY)));
        slowDown.setMinSize(79.5,57);
        //slow down button functionality
        slowDown.setOnAction(e -> {
            //I will not allow the user to decrease the rate to 0
            if(rate < 5){
                slowDown.setBackground(new Background(new BackgroundFill(Color.DARKRED,
                        new CornerRadii(50,25,25,50,false), Insets.EMPTY)));
                rate += 1;
                rateButtonEffect.play();
            }
        });
        //increase speed button
        Button speedUp = new Button(">");
        speedUp.setTextFill(Color.BLACK);
        speedUp.setFont(Font.font(null, FontWeight.BOLD, 22));
        speedUp.setBackground(new Background(new BackgroundFill(Color.RED,
                new CornerRadii(0,50,50,0,false), Insets.EMPTY)));
        speedUp.setMinSize(79.5,57);
        //speed up button functionality
        speedUp.setOnAction(e -> {

            //I will not allow the rate to go above 5
            if(rate > 1){
                speedUp.setBackground(new Background(new BackgroundFill(Color.DARKRED,
                        new CornerRadii(25,50,50,25,false), Insets.EMPTY)));
                rate -= 1;
                rateButtonEffect.play();
            }
        });
        //add the buttons to their interior HBox
        HBox fpsArrows = new HBox(0);
        fpsArrows.setAlignment(Pos.CENTER);
        fpsArrows.setEffect(shadow);
        fpsArrows.setMaxSize(LABEL_AND_BUTTON_WIDTH,65);
        fpsArrows.getChildren().addAll(slowDown,speedUp);

        /*Define rateButtonEffect's functionality.
        using this to show the user that the rate has been changed. If rate can be changed, text color change on
        click, then change back after pause. This is a simulated effect on the speed up and speed down buttons*/
        rateButtonEffect.setDuration(Duration.millis(150));//quarter a second pause
        rateButtonEffect.setOnFinished(event -> {
            //if it is the speedUp button that has been clicked
            if(speedUp.getBackground().getFills().get(0).getFill().equals(Color.DARKRED)) {
                //restore speedUps default background
                speedUp.setBackground(new Background(new BackgroundFill(Color.RED,
                        new CornerRadii(0,50,50,0,false), Insets.EMPTY)));
            }
            //else it must have been slowDown that was clicked. speedUp and slowDown have slightly different backs
            else {
                slowDown.setBackground(new Background(new BackgroundFill(Color.RED,
                        new CornerRadii(50,0,0,50,false), Insets.EMPTY)));
            }
        });

        //The increment by slider label
        Label sliderLabel = new Label("Increment By");
        sliderLabel.setTextFill(Color.WHITE);
        sliderLabel.setMinSize(LABEL_AND_BUTTON_WIDTH,65);
        sliderLabel.setFont(Font.font(null, FontWeight.BOLD, 22));
        sliderLabel.setAlignment(Pos.CENTER);
        //The actual slider, defined here so objects below it can see it
        sliderChanged = false;
        INCREMENT_BY = new Slider(0,5,0);
        INCREMENT_BY.setBackground(new Background(new BackgroundFill(Color.RED,
                new CornerRadii(50), new Insets(0))));
        INCREMENT_BY.setEffect(shadow);
        INCREMENT_BY.setMinSize(LABEL_AND_BUTTON_WIDTH, 65);
        INCREMENT_BY.setMajorTickUnit(0.5);
        INCREMENT_BY.setMinorTickCount(1);
        INCREMENT_BY.setStyle("-fx-font-size: 20");
        INCREMENT_BY.setShowTickMarks(true);
        INCREMENT_BY.setShowTickLabels(true);
        //detects a change in the slider
        INCREMENT_BY.valueProperty().addListener((v, oldValue, newValue) -> {
            sliderChanged = true;
        });

        Label multiplyByLabel = new Label("Multiply By");
        multiplyByLabel.setFont(Font.font(null, FontWeight.BOLD, 22));
        multiplyByLabel.setMinSize(LABEL_AND_BUTTON_WIDTH,65);
        multiplyByLabel.setTextFill(Color.WHITE);
        multiplyByLabel.setAlignment(Pos.CENTER);
        //change the time table number button and its label
        timesTableSelector = new ComboBox<>();
        //also going to fill the points selector at same time for speed, but define its format and function below
        ComboBox<Integer> pointsSelector = new ComboBox<>();
        //fill timesTableSelector with values for 0 - 360
        timesTableSelector.getItems().addAll(0,1,2);
        //I am imposing that there must be at least 3 points: the starting point and two additional
        for(int i = 3; i < 361; i++){
            timesTableSelector.getItems().add(i);
            pointsSelector.getItems().add(i);
        }
        timesTableSelector.setMinSize(LABEL_AND_BUTTON_WIDTH, 65);
        timesTableSelector.setEffect(shadow);
        timesTableSelector.setValue(timesTableSelector.getItems().get(0));//set 2 in the boc by default
        timesTableSelector.setBackground(buttonBack);
        timesTableSelector.setStyle("-fx-font-size: 29");
        timesTableSelector.setValue(82);
        //functionality of combo box
        timesTableSelector.setOnAction(e -> {
            //the user is not using the cycle visuals button
            cyclingVisuals = false;
            //get the value in the combo box as the global multiplyBy
            multiplyBy = timesTableSelector.getValue();
            //if the simulation is paused, redraw here
            if(!unpaused){
                visualizer.drawLines(1);
            }
        });

        //change the number of points, combo box for it filled above and its label is below
        Label points = new Label("Points");
        points.setAlignment(Pos.CENTER);
        points.setTextFill(Color.WHITE);
        points.setMinSize(LABEL_AND_BUTTON_WIDTH,65);
        points.setFont(Font.font(null, FontWeight.BOLD, 22));
        //the points combo box
        pointsSelector.setMinSize(LABEL_AND_BUTTON_WIDTH, 65);
        pointsSelector.setEffect(shadow);
        pointsSelector.setValue(pointsSelector.getItems().get(0));//set 2 in the boc by default
        pointsSelector.setBackground(buttonBack);
        pointsSelector.setStyle("-fx-font-size: 29");
        pointsSelector.setValue(36);
        //functionality of combo box
        pointsSelector.setOnAction(e -> {
            //the user is not using the cycle visuals button
            cyclingVisuals = false;
            //change the number of points around the circle
            circlePoints = pointsSelector.getValue();
            //if the simulation is paused, redraw here
            if(!unpaused){
                visualizer.drawLines(1);
            }
        });
        //labels
        Label visualCycle = new Label("Visuals");
        visualCycle.setFont(Font.font(null, FontWeight.BOLD, 22));
        visualCycle.setAlignment(Pos.CENTER);
        visualCycle.setTextFill(Color.WHITE);
        visualCycle.setMinSize(LABEL_AND_BUTTON_WIDTH,65);
        //using this pause transition for the visuals button pressed effect
        PauseTransition visualsButtonEffect = new PauseTransition();
        //cycle through favorite visualizations
        Button cycleVisualizations = new Button(" > ");
        cycleVisualizations.setEffect(shadow);
        cycleVisualizations.setTextFill(Color.BLACK);
        cycleVisualizations.setFont(Font.font(null, FontWeight.BOLD, 22));
        cycleVisualizations.setBackground(buttonBack);
        cycleVisualizations.setMinSize(150,65);
        //speed up button functionality
        cycleVisualizations.setOnAction(e -> {
            //the user is now cycling through the preset visuals
            cyclingVisuals = true;
            //inc to the next visualization
            visualizer.incPresetCounter();

            //force a redraw so that it is not dependent on the rate
            visualizer.drawLines(1);
            //change the buttons background color briefly
            cycleVisualizations.setBackground(new Background(new BackgroundFill(Color.DARKRED,
                    new CornerRadii(50), Insets.EMPTY)));
            //this pause transition turns the button's color back to normal after 150 milliseconds
            visualsButtonEffect.play();
        });

        visualsButtonEffect.setDuration(Duration.millis(150));
        visualsButtonEffect.setOnFinished(e -> {
            //change the cycle visualizations button back to normal background on pause finish
            cycleVisualizations.setBackground(buttonBack);
        });


        //the buttons will be put in an HBox
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.BOTTOM_CENTER);
        buttonBox.setPadding(new Insets(35));
        buttonBox.getChildren().addAll(pause,fpsArrows,timesTableSelector,INCREMENT_BY,pointsSelector,cycleVisualizations);
        //the labels will be in an HBox
        HBox labelBox = new HBox(10);
        labelBox.setAlignment(Pos.BOTTOM_CENTER);
        labelBox.setPadding(new Insets(35));
        labelBox.getChildren().addAll(pauseLabel,speed,multiplyByLabel,sliderLabel,points,visualCycle);
        //put the label and button boxes in a VBox so i can snug them up close to one another
        VBox buttonsAndLabels = new VBox(-90);
        buttonsAndLabels.setAlignment(Pos.CENTER);
        buttonsAndLabels.getChildren().addAll(labelBox,buttonBox);

        //put title label and circle and buttons and labels in a an VBox, do some formatting and style on it
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(sceneTitle,circleWithLines,buttonsAndLabels);
        layout.setBackground(new Background(new BackgroundFill(Color.rgb(0,0,0,0.9),
                new CornerRadii(55), Insets.EMPTY)));

        //now that we have the whole GUI defined, declare a visualizer
        visualizer = new Visualizer(this);
        //a scene for the stage
        SCENE = new Scene(layout,1100,1010);
    }

    public Scene getSCENE() {
        return SCENE;
    }
    //getter for the rate of visualizations
    public long getRate() {
        return rate;
    }
    //getter for the canvas' graphics context
    public GraphicsContext getCanvasGraphics() {
        return CANVAS_GRAPHICS;
    }
    //getter for number to multiply by
    public int getMultiplyBy() {
        return multiplyBy;
    }
    //getter for number of points around circle
    public int getCirclePoints() {
        return circlePoints;
    }

    //getter for visualizer
    public Visualizer getVisualizer() {
        return visualizer;
    }
    //getter for whether the user is cycling preset visuals
    public boolean isCyclingVisuals() {
        return cyclingVisuals;
    }
    //getter for the pla/pause boolean
    public boolean isUnpaused() {
        return unpaused;
    }
    //getter for slider and its boolean value that knows when the slider has changed
    public Slider getIncrementBy() {
        return INCREMENT_BY;
    }
    public boolean isSliderChanged() {
        return sliderChanged;
    }
    public void setSliderChanged(boolean sliderChanged) {
        this.sliderChanged = sliderChanged;
    }
    //getter for multiplied by combo box
    public ComboBox<Integer> getTimesTableSelector() {
        return timesTableSelector;
    }
}
