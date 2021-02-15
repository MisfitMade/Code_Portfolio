import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * The thread that is the logic for each connection made to a house
 */
public class HouseConnection implements Runnable {

    //private int house_accountNumber;
    //readers and writers for the bank and agent that this thread is responsible for
    private final PrintWriter OUT_TO_BANK;
    private final PrintWriter OUT_TO_AGENT;
    private final BufferedReader AGENT_IN;
    private final BufferedReader BANK_IN;
    private int agentAccountNumber;
    private boolean keepRunning;

    /**
     * Constructor for the House logic thread
     * @param clientSocket is the socket
     * @param house_accountNumber is the unique account number that this house has at the bank
     * @param OUT_TO_BANK is the printer to the bank
     * @param BANK_IN is the reader from the bank
     * @throws IOException when there is an error printing out to the Agent_Pack.Agent that a connection is made
     */
    public HouseConnection(Socket clientSocket, int house_accountNumber,
                           PrintWriter OUT_TO_BANK, BufferedReader BANK_IN) throws IOException {
        //writer, reader and account number of house for bank
        //this.house_accountNumber = house_accountNumber;
        this.OUT_TO_BANK = OUT_TO_BANK;
        this.BANK_IN = BANK_IN;
        //connection with the agent
        OUT_TO_AGENT = new PrintWriter(clientSocket.getOutputStream(), true);
        AGENT_IN = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        //print the items currently up for auction
        OUT_TO_AGENT.println("Connected to House having deposit account number " + house_accountNumber);

        keepRunning = true;
    }

    /**
     * Commands used for parsing inputs
     */
    private enum Commands {
        ITEMS, BID, REVIEW, //agent commands
        CONFIRMED,  //bank commands
        STATUS //for verifying if an agent needs to authorize a transfer
    }

    /**
     * Just reads for input, passes it off for processing, then sends back
     */
    @Override
    public void run() {
        String inputLine;
        String outputLine;
        do{
            try {
                inputLine = AGENT_IN.readLine();
                System.out.println("Processing " + inputLine + " for Agent with account number " + agentAccountNumber);
                outputLine = processLine(inputLine);
                if(outputLine != null){
                    System.out.println("Transmitting: " + outputLine + " to Agent with account number "
                            + agentAccountNumber);
                    OUT_TO_AGENT.println(outputLine);
                }
            }
            catch (IOException | NullPointerException e){
                System.out.println("Connection with account " + agentAccountNumber + "  lost");
                //e.printStackTrace();
                keepRunning = false;//exit
            }
        } while (keepRunning);

    }

    /**
     * Processes an input from which ever connection this object oversees
     * @param inputLine is the line to process
     * @return a response
     * @throws IOException when an error sending to or getting input from a connection
     */
    private String processLine(String inputLine) throws IOException{

        String output = null;
        String[] inputs = inputLine.split(" ");

        //if one word command
        if(inputs.length == 1){
            //if requesting to see items
            if(inputs[0].equalsIgnoreCase(Commands.ITEMS.name())){
                output = AuctionHouse.presentCurrentItemsForAuction();
            }
            else if(inputs[0].equalsIgnoreCase(Commands.STATUS.name())){
                String toFinalizes = AuctionHouse.doesThisAgentNeedToAuthorizeTransfer(agentAccountNumber);

                output = (toFinalizes.equals("V")) ? "V" : toFinalizes;
            }
            else if(inputs[0].equalsIgnoreCase(Commands.REVIEW.name())){
                output = AuctionHouse.checkThisAgentsBids(agentAccountNumber);
            }
        }
        //if a bid request: Bid Item_number Amount_To_Bid Account_Number
        else if(inputs.length == 4) {
            if (inputs[0].equalsIgnoreCase(Commands.BID.name())) {
                int itemNumber = parseInteger(inputs[1]);
                //if itemNumber parsed correctly
                if (itemNumber > -1 && itemNumber < 3) {
                    //if the bid is more or equal to the min bid for item itemNumber
                    double bid = parseFunds(inputs[2]);
                    String itemID = AuctionHouse.getItemId(itemNumber);

                    //check if the bank says there is enough money
                    OUT_TO_BANK.println("Block " +
                          itemID + " " + inputs[2] + " " + inputs[3]);
                    String bankResponse = BANK_IN.readLine();
                    //if not enough funds
                    if (bankResponse.equals("X")) {
                        output = "Not enough funds.";
                    }
                    //if error parsing args
                    else if (bankResponse.equals("E")) {
                        output = "Invalid amount or account number.";
                    }
                    //else, the funds are blocked off now
                    else {
                        //we know this number is a legit account number cuz we made it this far
                        agentAccountNumber = parseInteger(inputs[3]);
                        output = AuctionHouse.bidOnItem(itemNumber, bid, agentAccountNumber, OUT_TO_BANK);
                    }
                }
                //else, bogus item number
                else {
                    output = "Invalid Item number. Try again.";
                }
            }
            else if (inputs[0].equalsIgnoreCase(Commands.CONFIRMED.name())) {
                //if confirmed transfer
                double payment = parseFunds(inputs[2]);
                int accountThatBought = parseInteger(inputs[3]);
                //if parsed correctly
                if(payment != -1.0 && accountThatBought != -1){
                    Item bought = AuctionHouse.finalizeItem(payment, accountThatBought);
                    output = "Winner: " + bought.getItemID() + " is now yours!";
                }
                else{
                    output = "Error in confirmation of transfer values.";
                }
            }
            //if(inputs[1].equalsIgnoreCase(Commands.FAILED.name())){
            //this should not happen
        }

        return output;
    }

    /**
     * Parses a string that is expected to be a double
     * @param input is what to parse
     * @return the parsed String into a Double
     */
    private double parseFunds(String input) {
        try {
            return Double.parseDouble(input);
        }
        catch (NumberFormatException e){
            System.out.println("Fund number could not be parsed -> Error Code: H.C. 168");
            e.printStackTrace();
            return -1.0;
        }
    }

    /**
     * Parses a String that is expected to be an int
     * @param input is the String to parse
     * @return the parsed int
     */
    private int parseInteger(String input){
        try{
            return Integer.parseInt(input);
        }
        catch (NumberFormatException e){
            System.out.println("Account number could not be parsed -> Error Code: H.C. 180");
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Prints a message to the agent that this thread oversees
     * @param message is what is sent
     */
    public void printMessageToAgent(String message){
        OUT_TO_AGENT.println(message);
    }

    /**
     * Has this connection send a message to the bank
     * @param message to send to the bank from this house
     */
    public void printMessageToBank(String message){
        OUT_TO_BANK.println(message);
    }

    /**
     * Gets the account number for the agent that this house oversees
     * @return the agent account number that this house thread is listening to
     */
    public int getAgentAccountNumber() {
        return agentAccountNumber;
    }
}
