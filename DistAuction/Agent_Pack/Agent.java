
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

/**
 * The Agent_Pack.Agent class. Does the interaction with the user.
 */
public class Agent {
    //the connections to houses
    private static final LinkedList<ConnectionPair> HOUSE_CONNECTIONS = new LinkedList<>();
    //the bank account number given to this agent
    private static int bankAccountNumber;
    //the items purchased by this agent
    private static final LinkedList<String> PURCHASED_ITEMS = new LinkedList<>();
    //List of threads that listen for input from auction houses
    private static final LinkedList<AgentInbox> LISTENERS = new LinkedList<>();
    //the connection to the bank
    private static ConnectionPair Bank_connection;
    //to get out of the do while loop in the main
    private static boolean keepRunning = true;
    //to make sure the agent does not connect to the same port twice
    private static ArrayList<String> ports = new ArrayList<>();

    /**
     * Walks through a connection to the bank and then getting info on the houses, then sets the user off
     * on their own, in a loop, with a collection of options.
     * @param args = 7999 localhost = bank info
     * @throws InterruptedException when connection error
     * @throws IOException when connection error
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        //as long as it has bank port number and host name
        if(args.length == 2){
            //parse input
            int bank_port = Integer.parseInt(args[0]);
            String bank_host_name = args[1];

            //try connecting to bank
            try(Socket socket_to_bank = new Socket(bank_host_name, bank_port);
                PrintWriter out_to_bank = new PrintWriter(socket_to_bank.getOutputStream(), true);
                BufferedReader in_from_bank = new BufferedReader(new InputStreamReader(socket_to_bank.getInputStream()));
                BufferedReader standard_in = new BufferedReader(new InputStreamReader(System.in))) {

                //show that the bank connection has been made
                System.out.println(in_from_bank.readLine());

                //open an account with the bank
                openAccount(standard_in,out_to_bank,in_from_bank);

                //connect to first house
                connectToFirstHouse(standard_in,out_to_bank,in_from_bank);

                //now, we have the Bank connection and pass off the listening to the bank to a thread
                Bank_connection = new ConnectionPair(in_from_bank,out_to_bank);
                //the bank connection is both the bank connection and the primary connection for the AgentInbox thread
                LISTENERS.add(new AgentInbox(Bank_connection, Bank_connection, bankAccountNumber));

                //will be the options usually given to the user
                String basicUserOptions =
                        "[B] Check your bank balance\n" +
                                "[H] Request Auction House Connection info\n" +
                                    "[C] Connect to an Auction House\n" +
                                        "[O] See the items you own\n" +
                                            "[X] Close account and exit\n" +
                                                "*************************************";

                //now the main Agent_Pack.Agent loop
                try{
                    //for input and output from and to the user
                    String to_std_out;
                    String from_std_in;

                    do {
                        System.out.println("*********Pick your poison************");
                        //Get some options based off of the houses that are currently connected to
                        StringBuilder userOptions = new StringBuilder();
                        for(int i = 1; i < LISTENERS.size(); i++){
                            AgentInbox listener = LISTENERS.get(i);
                            int house_num = listener.getHouse_Number();
                            //can request a list of items
                            userOptions.append("[I").append(house_num).
                                    append("] Request item list from Auction House ").append(house_num).append("\n");

                            //can bid on an item at this auction house
                            userOptions.append("[B").append(house_num).
                                    append("] Bid on an item from Auction House ").append(house_num).append("\n");

                            //if there are currently transfers in need of authorization for this house
                            if(listener.getItemsInNeedOfTransfer() != null) {
                                userOptions.append("[T").append(house_num).append("] Authorize fund transfer to Auction " +
                                        "House ").append(house_num).append(" for ").
                                        append((listener.getItemsInNeedOfTransfer().length - 2) / 2).append(" items.\n");
                            }
                        }

                        //if there are houses to which this agent is connected
                        if(HOUSE_CONNECTIONS.size()>0){
                            userOptions.append("[R] Review your current bids\n");
                        }
                        //tack on basicUserOptions to userOptions
                        userOptions.append(basicUserOptions);
                        //show these options
                        System.out.println(userOptions.toString());

                        //get the response
                        from_std_in = standard_in.readLine();
                        /*process choice as long as it is not an accidental enter key pressed with no chars and
                        as long as it has length > 0 and length <= 4 (this assumes there will not be more than
                        100 bank accounts, as the number for the Auction House is their bank account number   */
                        if(from_std_in.length() > 0 &&
                                from_std_in.length() <= 4 &&
                                    from_std_in.charAt(0) != 13){
                            //if gave a transfer request
                            if(from_std_in.charAt(0) == 'T' || from_std_in.charAt(0) == 't'){
                                to_std_out = processTransfers(from_std_in);
                            }
                            //else, some other request
                            else{
                                to_std_out = processBasicOptionLine(from_std_in, standard_in);
                            }
                            //print response if it is not just ""
                            if(!to_std_out.equals("")){
                                System.out.println(to_std_out);
                            }
                            //sleep a little bit
                            TimeUnit.MILLISECONDS.sleep(1200);
                        }
                        else{
                            System.out.println(from_std_in + " is an invalid input. Try again.");
                        }
                        /*is turned to false when and exit request is made and deemed okay. The thread sleeping should
                        hopefully be long enough for the keepRunning to get changed in time...*/
                    } while (keepRunning);
                }
                catch(SocketException e){
                    System.out.println("Connections lost. Try to restart.");
                }
            }
        }
        //else, did not give bank port number and host name
        else{
            System.out.println("Must provide bank port and host name, in that order");
        }
    }

    /**
     * Processes a transfer request from the user
     * @param from_std_in is what the user gives when the are authorizing a transfer
     * @return the response
     */
    public static String processTransfers(String from_std_in){

        //first get all the digits for the house number to authorize a transfer to
        StringBuilder accumulateHouseNumber = new StringBuilder();
        for(int i = 1; i < from_std_in.length(); i++){
            accumulateHouseNumber.append(from_std_in.charAt(i));
        }

        //try to parse which house number
        try {
            //now parse this to an int
            int houseNumber = Integer.parseInt(accumulateHouseNumber.toString());
            /*need to transfer funds to houseNumber for every item that this agent has bought from house
            houseNumber. The command to send to bank is, Transfer Amount_To_Transfer Account_To*/
            for(AgentInbox inbox : LISTENERS){
                if(inbox.getHouse_Number() == houseNumber){
                    inbox.sendTransferRequests();
                    break;
                }
            }
        }
        catch (NumberFormatException e){
            e.printStackTrace();
            return "Invalid house number. Try again.";
        }

        return "Transfer processing...";
    }

    /**
     * Processes user input that is not a transfer request
     * @param from_std_in is the input
     * @param in_from_std is the buffered reader that brings in the user input
     * @return the response
     * @throws IOException when there is an output via a socket error
     * @throws InterruptedException when the TimeUnit.sleep fails
     */
    public static String processBasicOptionLine(String from_std_in, BufferedReader in_from_std)
            throws IOException, InterruptedException{

        //can assume invalid input was given
        String output = from_std_in + " is not a valid input option. Try again.";

        StringBuilder accumulateHouseNumber = new StringBuilder();
        int houseNumber;

        /*-------------First, Send out the command to the right place----------*/

        //if check balance request
        if(from_std_in.equalsIgnoreCase("B")) {
            output = "Account balance info";
            Bank_connection.getValue().println("Balance");
        }
        //else if requesting house info
        else if(from_std_in.equalsIgnoreCase("H")) {
            Bank_connection.getValue().println("Houses");
            output = "";
        }
        //else is connect to auction house
        else if(from_std_in.equalsIgnoreCase("C")){
            System.out.println("In the following format and order, please give: Port_Number Host_Name");
            String[] response = in_from_std.readLine().split(" ");

            if(response.length == 2){
                //make sure this house is not already connected to
                boolean clearedForConnect = true;//assume so
                for(String str : ports){
                    if(response[0].equalsIgnoreCase(str)){
                        clearedForConnect = false;
                        break;
                    }
                }
                if(clearedForConnect){
                    //try to parse
                    try {
                        int port = Integer.parseInt(response[0]);
                        //try to develop this connection
                        Socket socket_to_house = new Socket(response[1], port);

                        PrintWriter out_to_house =
                                new PrintWriter(socket_to_house.getOutputStream(), true);
                        BufferedReader in_from_house =
                                new BufferedReader(new InputStreamReader(socket_to_house.getInputStream()));

                        //add this to the lists of connections
                        ports.add(response[0]);
                        ConnectionPair house = new ConnectionPair(in_from_house, out_to_house);
                        HOUSE_CONNECTIONS.add(house);
                        LISTENERS.add(new AgentInbox(Bank_connection,house,bankAccountNumber));
                        output = "";//the output we want to display comes from the AgentInbox
                    }
                    catch (NumberFormatException | ConnectException | IndexOutOfBoundsException | UnknownHostException e1){
                        output = "Invalid port number and/or host name. Try again.";
                    }
                }
                else{
                    output = "Already connected to this house.";
                }
            }
            else {
                output = "Invalid input length. Give: Port_Number Host_Name";
            }
        }
        //else if close account and exit request
        else if(from_std_in.equalsIgnoreCase("X")){
            //sleep the bank Agent_Pack.AgentInbox to make sure that that the message from bank comes to here
            output = "...Processing exit request...";
            //say in the Bank connection Inbox thread that there has been an exit request
            Bank_connection.getValue().println("EXIT");
        }
        //else if requesting an item list from an auction house or to bid on an item in the auction house
        else if(from_std_in.charAt(0) == 'I' || from_std_in.charAt(0) == 'i' ||
                from_std_in.charAt(0) == 'B' || from_std_in.charAt(0) == 'b') {

            //first get all the digits
            for (int i = 1; i < from_std_in.length(); i++) {
                accumulateHouseNumber.append(from_std_in.charAt(i));
            }

            //try to parse which house number
            try {
                //now parse this to an int
                houseNumber = Integer.parseInt(accumulateHouseNumber.toString());
                //request the item presentation
                ConnectionPair houseConnection = null;
                //find the right house
                for(AgentInbox inbox : LISTENERS){
                    if(inbox.getHouse_Number() == houseNumber){
                        houseConnection = inbox.getPRIMARY_CONNECTION();
                    }
                }
                //if found the connection
                if(houseConnection != null){
                    //request
                    houseConnection.getValue().println("Items");
                    output = "";
                    //if this is a bid request
                    if (from_std_in.charAt(0) == 'B' || from_std_in.charAt(0) == 'b') {

                        //the items should be printed here, so allow a moment for this
                        TimeUnit.MILLISECONDS.sleep(1200);
                        System.out.println("In the following format and order, please give: " +
                                "Item_Number Amount_To_Bid");

                        String[] fromUser = in_from_std.readLine().split(" ");
                        //try to parse
                        if (fromUser.length == 2) {
                            //send this command to the house
                            String toSend = "Bid " + fromUser[0] + " " + fromUser[1] + " " + bankAccountNumber;
                            houseConnection.getValue().println(toSend);
                        }
                    }
                }
                else {
                    output = "Connection to house number " + accumulateHouseNumber.toString() + " not found. Perhaps " +
                            "this is the wrong number?";
                }
            }
            catch (NumberFormatException | InterruptedException e) {
                output = "Error. Try again";
            }
        }
        //else an items this agent owns request
        else if(from_std_in.equalsIgnoreCase("O")){

            if(PURCHASED_ITEMS.size() > 0){
                output = "You've bought\n" + Arrays.toString(PURCHASED_ITEMS.toArray());
            }
            else{
                output = "You currently own no auction items.";
            }
        }
        //else if this is a review bids request
        else if(from_std_in.equalsIgnoreCase("R")) {
            //traverse all the house connections and request a Review
            for (ConnectionPair hc : HOUSE_CONNECTIONS) {
                hc.getValue().println("REVIEW");
                //a review is coming back, to be printed from the listener, so wait a sec for it to come in
                TimeUnit.MILLISECONDS.sleep(400);
            }
            output = (HOUSE_CONNECTIONS.size() == 0) ? "You are not currently connected to any houses." : "";
        }
        return output;
        /*---------------Now some response will come back via a listener thread--------------------   */
    }

    /**
     * Walks the user through getting the house info. Does not let the method end until there
     * is a connected house
     * @param standard_in the reader that brings in user input
     * @param out_to_bank the printer to send to bank
     * @param in_from_bank the reader that brings in from the bank
     * @throws IOException when there is an error sending to or getting from the bank
     */
    public static void connectToFirstHouse(BufferedReader standard_in,
                                           PrintWriter out_to_bank, BufferedReader in_from_bank) throws IOException{
        //now this agent has an account
        String[] fromUser;
        //lets get the agent connected to some auction house
        String houses = "N";
        String[] housesByHouse = null;
        do{
            System.out.println("Now, for an Auction House...\n" +
                    "[H] Request Auction Houses from bank\n" +
                    "[X] Exit");
            fromUser = standard_in.readLine().split(" ");

            if(fromUser[0].equalsIgnoreCase("X")){
                out_to_bank.println("Close");
                String balance = in_from_bank.readLine();
                System.out.println("Thank you for joining the Distributed Auction." +
                        " Here is your balance back.\n" + balance);

                System.exit(1);
            }
            //if requesting houses
            else if(fromUser[0].equalsIgnoreCase("H")){
                out_to_bank.println("Houses");
                //either get N or a list of houses to connect to
                houses = in_from_bank.readLine();

                //if houses is still N, then no houses yet
                if(houses.charAt(0) == 'N'){
                    System.out.println("Currently there are no registered Auction Houses.\nWait awhile, then" +
                            " try again");
                }
                //else, parse it into being by house
                else{
                    housesByHouse = houses.split("#");
                }
            }
            //else, wrong input
            else {
                System.out.println("Oops. Try again");
            }
        } while (houses.charAt(0) == 'N');

        /*now this agent has an account and a list of houses to connect too, so now the real fun begins
         print the available houses            */
        System.out.println("Currently connected Auction Houses");
        //cannot make it here without housesByHouse being not null, but guard anyways to remove warning in the for loop
        if(housesByHouse != null){
            for (String house : housesByHouse) {
                System.out.println(house);
            }
        }
    }

    /**
     * Walks the user through opening a bank account
     * @param standard_in is the reader from the user
     * @param out_to_bank is the printer out to the bank
     * @param in_from_bank is the reader from the bank
     * @throws IOException when there is an error sending to or coming from the bank
     */
    public static void openAccount(BufferedReader standard_in, PrintWriter out_to_bank, BufferedReader in_from_bank)
            throws IOException{

        //lets create an account at the bank for this agent
        StringBuilder commandForBank = new StringBuilder("Create ");

        //get a name and a initial balance
        System.out.println("Welcome to Distributed Auction!\n" +
                "First, lets create an account at the bank.\n");

        String[] response;
        String[] fromUser;

        do{
            //get the name for the account
            System.out.println("What is your name?");
            String in =  standard_in.readLine();
            fromUser = in.split(" ");
            if(fromUser.length == 1 && in.charAt(0) != 13) {
                //initial balance
                System.out.println("Okay " + fromUser[0] + ", what will your initial balance be?");
                String[] fromUser2 = standard_in.readLine().split(" ");

                commandForBank.append(fromUser2[0]).append(" ").append(fromUser[0]);
                //send command to bank
                out_to_bank.println(commandForBank.toString());

                //get the banks response
                String response2 = in_from_bank.readLine();
                //print the banks response
                System.out.println("New account info\n" + response2);

                response = response2.split(" ");
                //if the user did not correctly create account, repeat
                if (response[0].equals(fromUser[0])) {
                    System.out.println("Let's try that again.");
                }
                //else, get the account number
                else {
                    bankAccountNumber = Integer.parseInt(response[2]);
                }
            }
            else{
                response = new String[1];
                response[0] = " ";
                fromUser = new String[1];
                fromUser[0] = " ";
                System.out.println("Let's try that again.");
            }

        }while (response[0].equals(fromUser[0]));
    }

    /**
     * Closes this agent and all its Agent_Pack.AgentInbox listeners
     * @param closeLast is the inbox that initiated the closing
     */
    public static void closeAgent(AgentInbox closeLast){

        for(AgentInbox close : LISTENERS){
            if(!close.equals(closeLast)){
                close.stop();
            }
        }
        //close bank account
        Bank_connection.getValue().println("Close");
        //end program
        System.exit(1);
    }

    /**
     * Adds an item to the List of purchased items.
     * @param item is the newly purchased item
     */
    public static void ownAnItem(String item) {
        PURCHASED_ITEMS.add(item);
    }

}
