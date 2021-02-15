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
 * The logic behind the FastMath game
 */
public class FastMath {
    @FXML
    public VBox originalBox;
    @FXML
    public StackPane root;
    @FXML
    public Button start;

    private int level;
    private int second;
    private int solution;

    /**
     * Runs some preparations on the page. Puts in an image
     */
    public void initialize(){

        /*start at level 1 = 2 numbers, 1 operator. Only doing adds */
        level = 1;
        //add in the plus and minus sign
        Image plusMinus= new Image("main/resources/fastMath.png");
        ImageView view = new ImageView(plusMinus);
        view.setFitHeight(220);
        view.setFitWidth(220);
        view.setPreserveRatio(true);
        originalBox.getChildren().add(0,view);
    }

    /**
     * The on action for the button defined in the fastMath.fxml
     * @param actionEvent : on click
     */
    public void startPressed(ActionEvent actionEvent) {

        //pull out the box that was in there
        VBox box = (VBox)root.getChildren().remove(0);
        //get the back to home button
        Button backToHome = (Button) box.getChildren().get(box.getChildren().size()-1);

        //make a new VBox
        VBox mainBox = new VBox(16);
        mainBox.setAlignment(Pos.CENTER);

        //make a new equation Text, only using +s
        Text currentEquation = makeNewEquation();

        /*give as many seconds as the level they are on, until they get to level 10, then it gets harder cuz the time
        does not increase, even though the equation length does      */
        second = Math.min(level, 10);
        /*want to display the number for second seconds. Make a timeline to show a count down
        make a label to show a count down*/
        Label counter = new Label("Time left: " + second);
        counter.setTextFill(Color.WHITE);
        counter.setFont(Font.font(25));

        Timeline countDown = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            second--;
            counter.setText("Time left: " + second);

            //if count down has reached 0, switch to new VBox
            if (second == 0) {
                Background submitAndNextBack = new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(6),
                        Insets.EMPTY));
                //remove current VBox
                VBox boxMain = (VBox) root.getChildren().remove(0);
                //get back to home button
                Button backToHomeButton = (Button) boxMain.getChildren().get(boxMain.getChildren().size()-1);
                //make new box for screen
                boxMain = new VBox(16);
                boxMain.setAlignment(Pos.CENTER);

