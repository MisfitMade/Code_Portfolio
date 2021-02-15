import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.util.ArrayList;

/**
 * Is the GUI object
 */
public class Display {

    private GridPane tableDisplay;//always pointing at the current round's display
    private final SimpleStringProperty[] P_SCORES; //going to bind these to the Player objects scores
    private final int NUMBER_OF_HUMANS;//number of humans for the game
    private final int NUMBER_OF_PLAYERS;//players.length
    private VBox infoBox;//Always pointing at the current round's info label
    private final Stage WINDOW;//the Stage
    private Controller controller;


    /**
     * The Display constructor
     * @param players : The list of players
     * @param numberOfHumans : The number of humans in players
     */
    public Display(ArrayList<Player> players, int numberOfHumans, Controller controller) {

        NUMBER_OF_HUMANS = numberOfHumans;
        NUMBER_OF_PLAYERS = players.size();

        P_SCORES = new SimpleStringProperty[players.size()];

        this.controller = controller;

        WINDOW = new Stage();
        WINDOW.setTitle("Mexican Train!!");
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        WINDOW.setX(bounds.getMinX());
        WINDOW.setY(bounds.getMinY());
        WINDOW.setWidth(bounds.getWidth());
        WINDOW.setHeight(bounds.getHeight());

        WINDOW.show();
    }

    /**
     * Creates the Flip, Draw and Open Train buttons. Puts them in a VBox and that in the Grid
     * @param controller : The controller from where to get the event handlers
     */
    public void setFlipDrawOpenButtons(Controller controller){
        //flip domino button
        Button flip = new Button("Flip Domino");
        flip.setMinSize(150, 35);
        flip.setStyle("-fx-font-size: 17");
        flip.setOnAction(controller.getFLIP_DOMINO_LISTENER());
        //draw from boneyard button
        Button draw = new Button("Draw Domino");
        draw.setMinSize(150, 35);
        draw.setStyle("-fx-font-size: 17");
        draw.setOnAction(controller.getDRAW_DOMINO_LISTENER());
        //open train button
        Button openTrain = new Button("Open Train");
        openTrain.setMinSize(150, 35);
        openTrain.setStyle("-fx-font-size: 17");
        openTrain.setOnAction(controller.getOPEN_TRAIN_LISTENER());
        /*put these in a VBox*/
        VBox buttonBox = new VBox(2);
        buttonBox.getChildren().addAll(flip,draw,openTrain);
        //add this to the grid at the bottom, after player trains and mexican trains play buttons, to the left of tray
        GridPane.setConstraints(buttonBox,0,(NUMBER_OF_PLAYERS+1));
        tableDisplay.getChildren().add(buttonBox);
    }

    /**
     * Makes a new Scene and displays it in the window.
     */
    public void setDisplay(){
        HBox display = new HBox(20);
        display.setBackground(new Background(new BackgroundFill(Color.rgb(153,0, 0),
                new CornerRadii(55), new Insets(20))));

        display.getChildren().addAll(tableDisplay,infoBox);
        Scene scene = new Scene(display,1870, 1000);
        WINDOW.setScene(scene);
    }

