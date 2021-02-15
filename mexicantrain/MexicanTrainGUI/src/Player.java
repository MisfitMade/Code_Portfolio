import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Is the player object. Can be a human or computer
 */
public class Player {

    private LinkedList<Domino> hand;//the player's hand
    private final Character HUMAN_OR_COMPUTER;//to differentiate between computer and human
    private List<Domino> bestCombo;//the computer's best combo
    private final int PLAYER_NUMBER;//the number the player is, from 1, up to 4.
    private SimpleStringProperty playerScore;//the sum total of the dots left after every round
    private boolean hasNoPlayToMake;//if true, the player has no move to make, even after drawing from boneyard
    private final String ID;//either Computer or Human
    private ArrayList<ArrayList<ArrayList<Domino>>> adjacencyLists;//used for DFS to find best combo
    private VBox playersTray;//the player's tray graphic
    private ToggleGroup trayButtons;//the toggle group of the player's tray
    private boolean secondTurnCusDoublePlayed;//says whether a player just played a double or not
    private boolean thePlayerJustDraw;//says whether the played just drew or not

    /**
     * Constructor for Player
     * @param humanOrComp char : 'c' or 'h'
     * @param PLAYER_NUMBER int : 1 - 4
     */
    public Player(char humanOrComp, int PLAYER_NUMBER) {
        this.PLAYER_NUMBER = PLAYER_NUMBER;
        this.HUMAN_OR_COMPUTER = humanOrComp;
        playerScore = new SimpleStringProperty("0");
        hasNoPlayToMake = false;
        secondTurnCusDoublePlayed = false;
        thePlayerJustDraw = false;
        //makingMove = false;

        if(HUMAN_OR_COMPUTER == 'c'){
            ID = "Computer";
        }
        else{
            ID = "Human";
        }
    }

    /**
     * Computes the best combo in the player's hand
     * This is for the computer only. Will be leftToLeft, leftToRight, rightToRight, rightToLeft
     * computes the adjacency lists for left domino sides and right then finds the best combo*
     */
    public void computeBestCombo(){
        bestCombo = new ArrayList<>();
        adjacencyLists = new ArrayList<>();
        /*will be hand.size's number of rows in each list inside adjacency list,
        such that row 0 is index 0 of comps hand, row 1 is index 1 of comps hand...

        initialize the adjacency lists: leftToLeft, leftToRight, rightToRight, rightToLeft*/
        for(int i = 0; i < 4; i++){
            adjacencyLists.add(new ArrayList<>());
        }
        for(int i = 0; i < hand.size(); i++){
            /*add a new list for each domino in the comps hand to each of
             leftToLeft, leftToRight, rightToRight, rightToLeft */
            for(int j = 0; j < 4; j++){
                adjacencyLists.get(j).add(new ArrayList<>());
            }
            //for all the dominoes in the comp's hand
            for(Domino potentialNeighbor : hand){
                //as long as it is not the domino to which we are assigning neighbors
                if(hand.get(i) != potentialNeighbor){
                    //if a left to left match is found
                    if(hand.get(i).getLeft() == potentialNeighbor.getLeft()){
                        adjacencyLists.get(0).get(i).add(potentialNeighbor);
                    }
                    //if a left to right match is found
                    if(hand.get(i).getLeft() == potentialNeighbor.getRight()){
                        adjacencyLists.get(1).get(i).add(potentialNeighbor);
                    }
                    //if a right to right match is found
                    if(hand.get(i).getRight() == potentialNeighbor.getRight()){
                        adjacencyLists.get(2).get(i).add(potentialNeighbor);
                    }
                    //if a right to left match is found
                    if(hand.get(i).getRight() == potentialNeighbor.getLeft()){
                        adjacencyLists.get(3).get(i).add(potentialNeighbor);
                    }
                }
            }
        }
        //now I have the adjacency lists. Use them to find the longest combo
        for(int i = 0; i < 4; i++){
            /*for all of the adjacency lists, leftToLeft, leftToRight, rightToRight, rightToLeft*/
            for(int j = 0; j < adjacencyLists.get(i).size(); j++){
                //for each list of neighbors
                for(int k = 0; k < adjacencyLists.get(i).get(j).size(); k++){
                    //for all the neighbors
                    LinkedList<Domino> combo = new LinkedList<>();
                    Domino dominoInHand = hand.get(j);
                    dominoInHand.setVisited(true);
                    combo.add(dominoInHand);
                    Domino neighbor = adjacencyLists.get(i).get(j).get(k);
                    neighbor.setVisited(true);
                    combo.add(neighbor);
                    /*Now, if this is leftToLeft or rightToLeft list, this means that neighbor is connecting to
                    dominoInHand using its left side, and so only neighbor's right side is available for making a
                    connection, thus must only search the lists which connect neighbor's right to something:
                    rightToRight, rightToLeft*/
                    if(i == 0 || i == 3){
                        dfsRightSide(hand.indexOf(neighbor),combo);
                    }
                    /*Same for if this is a leftToRight or rightToRight list, then this means that neighbor is using
                    its right side to connect to dominoInHand, meaning only its left side is available for making
                    a connection, so that is lists: leftToLeft and leftToRight*/
                    if(i == 1 || i == 2){
                        dfsLeftSide(hand.indexOf(neighbor),combo);
                    }
                    //reset for next iteration
                    neighbor.setVisited(false);
                    dominoInHand.setVisited(false);
                }
            }
        }
    }

