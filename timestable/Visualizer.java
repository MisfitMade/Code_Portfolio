
/*Michael Nafe | nafem@unm.edu*/


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Visualizer {

    private final Display DISPLAY;
    private final GraphicsContext CANVAS_GRAPHICS;
    final double RADIUS = 300.0;

    //the possible visualization colors
    private final Color[] VISUALIZATION_COLORS = {Color.BLUE, Color.RED, Color.NAVY, Color.PURPLE, Color.YELLOW,
            Color.HOTPINK, Color.GREEN,Color.SILVER, Color.BLACK,Color.WHITE};
    //the preset visual values. 10 presets for mulitplydBy and circlePoints. Row 0 is multiplyBy, Row 1 points
    private final int[][] presets = {{82,215,234,3,25,253,335,335,277,277}, {36,72,220,60,243,260,11,139,184,185}};
    private int presetCounter;
    //traverses the VISUALIZATION_COLORS array
    private int lineColorCounter;
    private int circleColorCounter;
    public Visualizer(Display display) {
        this.DISPLAY = display;
        CANVAS_GRAPHICS = display.getCanvasGraphics();
        //the following two values are used to traverse the Color[]
        circleColorCounter = VISUALIZATION_COLORS.length - 1;
        lineColorCounter = 0;
        presetCounter = 0;
        /*by initiating the presets, the user increments to the next preset, so the initial preset shown is preset
        index 1. Thus, clicking the cycle visualizations button 9 times will get you to preset index 0, which is really
        the 10th preset to be shown. Clicking again will get you back to where you started: preset index 1.*/
        presetCounter = 0;
    }

    //draws a line from one point to another on the canvas
    public void drawLines(int incrementByCounter){
        //first draw the circle: clear what is there and redraw
        drawCircle();
        /*if the user is cycling visuals, then pull from the presets. Else, pull from the display's circlePoints and
        multipliedBy*/
        int circlePoints;
        double multiplyBy;
        int multiplyByCounter;
        //if the user is cycling visuals
        if(DISPLAY.isCyclingVisuals()){
            //use the preset values
            multiplyByCounter = presets[0][presetCounter];
            circlePoints = presets[1][presetCounter];
        }
        //the user is not cycling visuals. They have just started program or has selected a point or multiplyBy
        else{
            //get what we are multiplying by
            multiplyBy = DISPLAY.getMultiplyBy() + DISPLAY.getIncrementBy().getValue()*incrementByCounter;
            //round to nearest point for when incrementBy is fractional
            multiplyByCounter = (int) Math.round(multiplyBy);
            circlePoints = DISPLAY.getCirclePoints();
        }

        //passed in the circle points, either what is in the display as defined by the user, or the preset value
        Double[][] coordinates = findXsandYs(circlePoints);

        //now I have circlePoints number of coordinates around the circle
        int pointCounter = 0;
        int timesCounter;
        double startX;
        double startY;
        double endX;
        double endY;
        while(pointCounter < circlePoints){

            timesCounter = pointCounter * multiplyByCounter;

            /*if/while timesCounter is out of the point number's domain, reduce it by points number so that it lines
            up with the right point on the circle*/
            while(timesCounter >= circlePoints){
                timesCounter -= circlePoints;
            }
            //get the x and y values
            startX = coordinates[0][pointCounter];
            startY = coordinates[1][pointCounter];
            endX = coordinates[0][timesCounter];
            endY = coordinates[1][timesCounter];


            /*draw the number along the circle edge. My circle's radius is only 600, but the entire canvas container
            is 700. Then x and y coordinates for the point lables are found in coordinates[2] and coordinates[3]
            point labels get very cluttered at > 120 points, so*/
            if(circlePoints < 121){
                CANVAS_GRAPHICS.setStroke(Color.WHITE);//draw the labels in white
                CANVAS_GRAPHICS.setLineWidth(1);
                CANVAS_GRAPHICS.strokeText(String.format("%d",pointCounter),coordinates[2][pointCounter],
                        coordinates[3][pointCounter]);
            }

            //System.out.printf("(%f,%f) -> (%f,%f)\n", startX,startY,endX,endY);
            CANVAS_GRAPHICS.setStroke(VISUALIZATION_COLORS[lineColorCounter]);
            CANVAS_GRAPHICS.setLineWidth(1);
            CANVAS_GRAPHICS.strokeLine(startX,startY,endX,endY);

            pointCounter++;

        }
        lineColorCounter++;
        //if we have gone out of the colors array's domain, reset
        if(lineColorCounter == VISUALIZATION_COLORS.length){
            lineColorCounter = 0;
        }

    }
    //gathers up the xs and ys for the GraphicsContext
    public Double[][] findXsandYs(int circlePoints){

        /*is the degrees per circle point*/
        double degreesPerPoint = 360.0/(double)circlePoints;
        /*Using a normal unit circle for degrees below, but in my canvas container:
        0 degrees for x = 600, 90 degrees for x = 300, 180 degrees for x = 0, 270 degree for x = 300, 360 degrees for
        x = 600. This is the unit circle flipped as the normal unit circle's 180 degrees is my circle's 0 degrees;
        the normal units circle's 0 degrees is my circle's 180 degrees.
        So, when I get a angle from the standard unit circle, I add 180 to it to get the angle it would be
        if 0 degrees was on the left side of the circle rather than the right. If angle+180 > 360, substract 360 from
        it to get it to 0 <= degree <= 360.*/
        Double[][] coordinates = new Double[6][circlePoints];
        /*Since I am adding the 180, this will give me the degrees I need in reverse order from which they should be
        drawn, so collect the coordinates in reverse using*/
        int counter = circlePoints-1;
        double degreePrior = 0;//stepping from degreesPrior degreesPerPoint units

        while (counter >= 0){

            degreePrior += degreesPerPoint;
            double degree = degreePrior+180;

            if(degree > 360){
                degree -= 360;
            }

            /*now I have the theta for x = r cos(theta) and y = r sin(theta)
            Since my (0,0) is really (300,300), must add 300 to x = r cos(theta) and subtract 300 from y = r sin(theta),
            so...
            x = (r cos(theta) + r)
            y = (r - r sin(theta))
            Then, must also do a + 50. Plus 50 is so that the whole thing is centered in my canvas.
            My circle has diameter 600, but my canvas is 700x700, to allow for plenty of room around the circle for the
            point labels. Therefore, really,
            x = (r cos(theta) + r) + 50
            y = (r - r sin(theta)) + 50
            Also, coordinates[4] and coordinates[5] are the x and y for point labels, "1", "2", etc. I added 25 to the
            radius in the x = r cos(theta) and y = r sin(theta) for the point label coordinates in order to push them
            outside of the circle*/
            coordinates[0][counter] = (RADIUS*Math.cos(Math.toRadians(degree)) + RADIUS) + 50;//xs
            coordinates[1][counter] = (RADIUS - RADIUS*Math.sin(Math.toRadians(degree))) + 50;//ys
            coordinates[2][counter] = ((RADIUS+25.0)*Math.cos(Math.toRadians(degree)) + RADIUS) + 50;
            coordinates[3][counter] = (RADIUS - (RADIUS+25.0)*Math.sin(Math.toRadians(degree))) + 50;

            counter--;
        }
        return coordinates;
    }

    //redraws the blank circle
    public void drawCircle(){

        final double DIAMETER = 600;
        //clear canvas and prep it for a new drawing sequence
        CANVAS_GRAPHICS.clearRect(0,0,700,700);
        //redraw the circle
        CANVAS_GRAPHICS.setFill(VISUALIZATION_COLORS[circleColorCounter]);
        CANVAS_GRAPHICS.fillOval(50,50,DIAMETER,DIAMETER);
        circleColorCounter--;
        //if we have left the color array's domain, reset
        if(circleColorCounter < 0){
            circleColorCounter = VISUALIZATION_COLORS.length-1;
        }
    }

    //setter for presetCounter
    public void incPresetCounter() {
        this.presetCounter += 1;//inc to next preset
        //there are only 10 presets
        if(presetCounter > 9){
            this.presetCounter = 0;
        }
    }
}
