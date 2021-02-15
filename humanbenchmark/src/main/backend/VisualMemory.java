package main.backend;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The logic for the Visual Memory Page
 */
public class VisualMemory {

    @FXML
    public StackPane root;

    private int level;
    private int lives;
    private int gridDimensions;

    private boolean isNotALevelRetry;

    //current correct buttons to click
    private ArrayList<Button> currentCorrectButtons;
    //the buttons they have clicked
    private ArrayList<Button> buttonsClicked;

    /**
     * Some prep work ran when this page is loaded by the Loader.loadFXML(). Puts a pic in the scene
     */
    public void initialize(){
        //get the VBox from the fxml defined root
        VBox child = (VBox) root.getChildren().get(0);

        //the squares picture
        Image squares = new Image("main/resources/square.png");
        ImageView view = new ImageView(squares);
        view.setFitHeight(220);
        view.setFitWidth(220);
        view.setPreserveRatio(true);

        //put the picture in as the first element in child
        child.getChildren().add(0,view);
    }

    /**
     * The on action for the button as defined in home.fxml
     * @param actionEvent : onClick for the button
     */
    public void startVisualMem(ActionEvent actionEvent) {
        //initial fields
        lives = 3;
        level = 1;
        gridDimensions = 3;
        isNotALevelRetry = true;

        makeNewBoard();
    }

    /**
     * Makes a new board based off the level and gridDimensions
     */
    public void makeNewBoard(){
        //used to keep the player from playing before things are ready
        AtomicBoolean buttonsSet = new AtomicBoolean(false);

        //if the player has past a certain level, increment the grid dimension
        if(level % 3 == 0 && isNotALevelRetry){
            gridDimensions++;
        }
        //to gather the buttons they click
        buttonsClicked = new ArrayList<>();
        //declare a grid
        //gonna put the squares into a grid pane
        GridPane forSquares = new GridPane();
        forSquares.setHgap(10);
        forSquares.setVgap(10);

        double buttonSize = 300.0/gridDimensions;
        double gridSize = (buttonSize*gridDimensions)+(10*gridDimensions);
        forSquares.setMaxSize(gridSize,gridSize);
        int[] strikes = {0};
        //make buttons for the grid
        for(int i = 0; i < gridDimensions; i++) {
            for (int j = 0; j < gridDimensions; j++) {

                Button button = new Button();
                button.setPrefSize(buttonSize, buttonSize);
                //every button will have ROYALBLUE backgrounds to start
                button.setBackground(new Background(new BackgroundFill(Color.ROYALBLUE, new CornerRadii(6),
                        Insets.EMPTY)));
                button.setOnAction(e -> {
                    //use a pause so the player can see they have flipped a or last wrong square
                    PauseTransition pt = new PauseTransition();
                    pt.setDuration(Duration.seconds(0.5));

                    /*if the pause transition that turns the squares back to ROYALBLUE from white when showing player
                    the squares they should click*/
                    if (buttonsSet.get()) {
                        //if they clicked a correct button
                        if (currentCorrectButtons.contains(button)) {
                            //change background to white
                            button.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(6),
                                    Insets.EMPTY)));
                            //remove this button from the list of correct buttons, add it to clicked buttons
                            buttonsClicked.add(currentCorrectButtons.get(currentCorrectButtons.indexOf(button)));
                            currentCorrectButtons.remove(button);
                            //if they got the last button
                            if (currentCorrectButtons.size() == 0) {
                                isNotALevelRetry = true;
                                pt.setOnFinished(e1 -> {
                                    level++;
                                    makeNewBoard();//start a new board on a new level
                                });
                                pt.play();
                            }
                        }
                        /*else they have clicked a wrong button or a button they have already clicked. So, if this is a
                        wrong button and not one already clicked.    */
                        else if (!buttonsClicked.contains(button)) {
                            buttonsClicked.add(button);
                            //change background to dark blue
                            button.setBackground(new Background(new BackgroundFill(Color.DARKBLUE, new CornerRadii(6),
                                    Insets.EMPTY)));

                            //increment strikes
                            strikes[0] = strikes[0] + 1;
                            //if that was their 3rd strike
                            if (strikes[0] == 3) {
                                lives--;
                                pt.setOnFinished(e1 -> {
                                    //if they just used their last life
                                    if (lives == 0) {
                                        showResults();
                                    }
                                    //else, try this level again
                                    else {
                                        isNotALevelRetry = false;
                                        makeNewBoard();
                                    }
                                });
                                pt.play();
                            }
                        }
                    }
                });

