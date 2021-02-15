import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Controls the flow of rounds and whose turn it is
 */
public class Controller {

    private final Scanner SCANNER;
    //is the indices of the double dominoes 9,8,7,...,1,0 in a brand new Boneyard
    private final int[] INDICES_OF_DOUBLE_DOM_IN_NEW_BONEYARD = {54,52,49,45,40,34,27,19,10,0};
    //private Table table;

    public Controller(){
        SCANNER = new Scanner(System.in);
    }

    /**
     * This plays through all the rounds until the game is over
     * @param players : The list of players
     * @param dominoesPerHand : Number of dominoes per hand, based off number of players
     * @param numberOfHumans : Number of humans in the players list
     */
    public void playRounds(ArrayList<Player> players, int dominoesPerHand, int numberOfHumans) {

        //then, for each round
        int roundCounter = 1;
        for (int index : INDICES_OF_DOUBLE_DOM_IN_NEW_BONEYARD) {
            System.out.println("\nNew round! Round " + roundCounter + ". Ready? Fight!");
            roundCounter++;
            /*Construct a BoneYard
            A BoneYard populates the 55 dominoes and holds the dominoes that are not in play*/
            Boneyard boneyard = new Boneyard();
            /*get the round's double domino from boneyard and build new table with it
            a.k.a. a new board with new player trains and new center domino. Must do this first to draw the
            center domino from the boneyard before randomly drawing hands
            Takes the center domino and number of players, which is number of player trains*/
            Table table = new Table(boneyard.drawParticularDoubleDomino(index), players.size(), numberOfHumans);
            /*---------------Draw the players.size() number of initial hands-----------
            give each player a hand of dominoes drawn randomly*/
            for (Player player : players) {
                player.setHand(new LinkedList<>());//initialize a new hand
                int dominoesInHandCounter = 0;
                while (dominoesInHandCounter < dominoesPerHand) {
                    Domino drawn = boneyard.drawDomino();
                    player.getHand().add(drawn);//draw the dominoes from the boneyard
                    dominoesInHandCounter++;
                }
                //if this a computer player, we have its hand now, so calculate its best combo
                if (player.getHUMAN_OR_COMPUTER() == 'c') {
                    player.computeBestCombo();
                }
            }
            /*by here we have players.size number of players and they all have hands drawn from the boneyard
            and the table is ready with the necessary trains ready*/
            boolean thereArePlaysToMake = true;
            //while this round is still going
            while (thereArePlaysToMake) {
                //for each players turn
                for (int i = 0; i < players.size(); i++) {
                    //print the table for each player's turn
                    Player currentPlayer = players.get(i);
                    currentPlayer.setThePlayerJustDraw(false);//start of turn, player did not just draw
                    //to keep from doing the small amount of work that would get done in here even after the round ends
                    if (thereArePlaysToMake) {
                        //here, this is not a player's second turn
                        table.printTable(players,false);
                        //if this is a human's turn
                        if (currentPlayer.getHUMAN_OR_COMPUTER() == 'h') {
                        /*the player gets a chance to play, if cannot, then must draw from boneyard. If after drawing
                        from boneyard, human cannot play, then it is the next players turn. So, attemptCount tracks
                        this*/
                            boolean secondTurnCusDoublePlayed = false;
                            int attemptCount = 0;
                            //while the user has a play to make/hasn't given valid input
                            while (attemptCount < 2) {
                                /*Lets check if the player has a move to make to control the options which are offered
                                 to them. If possiblePlays comes back with length 0, then player has no plays. Else,
                                 it comes back with the dominoes from currentPlayers hand that can be played*/
                                List<Domino> possiblePlays = table.doesHumanHaveAPlayToMake(i, currentPlayer,
                                                                                        secondTurnCusDoublePlayed);

                                /*Now I know if the human has a play to make. If so, include the [p] option, exclude the
                                [d] option. If not, exclude [p] and include [d]*/
                                String optionsForUser = possiblePlays.size() > 0 ?
                                        String.format("Human %d's Turn\n[p] play domino\n" +
                                                "[q] quit\n", i + 1) :
                                        String.format("Human %d's Turn\n" + "You have no move to make\n" +
                                                "[d] draw from boneyard\n[q] quit\n%s\n", i + 1,
                                                ("Hand: " + currentPlayer.getHandAsText()));

                                 /*if after drawing, they still do not have a domino to play, they must open their train
                                 and pass*/
                                if (attemptCount == 1 && possiblePlays.size() == 0) {

                                    String open = "";
                                    //if the train is not already open
                                    if(!table.getOPEN_TRAINS()[i]){
                                        System.out.printf("\nNo play to make.\n[o] open your train.\n%s\n",
                                                ("Hand: " + currentPlayer.getHandAsText()));
                                        open = SCANNER.next();
                                    }
                                    //else it is already open, so just give the String open a letter
                                    else{
                                        open = "o";
                                    }
                                    /*if human gave correct letter to open train or human's train is already
                                    open               */
                                    if (open.charAt(0) == 'o' || open.charAt(0) == 'O'){
                                        attemptCount++;//they chose open, so let the while loop exit
                                        table.getOPEN_TRAINS()[i] = true;
                                        /*mark that this player has no play to make. Used for checking that a round has
                                        ended by no one having a play to make    */
                                        currentPlayer.setHasNoPlayToMake(true);
                                        //check if all the players are out of moves
                                        boolean endRound = true;//assume round is over
                                        for (Player player : players) {
                                            //if they all have no play to make then, all trues = true
                                            endRound &= player.isHasNoPlayToMake();
                                        }
                                        /*so if endRound = true, I want thereArePlaysToMake to = false. If endRound
                                        = false then I want thereArePlaysToMake = true, so  */
                                        thereArePlaysToMake = !endRound;
                                    } else {
                                        System.out.print("I said, you have ");//invalid open train input
                                    }
                                }
                                //else they get their options
                                else {
                                    System.out.print(optionsForUser);

                                    boolean invalidOption = true;//assume invalid input given by user
                                    //while the input is invalid. Keeps the user here while they give bogus input
                                    while (invalidOption) {
                                        String humansChoice = SCANNER.next();
                                        //this allows for typos, but keeps user here til they give an appropriate input
                                        switch (humansChoice.charAt(0)) {
                                            //case 113 = q
                                            case 113: {
                                                //verify user wants to exit
                                                System.out.print("You sure you want to exit? [y/n] ");
                                                humansChoice = SCANNER.next();
                                                if (humansChoice.charAt(0) == 'y') {
                                                    System.exit(1);
                                                }
                                            }
                                            //case 100 = d
                                            case 100: {
                                                //can only draw when there is no play that can be made and haven't drawn
                                                if (possiblePlays.size() == 0
                                                        && !currentPlayer.isThePlayerJustDraw()) {
                                                    invalidOption = false;
                                                    //they're making a valid attempt to play
                                                    attemptCount++;

                                                    /*if this is their first time drawing, implied by else above
                                                    draw a random domino to the players hand
                                                    Can only draw if the boneyard is not empty*/
                                                    if (boneyard.getBoneyard().size() > 0) {
                                                        Domino toHand = boneyard.drawDomino();
                                                        currentPlayer.getHand().add(toHand);
                                                        //print the dom that was drawn
                                                        System.out.println("Drawn: " + toHand.getDominoAsText());
                                                        //here this is not a second turn cuz double played
                                                        table.printTable(players, false);

                                                        currentPlayer.setThePlayerJustDraw(true);
                                                    }
                                                    //else the boneyard is empty or the player drew already
                                                    else{
                                                        System.out.println("Boneyard is empty.");
                                                        //exit while loop
                                                        attemptCount = 2;

                                                        //check if round should end
                                                        currentPlayer.setHasNoPlayToMake(true);
                                                        //check if all the players are out of moves
                                                        boolean endRound = true;//assume round is over
                                                        for (Player player : players) {
                                                            //if they all have no play to make then, all trues = true
                                                            endRound &= player.isHasNoPlayToMake();
                                                        }
                                                        /*so if endRound = true, I want thereArePlaysToMake to =
                                                        false. If endRound  = false then I want thereArePlaysToMake
                                                        = true, so  */
                                                        thereArePlaysToMake = !endRound;
                                                    }
                                                }
                                                else {
                                                    /*else the user gave 'd' even though it was not an option*/
                                                    if(possiblePlays.size() == 0) {
                                                        System.out.println("You cannot draw. You have a valid play!" +
                                                                "\n[p] play domino\n[q] quit");
                                                    }
                                                    /* or they are trying to draw when they already have. This may
                                                    not occur, but just in case */
                                                    else{
                                                        System.out.println("You've already drawn.");
                                                        //exit while loop
                                                        attemptCount = 2;
                                                    }
                                                }
                                                break;
                                            }
                                            //case 112 = p
                                            case 112: {
                                            /*can only make a play when there are matching dominoes.
                                            This is just to verify that the user did not enter p even tho it
                                            was not presented in the options*/
                                                if (possiblePlays.size() > 0) {
                                                    invalidOption = false;
                                                    //if the player has played their last domino, thereArePlaysToMake = false
                                                    boolean[] results = whichDomino(currentPlayer, possiblePlays, i,
                                                            players,secondTurnCusDoublePlayed, table);
                                                    thereArePlaysToMake = results[0];
                                                    //if a double was played and it was not the last domino in the hand
                                                    if (thereArePlaysToMake && results[1]) {
                                                        //inform user but don't exit by not incrementing attemptCount
                                                        System.out.println("You've played a double and get to" +
                                                                        " go again.\n");
                                                        secondTurnCusDoublePlayed = true;
                                                        //here this is a second turn cuz double played
                                                        table.printTable(players,true);
                                                    }
                                                    //else the human used last hand domino or did not play a double
                                                    else {
                                                        /*only makes it back to here after playing domino, so
                                                        force while loop exit*/
                                                        attemptCount = 2;
                                                        /*if in here then the human played a domino so need to at
                                                        least allow all players another chance to play if any of them
                                                        have no play to make prior  */
                                                        for(Player player : players){
                                                            player.setHasNoPlayToMake(false);
                                                        }
                                                    }
                                                }
                                                break;
                                            }
                                            default: {
                                                System.out.println(humansChoice + " is invalid. Try again.");
                                            }
                                        }
                                    }

                                }
                            }
                        }
                        //else this is a computer player
                        else {
                            //while the computer has played a double and not the last domino in their hand
                            boolean doublePlayed = true;//assume comp plays a double
                            boolean secondTurnCuzDouble = false;
                            while (doublePlayed && thereArePlaysToMake) {
                                boolean[] results = table.computerAttemptsAPlay(currentPlayer, i, boneyard, players,
                                        secondTurnCuzDouble);
                                //if the computer played their last domino, then end round
                                thereArePlaysToMake = results[0];
                                //only stays true if the comp plays a double
                                doublePlayed = results[1];
                                secondTurnCuzDouble = results[1];
                            }
                        }
                    }

                }
            }
            /*if here, a round has ended, so tally up the players scores and add them to their totals */
            System.out.println("End of Round " + (roundCounter-1));
            for (Player player : players) {
                player.setPlayerScore();
                //print the scores so far
                int playerNumber = (player.getHUMAN_OR_COMPUTER() == 'c') ?
                        player.getPLAYER_NUMBER() - numberOfHumans : player.getPLAYER_NUMBER();
                System.out.printf("%s%d's score: %d\n", player.getID(), playerNumber,player.getPlayerScore());
            }
        }
    }

