import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Toggle;
import javafx.scene.layout.*;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This class holds the event handlers for the buttons in the display and also controls flow between players and
 * between rounds
 */
public class Controller {

    //the indexes of the double doms in a new boneyard
    private final int[] INDICES_OF_DOUBLE_DOM_IN_NEW_BONEYARD = {54,52,49,45,40,34,27,19,10,0};
    private int roundCounter = 0;
    private final int DOMINOES_PER_HAND;
    private Display display;    //the GUI
    private final ArrayList<Player> players;
    private int currentPlayersIndex;//always has the player whose turn it is's index in players

    //the EventHandlers for the buttons back in Display
    private final EventHandler<ActionEvent> PLAY_DOMINO_LISTENER;
    private final EventHandler<ActionEvent> FLIP_DOMINO_LISTENER;
    private final EventHandler<ActionEvent> DRAW_DOMINO_LISTENER;
    private final EventHandler<ActionEvent> OPEN_TRAIN_LISTENER;

    //number of humans and number of total players
    private final int NUMBER_OF_HUMANS;
    private final int NUMBER_OF_PLAYERS;

    //always has the current rounds table and boneyard
    private Table table;
    private Boneyard boneyard;

    private boolean computerMoving;//to keep from the GUI buttons working when the computer is moving

    /**
     * Controller constructor
     * @param players : The list of players
     * @param numberOfHumans : The number of humans inside players
     * @param DOMINOES_PER_HAND : The number of dominoes in a hand at the start of a round
     */
    public Controller(ArrayList<Player> players, int numberOfHumans, int DOMINOES_PER_HAND) {

        display = new Display(players,numberOfHumans,this);

        this.players = players;
        this.DOMINOES_PER_HAND = DOMINOES_PER_HAND;
        NUMBER_OF_HUMANS = numberOfHumans;
        NUMBER_OF_PLAYERS = players.size();


        //The listener for the Play here buttons on the display
        PLAY_DOMINO_LISTENER = event -> {
            //only respond when it is not the computer moving
            if(!computerMoving){
                Domino toggled = null;
                boolean invalidPlay = true;//assume this domino does not match
                Player currentPlayer = players.get(currentPlayersIndex);
                Button trainButtonClicked = (Button)event.getSource();

                int trainIndexInGrid = GridPane.getRowIndex(trainButtonClicked);
                /*now, if the mexican train was chosen, then this below becomes 2, while the trains in memory have
                mexican train at index NUMBER_OF_PLAYERS and players [0 - NUMBER_OF_PLAYERS) are
               at [0-NUMBER_OF_PLAYERS-1], so            */
                int trainIndexInMemory = trainIndexInGrid;
                //if mexican train
                if(trainIndexInMemory == 2){
                    trainIndexInMemory = NUMBER_OF_PLAYERS;
                }
                /*if this is one of the trains below the mexican train in the display, so it is row index 3 or 4
                 since it did not go into the if mexican train statement above    */
                else if(trainIndexInMemory > 2){
                    trainIndexInMemory--;
                }
                //else, it is at the right index by int trainIndexInMemory = trainIndexInGrid

                /*lets get the domino now in case even if there is a double in play played by some other player, but
                this player has chosen a double that matches*/
                //Then they chose an eligible train. get the train engine they are trying to play on
                Domino trainEngineChosen = table.getPLAYERS_TRAINS().get(trainIndexInMemory).getLast();

                //find the toggle domino
                ObservableList<Toggle> toggles = currentPlayer.getTrayButtons().getToggles();
                //for all the toggles
                for(int i = 0; i < toggles.size(); i++){
                    //if it is the selected button
                    if(toggles.get(i).isSelected()){
                        //get a reference to the domino object
                        toggled = currentPlayer.getHand().get(i);
                        //check that it matches
                        if(toggled.getLeft() == trainEngineChosen.getRight()){
                            //if so, get reference to this domino
                            toggled = currentPlayer.getHand().get(i);
                            /*now I have the chosen train number index.
                            Following epic if does:
                            If no double in play and it is open or is the current player's train
                                or
                            a double is in play, played by some other player, but this player has
                            chosen to play a matching double
                                or
                            a double is in play and it was not just played by this player and the button they clicked
                            the right train
                                or
                            there is a double in play and this player was the one who played it and it is one of the
                            eligible trains: their own train, mexican train, double played train, any open trains

                            The noDoubleInPlay guards the Queue peek from null exceptions*/
                            boolean noDoubleInPlay = table.isNotDoubleDomino();
                            if((noDoubleInPlay &&
                                    (table.getOPEN_TRAINS()[trainIndexInMemory] ||
                                            trainIndexInMemory == currentPlayersIndex))
                                    ||
                                    (!noDoubleInPlay &&
                                            (!currentPlayer.isSecondTurnCusDoublePlayed() &&
                                                    (trainIndexInMemory ==
                                                                    table.getDOUBLE_PLAYED_OPEN_TRAINS().peek()))
                                            ||
                                            (currentPlayer.isSecondTurnCusDoublePlayed() &&
                                                    (trainIndexInMemory == currentPlayersIndex ||
                                                            table.getOPEN_TRAINS()[trainIndexInMemory]))
                                    )
                            ){
                                //if so, remove it from hand and tray
                                toggled = currentPlayer.removeDominoFromHand(i);
                                //display on train
                                display.playOnTrain(toggled.createDominoForBoard(),trainIndexInGrid);
                                //store in memory
                                table.getPLAYERS_TRAINS().get(trainIndexInMemory).addLast(toggled);

                                //if the player played on their own train and their train is open, close their train
                                if(table.getOPEN_TRAINS()[currentPlayersIndex] &&
                                        trainIndexInMemory == currentPlayersIndex){
                                    display.closeATrain(trainIndexInGrid);
                                    table.closeATrain(currentPlayersIndex);
                                }
                                //if the round should end
                                if(currentPlayer.getHand().size() == 0) {
                                    //end this round, maybe start a new one
                                    endRound();
                                }

                                //if they are playing a double
                                else if(toggled.getRight() == toggled.getLeft()){
                                    //add this train to the doubles played on trains
                                    table.getDOUBLE_PLAYED_OPEN_TRAINS().add(trainIndexInMemory);

                                    //say that this player just played a double and all the other players did not
                                    for(int j = 0; j < players.size(); j++){
                                        players.get(j).setSecondTurnCusDoublePlayed(j == currentPlayersIndex);
                                    }
                                    //tell the human they get to go again
                                    display.setInfo("You played a double domino.\nHuman" +
                                            currentPlayer.getPLAYER_NUMBER() + ", go again.", false);
                                      //if they have no play now, tell them to draw or open train
                                    if(table.doesHumanHaveAPlayToMake(currentPlayersIndex,currentPlayer,
                                            currentPlayer.isSecondTurnCusDoublePlayed()).size() == 0){

                                        display.setInfo("\nYou have no move now.",true);

                                        /*if this player has yet to draw, for example,
                                        player has no move, draws a double, plays it, they do not get to draw again
                                        even tho they played a double*/
                                        if(!currentPlayer.didThePlayerJustDraw()){
                                            //tell em to draw
                                            display.setInfo("\nDraw.",true);
                                        }
                                        /*If their train is not already open, inform them to open their train*/
                                        else if(!table.getOPEN_TRAINS()[currentPlayersIndex]){
                                            display.setInfo("\nOpen your train",true);
                                        }
                                        /*else their train is already open and they have no move so check if
                                         round should end*/
                                        else{
                                            display.setInfo("\nBummer.",true);
                                            shouldRoundEnd();
                                        }
                                    }
                                }
                                //else the round is not over and the player's turn is over
                                else{
                                    /*if they have satisfied a double domino to play on. Cannot play a double on
                                      a double. !isNotDoubleDomino guards the queue peek  */
                                    if(!table.isNotDoubleDomino()){
                                        table.satisfyDoublePlay(trainIndexInMemory);
                                    }

                                    /*need to let all players try playing again since a player just played and
                                     round is not over*/
                                    for(Player player : players){
                                        player.setHasNoPlayToMake(false);
                                    }

                                    goToNextPlayer();
                                }
                            }
                            //if play does not work
                            else {
                                display.setInfo("\nCannot play that there.\nMake sure you have a domino selected.",
                                        true);
                            }
                        }
                    }
                }
            }
        };

        //The listener for the Open Train button
        OPEN_TRAIN_LISTENER = event -> {
            //only respond when computer is not moving and when the player has no move to make
            if(!computerMoving){
                Player currentPlayer = players.get(currentPlayersIndex);
                //if there train is not already open
                if(!table.getOPEN_TRAINS()[currentPlayersIndex]){
                    /*if the player has a play to make, then a list of size > 0 is returned with valid plays*/
                    if(table.doesHumanHaveAPlayToMake(currentPlayersIndex,currentPlayer,
                            currentPlayer.isSecondTurnCusDoublePlayed()).size() == 0){
                        //open the train in the memory
                        table.openATrain(currentPlayersIndex);
                        /*if the currentPlayersIndex is > 1, then in the display, this is row index 3 or 4 since the
                        mexican train is in the center*/
                        int trainToOpenInDisplay = (currentPlayersIndex > 1) ? currentPlayersIndex+1 : currentPlayersIndex;
                        //open train in graphic
                        display.openATrain(trainToOpenInDisplay);

                        //say this player has no move to make
                        currentPlayer.setHasNoPlayToMake(true);
                        //check if round should end, if not, go to next player
                        shouldRoundEnd();
                    }
                    //else they are trying to open their train while they have a play to make
                    else{
                        display.setInfo("\nCannot open train.\nYou have a play to make.", true);
                    }
                }
                //else they are trying to open their train when it is already open
                else{
                    display.setInfo("\nYour train is already open.", true);
                }
            }
        };


        //The listener for the Flip button in the display
        FLIP_DOMINO_LISTENER = event -> {
            //only respond when it is not a computer' turn
            if(!computerMoving){
                //current player
                Player currentPlayer = players.get(currentPlayersIndex);
                //get the toggle buttons in the current player's tray
                ObservableList<Toggle> toggles = currentPlayer.getTrayButtons().getToggles();
                //for all the toggles
                for(int i = 0; i < toggles.size(); i++){
                    //if this is the toggle selected
                    if(toggles.get(i).isSelected()){
                        //get a reference to the domino in the player's hand
                        Domino toFlip = currentPlayer.getHand().get(i);
                        //flip it
                        toFlip.rotateDomino(true);
                        toggles.get(i).setSelected(true);
                        break;//only one button can be toggled
                    }
                }
            }
        };

        //The listener for the Draw button in the display
        DRAW_DOMINO_LISTENER = event -> {
            //only respond when it is not the computer moving
            if(!computerMoving){
                //need to check that the user is not trying to draw while there is a domino they can play
                Player currentPlayer = players.get(currentPlayersIndex);
                //if they have no possible plays
                if(table.doesHumanHaveAPlayToMake(currentPlayersIndex,currentPlayer,
                        currentPlayer.isSecondTurnCusDoublePlayed()).size() == 0) {

                    //if there are dominoes in the boneyard, add a domino to hand and to the players tray
                    if (boneyard.getBoneyard().size() > 0) {
                        //add domino to hand from boneyard
                        currentPlayer.addToHand(boneyard.drawDomino());
                        currentPlayer.setThePlayerJustDraw(true);//the player just drew

                        /*If human still has no play to make, */
                        if (table.doesHumanHaveAPlayToMake(currentPlayersIndex, currentPlayer,
                                currentPlayer.isSecondTurnCusDoublePlayed()).size() == 0) {

                            display.setInfo("You still have no play to make.", false);
                            currentPlayer.setHasNoPlayToMake(true);

                            /*If their train is not already open, inform them to open their train*/
                            if (!table.getOPEN_TRAINS()[currentPlayersIndex]) {
                                display.setInfo("\nOpen your train", true);
                            }
                            //else their train is already open and they have no move so check if round should end
                            else {
                                display.setInfo("\nBummer.", true);
                                //slow a few secs so the human can read what happened
                                PauseTransition pt = new PauseTransition();
                                pt.setDuration(Duration.seconds(2));
                                pt.setOnFinished(e -> {
                                    shouldRoundEnd();
                                });
                                pt.play();
                            }
                        }
                        //else they have a move
                        else{
                            display.setInfo("\nYou lucky dog.", true);
                        }
                    }
                    //else the boneyard is empty, so their turn is passed
                    else{
                        display.setInfo("\nThe boneyard is empty",true);
                        shouldRoundEnd();
                    }
                }
                //else the player has a move to make
                else{
                    display.setInfo("\nYou cannot draw.\nYou have a move to make.", true);
                }
            }
        };
    }

