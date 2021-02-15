import java.util.*;

public class Table {

    private final int NUMBER_OF_PLAYERS;
    private final int NUMBER_OF_HUMANS;
    //signals which of the players trains are open: {Player 1, Player 2, Player 3, Player 4}
    private final boolean[] OPEN_TRAINS;
    private final Queue<Integer> DOUBLE_PLAYED_OPEN_TRAINS;
    //PLAYERS_TRAINS will have Player1's train,Player2's train,... as its elements.
    private final LinkedList<LinkedList<Domino>> PLAYERS_TRAINS;
    //mexican train will have a pointer to it, but it will also be the last list in PLAYERS_TRAINS for easier traversals
    private final LinkedList<Domino> MEXICAN_TRAIN;

    /**
     * The Table Constructor
     * @param centerDomino : The current round's center domino
     * @param NUMBER_OF_PLAYERS : total number of players
     * @param NUMBER_OF_HUMANS : The number of humans
     */
    public Table(Domino centerDomino, int NUMBER_OF_PLAYERS, int NUMBER_OF_HUMANS){
        //initialize the mexican trains and the list which holds the trains
        MEXICAN_TRAIN = new LinkedList<>();
        PLAYERS_TRAINS = new LinkedList<>();
        MEXICAN_TRAIN.add(centerDomino);//add center domino into mexican train
        this.NUMBER_OF_PLAYERS = NUMBER_OF_PLAYERS;
        this.NUMBER_OF_HUMANS = NUMBER_OF_HUMANS;
        /*this will control normal game play for open trains. Number of players + mexican train being the last one*/
        OPEN_TRAINS = new boolean[NUMBER_OF_PLAYERS+1];
        /*this will control when a double is played. As doubles are played, the queue will fill up and by using the
        queue, the doubles that are played consecutively will be forced to be played on in the order that they were
         played*/
        DOUBLE_PLAYED_OPEN_TRAINS = new LinkedList<>();

        //initialize the the number of player trains
        for(int i = 0; i < NUMBER_OF_PLAYERS; i++){
            PLAYERS_TRAINS.add(new LinkedList<>());
            //putting the center domino at the start of all the player's trains for easier conditionals later
            PLAYERS_TRAINS.get(i).add(centerDomino);
            //at first, all player trains are closed and no doubles are played
            OPEN_TRAINS[i] = false;
        }
        //the last element in OPEN_TRAINS will always be the mexican train's openness
        OPEN_TRAINS[NUMBER_OF_PLAYERS] = true;
        //add the mexican train to the PLAYERS_TRAINS list as the last train for easier traversals
        PLAYERS_TRAINS.add(MEXICAN_TRAIN);
    }

