package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import main.backend.Home;

public class Main extends Application {
    public static void main(String[] args){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Human Benchmark");
        //set screen sizing
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());


        FXMLLoader loader =
                new FXMLLoader(getClass().getResource("resources/home.fxml"));

        GridPane root = loader.load();
        /*Add some graphics to the buttons on Home

        buttons go
        Reaction Time, Aim Trainer, Chimp Test
        Visual Memory,            , Typing
        Number Memory,            , Verbal Memory
        The images for each button in the above order*/
        Image[] urls = {new Image("main/resources/reactionTest.png"),new Image("main/resources/aimTest.png"),
                new Image("main/resources/chimpTest.png"), new Image("main/resources/visMem.png"),
                new Image("main/resources/highScoreTest.png"), new Image("main/resources/keyTest.png"),
                new Image("main/resources/numberMemTest.png"), new Image("main/resources/fastMathTest.png"),
                new Image("main/resources/verbalTest.png")};
        for(int i = 0; i < root.getChildren().size(); i++){
            //get the node as a button
            Button button = (Button) root.getChildren().get(i);
            //get this button's graphic
            ImageView view = new ImageView(urls[i]);
            view.setFitHeight(150);
            view.setFitWidth(170);
            view.setPreserveRatio(true);
            //give this button the graphic
            button.setGraphic(view);
        }

        Scene scene = new Scene(root);


        Home home = loader.getController();
        home.setScene(scene);

        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
