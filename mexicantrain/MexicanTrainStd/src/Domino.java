/**
 * Is the domino object
 */
public class Domino {

    private int left, right; //left and right dot numbers
    private final int TOTAL_DOTS;//left + right
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
        }
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

}
