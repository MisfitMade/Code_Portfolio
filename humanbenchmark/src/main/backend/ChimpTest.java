package main.backend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

/**
 * The logic behind the ChimpTest game
 */
public class ChimpTest {

    @FXML
    public StackPane root;

    private int strikes;
    private int maxNumbers;
    private boolean practiceRound;

    //gonna put the squares into a grid pane
    private GridPane forNumberSquares;

    /**
     * Runs some preparations on the page. Puts in an image
     */
    public void initialize(){
        //root formatting
        root.setAlignment(Pos.CENTER_RIGHT);
        root.setPadding(new Insets(80));

        //get the VBox from the fxml define root
        VBox child = (VBox) root.getChildren().get(0);

        //the squares picture
        Image squares = new Image("main/resources/square.png");
        ImageView view = new ImageView(squares);
        view.setFitHeight(220);
        view.setFitWidth(220);
        view.setPreserveRatio(true);

        //put the picture in as the first element in child
        child.getChildren().add(0,view);


        //initial fields
        strikes = 0;
        maxNumbers = 4;
        practiceRound = true;
    }

    /**
     * The on action for the button defined in the chimpTest.fxml file.
     * @param actionEvent : on click
     */
    public void startChimpTest(ActionEvent actionEvent) {
        //pull out the current vbox and only preserve the back to home button inside it
        VBox currentMainBox = (VBox) root.getChildren().remove(0);
        //get the back to home button
        Button backToHome = (Button) currentMainBox.getChildren().remove(currentMainBox.getChildren().size()-1);

        //for aligning on the screen
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        //define a new grid
        forNumberSquares = new GridPane();
        forNumberSquares.setMaxSize(bounds.getWidth()/3,bounds.getHeight()/2);
        forNumberSquares.setBackground(new Background( new BackgroundFill(Color.BLACK, CornerRadii.EMPTY,Insets.EMPTY)));
        //define some column and row size constraints
        for(int i = 0; i < 10; i++){
            //give the grid the col and row constraints, to keep from the rows and cols resizing when squares are removed
            forNumberSquares.getColumnConstraints().add(new ColumnConstraints(46));
            forNumberSquares.getRowConstraints().add(new RowConstraints(46));
        }
        //grid gaps
        forNumberSquares.setVgap(5);
        forNumberSquares.setHgap(5);
        //set up the board with its buttons
        makeBoard();

        //add tbe grid pane with the back to home button into a new current vbox
        currentMainBox = new VBox(12);
        currentMainBox.setAlignment(Pos.CENTER);
        currentMainBox.getChildren().addAll(forNumberSquares, backToHome);

        //add to scene
        root.getChildren().add(currentMainBox);
    }

    /**
     * Makes the board and defines the Queue that is used to check if the player is clicking the buttons in the
     * right order.
     */
    public void makeBoard(){

        //used to make sure no square is put in the same GridPane slot
        int[][] rowsAndCols = new int[10][10];

        //going to work with as if there is 10 rows and 10 cols in the grid pane
        SecureRandom random = new SecureRandom();

        int k = 0;
        Queue<Button> orderKeeper = new LinkedList<>();

        while (k < maxNumbers){
            //random row and col
            int randomRow = random.nextInt(10);
            int randomCol = random.nextInt(10);
            //check if this row and col is available
            if(rowsAndCols[randomRow][randomCol] == 0){
                //if so, make it so that it is not available now
                rowsAndCols[randomRow][randomCol] = 1;

                //new button square: formatting and functionality
                Button button = new Button(String.valueOf(k+1));
                button.setStyle("-fx-font-size: 18");
                button.setBackground(new Background(new BackgroundFill(Color.BURLYWOOD, new CornerRadii(5),
                        Insets.EMPTY)));
                button.setPrefSize(46,46);
                //add this button to the queue
                orderKeeper.add(button);
                //functionality of button
                button.setOnAction(e -> {
                    //the number on this square
                    int squareNumber = Integer.parseInt(button.getText());

                    //if this button was clicked in the correct order
                    if(orderKeeper.peek().equals(button)){
                        //remove it from the grid pane and the queue
                        forNumberSquares.getChildren().remove(button);
                        orderKeeper.poll();

                        //if this is the square with a 1 on it and it is not the practice round
                        if(squareNumber == 1 && !practiceRound){
                            //go through the rest of the buttons and make their text invisible
                            for (Node node : forNumberSquares.getChildren()) {
                                Button squareButton = (Button) node;
                                squareButton.setTextFill(Color.BURLYWOOD);
                            }
                        }
                        //if this is the last square, meaning this round ends
                        else if(squareNumber == maxNumbers){
                            maxNumbers++;//inc to a higher number
                            switchToTransitionBox();//switch to transition box
                            /*this really only needs to be set to false the fist time a round ends, but...I think it is
                            equally time expensive, if not less, to just set it to false everytime, rather than check
                            if it needs to be set to false everytime and set it to false that one time*/
                            practiceRound = false;
                        }
                    }
                    //else if was clicked out of order
                    else{
                        strikes++;//you get a strike
                        switchToTransitionBox();
                    }
                });
                //give button its grid constraints and add to grid
                GridPane.setConstraints(button,randomCol,randomRow);
                forNumberSquares.getChildren().add(button);

                //inc to next button square to make
                k++;
            }
        }
    }

