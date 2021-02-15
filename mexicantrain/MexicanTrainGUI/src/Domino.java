import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * The Domino object.
 */
public class Domino {

    private int left, right;//the left and right number of dots
    private GridPane leftHalf, rightHalf;//the left and right graphics
    private final int TOTAL_DOTS;//left + right
    private final HBox DOMINO_AS_GRAPHIC;//the domino as a graphic
    private boolean visited;//used for a DFS when finding comp's best combo

    /**
     * Constructor for Domino.
     * @param left int
     * @param right int
     */
    public Domino(int left, int right){

        this.left = left;
        this.right = right;
        this.TOTAL_DOTS = left + right;

        //used for doing DFS to find best combos
        this.visited = false;

        //graphic
        this.DOMINO_AS_GRAPHIC = new HBox(1.4);
        DOMINO_AS_GRAPHIC.setBackground(new Background(new BackgroundFill(Color.ROYALBLUE, CornerRadii.EMPTY,
                Insets.EMPTY)));
        //the left and right graphic objects
        DominoHalf leftHalf = new DominoHalf(this.left);
        this.leftHalf = leftHalf.getHalf();
        DominoHalf rightHalf = new DominoHalf(this.right);
        this.rightHalf = rightHalf.getHalf();
        //add them to the whole domino
        DOMINO_AS_GRAPHIC.getChildren().addAll(this.leftHalf, this.rightHalf);
    }

    /**
     * Rotates a domino if rotate = true
     * @param rotate: true to rotate, false to not
     */
    public void rotateDomino(boolean rotate){
        if(rotate){//if user chose to rotate domino
            int temp = left;
            this.left = right;
            this.right = temp;

            GridPane tempPane = leftHalf;
            this.leftHalf = rightHalf;

            this.rightHalf = tempPane;

            this.DOMINO_AS_GRAPHIC.getChildren().remove(0);
            this.DOMINO_AS_GRAPHIC.getChildren().add(rightHalf);
        }
    }

    /**
     * Makes a graphic out of a domino for the board
     * @return HBox : The graphic
     */
    public HBox createDominoForBoard(){
        //create and HBox graphic for placing on the Display board
        HBox toTheBoard = new HBox(1);
        toTheBoard.setBackground(new Background(new BackgroundFill(Color.ROYALBLUE,new CornerRadii(5.7),
                Insets.EMPTY)));
        DominoHalf left = new DominoHalf(this.getLeft());
        DominoHalf right = new DominoHalf(this.getRight());
        toTheBoard.getChildren().addAll(left.getHalf(),right.getHalf());

        return toTheBoard;
    }

    //ALL GETTERS AND SETTERS BELOW
    /**
     * marks a domino as visited if tOrF is true, unvisited if tOrF is false.
     * Used for DFS when finding comp's best combo
     * @param tOrF
     */
    public void setVisited(boolean tOrF){
        visited = tOrF;
    }

    /**
     * Returns true if this domino has been visited during DFS,
     * false otherwise
     * @return boolean
     */
    public boolean isVisited(){
        return visited;
    }

    /**
     * Getter for this Domino's left dot number
     * @return int
     */
    public int getLeft() {
        return left;
    }

    /**
     * Getter for this Domino's right dot number
     * @return int
     */
    public int getRight() {
        return right;
    }

    /**
     * Getter for the total number of dots for this Domino
     * @return int
     */
    public int getTOTAL_DOTS() {
        return TOTAL_DOTS;
    }

    /**
     * Formatting for domino as text
     * @return String: [& | &]
     */
    public String getDominoAsText() {
        /*if its a double domino return it [& | &], if not return it vertically*/
        return  String.format("[%d | %d]", this.left, this.right);
    }

    /**
     * Returns the domino's graphic
     * @return
     */
    public HBox getDOMINO_AS_GRAPHIC(){
        return DOMINO_AS_GRAPHIC;
    }

}