    /**
     * Does modified DFS on both the rightToRight and rightToLeft adjacency lists
     * @param indexOfMostRecentlyAddedDomino int
     * @param combo LinkedList<Domino>: the local combo accumulator
     */
    public void dfsRightSide(int indexOfMostRecentlyAddedDomino, LinkedList<Domino> combo){
        /*indexOfMostRecentlyAddedDomino is the index in the hand and the adjacency lists of the last
        domino on the end of combo

        needed for base case*/
        boolean noMoreNeighbors = true;//assume base case has been met
        //for all the rightToRight neighbors
        List<Domino> neighbors = adjacencyLists.get(2).get(indexOfMostRecentlyAddedDomino);
        for (Domino domino : neighbors) {
            //and with all the neighbor's visited values. If any of them have not been visited, noMoreNeighbors = false
            noMoreNeighbors &= domino.isVisited();
        }
        //for all the rightToLeft Neighbors
        neighbors = adjacencyLists.get(3).get(indexOfMostRecentlyAddedDomino);
        for (Domino domino : neighbors) {
            //and with all the neighbor's visited values. If any of them have not been visited, noMoreNeighbors = false
            noMoreNeighbors &= domino.isVisited();
        }
        /*if all the neighbors have been visited or there are no neighbors or any combo of both, then noMoreNeighbors
        = true */
        if(noMoreNeighbors){
            /*if this is a new larger combo || this is a combo of the same dot number and combo uses more dominoes than
            bestCombo*/
            if(sumListOfDominoes(combo) > sumListOfDominoes(bestCombo) ||
                    (sumListOfDominoes(combo) == sumListOfDominoes(bestCombo) &&
                            combo.size() > bestCombo.size()) ){
                //then make this the new best combo
                bestCombo = combo;
            }
            return;//no matter what, return here.
        }
        /*Otherwise, for this domino's neighbors in the rightToRight and rightToLeft adjacency list*/
        for(int i = 2; i < 4; i++){
            neighbors = adjacencyLists.get(i).get(indexOfMostRecentlyAddedDomino);
            for (Domino neighbor : neighbors) {
                //if the neighbor has not been visited yet
                if (!neighbor.isVisited()) {
                    //make new list to keep from mutating the original
                    LinkedList<Domino> newCombo = new LinkedList<>(combo);
                    newCombo.add(neighbor);
                    neighbor.setVisited(true);
                    /*if doing a rightToRight connection, this means must try to make a connection with the newest
                    domino on its left*/
                    if (i == 2) {
                        dfsLeftSide(hand.indexOf(neighbor), newCombo);
                    }
                    /*if doing a rightToLeft connection, this means must try to make a connection with the newest
                    domino on its right*/
                    if (i == 3) {
                        dfsRightSide(hand.indexOf(neighbor), newCombo);
                    }
                    //reset for future recurses
                    neighbor.setVisited(false);
                }
            }
        }
    }