    /**
     * This is the method that runs when the computer goes to make a play
     * @param computerPlayer : the computer whose turn it is
     * @param indexOfComputerPlayer : the index of the current computer
     * @param boneyard : The current boneyard
     * @param players : The list of players
     * @param secondTurnCuzDouble : true if this is the computer's 2nd turn
     * @param display : The display object
     * @return boolean[] : {if the round should end, if the comp played a double}
     */
    public boolean[] computerAttemptsAPlay(Player computerPlayer, int indexOfComputerPlayer, Boneyard boneyard,
                                           ArrayList<Player> players, boolean secondTurnCuzDouble,
                                           Display display){

        Domino toPlay = null;
        //pointers
        LinkedList<Domino> computerHand = computerPlayer.getHand();
        List<Domino> bestCombo = computerPlayer.getBestCombo();
        //if there is a best combo
        Domino firstInCombo = null;

        if(bestCombo.size() > 0){
            firstInCombo = bestCombo.get(0);
        }

        //assume the computer does not play its last domino and does not play a double
        boolean[] keepPlayingRound_doublePlayed = {true, false};
        //first find if a double domino is in play. If so, must consider DOUBLE_PLAYED_OPEN_TRAINS
        boolean doubleNotInPlay = isNotDoubleDomino();

        int attemptCount = 0;

        //while the computer has either not played, or then drawn and tried to play and still could not
        while(attemptCount < 2) {
            String messageToUser = "";
            /*find the best domino to play out of all the open trains. If openness = OPEN_TRAINS[0], then there is not
            a double domino that must be played on. If openness = OPEN_TRAINS[1], then there is only one open train: the
            train with the double domino played on it*/
            int bestHandIndex = -1;
            int bestTrainIndex = -1;
            int bestTotalDots = -1;
            /*if it is this computer's train is open, it really wants to play on it before looking
             at the other options */
            if(doubleNotInPlay && OPEN_TRAINS[indexOfComputerPlayer]){
                Domino engineOfCompsTrain = PLAYERS_TRAINS.get(indexOfComputerPlayer).getLast();
                //for all the dominoes in the comps hand
                for(int i = 0; i < computerPlayer.getHand().size(); i++) {
                    Domino fromHand = computerPlayer.getHand().get(i);
                    //see if it can play on the comp's train. Try to play not from combo first
                    if(!computerPlayer.getBestCombo().contains(fromHand)){
                        //if it matches and is more dots
                        if(fromHand.getLeft() == engineOfCompsTrain.getRight() ||
                                fromHand.getRight() == engineOfCompsTrain.getRight() &&
                                        fromHand.getTOTAL_DOTS() > bestTotalDots){
                            bestHandIndex = i;
                            bestTrainIndex = indexOfComputerPlayer;
                            bestTotalDots = fromHand.getTOTAL_DOTS();
                            //if it matches by flip
                            if(fromHand.getRight() == engineOfCompsTrain.getRight()){
                                fromHand.rotateDomino(true);
                            }
                        }
                    }
                }
                //if no play was found that is not from the combo, check the combo
                if(bestHandIndex == -1){
                    /*the computer's best combo is played left to right with respect to the bestCombo list.
                    So, if the player can make more than one play from its best combo and one of the plays is the
                    first domino in the best combo list, then force the play of that domino so that the combo does not
                    get screwed up
                    So check that first domino first if there is a best combo.
                    If it can be played without screwing up the combo*/
                    int firstInCombosLeft = - 1;
                    if(firstInCombo != null){
                        firstInCombosLeft = firstInCombo.getLeft();
                    }
                    if(firstInCombosLeft > -1 && firstInCombo.getLeft() == engineOfCompsTrain.getRight()){
                        bestHandIndex = computerPlayer.getHand().indexOf(firstInCombo);
                        bestTrainIndex = indexOfComputerPlayer;
                    }
                    //if it cannot be played this way, find any play that works from the combo
                    if(bestHandIndex == -1){
                        for(int i = 0; i < computerPlayer.getBestCombo().size(); i++){
                            Domino fromBestCombo = computerPlayer.getBestCombo().get(i);
                            //if it matches without a flip and is more dots
                            if(fromBestCombo.getLeft() == engineOfCompsTrain.getRight() ||
                                    fromBestCombo.getRight() == engineOfCompsTrain.getRight() &&
                                            fromBestCombo.getTOTAL_DOTS() > bestTotalDots){
                                bestHandIndex = computerPlayer.getHand().indexOf(fromBestCombo);
                                bestTrainIndex = indexOfComputerPlayer;
                                bestTotalDots = fromBestCombo.getTOTAL_DOTS();
                                //if it matches by flip
                                if(fromBestCombo.getRight() == engineOfCompsTrain.getRight()){
                                    fromBestCombo.rotateDomino(true);
                                }
                            }
                        }
                    }
                    /*if we are in here and a domino was chosen and it is not firstInCombo played without rotation
                    then this means i need to recompute best combo cus the combo got screwed up  */
                    if(bestHandIndex > -1) {
                        Domino chosenDomino = computerPlayer.getHand().get(bestHandIndex);
                        //if this domino is not the first in combo domino || it is the first combo domino but rotated
                        if(chosenDomino != firstInCombo || chosenDomino.getRight() == firstInCombosLeft){
                            //remove this domino from hand now to do a recompute of best combo
                            toPlay = computerPlayer.getHand().remove(bestHandIndex);
                            computerPlayer.computeBestCombo();
                        }
                    }
                }
            }
            /*Now here, if bestHandIndex != -1, that means that the computer's train is open and a
            matching domino was found or a double domino is in play. So, if it failed to go into the if above
            because either a double is in play, !doubleNotInPlay, or the computer's train is not open or the computer's
            train is open, but a matching domino was not found, then I want it to go into here.
            If a double is not in play, doubleNotInPlay, but the computer's train is not open, in which case
            bestHandIndex never changes from -1, then computer just wants to find its best play

            so if a double is in play*/
            if(!doubleNotInPlay){
                /*if this is not a double played by this computer and if no double found to play else where
                check for any domino that's not double that matches the oldest double played*/
                if(bestHandIndex == -1 && !secondTurnCuzDouble){
                    //first from hand but not from best combo
                    Domino doubleToPlayOnEngine = PLAYERS_TRAINS.get(DOUBLE_PLAYED_OPEN_TRAINS.peek()).getLast();
                    for(int i = 0; i < computerHand.size(); i++) {
                        Domino fromHand = computerHand.get(i);
                        //if it matches and is more dots and is not from best combo
                        if(!bestCombo.contains(fromHand) &&
                                fromHand.getLeft() == doubleToPlayOnEngine.getRight() ||
                                fromHand.getRight() == doubleToPlayOnEngine.getRight() &&
                                        fromHand.getTOTAL_DOTS() > bestTotalDots){
                            bestHandIndex = i;
                            bestTrainIndex = DOUBLE_PLAYED_OPEN_TRAINS.peek();
                            bestTotalDots = fromHand.getTOTAL_DOTS();
                            //if it matches by flip. A double cannot be played on a double
                            if(fromHand.getRight() == doubleToPlayOnEngine.getRight()){
                                //rotate the domino
                                fromHand.rotateDomino(true);
                            }
                        }
                    }
                    //else, no match found so lets check from the combo
                    if(bestHandIndex == -1) {
                        /*first check if the combo can be played without messing up its order onto the computer's own
                        train, if there is a combo. If no combo firstInComboLeft
                        does not = doubleToPlayOnEngine.getRight*/
                        int firstInCombosLeft = -1;
                        if(firstInCombo != null){
                            firstInCombosLeft = firstInCombo.getLeft();
                        }
                        if(firstInCombosLeft == doubleToPlayOnEngine.getRight() &&
                                DOUBLE_PLAYED_OPEN_TRAINS.peek() == indexOfComputerPlayer) {
                            bestHandIndex = computerHand.indexOf(firstInCombo);
                            bestTrainIndex = indexOfComputerPlayer;
                        }
                        //else, best combo is pretty much toast
                        else {
                            //check the whole best combo for a match on the double that must be played on
                            for (Domino fromBestCombo : bestCombo) {
                                //if this domino matches and is more dots
                                if (fromBestCombo.getRight() == doubleToPlayOnEngine.getRight() ||
                                        fromBestCombo.getLeft() == doubleToPlayOnEngine.getRight() &&
                                                fromBestCombo.getTOTAL_DOTS() > bestTotalDots) {
                                    bestHandIndex = computerHand.indexOf(fromBestCombo);
                                    bestTrainIndex = DOUBLE_PLAYED_OPEN_TRAINS.peek();
                                    bestTotalDots = fromBestCombo.getTOTAL_DOTS();
                                    //if it matches by flip
                                    if (fromBestCombo.getRight() == doubleToPlayOnEngine.getRight()) {
                                        fromBestCombo.rotateDomino(true);
                                    }
                                }
                            }
                            /*if a match was found, then the best combo is screwed up, so pull out the domino from
                            hand and recompute best combo*/
                            if(bestHandIndex > -1) {
                                toPlay = computerHand.remove(bestHandIndex);
                                computerPlayer.computeBestCombo();
                            }
                        }
                    }
                }
                /*else if a double is in play, no double was found to play and this is this player's at least
                2nd turn after playing a double     */
                else if(bestHandIndex == -1 && secondTurnCuzDouble){
                    /*first check for doubles in the computer's hand that do not belong to best combo to see if the
                     computer can chain together some doubles */
                    for(int i = 0; i < computerHand.size(); i++){
                        Domino fromHand = computerHand.get(i);
                        //if this is a double domino not from bestCombo
                        if(fromHand.getRight() == fromHand.getLeft() && !bestCombo.contains(fromHand)){
                            //for the open trains or if it is the computer's own train
                            for(int j = 0; j < OPEN_TRAINS.length; j++){
                                if(OPEN_TRAINS[j] || j == indexOfComputerPlayer){
                                    Domino engineOfTrain = PLAYERS_TRAINS.get(j).getLast();
                                /*if it matches and is more than total dots. Need not check if match by flip
                                cus this is a double               */
                                    if(fromHand.getLeft() == engineOfTrain.getRight() &&
                                            fromHand.getTOTAL_DOTS() > bestTotalDots){
                                        bestHandIndex = i;
                                        bestTrainIndex = j;
                                        bestTotalDots = fromHand.getTOTAL_DOTS();
                                    }
                                }
                            }
                        }
                    }
                    /*Now if no double was found without messing with the bestCombo, check if the computer
                    can play a double while preserving the best combo, meaning the first domino of
                    its bestCombo is a double and can be played onto its own train.
                    Otherwise, don't mess up bestCombo unless forced to further below*/
                    if (bestHandIndex == -1 && firstInCombo != null &&
                            firstInCombo.getLeft() == firstInCombo.getRight() &&
                            firstInCombo.getLeft() == PLAYERS_TRAINS.get(indexOfComputerPlayer).getLast().getRight()){

                        bestHandIndex = computerHand.indexOf(firstInCombo);
                        bestTrainIndex = indexOfComputerPlayer;
                        bestTotalDots = firstInCombo.getTOTAL_DOTS();

                        //no need to recompute best combo cuz it is preserved
                    }
                    //now if no other double was found to play
                    if (bestHandIndex == -1){
                        /*In this case, the eligible trains are the mexican train, any open trains without a double
                        and the train with the oldest played double    */
                        HashMap<Integer, Domino> eligibleTrains = new HashMap<>();
                        //get mexican train engine
                        eligibleTrains.put(NUMBER_OF_PLAYERS, PLAYERS_TRAINS.getLast().getLast());
                        //get the train with the oldest double played. If that is not the mexican train
                        if(DOUBLE_PLAYED_OPEN_TRAINS.peek() != NUMBER_OF_PLAYERS){
                            eligibleTrains.put(DOUBLE_PLAYED_OPEN_TRAINS.peek(),
                                    PLAYERS_TRAINS.get(DOUBLE_PLAYED_OPEN_TRAINS.peek()).getLast());
                        }
                        //get the open trains
                        for(int i = 0; i < OPEN_TRAINS.length; i++){
                        /*if (this is a train that is not already in eligibleTrains) and it is open or this is the
                        computer's own train*/
                            if((i != NUMBER_OF_PLAYERS && i != DOUBLE_PLAYED_OPEN_TRAINS.peek()) && OPEN_TRAINS[i]
                                    || i == indexOfComputerPlayer){
                                eligibleTrains.put(i,PLAYERS_TRAINS.get(i).getLast());
                            }
                        }
                        /*now i have a list of the eligible train engines. Check for matches on dominoes not in
                        the comps best combo, but in the comps hand*/
                        for(int i = 0; i < computerHand.size(); i++){
                            Domino fromHand = computerHand.get(i);
                            //if this is not in the best combo
                            if(!bestCombo.contains(fromHand)){
                                //check the eligible train engines. If a match and it is a higher number of dots
                                for(Domino engine : eligibleTrains.values()){
                                    if(fromHand.getLeft() == engine.getRight() ||
                                            fromHand.getRight() == engine.getRight() &&
                                                    fromHand.getTOTAL_DOTS() > bestTotalDots){
                                        bestHandIndex = i;
                                        bestTrainIndex = getKey(engine,eligibleTrains);
                                        bestTotalDots = fromHand.getTOTAL_DOTS();

                                        //if it matches by flip
                                        if(fromHand.getRight() == engine.getRight()){
                                            fromHand.rotateDomino(true);
                                        }
                                    }
                                }
                            }
                        }
                        //if a play was not found, try using the best combo
                        if(bestHandIndex == -1){
                            //first see if the best combo can be preserved
                            int firstInCombosLeft = -1;
                            if(firstInCombo != null){
                                firstInCombosLeft = firstInCombo.getLeft();
                            }
                            //if it can be played without flip
                            if(firstInCombosLeft == eligibleTrains.get(indexOfComputerPlayer).getRight()) {
                                bestHandIndex = computerHand.indexOf(firstInCombo);
                                bestTrainIndex = indexOfComputerPlayer;
                            }
                            //else the best combo is toast
                            else{
                                //for each domino in the best combo
                                for(int i = 0; i < bestCombo.size(); i++){
                                    Domino fromBestCombo = bestCombo.get(i);
                                    //for each of the eligible engines
                                    for(Domino domino : eligibleTrains.values()){
                                        //if there is a match and it is more dots
                                        if(fromBestCombo.getRight() == domino.getRight() ||
                                                fromBestCombo.getLeft() == domino.getRight() &&
                                                        fromBestCombo.getTOTAL_DOTS() > bestTotalDots){

                                            bestHandIndex = computerHand.indexOf(fromBestCombo);
                                            bestTrainIndex = getKey(domino,eligibleTrains);
                                            bestTotalDots = fromBestCombo.getTOTAL_DOTS();

                                            //if it matches by flip
                                            if(fromBestCombo.getRight() == domino.getRight()){
                                                fromBestCombo.rotateDomino(true);
                                            }
                                        }
                                    }
                                }
                            /*if a match was found, then the best combo is screwed up, so pull out the domino from
                            hand and recompute best combo*/
                                if(bestHandIndex > -1) {
                                    toPlay = computerHand.remove(bestHandIndex);
                                    computerPlayer.computeBestCombo();
                                }
                            }
                        }
                    }
                }
            }
            /*so if a double is not in play, implied by else if,
             and the computer has not already chosen to play on its own train cuz it is open, meaning
             it either went into if(doubleNotInPlay && OPEN_TRAINS[indexOfComputerPlayer]) but found no match or it
             did not go into if(doubleNotInPlay && OPEN_TRAINS[indexOfComputerPlayer]) nor did it go into
             if(doubleNotInPlay)*/
            else if(bestHandIndex == -1) {
                //just find the best play out of the open trains, starting with dominoes not in best combo
                Domino openTrainEngine;
                for(int i = 0; i < computerHand.size(); i++) {
                    Domino fromHand = computerHand.get(i);
                    //if this is not from best combo
                    if(!bestCombo.contains(fromHand)) {
                        //for all the open trains
                        for(int j = 0; j < OPEN_TRAINS.length; j++) {
                            //if the train is open || this is the players train and there is a match and it is more dots
                            if(OPEN_TRAINS[j] || j == indexOfComputerPlayer) {
                                openTrainEngine = PLAYERS_TRAINS.get(j).getLast();
                                if (fromHand.getLeft() == openTrainEngine.getRight() ||
                                        fromHand.getRight() == openTrainEngine.getRight() &&
                                                fromHand.getTOTAL_DOTS() > bestTotalDots) {
                                    bestHandIndex = i;
                                    bestTrainIndex = j;
                                    bestTotalDots = fromHand.getTOTAL_DOTS();

                                    //if it matches by flip
                                    if(fromHand.getRight() == openTrainEngine.getRight()) {
                                        fromHand.rotateDomino(true);
                                    }
                                }
                            }
                        }
                    }
                }
                //if here there is still no match, try using the best combo
                if(bestHandIndex == -1) {
                    //first see if the combo can be preserved

                    /*if the comp can play the first domino of an existing bestCombo on his own train without flipping
                    if best combo does not exist, firstInCombosLeft = -1 */
                    int firstInCombosLeft = -1;
                    if(firstInCombo != null){
                        firstInCombosLeft = firstInCombo.getLeft();
                    }
                    if(firstInCombosLeft == PLAYERS_TRAINS.get(indexOfComputerPlayer).getLast().getRight()) {
                        bestHandIndex = computerHand.indexOf(firstInCombo);
                        bestTrainIndex = indexOfComputerPlayer;
                    }
                    //else this combo is toast
                    else {
                        //do what must be done using the combo
                        for(Domino fromBestCombo : bestCombo) {
                            //for all the open trains or the comps own train
                            for (int j = 0; j < OPEN_TRAINS.length; j++) {
                                if (OPEN_TRAINS[j] || j == indexOfComputerPlayer) {
                                    Domino engineOfOpenTrain = PLAYERS_TRAINS.get(j).getLast();
                                    //if it matches and is more dots
                                    if (fromBestCombo.getRight() == engineOfOpenTrain.getRight() ||
                                            fromBestCombo.getLeft() == engineOfOpenTrain.getRight() &&
                                                    fromBestCombo.getTOTAL_DOTS() > bestTotalDots) {
                                        bestHandIndex = computerHand.indexOf(fromBestCombo);
                                        bestTrainIndex = j;
                                        bestTotalDots = fromBestCombo.getTOTAL_DOTS();
                                        //if it matches by flip
                                        if(fromBestCombo.getRight() == engineOfOpenTrain.getRight()){
                                            fromBestCombo.rotateDomino(true);
                                        }
                                    }
                                }
                            }
                        }
                        /*if we are in here and have found a match, then the best combo got screwed up, so must
                        recompute best combo  */
                        if(bestHandIndex > -1) {
                            toPlay = computerHand.remove(bestHandIndex);
                            computerPlayer.computeBestCombo();
                        }
                    }
                }
            }

            /*by here, we have either found a best match in which case bestHandIndex > -1, or we found no match,
             in which case bestHandIndex = -1
            So, if there is a match*/
            if(bestHandIndex > -1) {
                Player playerToPlayOn = null;
                /*if there is a double domino in play, bestTrainIndex is the index of the train to which the double
                belongs. If not, then bestTrainIndex is the index to the train to which the comp's best play can be
                made. The domino was flipped if it needed to be
                Make the play. Add to train, remove from hand

                If the domino to play was not already taken from best combo or it was the first domino in best combo*/
                if(toPlay == null){
                    toPlay = computerPlayer.removeDominoFromHand(bestHandIndex);
                    //also remove from best combo if toPlay is the first domino in bestCombo
                    bestCombo.remove(toPlay);
                }
                /*else if its already assigned from best combo

                if hand = zero, then keep playing round = false*/
                keepPlayingRound_doublePlayed[0] = computerPlayer.getHand().size() > 0;

                //make the play in memory
                PLAYERS_TRAINS.get(bestTrainIndex).add(toPlay);
                /*make the play on display
                find train index for display*/
                int displayTrain = bestTrainIndex;
                if(bestTrainIndex == NUMBER_OF_PLAYERS){
                    displayTrain = 2;
                }
                /*if this is a player 3 or 4 train, then bestTrainIndex = 2 or 3 and in the display these train indexes
                 are 3 and 4 since the mexican train is in the center   */
                else if(bestTrainIndex > 1){
                    displayTrain++;
                }
                //else bestTrainIndex = 0 or 1, which is already correct for display
                display.playOnTrain(toPlay.createDominoForBoard(),displayTrain);
                //force exit of while loop
                attemptCount = 2;

                //if the computer has played on their own train and it is open, their train now closes
                if(bestTrainIndex == indexOfComputerPlayer){
                    OPEN_TRAINS[indexOfComputerPlayer] = false;
                    display.closeATrain(displayTrain);
                }
                //if play was on mexican train
                if(bestTrainIndex == NUMBER_OF_PLAYERS){
                    messageToUser = String.format("\nComputer%d plays %s on Mexican Train.",
                            computerPlayer.getPLAYER_NUMBER() - NUMBER_OF_HUMANS,toPlay.getDominoAsText());
                }
                //else it is on a player train
                else{
                    playerToPlayOn = players.get(bestTrainIndex);
                    //if it played on his own train
                    if(playerToPlayOn == computerPlayer){
                        messageToUser = String.format("\nComputer%d plays %s on its own train.",
                                computerPlayer.getPLAYER_NUMBER() - NUMBER_OF_HUMANS,toPlay.getDominoAsText());
                    }
                    //else, not on its own train
                    else{
                        //if it played on another computer's train
                        if(playerToPlayOn.getHUMAN_OR_COMPUTER() == 'c'){
                            messageToUser += String.format("\nComputer%d plays %s on Computer%d's train.",
                                    computerPlayer.getPLAYER_NUMBER() - NUMBER_OF_HUMANS,toPlay.getDominoAsText(),
                                    playerToPlayOn.getPLAYER_NUMBER()-NUMBER_OF_HUMANS);
                        }
                        //else it is on a human train
                        else{
                            messageToUser = String.format("\nComputer%d plays %s on Human%d's train.",
                                    computerPlayer.getPLAYER_NUMBER() - NUMBER_OF_HUMANS,toPlay.getDominoAsText(),
                                    playerToPlayOn.getPLAYER_NUMBER());
                        }
                    }
                }
                /*if we are in here and the domino being played is a double */
                if(toPlay.getLeft() == toPlay.getRight()){
                    DOUBLE_PLAYED_OPEN_TRAINS.add(bestTrainIndex);//add this train to must plays
                    //say that this player just played a double and all the other players did not
                    for(int i = 0; i < players.size(); i++){
                        players.get(i).setSecondTurnCusDoublePlayed(i == indexOfComputerPlayer);
                    }
                    //also, make it so that the computer gets to go again
                    keepPlayingRound_doublePlayed[1] = true;
                    messageToUser += "\nThe computer has played a double.";
                }
                //else if a double is in play and this domino is not a double
                else if(!doubleNotInPlay){
                    //then this domino may be breaking a must play on double
                    satisfyDoublePlay(bestTrainIndex);
                    //reset the double played labels
                    display.resetDoublePlayedButtons();
                    if(!isNotDoubleDomino()){
                        display.setDoublePlayedButton(DOUBLE_PLAYED_OPEN_TRAINS.peek());
                    }
                }
                /*else a double is not in play and this domino being played is not a double

                If in here then a play has been made. Need to give all the players who may not have a play to make
                to try and make a play        */
                for(Player playerToSet : players){
                    playerToSet.setHasNoPlayToMake(false);
                }
            }
            //else, no match and this is the first attempt
            else if(attemptCount == 0 && !computerPlayer.didThePlayerJustDraw()){
                /*draw from boneyard, compute new best combo, and increment attempt count
                addFirst for speed. Can only draw from boneyard if it is not empty*/
                int compNumber = computerPlayer.getPLAYER_NUMBER() - NUMBER_OF_HUMANS;
                messageToUser = String.format("\nComputer%d has no play",compNumber);

                //inc the attempt count no matter what
                attemptCount = 1;

                //if they haven't drawn already and there are dominoes in the boneyard
                if(boneyard.getBoneyard().size() > 0){
                    Domino toHand = boneyard.drawDomino();
                    computerPlayer.addToHand(toHand);
                    computerPlayer.computeBestCombo();
                    computerPlayer.setThePlayerJustDraw(true);

                    messageToUser += " and draws from the Boneyard.";
                }
                else{
                    messageToUser += "\nBoneyard is empty.";
                    attemptCount = 2;//force exit

                    //computer has no play
                    computerPlayer.setHasNoPlayToMake(true);
                    boolean endRound = true;
                    for(Player player : players){
                        /*is true if all players have no play to make*/
                        endRound &= player.isHasNoPlayToMake();
                    }
                    /*need keepPlayingRound to = false if endRound = true. Need keepPlayingRound to = true if endRound
                    = false, so  */
                    keepPlayingRound_doublePlayed[0] = !endRound;
                }
            }
            //else no match and the computer has already drawn once and still cannot play
            else {
                int compNumber = computerPlayer.getPLAYER_NUMBER() - NUMBER_OF_HUMANS;
                messageToUser = String.format("\nComputer%d still has no play.",compNumber);

                //open this computer's train and update message to user. If train is open, don't say comp opens train
                if(!OPEN_TRAINS[indexOfComputerPlayer]){
                    messageToUser += String.format("\nComputer%d opens their train.",compNumber);
                    //open train in memory
                    OPEN_TRAINS[indexOfComputerPlayer] = true;

                    //open train in display. find the index of the train in display
                    int displayTrain = (indexOfComputerPlayer > 1) ? indexOfComputerPlayer+1 : indexOfComputerPlayer;
                    display.openATrain(displayTrain);
                }

                attemptCount = 2;//exit while loop
                computerPlayer.setHasNoPlayToMake(true);
                /*need to check if there are no plays to make for all players*/
                boolean endRound = true;
                for(Player player : players){
                    /*is true if all players have no play to make*/
                    endRound &= player.isHasNoPlayToMake();
                }
                /*need keepPlayingRound to = false if endRound = true. Need keepPlayingRound to = true if endRound
                = false, so  */
                keepPlayingRound_doublePlayed[0] = !endRound;
            }
            //update user with computer info
            display.setInfo(messageToUser,true);
        }
        //if computer played its last domino, keepPlayingRound = false
        return keepPlayingRound_doublePlayed;
    }

