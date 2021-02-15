package main.backend;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
 * The logic behind the Typing test
 */
public class Typing {

    @FXML
    public StackPane root;
    @FXML
    public Label WPM;
    @FXML
    public TextArea textArea;
    @FXML
    public Label elapsedSeconds;
    @FXML
    public Label howMany;


    private String[] chosenParagraph;
    private int second;
    private boolean timerNotRunning;

    /*make a timer to show how much time as passed. Global since i do not want to recreate a new Timer everytime
    initialize() is called   */
    Timeline countUp = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
        second++;
        int mins = second/60;
        int secs = second % 60;
        elapsedSeconds.setText(String.format("%02d:%02d", mins, secs));

        //every second, check the words per minute
        int wordCounter = 0;
        String[] typedSoFar = textArea.getText().split(" ");
        while(wordCounter < typedSoFar.length &&
                chosenParagraph[wordCounter].equals(typedSoFar[wordCounter])){
            wordCounter++;
        }
        //words typed = how ever many words they have gotten correct
        double wordsTyped = wordCounter;
        //cast the timer's value to a double
        double seconds = second;

        //wordsPerMinute = wordsPerSecond * 60 seconds
        double wordsPerMinute = (wordsTyped / seconds) * 60;
        WPM.setText(String.format("%2.0f", wordsPerMinute));
    }));


    //list of possible paragraphs to type
    String[] paragraphs = {
            "Water. Earth. Fire. Air. Long ago, the four nations lived together in harmony. Then, everything " +
                    "changed when the Fire Nation attacked. Only the Avatar, master of all four elements, could stop " +
                    "them, but when the world needed him most, he vanished. A hundred years passed and my brother and I " +
                    "discovered the new Avatar, an airbender named Aang. And although his airbending skills are great, " +
                    "he has a lot to learn before he's ready to save anyone. But I believe Aang can save the world.",
            "Stranded. Yes, she was now the first person ever to land on Venus, but that was of little consequence. " +
                    "Her name would be read by millions in school as the first to land here, but that celebrity would " +
                    "never actually be seen by her. She looked at the control panel and knew there was nothing that " +
                    "would ever get it back into working order. She was the first and it was not clear this would " +
                    "also be her last. The sun rose and the panel short circuited, melted and sizzled.",
            "She's asked the question so many times that she barely listened to the answers anymore. The answers " +
                    "were always the same. Well, not exactly the same, but the same in a general sense. A more accurate " +
                    "description was the answers never surprised her. So, she asked for the 10,000th time, What's your " +
                    "favorite animal? But this time was different. When she heard the young boy's answer, she wondered " +
                    "if she had heard him correctly. He was ignoring her texts and he planned to continue to do so."};

    /**
     * There is no start screen for the Typing Test. It goes straight to the text field and the test
     * just begins when the user starts typing. Everytime a user "Tries again" or starts from home.fxml page,
     * this initialize runs.
     */
    public void initialize(){
        //vars used by timer and textArea listener
        second = 0;
        timerNotRunning = true;
        //set timer cycle count
        countUp.setCycleCount(Timeline.INDEFINITE);

        //get one of three paragraphs from paragraphs.
        SecureRandom random = new SecureRandom();
        String pGraph = paragraphs[random.nextInt(3)];
        TextFlow flow = new TextFlow();
        for(int i = 0; i < pGraph.length(); i++){
            Text letter = new Text(Character.toString(pGraph.charAt(i)));
            letter.setFont(Font.font(22));
            flow.getChildren().add(letter);
        }
        //TextFlow formatting
        flow.setMaxSize(700,400);
        flow.setTextAlignment(TextAlignment.CENTER);
        //add this flow to the screen
        VBox mainBox = (VBox) root.getChildren().get(0);
        mainBox.getChildren().add(3, flow);

        //make a tokenized version of the chosen paragraph
        chosenParagraph = pGraph.split(" ");

        //textArea formatting and onChange listener
        textArea.setMaxSize(700, 400);
        textArea.setWrapText(true);
        textArea.textProperty().addListener((v, oldValue, newValue) -> {

            if(timerNotRunning && newValue.length() != 0){
                countUp.play();
                timerNotRunning = false;
            }

            boolean noErrors = true;
            for (int i = 0; i < newValue.length(); i++){
                //get the letter at i, if i is within flow's domain
                Text letter;
                if(flow.getChildren().size() > i){
                    letter = (Text) flow.getChildren().get(i);
                    //check it for a match
                    if(letter.getText().charAt(0) != newValue.charAt(i)){
                        noErrors = false;
                        //change the background to red to signal a mistake
                        root.setBackground(new Background(new BackgroundFill(Color.rgb(255,0,0,0.2),
                                new CornerRadii(100), Insets.EMPTY)));
                        //also change this letter to red
                        letter.setFill(Color.RED);
                    }
                    //else it is typed correctly, so turn it green
                    else{
                        letter.setFill(Color.GREEN);
                    }
                }
            }
            //if there are now no errors, or perhaps, there have not been any errors, change/keep background green
            if(noErrors){
                root.setBackground(new Background(new BackgroundFill(Color.rgb(0,102,0,0.2),
                        new CornerRadii(100), Insets.EMPTY)));

                //check if the entire paragraph has been typed, already know that there are no errors
                if(flow.getChildren().size() == newValue.length()){
                    countUp.stop();
                    //make a new vbox root
                    VBox newVBox = new VBox(18);
                    newVBox.setAlignment(Pos.CENTER);
                    //get the VBox that is in there now. Is put back into root if Try Again button is clicked
                    VBox typingTestVBox = (VBox) root.getChildren().remove(0);
                    //get the back to home button
                    Button backToHome = (Button) typingTestVBox.getChildren().remove
                            (typingTestVBox.getChildren().size()-1);

                    //the key pic
                    Image key = new Image("main/resources/key.png");
                    ImageView view = new ImageView(key);
                    view.setFitHeight(220);
                    view.setFitWidth(220);
                    view.setPreserveRatio(true);
                    //the text that says typing test
                    Text typingTest = new Text("Typing Test");
                    typingTest.setFont(Font.font(23));
                    //typingTest.setFill(Color.BLACK);
                    //text that says wpm
                    Text wpm = new Text(WPM.getText() + "wpm");
                    int finalWPM = Integer.parseInt(WPM.getText());
                    //wpm.setFill(Color.WHITE);
                    wpm.setFont(Font.font(35));
                    //the text that says Save your score
                    Text saveScoreText = new Text("Save your score to see how you compare.");
                    saveScoreText.setFont(Font.font(20));
                    //saveScoreText.setFill(Color.WHITE);
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
                            int lineIndexToGet = 5;
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
                            if(finalWPM > currentHighScore){
                                String wordPM = String.format("%d",finalWPM);
                                info.setText("New high score: " + currentHighScore + "->" + wordPM);
                                //put new high score in lines
                                lines[lineIndexToGet] = wordPM;
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
                        Scores popUp = new Scores(info, "Typing Test");
                        popUp.display();
                    });
                    //try again button
                    Button tryAgain = new Button("Try again");
                    tryAgain.setBackground(new Background(new BackgroundFill(Color.ROYALBLUE, new CornerRadii(5.5),
                            Insets.EMPTY)));
                    tryAgain.setStyle("-fx-font-size: 27");
                    tryAgain.setOnAction(e -> {
                        //pull out the VBox that is showing this try again button.
                        VBox transitionBox = (VBox) root.getChildren().remove(0);
                        //get the back to home button
                        Button backToHome1 = (Button) transitionBox.getChildren().remove(
                                transitionBox.getChildren().size()-1);

                        //new typing test VBox
                        VBox typingTestBox = new VBox(18);
                        typingTestBox.setAlignment(Pos.CENTER);
                        typingTestBox.getChildren().addAll(WPM, howMany,textArea,elapsedSeconds,backToHome1);
                        //reset the typing test parts
                        resetTypingTestParts();
                        //reset the label that shows the number of words per minute
                        WPM.setText("Typing Test");
                        //put in the game play VBox
                        root.getChildren().add(typingTestBox);
                        //stop the timer.
                        countUp.stop();
                        //reset
                        initialize();
                    });
                    //add buttons to button box
                    buttonBox.getChildren().addAll(saveScore,tryAgain);

                    //add all of that into newVBox
                    newVBox.getChildren().addAll(view,typingTest,wpm,saveScoreText,buttonBox,backToHome);
                    //add new VBox to the pane
                    root.getChildren().add(newVBox);
                }
            }

            //set everything past how far the typer has typed to black. This handles backspaces
            for(int i = newValue.length(); i < flow.getChildren().size(); i++){
                Text letter = (Text) flow.getChildren().get(i);
                letter.setFill(Color.BLACK);
            }
        });
    }

    /**
     * Resets the text area and its background and label to what it is before a user starts typing. Used between
     * attempts.
     */
    public void resetTypingTestParts(){
        //clear out the text area
        textArea.setText("");
        //reset background to blue
        root.setBackground(new Background(new BackgroundFill(Color.TEAL, new CornerRadii(5.5),
                Insets.EMPTY)));
        root.setOpacity(1);
        //reset the label that shows the timer
        elapsedSeconds.setText("To begin, start typing the above paragraph in the text area.");

    }
}