    /**
     * Goes to the next player after a turn has ended. Does a for loop for computers
     * Does recursion from human to human
     */
    public void goToNextPlayer(){

        display.resetDoublePlayedButtons();
        //if there is a double in play, display next non-double label
        if(!table.isNotDoubleDomino()){
            //peek guarded by !table.isNotDoubleDomino
            display.setDoublePlayedButton(table.getDOUBLE_PLAYED_OPEN_TRAINS().peek());
        }

        Player previousPlayer = players.get(currentPlayersIndex);

        //inc to next current player index. if it incs to more than there are players, back to zero
        currentPlayersIndex = (currentPlayersIndex+1 == NUMBER_OF_PLAYERS) ? 0 : currentPlayersIndex+1;
        Player nextPlayer = players.get(currentPlayersIndex);


        //check if this next player is a computer. If it is, the rest of the players in players are all computers
        if(nextPlayer.getHUMAN_OR_COMPUTER() == 'c'){
            boolean roundDidNotEnd = true;
            //reset display label
            display.setInfo("", false);
            //do the computers in a for loop to keep from getting stack overflow
            for(int i = currentPlayersIndex; i < players.size(); i++){
                nextPlayer = players.get(i);
                //the nextPlayer has yet to go, so they did not just draw
                nextPlayer.setThePlayerJustDraw(false);
                nextPlayer.setSecondTurnCusDoublePlayed(false);
                currentPlayersIndex = i;
                /*if this is the first time this computer is up. During gameplay, bestCombo is
                computed in the Table class  */
                if(nextPlayer.getBestCombo() == null){
                    nextPlayer.computeBestCombo();
                }

                boolean[] keepPlayingRound_doublePlayed = {true,false};
                computerMoving = true;
                boolean computerPlaysAgain = true;

                while(computerPlaysAgain){
                    if(keepPlayingRound_doublePlayed[0]){
                        keepPlayingRound_doublePlayed =
                                table.computerAttemptsAPlay(nextPlayer,i,boneyard,players,
                                        nextPlayer.isSecondTurnCusDoublePlayed(),display);

                        //if the computer played a double, they get to go again, else this is false and while loop exits
                        computerPlaysAgain = keepPlayingRound_doublePlayed[1];
                        nextPlayer.setSecondTurnCusDoublePlayed(computerPlaysAgain);
                    }
                   else {
                       computerPlaysAgain = false;
                    }
                }
                //if it was detected that this round should end
                if(!keepPlayingRound_doublePlayed[0]){
                    endRound();
                    roundDidNotEnd = false;
                    //break to keep from finishing the for loop after endRound pops back out
                    break;
                }
                /*else, the computer finished their turn, go to next player via for loop*/
            }
            /*here, the computers have finished, go to player 1, a human
            This if is used to keep this from running after a round ended while computer's were moving and
            startNewRound() was called then popped back out*/
            if(roundDidNotEnd){
                goToNextPlayer();
            }
        }

        //else, put the next human's tray up, inform human
        else{
            computerMoving = false;
            nextPlayer.setThePlayerJustDraw(false);
            nextPlayer.setSecondTurnCusDoublePlayed(false);

            //if the last player that went was a computer, tackOn
            if(previousPlayer.getHUMAN_OR_COMPUTER() == 'c'){
                //and inform the human that it is their turn
                display.setInfo(String.format("\nHuman%d's turn",nextPlayer.getPLAYER_NUMBER()),
                        true);
            }
            //else the last player was a human, so no tack on message
            else{
                display.setInfo(String.format("\nHuman%d's turn",nextPlayer.getPLAYER_NUMBER()),
                        false);
            }
            //show the current human's tray
            display.setTray(nextPlayer);

            //if the human has no play
            if(table.doesHumanHaveAPlayToMake(currentPlayersIndex,nextPlayer,
                    nextPlayer.isSecondTurnCusDoublePlayed()).size() == 0){

                //if there are dominoes in the boneyard inform the human to draw a domino
                if(boneyard.getBoneyard().size() > 0){
                    display.setInfo("\nYou have no play to make.\nDraw.", true);
                /*once they draw, the draw domino button will inform them to open their train if they still have no play
                to make. If no inform then there is a play the human can make*/
                }
                //else the boneyard is empty, inform human, go to next player
                else{
                    display.setInfo("\nYou have no play to make.\nBoneyard is empty.\n", true);
                    goToNextPlayer();
                }
            }
        }
    }