    /**
     * Determines if the human has a play to make
     * @param playerIndex : index of the player whose turn it is
     * @param currentPlayer : Player whose turn it is
     * @param secondTurnCusDoublePlayed : true if this is the human's 2nd turn cuz they played a double
     * @return List<Domino>: The list of possible plays of currentPlayer
     */
    public List<Domino> doesHumanHaveAPlayToMake(int playerIndex, Player currentPlayer,
                                                 boolean secondTurnCusDoublePlayed){
        List<Domino> possiblePlays = new ArrayList<>();
        //need to find out if a double domino was last played
        boolean doubleNotPlayed = isNotDoubleDomino(); //assume not
        //if a double is not in play or this the player's second turn cuz they played a double
        if(doubleNotPlayed || secondTurnCusDoublePlayed) {
            //for the dominoes in the current players hand
            for (Domino playersDomino : currentPlayer.getHand()) {
                //for all the trains
                for (int j = 0; j < PLAYERS_TRAINS.size(); j++) {
                    //for the trains that are open or the current players
                    if (OPEN_TRAINS[j] || playerIndex == j) {
                        Domino openPlayersEngine = PLAYERS_TRAINS.get(j).getLast();
                        /*if this domino is not already in possiblePlays && there is a match
                        on this train*/
                        if (!possiblePlays.contains(playersDomino) &&
                                playersDomino.getRight() == openPlayersEngine.getRight() ||
                                playersDomino.getLeft() == openPlayersEngine.getRight()) {
                            //save this hand domino for faster comparison later
                            possiblePlays.add(playersDomino);
                        }
                    }
                }
            }
        }
        //else must consider the trains differently since double domino last played
        else {
            //now check the train with a double on it
            int train = DOUBLE_PLAYED_OPEN_TRAINS.peek();
            Domino engine = PLAYERS_TRAINS.get(train).getLast();
            for(Domino playersDomino : currentPlayer.getHand()){
                /*if domino is not already in possible plays and there is a match*/
                if(!possiblePlays.contains(playersDomino) &&
                        playersDomino.getRight() == engine.getRight() ||
                        playersDomino.getLeft() == engine.getRight()){
                    //add this domino to possiblePlays
                    possiblePlays.add(playersDomino);
                }
            }
        }
        //return the possible plays
        return possiblePlays;
    }

