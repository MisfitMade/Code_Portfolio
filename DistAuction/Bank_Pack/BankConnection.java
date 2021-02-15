import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class BankConnection implements Runnable{

    private final PrintWriter OUT;//out to this connection
    private final BufferedReader IN;//in from this connection
    private final int CLIENT_ACCOUNT_NUMBER;
    private AddressPair PORT_ADDRESS;
    private boolean keepRunning;

    /**
     * Is a thread object that represents the bank being connected to one client. Each client gets its own one of these
     * @param client is the Socket connected
     * @param clientID is a unique to this client number and ends up as the bank account number
     * @throws IOException if connection fails
     */
    public BankConnection(Socket client, int clientID) throws IOException{
        CLIENT_ACCOUNT_NUMBER = clientID;
        OUT = new PrintWriter(client.getOutputStream(), true);
        IN = new BufferedReader(new InputStreamReader(client.getInputStream()));

        PORT_ADDRESS = null;//is given a value later if this is a connection with a house

        keepRunning = true;
        OUT.println("Connected to the bank");
    }

    /**
     * The first word in messages coming from an Agent_Pack.Agent or a House is one of these
     */
    private enum Commands {
        HOUSES, BALANCE, EXIT, TRANSFER,//Agent_Pack.Agent commands
        CREATE, CLOSE,         //shared commands
        BLOCK, UNBLOCK, ADDRESS //Auction House commands
    }

    /**
     * The thread's run. Listens for input, processes it, sends a message back
     */
    @Override
    public void run(){
        String inputLine;
        String outputLine;
        do{
            try {
                inputLine = IN.readLine();
                if(inputLine != null){
                    System.out.println("BC Processing: " + inputLine + " from client with account number "
                            + CLIENT_ACCOUNT_NUMBER);
                    outputLine = processLine(inputLine);

                    if(outputLine != null){
                        System.out.println("Transmitting: " + outputLine + " to client with account number " +
                                CLIENT_ACCOUNT_NUMBER);
                        OUT.println(outputLine);
                    }
                }
            }
            catch (IOException | NullPointerException e){
                //inform that this connection has been ceased, and then stop this thread
                System.out.println("Connection to client with account number " +
                        CLIENT_ACCOUNT_NUMBER + " lost.");// Attempting to close account.");

                //if this is a house connection, close its account
                if(PORT_ADDRESS != null){
                    Bank.closeAccount(CLIENT_ACCOUNT_NUMBER,this,PORT_ADDRESS);
                }

                keepRunning = false;
            }
        } while (keepRunning);
    }

    /**
     * Processes an input line from an Agent_Pack.Agent or a House. The inputs are formed by the code based off of user input.
     * User input is not passed directly to the bank connection
     * @param inputLine is what is processed
     * @return A String response
     */
    private String processLine(String inputLine) {
        String[] inputs = inputLine.split(" ");
        String output = null;

        //If this is request of one word
        if(inputs.length == 1) {
            String command = inputs[0];
            /*if list of auction house requests*/
            if (command.equalsIgnoreCase(Commands.HOUSES.name())) {
               output = Bank.getAuctionHouses();
            }
            //if this is a balance request
            else if(command.equalsIgnoreCase(Commands.BALANCE.name())){
                output = Bank.accountBalance(CLIENT_ACCOUNT_NUMBER);
            }
            //else if an account is being closed
            else if(command.equalsIgnoreCase(Commands.CLOSE.name())){
                output = Bank.closeAccount(CLIENT_ACCOUNT_NUMBER,this,null);
            }
            //else if Exit request
            else if(command.equalsIgnoreCase(Commands.EXIT.name())){
                String[] exitRequest = Bank.accountBalance(CLIENT_ACCOUNT_NUMBER).split(" ");

                System.out.println(Arrays.toString(exitRequest));
                //if balance = available balance, then exit is okay
                output = (exitRequest[1].equals(exitRequest[4])) ? "EXIT EXIT" : "EXIT X";
            }
        }
        else if(inputs.length == 3){
            String command = inputs[0];
            /*If command is CREATE: Either is Agent_Pack.Agent or A.H. and is
            Create initialBalance accountName*/
            if(command.equalsIgnoreCase(Commands.CREATE.name())){

                double initialBalance = parseFunds(inputs[1]);
                //if a valid initial balance was given and there is also some account name
                if(initialBalance >= 0.00){
                    String accountName = inputs[2];
                    Bank.createAccount(accountName, initialBalance, CLIENT_ACCOUNT_NUMBER);
                    output = "Account ID: " + CLIENT_ACCOUNT_NUMBER + " | Account Name: " + accountName + " | Balance: "
                                    + initialBalance;
                }
                else{
                    output = inputs[1] + " is an invalid initial balance.";
                }
            }
            //if command Address: Is an A.H. command
            else if(command.equalsIgnoreCase(Commands.ADDRESS.name())){
                //then this is an auction house's port number and host name
                int port = parseInteger(inputs[1]);
                String hostName = inputs[2];
                PORT_ADDRESS = new AddressPair(port, hostName);
                Bank.addAuctionHouse(port, hostName);
                output = "Added Auction House on port " + port + " at " + hostName;
            }
        }
        //else block or unblock command
        else if(inputs.length == 4){
            String command = inputs[0];
            //if command is BLOCK: Is A.H. command
            if(command.equalsIgnoreCase(Commands.BLOCK.name())){
                double amountToBlock = parseFunds(inputs[2]);
                int accountNumber = parseInteger(inputs[3]);
                if(amountToBlock > 0.00 && accountNumber > -1){
                    output = Bank.blockOrUnblock(accountNumber, amountToBlock, inputs[1],'B');
                }
                else{
                    output = "E";
                }
            }
            //if command is Unblock: Is A.H. command
            else if(command.equalsIgnoreCase(Commands.UNBLOCK.name())){
                double amountToUnblock = parseFunds(inputs[2]);
                int accountNumber = parseInteger(inputs[3]);
                if(amountToUnblock > 0.00 && accountNumber > -1){
                    output = Bank.blockOrUnblock(accountNumber, amountToUnblock, inputs[1], 'U');
                }
                else{
                    output = "Invalid amount to unblock or account number.";
                }
            }
            //else if a transfer command
            else if(command.equalsIgnoreCase(Commands.TRANSFER.name())) {
                double amountToTransfer = parseFunds(inputs[2]);
                int accountTo = parseInteger(inputs[3]);

                if (amountToTransfer > 0.00 && accountTo > -1) {
                    output = Bank.transfer(CLIENT_ACCOUNT_NUMBER, amountToTransfer, inputs[1], accountTo);
                }
                else {
                    output = "Invalid Transfer command. Try again.";
                }
            }
        }

        return output;
    }

    /**
     * Parses a string to a double
     * @param input String
     * @return double = to some amount of money
     * @throws NumberFormatException if parsing fails
     */
    private double parseFunds(String input) throws NumberFormatException {
        double funds = -1.0;
        funds = Double.parseDouble(input);
        return funds;
    }

    /**
     * Parses a string to an int
     * @param input String
     * @return An int = to a house or account number
     * @throws NumberFormatException if parsing fails
     */
    private int parseInteger(String input) throws NumberFormatException{
        int accountNumber = -1;
        accountNumber = Integer.parseInt(input);
        return accountNumber;
    }

    /**
     * Returns the client account number of the client on this connection thread
     * @return int = account number
     */
    public int getCLIENT_ACCOUNT_NUMBER() {
        return CLIENT_ACCOUNT_NUMBER;
    }

    /**
     * Sends a message to the client of this connection
     * @param message to send
     */
    public void printMessageToClient(String message){
        OUT.println(message);
    }
}