    /**
     * Ends a round. If last round, show results. If not, reset some player variables and
     * startNewRound
     */
    public void endRound(){
        //inform that the round ended
        Player lastPlayerToHavePlayed = players.get(currentPlayersIndex);
        //if round ended cuz last domino in hand played
        if(lastPlayerToHavePlayed.getHand().size() == 0){
            int playerNumber = (lastPlayerToHavePlayed.getHUMAN_OR_COMPUTER() == 'c') ?
                    lastPlayerToHavePlayed.getPLAYER_NUMBER() - NUMBER_OF_HUMANS :
                    lastPlayerToHavePlayed.getPLAYER_NUMBER();

            display.setInfo(String.format("\n%s%d played their last domino."
                    ,lastPlayerToHavePlayed.getID(), playerNumber), false);
        }
        //else if ended cus no one had a play to make
        else{
            display.setInfo("\nNo one had a move to make" , false);
        }
        //say round complete
        display.setInfo("\nRound " + (roundCounter+1) + " complete.", true);

        //count up the scores
        for(Player player : players){
            player.setPlayerScore();
        }

        //if the last round was just played
        if(roundCounter == 9){
            //end game
            showResults();
        }
        else{
            //reset booleans
            for(Player player : players){
                player.setThePlayerJustDraw(false);
                player.setSecondTurnCusDoublePlayed(false);
                player.setHasNoPlayToMake(false);
            }
            //next round
            currentPlayersIndex = 0;
            roundCounter++;
            startNewRound();
        }
    }

