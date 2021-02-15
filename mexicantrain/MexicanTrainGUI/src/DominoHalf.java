import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Is half of a domino in the graphic. Used for making the dots in half a domino and setting them in a grid.
 */
public class DominoHalf {

    private final GridPane half;

    /**
     * The constructor for the domino half graphic
     * @param numberOfDots : The number of dots for the half
     */
    public DominoHalf(int numberOfDots){

        //formatting and alignment for half
        half = new GridPane();
        half.setHgap(4);
        half.setVgap(4);
        half.setAlignment(Pos.CENTER);
        //half.setPadding(new Insets(1,1,1,1));
        half.setMaxSize(30,30);
        half.setMinSize(30,30);
        half.setBackground(new Background(new BackgroundFill
                (Color.rgb(247,221,158),new CornerRadii(1), Insets.EMPTY)));

        /*define the dots, give them grid constraints
        Top Left*/
        Circle topLeft = new Circle(1.25, Color.ROYALBLUE);
        GridPane.setConstraints(topLeft,0,0);
        GridPane.setValignment(topLeft, VPos.CENTER);
        GridPane.setHalignment(topLeft, HPos.CENTER);
        //Top Center
        Circle topCenter = new Circle(1.25, Color.ROYALBLUE);
        GridPane.setConstraints(topCenter,1,0);
        GridPane.setValignment(topCenter, VPos.CENTER);
        GridPane.setHalignment(topCenter, HPos.CENTER);
        //Top Right
        Circle topRight = new Circle(1.25, Color.ROYALBLUE);
        GridPane.setConstraints(topRight,2,0);
        GridPane.setValignment(topRight, VPos.CENTER);
        GridPane.setHalignment(topRight, HPos.CENTER);
        //Middle Left
        Circle middleLeft = new Circle(1.25,Color.ROYALBLUE);
        GridPane.setConstraints(middleLeft,0,1);
        GridPane.setValignment(middleLeft, VPos.CENTER);
        GridPane.setHalignment(middleLeft, HPos.CENTER);
        //Middle Center
        Circle middleCenter = new Circle(1.25, Color.ROYALBLUE);
        GridPane.setConstraints(middleCenter,1,1);
        GridPane.setValignment(middleCenter, VPos.CENTER);
        GridPane.setHalignment(middleCenter, HPos.CENTER);
        //Middle Right
        Circle middleRight = new Circle(1.25,Color.ROYALBLUE);
        GridPane.setConstraints(middleRight,2,1);
        GridPane.setValignment(middleRight, VPos.CENTER);
        GridPane.setHalignment(middleRight, HPos.CENTER);
        //Bottom Left
        Circle bottomLeft = new Circle(1.25, Color.ROYALBLUE);
        GridPane.setConstraints(bottomLeft,0,2);
        GridPane.setValignment(bottomLeft, VPos.CENTER);
        GridPane.setHalignment(bottomLeft, HPos.CENTER);
        //Bottom Center
        Circle bottomCenter = new Circle(1.25, Color.ROYALBLUE);
        GridPane.setConstraints(bottomCenter,1,2);
        GridPane.setValignment(bottomCenter, VPos.CENTER);
        GridPane.setHalignment(bottomCenter, HPos.CENTER);
        //Bottom Right
        Circle bottomRight = new Circle(1.25, Color.ROYALBLUE);
        GridPane.setConstraints(bottomRight, 2,2);
        GridPane.setValignment(bottomRight, VPos.CENTER);
        GridPane.setHalignment(bottomRight, HPos.CENTER);

        //add dots to the half
        switch (numberOfDots) {//defining left domino half
            case 1: { //a 1 dot domino half
                half.getChildren().add(middleCenter);
                break;
            }
            case 2: {//a 2 dot domino half
                half.getChildren().addAll(topLeft, bottomRight);
                break;
            }
            case 3: {//a 3 dot domino half
                half.getChildren().addAll(topLeft, middleCenter, bottomRight);
                break;
            }
            case 4: {//a 4 dot domino half
                half.getChildren().addAll(topLeft, topRight, bottomLeft, bottomRight);
                break;
            }
            case 5: {//a 5 dot domino half
                half.getChildren().addAll(topLeft, topRight, middleCenter, bottomLeft, bottomRight);
                break;
            }
            case 6: {//a 6 dot domino half
                half.getChildren().addAll(topLeft, topCenter, topRight, bottomLeft, bottomCenter, bottomRight);
                break;
            }
            case 7: {//a 7 dot domino half
                half.getChildren().addAll(topLeft,topCenter,topRight,middleCenter,bottomLeft,bottomCenter,bottomRight);
                break;
            }
            case 8: {
                half.getChildren().addAll(topLeft,topCenter,topRight,middleLeft,middleRight,bottomLeft,
                        bottomCenter,bottomRight);
                break;
            }
            case 9: {
                half.getChildren().addAll(topLeft,topCenter,topRight,middleLeft,middleCenter,middleRight,
                        bottomLeft,bottomCenter,bottomRight);
                break;
            }
        }

    }

    /**
     * Getter for domino half graphic
     * @return GridPane
     */
    public GridPane getHalf() {
        return half;
    }
}