                //make text that asks the solution
                Text whatIsIt = new Text("What does it add up to?");
                whatIsIt.setFont(Font.font(28));
                whatIsIt.setFill(Color.WHITE);
                //text field for answer
                TextField response = new TextField("");
                response.setMaxWidth(300.0);
                response.setFont(Font.font(31));
                response.setAlignment(Pos.CENTER);
                //button to submit
                Button submit = new Button("Submit");
                submit.setBackground(submitAndNextBack);
                submit.setStyle("-fx-font-size: 30");
                submit.setOnAction(e1 -> {

                    //pull out what is in the scene
                    VBox mainBox2 = (VBox) root.getChildren().remove(0);
                    //get the back to home button
                    Button backHomeButton = (Button) mainBox2.getChildren().get(mainBox2.getChildren().size()-1);
                    //make a new mainBox2
                    mainBox2 = new VBox(16);
                    mainBox2.setAlignment(Pos.CENTER);
                    //text that says the solution
                    Text soln = new Text("Solution: " + solution);
                    soln.setFill(Color.WHITE);
                    soln.setFont(Font.font(30));
                    /*text that says what the player gave. Using two texts so i can turn on the number red
                     when it is wrong    */
                    Text youGave = new Text("You gave: ");
                    youGave.setFill(Color.WHITE);
                    youGave.setFont(Font.font(30));
                    //given gets its color set below in the if-else statement
                    Text given = new Text(response.getText());
                    given.setFont(Font.font(30));
                    //this holds the You gave: and the number the user gives
                    TextFlow givenFlow = new TextFlow();
                    givenFlow.getChildren().addAll(youGave,given);
                    givenFlow.setTextAlignment(TextAlignment.CENTER);
                    //Button that either says next or try again
                    Button next_tryAgain = new Button();
                    next_tryAgain.setStyle("-fx-font-size: 30");

                    //this will either have a save score and try again button, or just a next button
                    HBox buttonBox = new HBox(13);
                    buttonBox.setAlignment(Pos.CENTER);
                    //if correct
                    if(response.getText().equals(Integer.toString(solution))){
                        level++;
                        given.setFill(Color.WHITE);
                        //next_tryAgain's onAction if correct, just restart with a level++
                        next_tryAgain.setOnAction(this::startPressed);
                        //give a yellow background and word "Next"
                        next_tryAgain.setBackground(submitAndNextBack);
                        next_tryAgain.setText("Next");
                        //add to button box
                        buttonBox.getChildren().add(next_tryAgain);
                    }
                    //if incorrect
                    else{
                        given.setFill(Color.RED);
                        //next_try again button has try again functionality and appearance
                        next_tryAgain.setText("Try again");
                        next_tryAgain.setBackground(new Background(new BackgroundFill(Color.DARKBLUE,
                                new CornerRadii(6), Insets.EMPTY)));
                        next_tryAgain.setTextFill(Color.WHITE);
                        next_tryAgain.setOnAction(e2 -> {
                            //want to start over, reset
                            level = 1;
                            //pull out the vbox in the scene
                            root.getChildren().remove(0);
                            //make a new one as a copy of original
                            VBox originalCopy = new VBox(originalBox);
                            originalCopy.setSpacing(16);
                            originalCopy.setAlignment(Pos.CENTER);
                            //add the back to home button in
                            originalCopy.getChildren().add(backToHomeButton);
                            //put it in the root. This puts the original VBox back on the screen
                            root.getChildren().add(originalCopy);
                        });
                        //then there is a save score button
                        Button saveScoreButton = new Button("Save score");
                        saveScoreButton.setBackground(submitAndNextBack);
                        saveScoreButton.setStyle("-fx-font-size: 30");
                        saveScoreButton.setOnAction(e2 -> {
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
                                int lineIndexToGet = 2;
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
                                if(level > currentHighScore){
                                    info.setText("New high score: " + currentHighScore + "->" + level);
                                    //put new high score in lines
                                    lines[lineIndexToGet] = Integer.toString(level);
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
                            Scores popUp = new Scores(info, "Fast Math");
                            popUp.display();
                        });
                        //add save and try again button to button box
                        buttonBox.getChildren().addAll(saveScoreButton,next_tryAgain);
                    }
                    //text that says the current level
                    Text levelText = new Text("Level " + level);
                    levelText.setFont(Font.font(33));
                    levelText.setFill(Color.WHITE);
                    //now add things to boxMain depending on if the user was correct
                    mainBox2.getChildren().addAll(soln,givenFlow,levelText);
                    //if the user got it wrong, then buttonBox has size 2
                    if(buttonBox.getChildren().size() > 1){
                        //then there is text that says save your score & a save score button
                        Text save = new Text("Save your score and see how you compare.");
                        save.setFill(Color.WHITE);
                        save.setFont(Font.font(28));
                        //add it to mainBox2
                        mainBox2.getChildren().add(save);
                    }
                    //now add button box, either having 1 or 2 buttons and back to home button
                    mainBox2.getChildren().addAll(buttonBox,backHomeButton);
                    //add this to the root
                    root.getChildren().add(mainBox2);

                });
                //add the text asking what it equals, text field, submit button and back to home to boxMain
                boxMain.getChildren().addAll(whatIsIt,response,submit,backToHomeButton);
                //add this to root
                root.getChildren().add(boxMain);
                //make the text field start with the cursor in it
                response.requestFocus();
            }
        }));
        //put the equation, count downer, back to home button in the main box
        mainBox.getChildren().addAll(currentEquation,counter,backToHome);
        //put this box in the scene
        root.getChildren().add(mainBox);
        //start the timer/countdown
        countDown.setCycleCount(second);
        countDown.play();
    }

    /**
     * Makes and returns the equation that is seen on the screen, the one to be solved.
     * @return a Text that is an equation of additions
     */
    private Text makeNewEquation() {

        /*There will level number of operators, meaning level+1 numbers*/
        StringBuilder forScreenEquation = new StringBuilder();

        //to generate random numbers and operators
        SecureRandom random = new SecureRandom();

        solution = 0;
        int equationLength = level + (level + 1);
        int counter = 0;
        //each loop will get a number, an addition sign
        while(counter < equationLength){

            //get a number, not making it super difficult, but kind of hard
            int operand = random.nextInt(16) + 1;
            //add to solution
            solution += operand;
            //save in toSolve and forScreen
            forScreenEquation.append(" ").append(operand).append(" ");

            //add an add sign, if counter is < equationLength
            counter++;
            if(counter < equationLength){
                forScreenEquation.append("+");
            }
            counter++;
        }

        //now that i have the solution and string, return the equation as a Text for the screen
        Text equation = new Text(forScreenEquation.toString());
        equation.setFill(Color.WHITE);
        equation.setFont(Font.font(27));
        return equation;
    }
}