    /**
     * Returns the key in map that returns domino. This will be used in a way
     * so that -1 is never returned
     * @param domino : The value of the key we want
     * @param map : The map to search through
     * @return
     */
    public Integer getKey(Domino domino, HashMap<Integer, Domino> map){
        for(Integer key : map.keySet()){
            if(map.get(key).equals(domino)){
                return key;
            }
        }
        return -1;
    }

    /**
     * Returns true if a double domino is not in play
     * @return boolean
     */
    public boolean isNotDoubleDomino(){
        //if is empty, no double in play, so return true
        return DOUBLE_PLAYED_OPEN_TRAINS.isEmpty();
    }

    /**
     * Opens a train in memory
     * @param indexOfTrainToOpen : index in PLAYERS_TRAINS to open
     */
    public void openATrain(int indexOfTrainToOpen){
        OPEN_TRAINS[indexOfTrainToOpen] = true;
    }

    /**
     * Closes a train in memory
     * @param indexOfTrainToClose : index in PLAYERS_TRAINS to close
     */
    public void closeATrain(int indexOfTrainToClose){
        OPEN_TRAINS[indexOfTrainToClose] = false;
    }

    /**
     * Pulls from the doubles played on trains queue
     * @param indexOfTrain : Train in memory that has just had a double domino satisfied
     */
    public void satisfyDoublePlay(int indexOfTrain){
        //it is possible that the player is playing on not the head of the queue
        DOUBLE_PLAYED_OPEN_TRAINS.remove(indexOfTrain);
    }

    /**
     * Gets the list of player's trains
     * @return : List of player's trains
     */
    public LinkedList<LinkedList<Domino>> getPLAYERS_TRAINS() {
        return PLAYERS_TRAINS;
    }

    /**
     * Gets the boolean[] that reflects the openness of trains
     * @return : boolean[]
     */
    public boolean[] getOPEN_TRAINS() {
        return OPEN_TRAINS;
    }

    /**
     * Gets the Queue that holds train indexes in the order that doubles were played
     * @return : Queue
     */
    public Queue<Integer> getDOUBLE_PLAYED_OPEN_TRAINS() {
        return DOUBLE_PLAYED_OPEN_TRAINS;
    }

}