    /**
     * Starts a new round. Sets the GUI display with new trains and stuff.
     * If the first player is a human, do recursion til a computer's turn.
     * If the first player is a computer, do the whole game in for loop so as not to
     * have stack overflow.
     */
    public void startNewRound(){
        /*if the first player is a human, then can do the game recursively as it all pops off when ever it is a
        human's turn*/

        Player firstPlayer = players.get(currentPlayersIndex);
        //if first player is a human
        if(firstPlayer.getHUMAN_OR_COMPUTER() == 'h'){
            currentPlayersIndex = 0;
            computerMoving = false;

            boneyard = new Boneyard();//new boneyard in memory
            Domino centerDomino = boneyard.drawParticularDoubleDomino(
                    INDICES_OF_DOUBLE_DOM_IN_NEW_BONEYARD[roundCounter]);//get te center domino

            table = new Table(centerDomino,NUMBER_OF_PLAYERS,NUMBER_OF_HUMANS);//new table in memory

            //new player hands in memory and new playerTrays
            for(Player player : players){
                int handCounter = 0;
                player.setHand(new LinkedList<>());
                //while the player does not had a full hand
                while (handCounter < DOMINOES_PER_HAND){
                    //draw a random domino
                    player.addToHand(boneyard.drawDomino());
                    handCounter++;
                }
            }

            //new tableDisplay for each round
            display.setTrains(players,this,centerDomino.createDominoForBoard());
            display.setTrainLabels(players);
            display.setFlipDrawOpenButtons(this);
            display.initializeTrays(players);
            display.resetDoublePlayedButtons();//give them their green color
            //show the first human's tray
            display.setTray(firstPlayer);
            //set info label that it is the human's turn
            display.setInfo(String.format("\nRound %d. Ready? Fight!", (roundCounter+1)),true);
            display.setInfo(String.format("\nHuman%d's turn", 1), true);
            //set the scene/display
            display.setDisplay();

            //if human has no play to make, tell them to draw
            if(table.doesHumanHaveAPlayToMake(currentPlayersIndex,firstPlayer,
                    firstPlayer.isSecondTurnCusDoublePlayed()).size() == 0){
                display.setInfo("\nYou have no move to make. Draw.", true);
            }
            //else, wait for a human to make a move, to interact with buttons
        }
        /*else this is a computer player, thus they are all computer players. If they are all computer players, need to
        use a for loop otherwise all the recursion overflows the stack */
        else{
            computerMoving = true;
            for(int round = 0; round < INDICES_OF_DOUBLE_DOM_IN_NEW_BONEYARD.length; round++){
                //do the new stuff in memory
                boneyard = new Boneyard();
                Domino centerDomino = boneyard.drawParticularDoubleDomino(INDICES_OF_DOUBLE_DOM_IN_NEW_BONEYARD[round]);
                table = new Table(centerDomino,NUMBER_OF_PLAYERS,NUMBER_OF_HUMANS);
                //new player hands in memory and new playersTrays
                for(Player player : players){
                    int handCounter = 0;
                    player.setHand(new LinkedList<>());
                    //while the player does not had a full hand
                    while (handCounter < DOMINOES_PER_HAND){
                        //draw a random domino
                        player.addToHand(boneyard.drawDomino());
                        handCounter++;
                    }
                }

                //new tableDisplay for each round
                display.setTrains(players,this,centerDomino.createDominoForBoard());
                display.setTrainLabels(players);
                display.setFlipDrawOpenButtons(this);
                display.initializeTrays(players);
                display.resetDoublePlayedButtons();//give them their green color
                //set the displays info label
                display.setInfo(String.format("Round %d. Ready? Fight!", round),false);
                //set the scene/display
                display.setDisplay();
                //compute the computers' best combos
                for(Player player : players){
                    player.computeBestCombo();
                    player.setHasNoPlayToMake(false);
                    player.setSecondTurnCusDoublePlayed(false);
                    player.setThePlayerJustDraw(false);
                }
                //start round play
                boolean thereArePlaysToMake = true;
                while (thereArePlaysToMake){
                    for(int i = 0; i < players.size(); i++){
                        Player currentPlayer = players.get(i);
                        if(thereArePlaysToMake){
                            //reset this player's booleans
                            currentPlayer.setThePlayerJustDraw(false);
                            currentPlayer.setSecondTurnCusDoublePlayed(false);

                            boolean goAgain = true;//if the computer plays a double, gets to go again
                            while (goAgain){
                                if(thereArePlaysToMake){
                                    boolean[] keepPlayingRound_doublePlayed =
                                            table.computerAttemptsAPlay(currentPlayer,i,boneyard,players,
                                                    currentPlayer.isSecondTurnCusDoublePlayed(),display);

                                    thereArePlaysToMake = keepPlayingRound_doublePlayed[0];//false if round should end
                                    goAgain = keepPlayingRound_doublePlayed[1];//true if double played
                                    //if this is a 2nd turn cuz double played, the valid trains are considered differently
                                    currentPlayer.setSecondTurnCusDoublePlayed(goAgain);
                                }
                                else {
                                    goAgain = false;
                                }
                            }
                        }
                    }
                }
                //if here, a round just ended, tally up scores
                for(Player player : players){
                    player.setPlayerScore();
                }
            }
            //if here, game just ended, show winner
            showResults();
        }
    }