                //give this button grid constraints
                GridPane.setConstraints(button,j,i);
                //add it to the grid
                forSquares.getChildren().add(button);
            }
        }

        //declare a new list for storing correct buttons
        currentCorrectButtons = new ArrayList<>();
        //use this to make sure i grab the right number of different buttons
        int[][] colsAndRows = new int[gridDimensions][gridDimensions];

        SecureRandom random = new SecureRandom();
        //looks like there is always level+2 correct squares to click in the game online, so
        int k = 0;
        int squares = level+2;
        while (k < squares){
            int randomRow = random.nextInt(gridDimensions);
            int randomCol = random.nextInt(gridDimensions);
            //if this button has yet to be chosen randomly
            if(colsAndRows[randomRow][randomCol] == 0){
                //mark this spot as taken
                colsAndRows[randomRow][randomCol] = 1;
                //get this particular button
                for(Node node : forSquares.getChildren()){
                    if(GridPane.getRowIndex(node) == randomRow && GridPane.getColumnIndex(node) == randomCol){
                        //change its background to white
                        Button chosenButton = (Button) node;
                        chosenButton.setBackground(new Background(new BackgroundFill(Color.WHITE,
                                new CornerRadii(6), Insets.EMPTY)));
                        //add it to the currentCorrectButtons list
                        currentCorrectButtons.add(chosenButton);
                    }
                }

                k++;
            }
        }
        /*now I have chosen (level+2) squares for the player to memorize and turned them white. Turn them back to
        ROYALBLUE after some time    */
        PauseTransition pt = new PauseTransition();
        pt.setDuration(Duration.seconds(1.5));
        pt.setOnFinished(e -> {
            for(Button button : currentCorrectButtons){
                button.setBackground(new Background(new BackgroundFill(Color.ROYALBLUE, new CornerRadii(6),
                        Insets.EMPTY)));
            }
            buttonsSet.set(true);
        });
        //this will play after this grid pane has been set up in the display
        pt.play();

        //Now pull out everything but for the back to home button from the current display
        VBox vBox = (VBox) root.getChildren().remove(0);
        Button backToHome = (Button) vBox.getChildren().get(vBox.getChildren().size()-1);

        /*make a new vbox with the grid and back to home button and level and lives labels
        Level text*/
        Text levelText = new Text("Level: " + level);
        levelText.setFill(Color.WHITE);
        levelText.setFont(Font.font(23));
        //lives text
        Text livesText = new Text("Lives: " + lives);
        livesText.setFont(Font.font(23));
        livesText.setFill(Color.WHITE);
        //put the texts in a HBox
        HBox textBox = new HBox(14);
        textBox.setAlignment(Pos.CENTER);
        textBox.getChildren().addAll(levelText,livesText);
        //put it all in a new VBox and then the VBox in the root
        vBox = new VBox(20);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(textBox, forSquares,backToHome);

        root.getChildren().add(vBox);
    }

    /**
     * Is called after the player loses all their lives to show the results.
     */
    public void showResults(){
        //get the VBox from the root
        VBox child = (VBox) root.getChildren().remove(0);
        //get the back to home button
        Button backToHome = (Button) child.getChildren().get(child.getChildren().size()-1);
        //the squares picture
        Image squares = new Image("main/resources/square.png");
        ImageView view = new ImageView(squares);
        view.setFitHeight(220);
        view.setFitWidth(220);
        view.setPreserveRatio(true);
        //the Visual Memory text
        Text visMem = new Text("Visual Memory");
        visMem.setFill(Color.WHITE);
        visMem.setFont(Font.font(21));
        //the level the player made it to text
        Text levelText = new Text("Level: " + level);
        levelText.setFill(Color.WHITE);
        levelText.setFont(Font.font(35));
        //the text that says Save your score
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
                int lineIndexToGet = 7;
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
            Scores popUp = new Scores(info, "Visual Memory");
            popUp.display();
        });
        //try again button
        Button tryAgain = new Button("Try again");
        tryAgain.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(5.5),
                Insets.EMPTY)));
        tryAgain.setStyle("-fx-font-size: 27");
        tryAgain.setOnAction(this::startVisualMem);
        //add buttons to button box
        buttonBox.getChildren().addAll(saveScore,tryAgain);
        //add this all into a VBox
        child = new VBox(20);
        child.setAlignment(Pos.CENTER);
        child.getChildren().addAll(view,visMem,levelText,saveScoreText,buttonBox,backToHome);
        //add to scene
        root.getChildren().add(child);
    }
}
