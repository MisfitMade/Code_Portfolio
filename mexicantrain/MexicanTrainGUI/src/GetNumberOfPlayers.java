import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * This guy is the screen that pops up at the beginning to get the game specs
 */
public class GetNumberOfPlayers {

    /**
     * Empty constructor. All work done in display method
     */
    public GetNumberOfPlayers(){

    }

    /**
     * Constructs and displays the window that gets the specs
     * @return : int[] : Returns the number of humans and number of computers
     */
    public int[] display(){
        int[] playerNumbers = {-1,-1};

        //Some labels and text fields to inform and receive specs respectively
        Stage getPlayers = new Stage();
        getPlayers.setTitle("First things first...");

        Label infoLabel = new Label("Choose how many players.\nThere must be at least 2 and at most 4.\nUse any " +
                "combo of Humans and Computers you'd like.");
        infoLabel.setStyle("-fx-font-size: 17");

        Label humanLabel = new Label("How many humans?");
        humanLabel.setStyle("-fx-font-size: 15");
        TextField humanPrompt = new TextField("");
        humanPrompt.setMaxWidth(50);

        Label compLabel = new Label("How many computers?");
        compLabel.setStyle("-fx-font-size: 15");
        TextField compPrompt = new TextField("");
        compPrompt.setMaxWidth(50);

        //The submit button and its functionality
        Button submitButton = new Button("SUBMIT");
        submitButton.setOnAction(e -> {
            String input = humanPrompt.getText();
            String input2 = compPrompt.getText();
            final boolean COMP_HAS_NUMBER = input2.length() == 1 && input2.charAt(0) > '/' && input2.charAt(0) < '5';
            final boolean HUMAN_HAS_NUMBER = input.length() == 1 && input.charAt(0) > '/' && input.charAt(0) < '5';
            /*Does if both text fields have a valid number || human field has a valid number and computer field has no
            text || computer field has valid number && human field has no text         */
            if(HUMAN_HAS_NUMBER && COMP_HAS_NUMBER ||
                    (HUMAN_HAS_NUMBER && input2.length() == 0) ||
                        (COMP_HAS_NUMBER && input.length() == 0)){

                int numberOfHumans = (input.length() > 0) ? Integer.parseInt(input) : 0;
                int numberOfComps = (input2.length() > 0) ? Integer.parseInt(input2) : 0;

                int total = numberOfComps + numberOfHumans;
                //if the input is legit
                if(total > 1 && total < 5){
                    playerNumbers[0] = numberOfHumans;
                    playerNumbers[1] = numberOfComps;

                    getPlayers.close();
                }
            }
        });

        //put the label, fields and button in a VBox
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));
        layout.getChildren().addAll(infoLabel,humanLabel,humanPrompt,compLabel,compPrompt,submitButton);

        Scene scene = new Scene(layout, 500, 400);

        getPlayers.setScene(scene);
        getPlayers.showAndWait();

        //return the number of players
        return playerNumbers;
    }
}