    /**
     * Does modified DFS on both the leftToLeft and leftToRight adjacency lists.
     * @param indexOfMostRecentlyAddedDomino int
     * @param combo LinkedList<Domino>: the local combo accumulator
     */
    public void dfsLeftSide(int indexOfMostRecentlyAddedDomino, LinkedList<Domino> combo) {
      /*indexOfMostRecentlyAddedDomino is the index in the hand and the adjacency lists of the last
        domino on the end of combo

        needed for base case*/
        boolean noMoreNeighbors = true;//assume base case has been met
        //for all the leftToLeft neighbors
        List<Domino> neighbors = adjacencyLists.get(0).get(indexOfMostRecentlyAddedDomino);
        for (Domino domino : neighbors) {
            //and with all the neighbor's visited values. If any of them have not been visited, noMoreNeighbors = false
            noMoreNeighbors &= domino.isVisited();
        }
        //for all the leftToRight Neighbors
        neighbors = adjacencyLists.get(1).get(indexOfMostRecentlyAddedDomino);
        for (Domino domino : neighbors) {
            //and with all the neighbor's visited values. If any of them have not been visited, noMoreNeighbors = false
            noMoreNeighbors &= domino.isVisited();
        }
        /*base case: if all the neighbors have been visited or there are no neighbors or any combo of both,
        then noMoreNeighbors = true */
        if(noMoreNeighbors){
            /*if this is a new larger combo || this is a combo of the same dot number and combo uses more dominoes than
            bestCombo*/
            if(sumListOfDominoes(combo) > sumListOfDominoes(bestCombo) ||
                    (sumListOfDominoes(combo) == sumListOfDominoes(bestCombo) &&
                            combo.size() > bestCombo.size()) ){
                //then make this the new best combo
                bestCombo = combo;
            }
            return;//no matter what, return here.
        }

        //Otherwise, for this domino's neighbors in the leftToLeft and leftToRight adjacency list
        for (int i = 0; i < 2; i++) {
            neighbors = adjacencyLists.get(i).get(indexOfMostRecentlyAddedDomino);
            for (Domino neighbor : neighbors) {
                //if the neighbor has not been visited yet
                if (!neighbor.isVisited()) {
                    //make new list to keep from mutating the original
                    LinkedList<Domino> newCombo = new LinkedList<>(combo);
                    newCombo.add(neighbor);
                    neighbor.setVisited(true);
                    /*if doing a leftToLeft connection, this means must try to make a connection with the newest
                    domino on its right*/
                    if (i == 0) {
                        dfsRightSide(hand.indexOf(neighbor), newCombo);
                    }
                    /*if doing a leftToRight connection, this means must try to make a connection with the newest
                    domino on its left*/
                    if (i == 1) {
                        dfsLeftSide(hand.indexOf(neighbor),newCombo);
                    }
                    //reset for future recurses
                    neighbor.setVisited(false);
                }
            }
        }
    }

    /**
     * Returns the total number of dots in list of dominoes
     * @param dominoes List<Domino>
     * @return int
     */
    public int sumListOfDominoes(List<Domino> dominoes){
        int sum = 0;
        for(Domino domino : dominoes){
            sum += domino.getTOTAL_DOTS();
        }
        return sum;
    }

    /**
     * Setter for hand. Just used to initialize a new hand at the start
     * of the game and between rounds
     * @param hand new LinkedList<Domino>
     */
    public void setHand(LinkedList<Domino> hand) {
        this.hand = hand;
        //toggle group for the dominos in the tray
        trayButtons = new ToggleGroup();
        playersTray = new VBox(5);
        playersTray.getChildren().addAll(new HBox(3),new HBox(3));
    }

    /**
     * Adds a domino to the hand in memory and the tray
     * @param dominoToAdd : the domino being added to the player's hand
     */
    public void addToHand(Domino dominoToAdd){
        //add to hand
        hand.add(dominoToAdd);

        //add to tray graphic
        ToggleButton toTray = new ToggleButton();
        toTray.setGraphic(dominoToAdd.getDOMINO_AS_GRAPHIC());
        toTray.setToggleGroup(trayButtons);
        toTray.setSelected(false);
        //if the tray is longer than 15, add to row 2
        if(hand.size() > 10){
            HBox row2 = (HBox) playersTray.getChildren().get(1);
            row2.getChildren().add(toTray);
        }
        //else, row 1
        else{
            HBox row1 = (HBox) playersTray.getChildren().get(0);
            row1.getChildren().add(toTray);
        }
    }

