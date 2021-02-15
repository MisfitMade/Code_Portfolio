package main.backend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Scanner;

/**
 * The Class that does the logic for the AimTrainer game
 */
public class AimTrainer {

    @FXML
    public Pane root;
    @FXML
    public VBox vbox;
    @FXML
    public Button bullseye;

    //will count up to 30
    private int targetsLeft;

    //will just accumulate all the times it takes to click the targets
    private long times;
    private long localTime;

    /**
     * Ran on Loader.loadFXML(), does some prep for the page. Make a Image and puts it in.
     */
    public void initialize(){

        Image bulls = new Image("main/resources/bullseye.png");
        ImageView view = new ImageView(bulls);
        view.setFitHeight(80);
        view.setPreserveRatio(true);

        bullseye.setGraphic(view);

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        vbox.setLayoutX(bounds.getWidth()/2 - 200);
        vbox.setLayoutY(bounds.getHeight()/2 - 400);
    }

    /**
     * The onAction for the bullseye button defined in the aimTrainer.fxml
     * @param actionEvent : on click
     */
    public void startTrainer(ActionEvent actionEvent) {

        root.getChildren().remove(vbox);

        //for aligning on the screen
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        //the label that says how many targets remain
        Text remains = new Text("Remaining\n30");
        remains.setTextAlignment(TextAlignment.CENTER);
        remains.setFont(Font.font(30));
        remains.setFill(Color.WHITE);
        remains.setLayoutX(bounds.getWidth() - (bounds.getWidth()/2) - 100);
        remains.setLayoutY(120.0);
        //add this remains label
        root.getChildren().addAll(remains);
        //start with 30 targets
        targetsLeft = 30;
        //will just add up all the times to be divided by 30 at the end
        times = 0;

        //bounds for the random number generator
        double xBound = bounds.getWidth()-(bounds.getWidth()/2);
        double yBound = bounds.getHeight() - bounds.getHeight()/2;

        //generate the first randomly placed target
        SecureRandom random = new SecureRandom();
        bullseye.setLayoutX((bounds.getWidth()/4) + random.nextDouble() * xBound);
        bullseye.setLayoutY(random.nextDouble() * yBound);

        //initial start time
        localTime = System.currentTimeMillis();
        bullseye.setOnAction(e -> {
            //add to times the amount of time it took to click the target
            times += (System.currentTimeMillis() - localTime);

            //set remains label
            targetsLeft--;
            remains.setText("Remaining\n" + targetsLeft);

            root.getChildren().remove(bullseye);

            //if there are more targets to go
            if(targetsLeft > 0){
                //generate a new x and y coordinate
                bullseye.setLayoutX((bounds.getWidth()/4) + random.nextDouble() * xBound);
                bullseye.setLayoutY(random.nextDouble() * yBound);

                //add it in
                root.getChildren().add(bullseye);
                //get a start time
                localTime = System.currentTimeMillis();
            }
            //else the mini game has finished
            else{
                //remove the remains label
                root.getChildren().remove(remains);

                VBox results = new VBox(12);
                results.setAlignment(Pos.CENTER);

                /*add in a bullseye pic, the player's avg time and more info, then a save score button and try again
                button*/
                ImageView bullsI = new ImageView(new Image("main/resources/bullseye.png"));

                Text millis = new Text(String.valueOf(times/30));
                millis.setFont(new Font(35));
                Label avgTime = new Label("Average time per target\n" + millis.getText() + "ms\n\n" +
                        "Save your score to see how you compare.");
                avgTime.setTextAlignment(TextAlignment.CENTER);
                avgTime.setTextFill(Color.WHITE);
                avgTime.setStyle("-fx-font-size: 22");

                HBox buttons = new HBox(31);
                buttons.setAlignment(Pos.CENTER);
                Button saveScore = new Button("Save score");
                saveScore.setBackground(new Background( new BackgroundFill(Color.YELLOW,
                        new CornerRadii(6), Insets.EMPTY)));
                saveScore.setStyle("-fx-font-size: 30");
                saveScore.setOnAction(e1 -> {
                     /*There is a savedScores.txt file in which each line is a games best score, such that
                     the lines in savedScores.txt are alphabetical: AimTrainer, ChimpTest, FastMath, NumberMemory
                     ReactionTime, Typing, VerbalMemory, VisualMemory */

                    //make a Text that will inform the user
                    Text info = new Text();
                    info.setFont(Font.font(31));
                    info.setFill(Color.WHITE);

                    //Get the high score
                    double currentHighScore = 0;
                    File file = new File("src/main/resources/savedScores.txt");
                    try {
                        Scanner scanner = new Scanner(file);
                        int lineCounter = 0;
                        int lineIndexToGet = 0;
                        //traverse the file and grab its contents
                        String[] lines = new String[8];
                        while (lineCounter < 8){

                            lines[lineCounter] = scanner.nextLine();
                            if(lineCounter == lineIndexToGet){
                                currentHighScore = Double.parseDouble(lines[lineCounter]);
                            }

                            lineCounter++;
                        }
                        //now i have the current high score; if it is less than this round's play
                        double score = (times/30.0);
                        //currentHighScore = 0 if there is no high score yet
                        if(score < currentHighScore || currentHighScore == 0){
                            //if this is first score or a new score
                            String message = (currentHighScore == 0) ?
                                    String.format("New high score: %.0f",score) :
                                    String.format("New high score: %.0f -> %.0f",currentHighScore,score);
                            info.setText(message);
                            //put new high score in lines
                            lines[lineIndexToGet] = String.format("%.0f",score);
                            //over write the file with new high score
                            PrintWriter pw = new PrintWriter(
                                    new FileWriter("src/main/resources/savedScores.txt"));
                            for (String line : lines) {
                                //write the score and a new line
                                pw.printf("%s\n", line);
                            }
                            pw.close();
                        }
                        //else, not a new high score
                        else{
                            info.setText("You did not beat your high score of " + currentHighScore);
                        }

                    }
                    catch (IOException error){
                        //if error, let info text report there has been an error
                        info.setText("Error reading from file");
                    }

                    //now here, pop up with the info text
                    Scores popUp = new Scores(info, "Aim Trainer");
                    popUp.display();
                });
                Button tryAgain = new Button("Try again");
                tryAgain.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(6),
                        Insets.EMPTY)));
                tryAgain.setStyle("-fx-font-size: 30");
                tryAgain.setOnAction(e2 -> {
                    root.getChildren().remove(0);//pull out the result VBox
                    //new targets left and remains text
                    targetsLeft = 30;
                    remains.setText("Remaining\n" + targetsLeft);

                    //generate a new x and y coordinate for bullseye button
                    bullseye.setLayoutX((bounds.getWidth()/4) + random.nextDouble() * xBound);
                    bullseye.setLayoutY(random.nextDouble() * yBound);

                    //add in bullseye and remains label
                    root.getChildren().addAll(remains,bullseye);
                    //get a start time
                    localTime = System.currentTimeMillis();
                });
                buttons.getChildren().addAll(saveScore,tryAgain);

                results.getChildren().addAll(bullsI,avgTime,buttons);
                results.setLayoutX(bounds.getWidth() - (bounds.getWidth()/2) - 200);
                results.setLayoutY(bounds.getHeight() - (bounds.getHeight()/2) - 400);

                root.getChildren().add(0,results);
            }
        });
        root.getChildren().add(bullseye);
    }
}
