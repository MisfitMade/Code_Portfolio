package main.backend;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Logic for the ReactionTime page
 */
public class ReactionTime {
    @FXML
    public Button interact;
    @FXML
    public StackPane root;

    //for computing reaction time
    private long startTime;
    //some timing variables
    private final long SCALE = 10000 * 10000;
    private long localStartTime = System.nanoTime();
   
    private long rand = ThreadLocalRandom.current().nextLong(SCALE, SCALE*100);
    
    //Timer for the turning the screen to green
    AnimationTimer timer = new AnimationTimer() {
        
        @Override
        public void handle(long now) {
            //if RAND amount of time has passed
            long elapsed = now - localStartTime;
            if(elapsed > rand){
                root.setStyle("-fx-background-color: green");
                startTime = System.nanoTime();
                interact.setText("Click!");
                this.stop();//stop the timer
            }
        }
    };

    /**
     * Runs some prep work when Loader.loadFXML loads reactionTime.fxml. Puts a pic in the scene
     */
    public void initialize(){
        //a little padding for the scene
        root.setPadding(new Insets(0,0,50,0));
        //for alignment
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        interact.setPrefSize((bounds.getWidth()-100.0), (bounds.getHeight()-100.0));
        //put a lighting bolt and text on the button as a VBox for a graphic
        VBox vBox = new VBox(16);
        vBox.setAlignment(Pos.CENTER);
        //the bolt
        Image bolt = new Image("main/resources/bolt.jpg");
        ImageView view = new ImageView(bolt);
        view.setFitHeight(270);
        view.setFitWidth(220);
        view.setPreserveRatio(true);
        //text that says Reaction time
        Text reaction = new Text("Reaction Time Test");
        reaction.setFill(Color.WHITE);
        reaction.setFont(Font.font(50));
        //instructions text
        Text instruct = new Text("When the red box turns green, click as quickly as you can.\nClick anywhere to start");
        instruct.setTextAlignment(TextAlignment.CENTER);
        instruct.setFont(Font.font(25));
        instruct.setFill(Color.WHITE);
        //add to vbox
        vBox.getChildren().addAll(view,reaction,instruct);
        //add to pane for graphic
        StackPane graphic = new StackPane(vBox);
        graphic.setAlignment(Pos.CENTER);
        graphic.setPadding(new Insets(0,0,0,200));
        interact.setGraphic(graphic);
        interact.setTextFill(Color.BLUE);
    }

    /**
     * Is the button which is practically the entire screen for this game
     * @param actionEvent : on click for the button
     */
    public void interact(ActionEvent actionEvent) {
        //pull off the graphic
        interact.setGraphic(null);
        interact.setTextFill(Color.WHITE);
        //get the button's text
        String buttonsText = interact.getText();

        /*if the button's text is the initially set text or the text shown when the player clicks too soon or it is the
        text shown when displaying a player's reaction time      */
        if(buttonsText.charAt(0) == 'R' || buttonsText.charAt(0) == 'T' ||
                (buttonsText.charAt(0) > '/' && buttonsText.charAt(0) < ':')){
            //reset timing variables
            localStartTime = System.nanoTime();
            rand = ThreadLocalRandom.current().nextLong(SCALE, SCALE*100);
            //start timer
            timer.start();
            //start the timer, change the color of background, and the button's text
            root.setStyle("-fx-background-color: red");
            interact.setText("Wait for green");
        }
        //else if the button's text is 'Wait for green', meaning they clicked too soon.
        else if(buttonsText.charAt(0) == 'W'){
            //stop the timer
            timer.stop();
            //change color of background, tell them they clicked too soon
            root.setStyle("-fx-background-color: blue");
            interact.setText("Too soon!\nClick to try again.");
        }
        //else if they have clicked and the screen is green, the text is 'Click!'
        else if(buttonsText.charAt(0) == 'C'){
            long millis = (System.nanoTime() - startTime)/1000000;
            root.setStyle("-fx-background-color: blue");
            interact.setText(millis + " ms\nClick to keep going");
        }
    }
    /*---------Note-------------------------------
    ReactionTime does not have a "Save Score" functionality on humanbenchmark.com. At least not today, but
    savedScores.txt has space for it, though my implementation of ReactionTime does not have a Save Score button*/
}
