package main.backend;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Scores {

    private final Text SCORE_INFO;
    private String gameName;

    private final File FILE_OF_SCORES_TO_SHOW;

    /**
     * Use overloaded constructors to determine what the display() method does.
     * @param SCORE_INFO Text to show info after a Save Score button is clicked
     */
    public Scores(Text SCORE_INFO, String gameName){
        this.SCORE_INFO = SCORE_INFO;
        this.gameName = gameName;

        FILE_OF_SCORES_TO_SHOW = null;
    }

    /**
     * Takes a file of all scores to show
     * @param FILE_OF_SCORES_TO_SHOW : Used when Show high scores button is clicked from Home page
     */
    public Scores(File FILE_OF_SCORES_TO_SHOW){
        this.FILE_OF_SCORES_TO_SHOW = FILE_OF_SCORES_TO_SHOW;
        SCORE_INFO = null;
    }

    /**
     * Either displays just some info from a specific game, or all the high scores
     */
    public void display(){
        Stage popUp = new Stage();
        popUp.setTitle("Scoring");

        Scene scene = null;

        //the root and its background
        Pane root = new StackPane();
        root.setBackground(new Background(new BackgroundFill(Color.TEAL, new CornerRadii(65), Insets.EMPTY)));

        //for all the objects to come. For the scene
        VBox info = new VBox(13);
        info.setAlignment(Pos.CENTER);

        //if this is to display an individual score info
        if(FILE_OF_SCORES_TO_SHOW == null){
            //Make a text that says the game's name
            Text gamesName = new Text(gameName);
            gamesName.setUnderline(true);
            gamesName.setFont(Font.font(50));
            gamesName.setFill(Color.WHITE);
            //add to info
            info.getChildren().addAll(gamesName, SCORE_INFO);
        }
        //else, SCORE_INFO is null and FILE_OF_SCORES_TO_SHOW is not
        else{
            //text that says your high scores
            Text yourHighs = new Text("Your high scores");
            yourHighs.setFont(Font.font(52));
            yourHighs.setUnderline(true);
            //add yourHighs to box
            info.getChildren().add(yourHighs);
            //try to read from the file
            try{
                Scanner scanner = new Scanner(FILE_OF_SCORES_TO_SHOW);
                //counter and game names
                int lineCounter = 0;
                String[] gameNames = {"Aim Trainer: ","Chimp Test: ", "Fast Math: ", "Number Memory: ", "Reaction Time: ",
                        "Typing Test: ", "Verbal Memory: ", "Visual Memory: "};
                while(lineCounter < 8){
                    //get the score
                    String score = scanner.nextLine();
                    //want to skip index 4 since that is ReactionTime and I left it out to be like humanbenchmark.com
                    if(lineCounter != 4){
                        Text stat;
                        /*Now, catch the cases when there is no score yet
                        The below if does:
                        if (
                        (score == 0 for AimTrainer || TypingTest || VerbalMemory) ||
                        (score = 1 for FastMath || NumberMemory || VisualMemory) ||
                        (score = 4 for ChimpTest) )
                        Which is just how i defined the default high scores
                         */
                        if( (score.equals("0") && (lineCounter == 0 || lineCounter == 5 || lineCounter == 6)) ||
                                (score.equals("1") && (lineCounter == 2 || lineCounter == 3 || lineCounter == 7)) ||
                                (score.equals("4") && lineCounter == 1)) {
                            //in this case I want to say the game has no high score yet
                            stat = new Text(gameNames[lineCounter] + "None");
                        }
                        //else this is not the first time for this game to get a high score
                        else{
                            stat = new Text(gameNames[lineCounter] + score);
                        }
                        //give stat its formatting
                        stat.setFill(Color.WHITE);
                        stat.setFont(Font.font(34));
                        //add to info box
                        info.getChildren().add(stat);
                    }

                    lineCounter++;//next index
                }
                //close file just in case
                scanner.close();
            }
            catch (IOException e){
                //if error, then this below gets shown below "Your high scores" in the scene
                Text error = new Text("are currently unavailable. The file was not found.");
                error.setFont(Font.font(27));
                error.setFill(Color.WHITE);
                //add to info
                info.getChildren().add(error);
            }
        }
        //now i have 'info' which has the appropriate objects
        root.getChildren().add(info);
        //now i have root which either has all the scores, or one score. Add it to scene
        scene = new Scene(root);

        //sizing the pop-up
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        popUp.setWidth(bounds.getWidth()/1.5);
        popUp.setHeight(bounds.getHeight()/1.5);

        //show the pop up
        popUp.setScene(scene);
        popUp.show();
    }
}