    /**
     * Tallies up final scores and shows results
     */
    public void showResults(){
        /*Find first place*/
        int firstPlacePlayerNumber = 0;
        Player firstPlace = null;
        int bestScore = Integer.MAX_VALUE;
        display.setInfo("And the verdict is...",true);
        for(Player player : players){

            int playerNumber = (player.getHUMAN_OR_COMPUTER() == 'h') ?
                    player.getPLAYER_NUMBER() : player.getPLAYER_NUMBER()-NUMBER_OF_HUMANS;

            int playerScore = Integer.parseInt(player.getPlayerScore());
            display.setInfo(String.format("\n%s%d's final score: %d",player.getID(),playerNumber,playerScore),
                                                true);

            if(playerScore < bestScore){
                firstPlacePlayerNumber = playerNumber;
                firstPlace = player;
                bestScore = playerScore;
            }
        }
        /*Now I know first place. Find 2nd*/
        int secondPlacePlayerNumber = 0;
        Player secondPlace = null;
        int secondBestScore = Integer.MAX_VALUE;
        for(Player player : players){

            int playerNumber = (player.getHUMAN_OR_COMPUTER() == 'h') ?
                    player.getPLAYER_NUMBER() : player.getPLAYER_NUMBER()-NUMBER_OF_HUMANS;

            int playerScore = Integer.parseInt(player.getPlayerScore());
            if(player != firstPlace && playerScore < secondBestScore){
                secondPlacePlayerNumber = playerNumber;
                secondPlace = player;
                secondBestScore = playerScore;
            }
        }

        /*Now I have first and 2nd place*/
        display.setInfo(String.format("\nWith %d points,\nfirst place goes to %s%d!\n" +
                        "With %d points,\nsecond place goes to %s%d!\n" +
                        "Congratulations!\nThanks for playing Mexican Train!",
                bestScore,firstPlace.getID(),firstPlacePlayerNumber,secondBestScore,
                secondPlace.getID(),secondPlacePlayerNumber),true);

    }