    /**
     * Asks a human player which Domino they want to play and allows the play to be attempted
     * if all checks out with choosing the Domino
     * @param currentPlayer Player
     * @param possiblePlays possiblePlays
     * @param playerNumber int
     * @param players ArrayList<Player>
     * @param secondTurnCusDoublePlayed is true if this is a human's 2nd turn
     * @param table is the current rounds table
     * @return boolean[] will have {if the round should end, if the player played a double}
     */
    public boolean[] whichDomino(Player currentPlayer, List<Domino> possiblePlays, int playerNumber,
                                 ArrayList<Player> players, boolean secondTurnCusDoublePlayed, Table table){
        boolean invalidPlay = true; //assume the number given is invalid
        boolean[] results = null;
        boolean thereArePlaysToMake = true;
        while(invalidPlay){
            String messageToHuman = "";
            System.out.println("Which domino?\nHand: " + currentPlayer.getHandAsText());
            String dominoIndex = SCANNER.next();
            //ascii subtraction to get to the right decimal as long as they didnt give an input longer than 2 chars
            try {
                int dominosIndex = Integer.parseInt(dominoIndex);
                //if the input is in the hands domain
                if(dominosIndex > -1 && dominosIndex < currentPlayer.getHand().size()){
                    //if the chosen domino has a match somewhere
                    if(possiblePlays.contains(currentPlayer.getHand().get(dominosIndex))) {
                    /*if the play breaks down any where because of invalid input,
                      then false is returned to remain in the while
                      loop. If we are inside while(validPlay), then we know the human
                      has a play they can make. If the play is valid, then the play is made by the method
                      inside this if-statement below and true is returned to enter this if statement*/
                        results = table.humanAttemptsPlay(currentPlayer,dominosIndex, playerNumber,players,
                                                            secondTurnCusDoublePlayed);
                        if(results[0]){
                            invalidPlay = false;//play was successful, exit while loop
                            //check if the player emptied their hand
                            if(currentPlayer.getHand().size() == 0){
                                results[0] = false;//then the round should end, overwrite playSuccessful
                            }
                        /*if in here, then a human made a play, so need to give all the players who may not have
                        previously had no play to make to try and make a play again            */
                            for(Player player : players){
                                player.setHasNoPlayToMake(false);
                            }
                        }
                    }
                    else{
                        messageToHuman =
                                String.format("%s cannot be played. " +
                                        "Try again.", currentPlayer.getHand().get(dominosIndex).getDominoAsText());
                    }
                }
                else{
                    messageToHuman = String.format("%d is not within your hand's domain." +
                            "Try again.", dominosIndex);
                }
                //is just a println if successful play made. Success of play is reported in humanAttemptsPlay
                System.out.println(messageToHuman);
            }
            catch (NumberFormatException e){
                System.out.println("That's not a number! Try again.");
                table.printTable(players,secondTurnCusDoublePlayed);
            }

        }
        /*if the human played their last domino then results[0] = false. If player played a double then
        results[1] = true*/
        return results;
    }
}
