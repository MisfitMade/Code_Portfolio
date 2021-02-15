import javafx.application.Application;
import javafx.stage.Stage;
import java.util.ArrayList;

/**
 * The main class, with the main method. Displays the pop-up that gets the game specs, then computes the number
 * of dominoes per hand based off the number of players, then initializes a the game's controller and starts the first
 * round, via the controller
 */
public class Main extends Application {

    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        //first get the number of players info
        GetNumberOfPlayers request = new GetNumberOfPlayers();
        int[] numberOfPlayers = request.display();

        //if the user gave legit specs
        if(numberOfPlayers[0] != -1){
            //make the players
            ArrayList<Player> players = new ArrayList<>();
            //for the Humans
            for(int i = 0; i < numberOfPlayers[0]; i++){
                players.add(new Player('h',(i+1)));
            }
            //for the computers
            for(int i = 0; i < numberOfPlayers[1]; i++){
                players.add(new Player('c',(i+1) + numberOfPlayers[0]));
            }
            /*By here, we have the list players
            First get the number of dominoes that should be in each initial hand        */
            int dominoesPerHand = 0;
            if(players.size() == 2){
                dominoesPerHand = 15;
            }
            else if(players.size() == 3){
                dominoesPerHand = 13;
            }
            else if(players.size() == 4){
                dominoesPerHand = 10;
            }
            //Should not happen, but just in case. Just quit game if got here.
            else{
                System.out.println("Fatal Error!\n");
                System.exit(1);
            }
            //set up display and start rounds. From there, controller takes over
            Controller controller = new Controller(players,numberOfPlayers[0],dominoesPerHand);
            controller.startNewRound();
        }
    }
}
