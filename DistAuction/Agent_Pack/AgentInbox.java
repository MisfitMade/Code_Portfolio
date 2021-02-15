import java.io.IOException;

/**
 * Is a listener for input from connections. There is one of these for every connection and it just listens
 * for input
 */
public class AgentInbox implements Runnable{

    private final ConnectionPair BANK_CONNECTION;
    private final ConnectionPair PRIMARY_CONNECTION;
    private String[] itemsInNeedOfTransfer;
    private final int AGENT_BANK_ACCOUNT;
    private int house_number;
    private boolean keepRunning = true;

    /**
     * Constructor that passes itself to a Thread and starts itself
     * @param bankConnection Is the printer and reader to the bank
     * @param thisConnection Is the printer and reader to the connection that this Agent_Pack.AgentInbox listener is
     *                       responsible for
     * @param AGENT_BANK_ACCOUNT is the bank account number for the agent that this thread is listening for
     */
    public AgentInbox(ConnectionPair bankConnection,
                      ConnectionPair thisConnection, int AGENT_BANK_ACCOUNT){

        this.AGENT_BANK_ACCOUNT = AGENT_BANK_ACCOUNT;
        BANK_CONNECTION = bankConnection;

        //the connection that this listener listens to
        PRIMARY_CONNECTION = thisConnection;

        //if this is the bank listener, meaning, Primary_Connection = Bank_Connection, then give house_numer = -1
        if(BANK_CONNECTION.equals(PRIMARY_CONNECTION)){
            house_number = -1;
        }
        //else, get the house_number later

        //at the beginning, the items in need of transfer are none
        itemsInNeedOfTransfer = null;

        //stat this object as a thread
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * The run for this thread
     */
    @Override
    public void run() {

        String inputLine;
        String outputLine;
        do{
            try {
                inputLine = PRIMARY_CONNECTION.getKey().readLine();
                if(inputLine != null){
                    outputLine = processLine(inputLine);
                    //do not print on the occasion that output = ""
                    if(!outputLine.equals("")){
                        System.out.println(outputLine);
                    }
                }
            }
            catch (IOException e){
                String output = (house_number == -1) ?
                        "Connection to bank lost." : "Connection to Auction House " + house_number + " lost.";
                System.out.println(output);
               //e.printStackTrace();
                keepRunning = false;
            }
        } while (keepRunning);
    }

    /**
     * Processes an input from which ever connection this thread is listening to
     * @param inputLine is the line to process
     * @return the response after trying to process the line
     */
    private String processLine(String inputLine){

        String output;

        String[] inputAsTokens = inputLine.split(" ");

        //as long as there is input
        if(inputAsTokens.length > 0){
            switch (inputAsTokens[0]) {
                case "EXIT":
                    if (inputAsTokens[1].equals("EXIT")) {
                        System.out.println("Thank you for participating in Distributed Auction. Farewell.");
                        Agent.closeAgent(this);
                        output = "";
                    }
                    //else, it is EXIT X, thus inputAsTokens[1] = X
                    else {
                        output = "You cannot exit. Resolve any current bids first";
                    }
                    break;
                //if this is a balance | successful bid made | a you won an item notice | you have been outbid notice
                case "You":
                    output = "\n" + inputLine;
                    PRIMARY_CONNECTION.getValue().println("STATUS");
                    break;//if we just made a connection to a new house or won a bid
                //if this is a outbid notice
                case "Outbid":
                    output = "\n!!!" + inputLine;
                    break;
                case "WITHDRAW":
                    StringBuilder sb = new StringBuilder();
                    for (int i = 1; i < inputAsTokens.length; i++) {
                        sb.append(inputAsTokens[i]).append(" ");
                    }
                    output = sb.toString();
                    break;
                case "Connected":
                    //report it has connected
                    output = inputLine;
                    //if this is a house connection
                    if(inputAsTokens.length == 8){
                        house_number = Integer.parseInt(inputAsTokens[7]);
                    }
                    break;
                //if we just sent out a House request
                case "Port:":
                case "N":
                    //if houses is N, then no houses have connected yet
                    if (inputLine.charAt(0) == 'N') {
                        output = "Currently there are no registered Auction Houses.\nWait awhile, then" +
                                " try again";
                    }
                    //else parse into by house
                    else {
                        String[] housesByHouse = inputLine.split("#");
                        StringBuilder houseBuilder = new StringBuilder("Currently connected Auction Houses\n");
                        for (String house : housesByHouse) {
                            houseBuilder.append(house).append("\n");
                        }
                        //now it is nicely parsed
                        output = houseBuilder.toString();
                    }
                    break;
                //get back and print the items
                case "Item":
                    System.out.println("Auction House " + house_number + "'s items");

                    //parse
                    String[] byThePoundSign = inputLine.split("#");

                    StringBuilder itemBuilder = new StringBuilder();
                    for (String item : byThePoundSign) {
                        //form a line by line list
                        itemBuilder.append(item).append("\n");
                    }
                    //now I have parsed the items into lines
                    output = itemBuilder.toString();
                    break;
                //else a Review response is coming in
                case "Won":
                case "A":
                case "No":
                    String[] review = inputLine.split("#");
                    //print the response to the REVIEW request
                    System.out.println("~~~For Auction House " + house_number + " you currently have~~~");
                    StringBuilder reviewBuilder = new StringBuilder();
                    for (String str : review) {
                        reviewBuilder.append(str).append("\n");
                    }
                    //now i have it parsed into lines
                    output = reviewBuilder.toString();
                    break;
                //else this is a response of Items in need of transfer
                case "InNeedOf":
                    //make a transfer request option for the agent
                    output = "\n---------Attention Winner----------\n" +
                            "[T" + house_number + "] Authorize fund transfer to Auction House " +
                            house_number + " for " +
                            (inputAsTokens.length - 2)/2 + " items.";
                    //save this for future reference
                    itemsInNeedOfTransfer = inputAsTokens;
                    break;
                //else if this is a Confirmed response
                case "Winner:":
                    //by here it is owned item response from house.
                    Agent.ownAnItem(inputAsTokens[1]);
                    output = "You now own " + inputAsTokens[1];
                    break;
                default:
                    //else just print the response
                    output = inputLine;
                    break;
            }
        }
        else{
            output = "Input string was != null, but input as tokens has a length of 0";
        }
        return output;
    }

    /**
     * Is invoked by the Agent_Pack.Agent class after the user gives a transfer request and tells the bank to make the transfer and
     * then tells the auction house to "transfer the item"
     */
    public void sendTransferRequests(){
        String account_To_Transfer_to =
                itemsInNeedOfTransfer[itemsInNeedOfTransfer.length-1];

        //send a transfer request for each item currently in need of transfer
        for(int i = 1; i < itemsInNeedOfTransfer.length-1; i+=2) {
            BANK_CONNECTION.getValue().println("TRANSFER " + itemsInNeedOfTransfer[i] + " " + itemsInNeedOfTransfer[i+1]
                 + " " + account_To_Transfer_to);

            /*send a Confirm command to the house*/
            PRIMARY_CONNECTION.getValue().println("CONFIRMED " +itemsInNeedOfTransfer[i]+" "+itemsInNeedOfTransfer[i+1]
                    +" "+AGENT_BANK_ACCOUNT);
        }
        //only want one of these at a time
        itemsInNeedOfTransfer = null;
    }

    /**
     * Kills this thread
     */
    public void stop(){
        keepRunning = false;
    }

    /**
     * Gets the connection for which this thread is the listener
     * @return the ConnectionPair
     */
    public ConnectionPair getPRIMARY_CONNECTION() {
        return PRIMARY_CONNECTION;
    }

    /**
     * Returns the houses account number, assuming this is a connection with the house. Returns -1 if this is the
     * bank listener
     * @return -1 or the house number
     */
    public int getHouse_Number() {
        return house_number;
    }

    /**
     * Getter for the items in need of a transfer authorization
     * @return String[]
     */
    public String[] getItemsInNeedOfTransfer() {
        return itemsInNeedOfTransfer;
    }
}
