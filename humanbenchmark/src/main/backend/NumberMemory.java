package main.backend;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Scanner;

/**
 * The logic behind the NumberMemory game
 */
public class NumberMemory {

    @FXML
    public Button start;
    @FXML
    public StackPane root;

    private VBox originalBox;
    private int numberLength;
    private int second;

    /**
     * Does som prep work for numberMemory.fxml. Puts a pic in the scene
     */
    public void initialize(){
        //initial number length
        numberLength = 1;
        //add the box of numbers image into the scene
        Image numberBox = new Image("main/resources/numberBox.png");
        ImageView view = new ImageView(numberBox);
        view.setFitHeight(220);
        view.setFitWidth(220);
        view.setPreserveRatio(true);

        VBox mainBox = (VBox) root.getChildren().get(0);
        mainBox.getChildren().add(0,view);

        //this is also the original box
        originalBox = mainBox;
    }

    /**
     * The on action for the button in home.fxml.
     * @param actionEvent : on click of the button
     */
    public void startPressed(ActionEvent actionEvent) {

        //pull out the box that was in there
        VBox box = (VBox)root.getChildren().remove(0);
        //get the back to home button
        Button backToHome = (Button) box.getChildren().get(box.getChildren().size()-1);

        //make a new VBox to show the number to remember and a countdown
        VBox mainBox = new VBox(32);
        mainBox.setAlignment(Pos.CENTER);

        //get this levels number sequence
        Text sequence = generateNumberSequence();

        /*At humanbenchmark.com, the longer the number sequence the longer it gives you to memorize it. So, second is
        getting incremented by clicking the next button, which calls this method, startPressed().
        Will give 1 second for each digit to memorize, so second = numberLength*/
        second = numberLength;

        //make a label to show a count down
        Label counter = new Label("Time left: " + second);
        counter.setTextFill(Color.WHITE);
        counter.setFont(Font.font(25));

        /*want to display the number for second seconds. Make a timeline to show a count down */
        Timeline countDown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            second--;
            counter.setText("Time left: " + second);

            //if count down has reached 0, switch to new VBox
            if(second == 0){
                //pull out the box that is in root.
                root.getChildren().remove(0);
                //make new box
                VBox boxMain = new VBox(32);
                boxMain.setAlignment(Pos.CENTER);
                //text that asks what the number was
                Text whatWasIt = new Text("What was the number?");
                whatWasIt.setFont(Font.font(21));
                whatWasIt.setFill(Color.WHITE);
                //a text field to respond in
                TextField response = new TextField("");
                response.setAlignment(Pos.CENTER);
                response.setFont(Font.font(26));
                response.setStyle("-fx-text-fill: white");
                response.setBackground(new Background(new BackgroundFill(Color.DARKBLUE, CornerRadii.EMPTY,
                        Insets.EMPTY)));
                response.setPrefWidth(800);

                //a submit response button
                Button submit = new Button("Submit");
                submit.setBackground(new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(5),
                        Insets.EMPTY)));
                submit.setFont(Font.font(22));
                //want to check if the number given is correct, then display results in new vbox
                submit.setOnAction(e1 -> {
                    //Text that says Number and Your Answer
                    Text number = new Text("Number");
                    number.setFill(Color.rgb(255,255,255,0.8));
                    number.setFont(Font.font(30));
                    Text yourAnswer = new Text("Your answer");
                    yourAnswer.setFont(Font.font(30));
                    yourAnswer.setFill(Color.rgb(255,255,255,0.8));

                    //resize the sequence the player was supposed to remember
                    sequence.setFont(Font.font(45));

                    //text that says what level the player is on
                    Text level = new Text("Level " + numberLength);
                    level.setFill(Color.WHITE);
                    level.setFont(Font.font(55));
                    //this button either says try again, or next
                    Button next_TryAgain = new Button();
                    next_TryAgain.setFont(Font.font(22));
                    next_TryAgain.setMinWidth(150);

                    //for displaying the button options
                    HBox buttonBox = new HBox(15);
                    buttonBox.setAlignment(Pos.CENTER);

                    //get what the player gave and make a text with it
                    String givenNumber = response.getText();
                    //for displaying the player's response
                    TextFlow givenAsText= new TextFlow();
                    givenAsText.setTextAlignment(TextAlignment.CENTER);

                    //Now, if the the player gave the right number
                    if(sequence.getText().equals(givenNumber)){
                        //next_tryAgain says next
                        next_TryAgain.setText("Next");
                        //yellow background in this case
                        next_TryAgain.setBackground(new Background(new BackgroundFill(Color.YELLOW,
                                new CornerRadii(12), Insets.EMPTY)));
                        /*then next_TryAgain button on pressed does a new startPressed with the numberLength being
                        one more        */
                        next_TryAgain.setOnAction(this::startPressed);
                        //increment number length
                        numberLength++;
                        //make a text from givenNumber
                        Text numberGiven = new Text(givenNumber);
                        numberGiven.setFill(Color.WHITE);
                        numberGiven.setFont(Font.font(45));
                        //add it to the text flow
                        givenAsText.getChildren().add(numberGiven);
                        //just want next button in the button box in this case
                        buttonBox.getChildren().add(next_TryAgain);
                    }
                    //else they gave the wrong number
                    else{
                        /*if the player gave a number of a different length than sequence, then strike through their
                        whole answer         */
                        if(givenNumber.length() != sequence.getText().length()){
                            for(int i = 0; i < givenNumber.length(); i++){
                                //make a text from each digit
                                Text letter = new Text(Character.toString(givenNumber.charAt(i)));
                                letter.setFont(Font.font(45));
                                //strike it through
                                letter.setStrikethrough(true);
                                //add this digit to the text flow
                                givenAsText.getChildren().add(letter);
                            }
                        }
                        //otherwise, go through the number the player gave and make striked out the numbers they missed
                        else{
                            for(int i = 0; i < givenNumber.length(); i++){
                                //make a text from each digit
                                Text letter = new Text(Character.toString(givenNumber.charAt(i)));
                                letter.setFont(Font.font(45));
                                //if the digit is incorrect
                                if(sequence.getText().charAt(i) != givenNumber.charAt(i)){
                                    //strike it through
                                    letter.setStrikethrough(true);
                                }
                                //else it is right
                                else{
                                    //make it white
                                    letter.setFill(Color.WHITE);
                                }
                                //add this digit to the text flow
                                givenAsText.getChildren().add(letter);
                            }
                        }

                        //next_tryAgain says Try again in this case
                        next_TryAgain.setText("Try Again");
                        //blue background in this case
                        next_TryAgain.setBackground(new Background(new BackgroundFill(Color.BLUE,
                                new CornerRadii(12), Insets.EMPTY)));
                        next_TryAgain.setTextFill(Color.WHITE);
                        /*if wrong, then next_TryAgain on press restarts completely by putting the original
                         box in the display and resetting numberLength to 1                */
                        next_TryAgain.setOnAction(e4 -> {
                            //reset numberLength
                            numberLength = 1;
                            /*add the back to home button to a copy of original box. Doing this copy method
                            got rid of a bug that kept causing me to lose the back to home button     */
                            VBox originalCopy = new VBox(originalBox);
                            originalCopy.getChildren().add(backToHome);
                            originalCopy.setAlignment(Pos.CENTER);
                            originalCopy.setSpacing(32);
                            //replace the contents of the display
                            root.getChildren().remove(0);
                            root.getChildren().add(originalCopy);
                        });

                        //make a save score button
                        Button saveScore = new Button("Save score");
                        saveScore.setBackground(new Background(new BackgroundFill(Color.YELLOW,
                                new CornerRadii(12), Insets.EMPTY)));
                        saveScore.setFont(Font.font(22));
                        saveScore.setOnAction(e3 -> {
                            /*There is a savedScores.txt file in which each line is a games best score, such that
                            the lines in savedScores.txt are alphabetical: AimTrainer, ChimpTest, FastMath, NumberMemory
                            ReactionTime, Typing, VerbalMemory, VisualMemory */

                            //make a Text that will inform the user
                            Text info = new Text();
                            info.setFont(Font.font(31));
                            info.setFill(Color.WHITE);

                            //Get the high score
                            int currentHighScore = 0;
                            File file = new File("src/main/resources/savedScores.txt");
                            try {
                                Scanner scanner = new Scanner(file);
                                int lineCounter = 0;
                                int lineIndexToGet = 3;
                                //traverse the file and grab its contents
                                String[] lines = new String[8];
                                while (lineCounter < 8){

                                    lines[lineCounter] = scanner.nextLine();
                                    if(lineCounter == lineIndexToGet){
                                        currentHighScore = Integer.parseInt(lines[lineCounter]);
                                    }

                                    lineCounter++;
                                }
                                //now i have the current high score; if it is less than this round's play
                                if(numberLength > currentHighScore){
                                    info.setText("New high score: " + currentHighScore + "->" + numberLength);
                                    //put new high score in lines
                                    lines[lineIndexToGet] = Integer.toString(numberLength);
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
                            Scores popUp = new Scores(info, "Number Memory");
                            popUp.display();
                        });
                        /*if we are in here, want to add in save score text and button and have save score button
                        be next to try again button */
                        buttonBox.getChildren().addAll(saveScore,next_TryAgain);
                    }

                    /*now add in the text and buttons as defined by the if-else and above
                    to this VBox and make this VBox be displayed  */
                    VBox newMainBox = new VBox(32);
                    newMainBox.setAlignment(Pos.CENTER);
                    newMainBox.getChildren().addAll(number,sequence,yourAnswer,givenAsText,level);
                    //now if button box has save score button, want to add in the save score label
                    if(buttonBox.getChildren().size() > 1){
                        //make text that says save score
                        Text save = new Text("Save your score and see how you compare");
                        save.setFill(Color.WHITE);
                        save.setFont(Font.font(19));
                        //add it to newMainBox
                        newMainBox.getChildren().add(save);
                    }
                    //now add button box however it is and back to home button
                    newMainBox.getChildren().addAll(buttonBox,backToHome);
                    //now pull out the box that is in the root
                    root.getChildren().remove(0);
                    //and add in newMainBox
                    root.getChildren().add(newMainBox);
                });
                /*now add in the what was the number? question, the text field, and the submit button to boxMain
                and the back to home button          */
                boxMain.getChildren().addAll(whatWasIt,response,submit,backToHome);
                //now add in the boxMain to root
                root.getChildren().add(boxMain);
                //make the cursor start in the text field by default
                response.requestFocus();
            }
        }));
        //add timer and sequence and back to home button to the mainBox
        mainBox.getChildren().addAll(sequence,counter,backToHome);
        //add main box to scene
        root.getChildren().add(mainBox);
        //set cycle count
        countDown.setCycleCount(second);
        //start this timer
        countDown.play();
    }

    /**
     * Generates a numberLength digit number for the player to memorize
     * @return : A Text object which is the sequence
     */
    public Text generateNumberSequence(){
        //to generate random digits
        SecureRandom random = new SecureRandom();
        //to compile the sequence
        StringBuilder sequence = new StringBuilder();
        int counter = 0;
        while(counter < numberLength){
            sequence.append(random.nextInt(10));
            counter++;
        }
        //now put this in a text and return
        Text number = new Text(sequence.toString());
        number.setFill(Color.WHITE);
        number.setFont(Font.font(60));
        return number;
    }
}
