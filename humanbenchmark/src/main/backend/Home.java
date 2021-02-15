package main.backend;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import main.Loader;
import java.io.File;

/**
 * The home page
 */
public class Home {
    private static final String directory = "resources/";

    private final String reactionTimePage;
    private final String aimTrainerPage;
    private final String chimpTestPage;
    private final String visualMemoryPage;
    private final String typingPage;
    private final String numberMemoryPage;
    private final String verbalMemoryPage;
    private final String fastMathPage;

    @FXML
    public GridPane homePane;

    private Scene scene;
    //the button that is seen on all pages except for the home page
    private final Button BACK_TO_HOME;

    /**
     * Constructor for home that is called in Main
     */
    public Home() { //when load Home, this page is instantiated

        //will inject this button into all pages other than Home, in order to get back to Home page.
        BACK_TO_HOME = new Button("Back to Home");
        BACK_TO_HOME.setStyle("-fx-font-size: 22");
        BACK_TO_HOME.setBackground(new Background(new BackgroundFill(Color.CORAL, new CornerRadii(7),
                Insets.EMPTY)));
        BACK_TO_HOME.setOnAction(e -> scene.setRoot(homePane));

        reactionTimePage = directory + "reactionTime.fxml";
        aimTrainerPage = directory + "aimTrainer.fxml";
        chimpTestPage = directory + "chimpTest.fxml";
        visualMemoryPage = directory + "visualMemory.fxml";
        typingPage = directory + "typing.fxml";
        numberMemoryPage = directory + "numberMemory.fxml";
        verbalMemoryPage = directory + "verbalMemory.fxml";
        fastMathPage = directory + "fastMath.fxml";

    }

    /**
     * Getter for scene
     * @return the main scene
     */
    public Scene getScene() {
        return scene;
    }

    /**
     * Sets Home's scene
     * @param scene Scene coming from main
     */
    public void setScene(Scene scene) {
        this.scene = scene;
    }

    /**
     * Loads the reaction time page and puts the back to home button into it then sets it as the root
     * @param actionEvent : on click of reaction time button on home page
     */
    public void reactionTimeClicked(ActionEvent actionEvent) {
        //load reactionTime as defined in fxml
        Pane newRoot = Loader.loadFxmlFile(reactionTimePage);

        VBox child = (VBox) newRoot.getChildren().get(0);
        //inject back to home button into page we are going to
        child.getChildren().add(BACK_TO_HOME);
        child.setAlignment(Pos.CENTER);

        scene.setRoot(newRoot);
    }

    /**
     * Loads AimTrainer page, put back to home button in, set it as root.
     * @param actionEvent : on action for button defined in home.fxml
     */
    public void aimTrainerClicked(ActionEvent actionEvent) {
        //load aimTrainer as defined in fxml
        Pane newRoot = Loader.loadFxmlFile(aimTrainerPage);

        //give back to home button a certain x and y
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        BACK_TO_HOME.setLayoutY(bounds.getHeight() - 200);
        BACK_TO_HOME.setLayoutX(bounds.getWidth()/2 - 100);
        //inject back to home button into page we are going to
        newRoot.getChildren().add(BACK_TO_HOME);
        scene.setRoot(newRoot);
    }

    /**
     * Loads chimp test page, put back to home button in, set it as root.
     * @param actionEvent : on action for button defined in home.fxml
     */
    public void chimpTestClicked(ActionEvent actionEvent) {
        //load chimpTest as defined in fxml
        Pane newRoot = Loader.loadFxmlFile(chimpTestPage);

        VBox child = (VBox) newRoot.getChildren().get(0);
        //inject back to home button into page we are going to
        child.getChildren().add(BACK_TO_HOME);
        scene.setRoot(newRoot);
    }

    /**
     * Loads visual memory page, put back to home button in, set it as root.
     * @param actionEvent : on action for button defined in home.fxml
     */
    public void visualMemoryClicked(ActionEvent actionEvent) {
        //load visualMemory as defined in fxml
        Pane newRoot = Loader.loadFxmlFile(visualMemoryPage);

        VBox child = (VBox) newRoot.getChildren().get(0);
        //inject back to home button into page we are going to
        child.getChildren().add(BACK_TO_HOME);
        scene.setRoot(newRoot);
    }

    /**
     * Loads typing page, put back to home button in, set it as root.
     * @param actionEvent : on action for button defined in home.fxml
     */
    public void typingClicked(ActionEvent actionEvent) {
        //load typingTest as defined in fxml
        Pane newRoot = Loader.loadFxmlFile(typingPage);

        VBox child = (VBox) newRoot.getChildren().get(0);
        //inject back to home button into page we are going to
        child.getChildren().add(BACK_TO_HOME);
        scene.setRoot(newRoot);
    }

    /**
     * Loads number memory page, put back to home button in, set it as root.
     * @param actionEvent : on action for button defined in home.fxml
     */
    public void numberMemoryClicked(ActionEvent actionEvent) {
        //load numberMemory as defined in fxml
        Pane newRoot = Loader.loadFxmlFile(numberMemoryPage);

        VBox child = (VBox) newRoot.getChildren().get(0);
        //inject back to home button into page we are going to
        child.getChildren().add(BACK_TO_HOME);
        scene.setRoot(newRoot);
    }

    /**
     * Loads verbal memory page, put back to home button in, set it as root.
     * @param actionEvent : on action for button defined in home.fxml
     */
    public void verbalMemoryClicked(ActionEvent actionEvent) {
        //load verbalMemory as defined in fxml
        Pane newRoot = Loader.loadFxmlFile(verbalMemoryPage);

        VBox child = (VBox) newRoot.getChildren().get(0);
        //inject back to home button into page we are going to
        child.getChildren().add(BACK_TO_HOME);
        scene.setRoot(newRoot);
    }

    /**
     * Loads fast math page, put back to home button in, set it as root.
     * @param actionEvent : on action for button defined in home.fxml
     */
    public void fastMathClicked(ActionEvent actionEvent) {
        //load fast math page as defined in fxml
        Pane newRoot = Loader.loadFxmlFile(fastMathPage);

        VBox child = (VBox) newRoot.getChildren().get(0);
        //inject back to home button into page we are going to
        child.getChildren().add(BACK_TO_HOME);
        scene.setRoot(newRoot);
    }

    /**
     * Makes the High scores window pop up, put back to home button in, set it as root.
     * @param actionEvent : on action for button defined in home.fxml
     */
    public void scoreButtonClicked(ActionEvent actionEvent) {
        /*When user clicks this button, display all the games' high scores
        in a pop up window*/
        File savedScores = new File("src/main/resources/savedScores.txt");
        Scores scores = new Scores(savedScores);
        scores.display();
    }
}