    /**
     * Removes a domino from the players hand to be placed on the table.
     * @param indexToRemove int
     * @return Domino from the player's hand
     */
    public Domino removeDominoFromHand(int indexToRemove){
        //remove from hand
        Domino toRemove = hand.remove(indexToRemove);
        //figure out where this toggle is
        if(indexToRemove > 9){
            //then we are in row2
            HBox row2 = (HBox) playersTray.getChildren().get(1);
            ToggleButton removed = (ToggleButton) row2.getChildren().remove(indexToRemove-10);
            //pull it from the toggle group
            removed.setToggleGroup(null);
        }
        //else it is in row 1
        else{
            HBox row1 = (HBox) playersTray.getChildren().get(0);
            ToggleButton removed = (ToggleButton) row1.getChildren().remove(indexToRemove);
            //pull it from the toggle group
            removed.setToggleGroup(null);
            //if removing from the first row and there is a second row, re-organize hand
            HBox row2 = (HBox) playersTray.getChildren().get(1);
            if(row2.getChildren().size() > 0){
                //re-organize the player's hand
                row1.getChildren().add(row2.getChildren().remove(0));
            }
        }
        return toRemove;
    }

    /**
     * Getter for player number
     * @return int
     */
    public int getPLAYER_NUMBER() {
        return PLAYER_NUMBER;
    }

    /**
     * Getter for 'h' or 'c'
     * @return
     */
    public Character getHUMAN_OR_COMPUTER() {
        return HUMAN_OR_COMPUTER;
    }

    /**
     * Getter for the player's total score
     * @return String
     */
    public String getPlayerScore() {
        return playerScore.get();
    }

    /**
     * Getter for score property
     * @return : The player's score as a SimpleStringProperty
     */
    public SimpleStringProperty playerScoreProperty() {
        return playerScore;
    }

    /**
     * Counts the dots left in the player's hand and adds it to playerScore
     */
    public void setPlayerScore() {
        int roundScore = sumListOfDominoes(this.hand);

        playerScore.set(Integer.toString(Integer.parseInt(playerScore.getValue()) + roundScore));
    }

    /**
     * Getter for if a player has a move to make
     * @return boolean
     */
    public boolean isHasNoPlayToMake() {
        return hasNoPlayToMake;
    }

    /**
     * Sets the hasNoPlayToMake boolean
     * @param hasNoPlayToMake boolean
     */
    public void setHasNoPlayToMake(boolean hasNoPlayToMake) {
        this.hasNoPlayToMake = hasNoPlayToMake;
    }

    /**
     * Getter for the player's ID, 'Computer' or 'Human'
     * @return String
     */
    public String getID() {
        return ID;
    }

    /**
     * Gets the toggle group that is a human's tray of buttons
     * @return ToggleGroup
     */
    public ToggleGroup getTrayButtons() {
        return trayButtons;
    }

    /**
     * Gets whether this is a player's second turn cuz double played
     * @return boolean
     */
    public boolean isSecondTurnCusDoublePlayed() {
        return secondTurnCusDoublePlayed;
    }

    /**
     * Sets the boolean that represents whether a player just played a double
     * @param secondTurnCusDoublePlayed boolean
     */
    public void setSecondTurnCusDoublePlayed(boolean secondTurnCusDoublePlayed) {
        this.secondTurnCusDoublePlayed = secondTurnCusDoublePlayed;
    }

    /**
     * Returns whether the player just drew or not
     * @return boolean
     */
    public boolean didThePlayerJustDraw() {
        return thePlayerJustDraw;
    }

    /**
     * Sets wheter the player just drew or not
     * @param thePlayerJustDraw boolean
     */
    public void setThePlayerJustDraw(boolean thePlayerJustDraw) {
        this.thePlayerJustDraw = thePlayerJustDraw;
    }

    /** Returns the tray graphic
     * @return The tray graphic with the dominoes as toggle buttons
     */
    public VBox getPlayersTray(){
        return playersTray;
    }

    /**
     * Getter for bestCombo
     * @return List<Domino>
     */
    public List<Domino> getBestCombo() {
        return bestCombo;
    }

    /**
     * Getter for the player's hand
     * @return LinkedList<Domino>
     */
    public LinkedList<Domino> getHand() {
        return hand;
    }

}
