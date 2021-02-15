package main.backend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.Scanner;

/**
 * The logic behind the Verbal Memory Page
 */
public class VerbalMemory {

    @FXML
    public StackPane root;
    @FXML
    public Button start;

    private VBox originalBox;
    private int lives;
    private int score;
    private int randomIndex;

    /**
     * Does some prep work when the Loader.loadFXML() runs to load the .fxml file. Puts a pic in the scene
     */
    public void initialize(){
        //given 3 lives at start and score is 0
        lives = 3;
        score = 0;
        //add the book image into the scene
        Image book = new Image("main/resources/book.jpg");
        ImageView view = new ImageView(book);
        view.setFitHeight(220);
        view.setFitWidth(220);
        view.setPreserveRatio(true);

        VBox mainBox = (VBox) root.getChildren().get(0);
        mainBox.getChildren().add(0,view);

        //this is also the original box
        originalBox = mainBox;
    }

    /**
     * The onAction for the button as defined by verbalMemory.fxml
     * @param actionEvent : on click of the button
     */
    public void startPressed(ActionEvent actionEvent) {
        //get the back to home button and remove the VBox that is in there
        VBox mainBox = (VBox) root.getChildren().remove(0);
        Button backToHome = (Button) mainBox.getChildren().get(mainBox.getChildren().size()-1);

        //All the nodes below will be in this new mainBox
        mainBox = new VBox(17);
        mainBox.setAlignment(Pos.CENTER);

        //make the things that say Lives and Score
        Text livesText = new Text("Lives | " + lives);
        livesText.setFill(Color.WHITE);
        livesText.setFont(Font.font(30));
        Text scoreText = new Text("Score | " + score);
        scoreText.setFill(Color.WHITE);
        scoreText.setFont(Font.font(30));
        //put these in an HBox
        HBox textBox = new HBox(10);
        textBox.setAlignment(Pos.CENTER);
        textBox.getChildren().addAll(livesText,scoreText);

        //get the list of 100 words from the file wordBank.txt
        String[] words = new String[100];
        try{
            File wordFile = new File("src/main/resources/wordBank.txt");
            Scanner scanner = new Scanner(wordFile);
            //get the 100 lines of word per line from the file
            int lineCounter = 0;
            while (scanner.hasNextLine()){
                words[lineCounter] = scanner.nextLine();
                lineCounter++;
            }
        }
        catch (IOException e){
            //force the program back to home
            backToHome.fire();
        }

        //generate a boolean[] that reflects, by index, if a word has been seen
        boolean[] seen = new boolean[100];
        //choose one at random
        SecureRandom random = new SecureRandom();
        randomIndex = random.nextInt(100);
        //get this random word for the text
        Text word = new Text(words[randomIndex]);
        word.setFill(Color.WHITE);
        word.setFont(Font.font(50));

        //make the buttons SEEN and NEW, first their shared background
        Background buttonBack = new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(6),
                Insets.EMPTY));
        //the SEEN button
        Button seenButton = new Button("SEEN");
        seenButton.setBackground(buttonBack);
        seenButton.setFont(Font.font(31));
        //define functionality of SEEN Button
        seenButton.setOnAction(e -> {
            //if player is correct
            if(seen[randomIndex]){
                //inc score, update label
                score++;
                scoreText.setText("Score | " + score);
                //is already "seen", so don't need to do seen[randomIndex] = true
            }
            //else, the player is wrong
            else {
                //but it has been seen now
                seen[randomIndex] = true;
                //less lives, update label
                lives--;
                livesText.setText("Lives | " + lives);
            }
            //now see if the player died
            if(lives == 0){
                endGame();
            }
            //else, go to next word
            else{
                //to keep from displaying the same word twice in a row
                int currentRandom = randomIndex;

                randomIndex = random.nextInt(100);
                System.out.println(randomIndex);
                //if trying to display same word twice in a row
                if(randomIndex == currentRandom){
                    //decrement by one as long as that does not produce -1
                    randomIndex = (randomIndex > 0) ? randomIndex-1 : randomIndex+1;
                }
                word.setText(words[randomIndex]);
            }
        });
        //the NEW button
        Button newButton = new Button("NEW");
        newButton.setBackground(buttonBack);
        newButton.setFont(Font.font(31));
        //new button functionality
        newButton.setOnAction(e -> {
            //if the player is correct
            if(!seen[randomIndex]){
                //inc score, update label
                score++;
                scoreText.setText("Score | " + score);
                //word has been seen now
                seen[randomIndex] = true;
            }
            //else the player is wrong
            else{
                //less lives, update label
                lives--;
                livesText.setText("Lives | " + lives);
                //don't need to do seen[randomIndex] = true, cuz it is already "seen"
            }
            //if the player has died
            if(lives == 0){
                endGame();
            }
            //else, go to next word
            else {
                //to keep from displaying the same word twice in a row
                int currentRandom = randomIndex;

                randomIndex = random.nextInt(100);
                System.out.println(randomIndex);
                //if trying to display same word twice in a row
                if(randomIndex == currentRandom){
                    //decrement by one as long as that does not produce -1
                    randomIndex = (randomIndex > 0) ? randomIndex-1 : randomIndex+1;
                }
                word.setText(words[randomIndex]);
            }
        });
        //put the buttons in a button box
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(seenButton,newButton);

        //put all that in the mainBox and into the root
        mainBox.getChildren().addAll(textBox,word,buttonBox,backToHome);
        root.getChildren().add(mainBox);
    }

    /**
     * Is called whenever the player loses the game. Puts new VBox in scene with results
     */
    private void endGame() {
        //get the back to home button and remove the VBox that is in there
        VBox mainBox = (VBox) root.getChildren().remove(0);
        Button backToHome = (Button) mainBox.getChildren().get(mainBox.getChildren().size()-1);
        //make this be the VBox in the root now
        mainBox = new VBox(12);
        mainBox.setAlignment(Pos.CENTER);

        //make the book image for the scene
        Image book = new Image("main/resources/book.jpg");
        ImageView view = new ImageView(book);
        view.setFitHeight(220);
        view.setFitWidth(220);
        view.setPreserveRatio(true);
        //make the text that says Verbal Memory
        Text verbalMemory = new Text("Verbal Memory");
        verbalMemory.setFont(Font.font(25));
        verbalMemory.setFill(Color.WHITE);
        //make the text that says X words
        Text words = new Text(score + " words");
        words.setFill(Color.WHITE);
        words.setFont(Font.font(55));
        //make the text that says save your score
        Text save = new Text("Save your score and see how you compare.");
        save.setFont(Font.font(22));
        save.setFill(Color.WHITE);
        //make the save score button and try again button
        Button saveScore = new Button("Save score");
        saveScore.setBackground(new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(6),
                Insets.EMPTY)));
        saveScore.setFont(Font.font(31));
        //save score functionality
        saveScore.setOnAction(e -> {
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
                int lineIndexToGet = 6;
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
                if(score > currentHighScore){
                    info.setText("New high score: " + currentHighScore + "->" + score);
                    //put new high score in lines
                    lines[lineIndexToGet] = Integer.toString(score);
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
            Scores popUp = new Scores(info, "Verbal Memory");
            popUp.display();
        });
        //the try again button
        Button tryAgain = new Button("Try again");
        tryAgain.setBackground(new Background(new BackgroundFill(Color.DARKBLUE, new CornerRadii(6),
                Insets.EMPTY)));
        tryAgain.setStyle("-fx-text-fill: white");
        tryAgain.setFont(Font.font(31));
        tryAgain.setOnAction(e -> {
            //pull out the box that is in the scene
            root.getChildren().remove(0);
            //pull out the ImageView from originalBox, since initialize puts in the same ImageView
            originalBox.getChildren().remove(0);
            //add back to home button to original box
            originalBox.getChildren().add(backToHome);
            //add it to root and recall initialize
            root.getChildren().add(originalBox);
            initialize();
        });
        //put the buttons n a button box
        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(saveScore,tryAgain);

        //add this new stuff to the mainBox
        mainBox.getChildren().addAll(view,verbalMemory,words,save,buttonBox,backToHome);
        //add this box to home
        root.getChildren().add(mainBox);
    }
}
