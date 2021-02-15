import java.util.ArrayList;
import java.util.Scanner;

/**
 * The main class with the main method. Gets the game specs and calculates the winner at the end.
 */
public class Main {

    static Scanner scanner;
    static int numberOfHumans;

    public static void main(String[] args) {

        /*First we must see how many computer and human players
        Will hold the players and control the turns, as whose turn it is will be next in list*/
        ArrayList<Player> players = new ArrayList<>();
        System.out.println("Welcome to Mexican Train!\nRules and strategies may be found here: " +
                "https://www.mexicantrainrulesandstrategies.com\nAt least 2 and up to 4 players can play with " +
                "any mix of human and computer players.\n\nHow many human players? Max is 4.");
        scanner = new Scanner(System.in);
        int counter = 2;
        int maxComps = 4;
        int minComps = 0;
        while(counter > 0){
            String numberOfPlayers = scanner.next();
            int numOfPlayers = 0;

            try{
                numOfPlayers = Integer.parseInt(numberOfPlayers);
                //if input is a valid number
                if(numOfPlayers < 5 && numOfPlayers > -1){
                    //if this is human number of players
                    if(counter == 2){
                        numberOfHumans = numOfPlayers;
                        //create the appropriate number of human players in the players list
                        for(int i = 0; i < numOfPlayers; i++){
                            players.add(new Player('h', i+1));
                        }

                        maxComps = 4 - numOfPlayers;//the max number of computer players possible
                        //if the user selected 4 humans, then no computers
                        if(maxComps == 0){
                            counter--;//force the exit of the while loop by doing an extra decrement
                        }
                        //else, ask the user how many computer players
                        else{
                            //For showing user the min and max that is available for number of computer players
                            minComps = 0;//assume more than 1 human player was chosen
                            switch (numOfPlayers) {
                                case 0: {
                                    //if the user selected 0 humans, need at least 2 comps
                                    minComps = 2;
                                    break;
                                }
                                case 1: {
                                    //if the user selected 1 human, at least one comp must be given
                                    minComps = 1;
                                    break;
                                }
                            }
                            //ask the user how many comps
                            System.out.println("How many computer players? Max is " + maxComps + ". Min is " +
                                    minComps);
                        }
                        counter--;//decrement from human to computer or from human to no computer input allowed
                    }
                    //else this is a computer number of players, so counter == 1, implied by above if and while > 0
                    else if(numOfPlayers <= maxComps && numOfPlayers >= minComps) {
                        /*by here, we have the max and min number of comp players allowed based off the number of humans
                        given on the prior iterations*/
                        for(int i = 0; i < numOfPlayers; i++){
                            players.add(new Player('c', (numberOfHumans + (i+1)) ));
                        }
                        counter--;//decrement from human to computer or from human to no computer input allowed
                    }
                    else{
                        System.out.printf("Invalid number of computer players. Try again." +
                                " Max is %d. Min is %d.\n",maxComps,minComps);
                    }
                }
                else {
                    System.out.println("Not a valid player number. Try again.");
                }
            }
            catch (NumberFormatException e){
                System.out.println("Invalid input");
                System.exit(1);
            }
        }
        /*By here, we have the list players which has a 'numberOfHumans' human players as its first elements, then the
        rest of the players are computers
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

        //then, for each round
        Controller gameController = new Controller();
        gameController.playRounds(players,dominoesPerHand,numberOfHumans);


        /*If here, all rounds have been played. Show you won and who got what place*/
        int firstPlacePlayerNumber = 0;
        Player firstPlace = null;
        int bestScore = Integer.MAX_VALUE;
        System.out.println("And the verdict is...");
        for(Player player : players){
            int playerNumber = (player.getHUMAN_OR_COMPUTER() == 'h') ?
                    player.getPLAYER_NUMBER() : player.getPLAYER_NUMBER()-numberOfHumans;

            System.out.printf("%s%d got %d total points.\n",player.getID(), playerNumber,player.getPlayerScore());

            if(player.getPlayerScore() < bestScore){
                firstPlacePlayerNumber = playerNumber;
                firstPlace = player;
                bestScore = player.getPlayerScore();
            }
        }
        /*Now I know first place. Find 2nd*/
        int secondPlacePlayerNumber = 0;
        Player secondPlace = null;
        int secondBestScore = Integer.MAX_VALUE;
        for(Player player : players){
            int playerNumber = (player.getHUMAN_OR_COMPUTER() == 'h') ?
                    player.getPLAYER_NUMBER() : player.getPLAYER_NUMBER()-numberOfHumans;

            if(player != firstPlace && player.getPlayerScore() < secondBestScore){
                secondPlacePlayerNumber = playerNumber;
                secondPlace = player;
                secondBestScore = player.getPlayerScore();
            }
        }
        /*Now I have first and 2nd place*/
        System.out.printf("\nWith %d points, first place goes to %s%d!\n" +
                "With %d points, second place goes to %s%d!\n" +
                        "Congratulations and thanks for playing Mexican Train!\n",
                bestScore,firstPlace.getID(),firstPlacePlayerNumber,secondBestScore,
                secondPlace.getID(),secondPlacePlayerNumber);

    }
}