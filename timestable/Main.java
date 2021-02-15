
/*Michael Nafe | nafem@unm.edu*/

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    static Stage window;
    static int colorCounter;
    static private final double CIRCUMFERENCE = 600*Math.PI;
    static private final int DIAMETER = 600;
    static private final int RADIUS = 300;

    public static void main (String[] args){
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Times Table Visualization");//set title of window

        colorCounter = 0;
        Display display = new Display();

        //put the scene in the window and show
        window.setScene(display.getSCENE());
        window.show();

        timesTableVisualizationLoop(display);

    }
    //runs the drawing the lines in the circle sequence based off of the GUI's buttons
    static void timesTableVisualizationLoop(Display display){

        //the main loop
        AnimationTimer looper = new AnimationTimer() {
            private long startTime = 0;
            int incrementByCounter = 0;

            @Override
            public void handle(long now) {

                //if the simulation is not paused
                if(display.isUnpaused()){
                    double elapsed = now - startTime;//time elapsed since startTime
                    long rate = display.getRate();//rate as defined by user

                    /*Since now will be in nanos, elapsed will be in nanos. rate is a long, but only takes values 1,2,3,4,5
                    by doing elapsed/rate, I get a long. If say rate is set to 5, then as soon as elapsed is large enough
                    to make dividing by rate large enough, draw the lines.
                    This means that I want the fastest rate value to be equal to 1 and the slowest rate value to be equal
                    to 5*/
                    long GREATER_THAN = 300000000;
                    if(elapsed/rate > GREATER_THAN){
                        startTime = now;

                        display.getVisualizer().drawLines(incrementByCounter);

                        //if the user changed the slider
                        if(display.isSliderChanged()){
                            //reset incrementByCounter
                            incrementByCounter = 0;
                            //reset the sliderChanged boolean
                            display.setSliderChanged(false);

                        }
                        //else, increment the incrementByCounter for multiplying it by the increment size in the visualizer
                        else{
                            incrementByCounter++;
                        }
                    }
                }
            }
        };
        looper.start();
    }
}
