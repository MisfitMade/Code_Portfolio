import java.security.SecureRandom;
import java.util.ArrayList;

/**
 * Controls the access to dominoes and contains the dominoes left after dominoes have been taken from the bone yard
 */
public class Boneyard {

    private ArrayList<Domino> boneYard;//the boneyard

    /**
     * The Boneyard Constructor
     */
    public Boneyard() {
        boneYard = new ArrayList<>();
        int k = 0;//increments the number of dominoes we are making = 55
        int left = 0;//the left half of a domino
        int right = 0;//the right half of a domino

        while (k < 55) { //while we have made less than 55 dominoes: dominoes 0 through 54 = 55 dominoes
            while (right < 10) {
                this.boneYard.add(new Domino(left, right));
                right++;
                k++;
            }
            left++;
            right = left;
        }
    }

    /**
     * This method draws a single domino randomly selected from the boneyard
     * @return Domino
     */
    public Domino drawDomino() {
        SecureRandom randomNumbers = new SecureRandom();
        int randomDominoIndex = randomNumbers.nextInt(boneYard.size());//random index number for drawing random domino
        return boneYard.remove(randomDominoIndex);//return the random domino, remove it from bone yard and trim to size
    }

    /**
     *  Draws a particular domino. Used for drawing the current round's center domino.
     * @param doubleDom int
     * @return Domino
     */
    public Domino drawParticularDoubleDomino(int doubleDom){
        /*dominoes in boneyard list upon boneyard construction are like so:
        [0 | 0], [0 | 1], [0 | 2], ..., [1|1], [1|2]
        This means that double zero is at 0, double 1 at 10, double 2 at 19, double 3 at 27,
        double 4 at 34, double 5 at 40, double 6 at 45, double 7 at 49, double 8 at 52,
        double 9 at 54
        int doubleDom will be the index of the double domino for the current round*/
        return boneYard.remove(doubleDom);
    }

    /**
     * Prints the boneyard. used for debugging
     */
    public void printBoneYard(){
        for(Domino domino : boneYard){
            System.out.println(domino.getDominoAsText());
        }
    }

    /**
     * Getter for Boneyard
     * @return ArrayList<Domino>
     */
    public ArrayList<Domino> getBoneyard() {
        return boneYard;
    }
}