    /**
     * Initializes the trains and Play here buttons for a new round
     * @param controller : The Controller that runs the game
     * @param players : ArrayList of the players
     * @param centerDomino : The rounds center domino
     * */
    public void setTrains(ArrayList<Player> players, Controller controller, HBox centerDomino) {
        tableDisplay = new GridPane();
        tableDisplay.setVgap(20);
        tableDisplay.setHgap(50);
        tableDisplay.setPadding(new Insets(50));
        tableDisplay.setBackground(new Background(new BackgroundFill(Color.rgb(153,0, 0),
                new CornerRadii(55), new Insets(20))));

        //The info box, its formatting and
        infoBox = new VBox(150);
        infoBox.setPadding(new Insets(80,0,0,0));
        //add the label in that says blue play button means next non-double must play here
        Label nextNonDouble = new Label("A blue play button indicates where the next non-double domino must" +
                "\nbe played. A yellow train indicates that a train is open.");
        nextNonDouble.setStyle("-fx-font-size: 17");
        nextNonDouble.setTextFill(Color.WHITESMOKE);
        GridPane.setConstraints(nextNonDouble,3,0);
        infoBox.getChildren().add(nextNonDouble);

        //force the 1st and 2nd column of the table display to be a certain width to keep from the display resizing
        ColumnConstraints col0 = new ColumnConstraints();
        col0.setMinWidth(162);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(800);
        tableDisplay.getColumnConstraints().addAll(col0,col1);

        //at the top, a label that informs of gameplay*/
        Label info = new Label("");
        info.setStyle("-fx-font-size: 16");
        info.setTextFill(Color.WHITE);
        //GridPane.setConstraints(info, 3, 1);
        infoBox.getChildren().add(info);

        DropShadow shadow = new DropShadow();
        shadow.setOffsetY(5);
        shadow.setOffsetX(5);
        //put 2 HBoxes in a VBox for staggering in for each train. Each player, mexican train
        for (int i = 0; i < (players.size() + 1); i++) {
            VBox train = new VBox(1.23);
            train.setEffect(shadow);
            train.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0,0.5),
                    CornerRadii.EMPTY, Insets.EMPTY)));
            HBox row1 = new HBox(1.5);
            row1.setMinHeight(30);//so that its colored background is shown at initialization
            HBox row2 = new HBox(1.5);
            //row2 will be the staggered row, so it needs to always be 1/2 a domino longer than row1. Make s dummy half
            GridPane dummyHalf = new GridPane();
            dummyHalf.setMinSize(30,30);
            row2.getChildren().add(dummyHalf);
            //add the rows to the train VBox
            train.getChildren().addAll(row1, row2);
            //add this train to the grid.
            GridPane.setConstraints(train, 1, i);
            tableDisplay.getChildren().add(train);
            /*also, put a play button for each train, to the right of each train. Grows with the train*/
            Button playHere = new Button("Play here");
            playHere.setMinWidth(125);
            playHere.setTextFill(Color.WHITE);
            playHere.setStyle("-fx-font-size: 16");
            playHere.setOnAction(controller.getPLAY_DOMINO_LISTENER());
            GridPane.setConstraints(playHere,2, i);
            tableDisplay.getChildren().add(playHere);

            //if this is the mexican train, add this rounds center domino
            if(i == 2){
                row1.getChildren().add(centerDomino);
            }
        }
    }

    /**
     * Used to turn the Play here buttons back to green after a double domino play has been satisfied.
     * Also used to give them their intitial green color
     */
    public void resetDoublePlayedButtons(){
        //for each train. +1 for mexican train
        for(Node node : tableDisplay.getChildren()){
            //set all the play here buttons to green
            if(GridPane.getRowIndex(node) < NUMBER_OF_PLAYERS+1 &&
                    GridPane.getColumnIndex(node) == 2){

                Button playButton = (Button) node;
                playButton.setBackground(new Background(new BackgroundFill(Color.rgb(0,102,0),
                        new CornerRadii(3),  Insets.EMPTY)));
            }
        }
    }

    /**
     * Sets a Play Here button blue to show that a double was played there and the next play must go there
     * @param trainDoublePlayedOn : The index of the train for which the Play here button must be turned
     *                              blue to indicate that the next non-double must play there.
     */
    public void setDoublePlayedButton(int trainDoublePlayedOn){

        /*if this a the mexican train*/
        if(trainDoublePlayedOn == NUMBER_OF_PLAYERS){
            trainDoublePlayedOn = 2;
        }
        //if this is a player higher than player index 1, then need to increment for display's trains
        else if(trainDoublePlayedOn > 1){
            trainDoublePlayedOn++;
        }
        //else it is already at the right index
        for(Node node : tableDisplay.getChildren()){
            //if this is the play here button
            if(GridPane.getRowIndex(node) == trainDoublePlayedOn &&
                    GridPane.getColumnIndex(node) == 2){
                //set its color to blue
                Button playButton = (Button) node;
                playButton.setBackground(new Background(new BackgroundFill(Color.rgb(0,0,255),
                        CornerRadii.EMPTY,Insets.EMPTY)));
                break;
            }
        }
    }

    /**
     * Makes and places the labels that I.D. the trains in the display. Also includes the String
     * Properties in the labels that are bound to each player's score.
     * @param players : The list of players
     */
    public void setTrainLabels(ArrayList<Player> players) {
        //initialize the string properties that connect to each player's score
        for (int i = 0; i < players.size(); i++) {
            P_SCORES[i] = new SimpleStringProperty();
            P_SCORES[i].bindBidirectional(players.get(i).playerScoreProperty());
        }
        //build the labels for each train, plus 1 for the mexican train
        int propertyCounter = 0;
        for (int i = 0; i < NUMBER_OF_PLAYERS + 1; i++) {
            //if i == 2, this is the mexican train label. Labels go by row, 1,2,3,4,5, so do (i+1)
            Label trainLabel = new Label();
            //if this is the mexican train label
            if (i == 2) {
                //mexican train is really always open
                trainLabel.setText("Mexican train");
            }
            else {
                //else, a player train, show their score and train number. Can us propertyCounter here too
                Player thisPlayer = players.get(propertyCounter);
                //if this is a human
                if(thisPlayer.getHUMAN_OR_COMPUTER() == 'h'){
                    trainLabel.setText(String.format("Human%d's Train" , thisPlayer.getPLAYER_NUMBER()) +
                                    "\nScore: " + P_SCORES[propertyCounter].getValue());
                }
                //else this is a computer
                else{
                    trainLabel.setText(String.format("Computer%d's Train" ,
                            (thisPlayer.getPLAYER_NUMBER() - NUMBER_OF_HUMANS)) +
                                    "\nScore: " + P_SCORES[propertyCounter].getValue());
                }

                propertyCounter++;
            }
            trainLabel.setTextFill(Color.WHITESMOKE);
            trainLabel.setStyle("-fx-font-size: 15");
            GridPane.setConstraints(trainLabel, 0, i);
            tableDisplay.getChildren().add(trainLabel);
        }
    }

    /**
     * Updates the info label with info of gameplay
     * @param info   String: information for the players
     * @param tackOn boolean: if true, add to what is in the label, false
     *               replace what is there
     */
    public void setInfo(String info, boolean tackOn) {
        /*Get the info label*/
        Label infoLabel = (Label)infoBox.getChildren().get(1);
        //update the info label. if this is to be added on
        if(tackOn){
            infoLabel.setText(infoLabel.getText() + info);
        }
        //else, overwrite
        else{
            infoLabel.setText("");
            infoLabel.setText(info);
        }
    }

    /**
     * Places the domino graphic in the right spot on the right train
     * @param dominoGraphic : The domino to be played as a graphic
     * @param trainNumber : The index of the train to be played on
     */
    public void playOnTrain(HBox dominoGraphic, int trainNumber) {
        /*train number comes in as the row index of the train in the grid
        * trains are in col 1 of the grid, find this train node*/
        //get the label that corresponds to the train to open
        VBox train = null;
        for(Node node : tableDisplay.getChildren()){
            /*if this is the right row and col in the grid*/
            if(GridPane.getRowIndex(node) == trainNumber && GridPane.getColumnIndex(node) == 1){
                train = (VBox) node;
                break;
            }
        }
        /*Now i have the train, which is two rows.
         Since row2 holds a dummy half for staggering,
         if row2 is longer than row 1, I want to play on row1, else on row2*/
        HBox row1 = (HBox) train.getChildren().get(0);
        HBox row2 = (HBox) train.getChildren().get(1);
        if(row2.getChildren().size() > row1.getChildren().size()) {
            row1.getChildren().add(dominoGraphic);
        }
        else {
            row2.getChildren().add(dominoGraphic);
        }

    }

    /**
     * Opens a train in the GUI by turning it yellow
     * @param trainToOpen : The train that needs to be turned yellow
     */
    public void openATrain(int trainToOpen){
        //get train box that corresponds to this train
        for(Node node : tableDisplay.getChildren()){
            /*if this is the right row and col in the grid*/
            if(GridPane.getRowIndex(node) == trainToOpen && GridPane.getColumnIndex(node) == 1){
                VBox train = (VBox) node;
                //change it's color
                train.setBackground(new Background(new BackgroundFill(Color.rgb(255, 255, 0),
                        CornerRadii.EMPTY,Insets.EMPTY)));
                break;
            }
        }
    }

    /**
     * Closes the train by turning it back to black
     * @param trainToClose : The index of the train that needs to be turned black
     */
    public void closeATrain(int trainToClose){
        //get the train box that corresponds to the train to open
        for(Node node : tableDisplay.getChildren()) {
            /*if this is the right row and col in the grid*/
            if (GridPane.getRowIndex(node) == trainToClose && GridPane.getColumnIndex(node) == 1) {
                VBox train = (VBox) node;
                //change it back to transparent-ish black
                train.setBackground(new Background(new BackgroundFill(Color.rgb(0, 0, 0, 0.5),
                        CornerRadii.EMPTY, Insets.EMPTY)));
                break;
            }
        }
    }

    /**
     * Makes the current human's tray visible and all the other human's tray's invisible
     * @param playerWhoseTurnItIs : The player whose tray need to be shown
     */
    public void setTray(Player playerWhoseTurnItIs) {
        //each human gets their own tray row, so set all other rows to not visible
        int row = NUMBER_OF_PLAYERS + playerWhoseTurnItIs.getPLAYER_NUMBER();
        //set the other trays to not visible and this one to visible
        for(Node node : tableDisplay.getChildren()){
            if(GridPane.getColumnIndex(node) == 1 &&
                GridPane.getRowIndex(node) > NUMBER_OF_PLAYERS){

                node.setVisible(GridPane.getRowIndex(node) == row);
            }
        }
    }

    /**
     * Puts all the human's trays in the display
     * @param players : The list of all the players
     */
    public void initializeTrays(ArrayList<Player> players){
        //each human gets their own tray row
        for(Player player : players){
            if(player.getHUMAN_OR_COMPUTER() == 'h'){
                //add their tray
                VBox tray = player.getPlayersTray();
                GridPane.setConstraints(tray,1,(NUMBER_OF_PLAYERS+player.getPLAYER_NUMBER()));
                tableDisplay.getChildren().add(tray);
            }
        }
    }
}