    /**
     * Checks if all the players have no move to make. If so, endRound.
     * If not, goToNextPlayer
     */
    public void shouldRoundEnd(){
        //check if no players have a move to make
        boolean outOfMoves = true;
        //for all the players, check their noMoveToMake variable
        for(Player player : players){
            //only stays true if all players have no move
            outOfMoves &= player.isHasNoPlayToMake();;
        }
        //if out of moves, end this round
        if(outOfMoves){
            endRound();
        }
        //else, go to next player
        else{
            goToNextPlayer();
        }
    }

    /**
     * Gets the Play Here button event handler
     * @return Play here EventHandler
     */
    public EventHandler<ActionEvent> getPLAY_DOMINO_LISTENER() {
        return PLAY_DOMINO_LISTENER;
    }

    /**
     * Gets the Flip button event handler
     * @return Flip EventHandler
     */
    public EventHandler<ActionEvent> getFLIP_DOMINO_LISTENER() {
        return FLIP_DOMINO_LISTENER;
    }

    /**
     * Gets the Draw button event handler
     * @return Draw EventHandler
     */
    public EventHandler<ActionEvent> getDRAW_DOMINO_LISTENER() {
        return DRAW_DOMINO_LISTENER;
    }

    /**
     * Gets the Open Train button event handler
     * @return Open Train EventHandler
     */
    public EventHandler<ActionEvent> getOPEN_TRAIN_LISTENER() {
        return OPEN_TRAIN_LISTENER;
    }
}