    /**
     * Puts the VBox in that is shown between rounds
     */
    public void switchToTransitionBox(){
        //main container
        VBox vBox = new VBox(20);
        vBox.setAlignment(Pos.CENTER);

        //the text that shows how many squares the player will face next
        Text number = new Text(String.valueOf(maxNumbers));
        number.setFont(Font.font(32));
        number.setFill(Color.WHITE);

        //the button to continue
        Button continueButton = new Button();
        continueButton.setOnAction(this::startChimpTest);
        continueButton.setStyle("-fx-font-size: 27");
        //if less than 3 strikes
        if(strikes < 3){

            //the text that says numbers
            Text numbers = new Text("NUMBERS");
            numbers.setFill(Color.WHITE);
            numbers.setFont(Font.font(25));
            //Text that says strikes
            Text strikes = new Text("STRIKES");
            strikes.setFill(Color.WHITE);
            strikes.setFont(Font.font(25));
            //Text that says numberOfStrikes / 3
            Text strikesOutOfThree = new Text(this.strikes + " of 3");
            strikesOutOfThree.setFont(Font.font(28));
            strikesOutOfThree.setFill(Color.WHITE);
            //format for continueButton in this case
            continueButton.setText("Continue");
            continueButton.setBackground(new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(5.5),
                    Insets.EMPTY)));

            vBox.getChildren().addAll(numbers,number,strikes,strikesOutOfThree,continueButton);
        }
        //else, game is over
        else{
            //save this for the save button on action
            int numbersPlayerMadeItTo = maxNumbers;
            //reset maxNumbers and strikes and practiceRound
            maxNumbers = 4;
            strikes = 0;
            practiceRound = true;
            //the squares picture
            Image squares = new Image("main/resources/square.png");
            ImageView view = new ImageView(squares);
            view.setFitHeight(220);
            view.setFitWidth(220);
            view.setPreserveRatio(true);
            //text that says Score
            Text score = new Text("Score");
            score.setFill(Color.WHITE);
            score.setFont(Font.font(25));
            //text that says Save your score
            Text saveScoreText = new Text("Save your score to see how you compare.");
            saveScoreText.setFont(Font.font(20));
            saveScoreText.setFill(Color.WHITE);
            //container for buttons
            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER);
            //save score button
            Button saveScore = new Button("Save score");
            saveScore.setBackground(new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(5.5),
                    Insets.EMPTY)));
            saveScore.setStyle("-fx-font-size: 27");
            saveScore.setOnAction(e1 -> {
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
                    int lineIndexToGet = 1;
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
                    if(numbersPlayerMadeItTo > currentHighScore){
                        info.setText("New high score: " + currentHighScore + "->" + numbersPlayerMadeItTo);
                        //put new high score in lines
                        lines[lineIndexToGet] = Integer.toString(numbersPlayerMadeItTo);
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
                Scores popUp = new Scores(info, "Chimp Test");
                popUp.display();
            });
            //format for continueButton in this case
            continueButton.setText("Try again");
            continueButton.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(5.5),
                    Insets.EMPTY)));
            //add buttons to button box
            buttonBox.getChildren().addAll(saveScore,continueButton);
            //add everything above to main container vBox
            vBox.getChildren().addAll(view,score,number,saveScoreText,buttonBox);
        }

        //change what is being displayed...get the main VBox
        VBox mainBox = (VBox) root.getChildren().get(0);
        //pull out the grid pane
        mainBox.getChildren().remove(0);
        //put in transition vbox in main box, leaving the back to home button in there
        mainBox.getChildren().add(0,vBox);
    }
}
