import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Bank{

    /*A unique instance*/
    private static HashMap<Integer, Account> ACCOUNTS;
    //list of all clients connected to this bank
    private static final ArrayList<BankConnection> ACTIVE_CLIENTS = new ArrayList<>();
    //list of house info for the agents
    private static ArrayList<AddressPair> AuctionHouse_Port_HostName;

    /**
     * Main for Bank_Pack.Bank
     * @param args -> Should be: port_num host_name
     * @throws IOException if the parsing of the port_num input fails
     */
    public static void main(String[] args) throws IOException{

        int clientIncrementer = 0;
        try{
            int port = Integer.parseInt(args[0]);
            ServerSocket server = new ServerSocket(port);
            System.out.println("Listening on port " + port);
            boolean keepRunning;
            do{
                Socket clientSocket = server.accept();

                keepRunning = clientSocket != null;
                BankConnection bc = new BankConnection(clientSocket, clientIncrementer);

                Thread connection = new Thread(bc);
                ACTIVE_CLIENTS.add(bc);

                connection.start();

                clientIncrementer++;
            }while (keepRunning);
        }
        catch (NumberFormatException e){
            System.out.println("Invalid port number\nForm is: jar Bank_Pack.Bank port_number host");
            System.exit(1);
        }
    }

    /**
     * Is used to block and unblock funds in an account
     * @param accountNumber is the account number of the funds to block or unblock
     * @param funds is the amount to block or unblock
     * @param purposeOfAccess is what dictates block or unblock
     * @param itemID is the item that this blocked fund would be for
     * @return String that is the repsonse to the block/unblock request
     */
    public static String blockOrUnblock(int accountNumber, double funds, String itemID, char purposeOfAccess){
        String output = "";

        //get account accountNumber
        Account accountToAccess = ACCOUNTS.get(accountNumber);
        synchronized (accountToAccess){
            //depending on the purpose of access
            switch (purposeOfAccess){
                //case block funds
                case 'B': {
                    /*Figure out if there is enough funds available to block*/
                    LinkedList<BidPair> currentBlockedFunds =
                            new LinkedList<>(accountToAccess.getBlockedFunds());

                    double currentBlockedSum = sumListOfFunds(currentBlockedFunds);

                    /*if blocking these funds does not block more than there is in the account*/
                    double availableBalance = accountToAccess.getAccountBalance() - currentBlockedSum;
                    double balanceIfBlockSuccessful = (availableBalance-funds);
                    if(balanceIfBlockSuccessful  >= 0){
                        //add funds to the blocked funds list for this account
                        currentBlockedFunds.add(new BidPair(itemID, funds));
                        accountToAccess.setBlockedFunds(currentBlockedFunds);

                        output = funds + " reserved. New available balance: " + balanceIfBlockSuccessful;
                    }
                    //else, not enough funds to block
                    else{
                        output = "X";//is interpreted on the other side of the connection
                    }
                    break;
                }
                //case unblock funds
                case 'U':{
                    /*remove funds from the list of blocked funds for this account*/
                    LinkedList<BidPair> currentBlockedFunds =
                            new LinkedList<>(accountToAccess.getBlockedFunds());

                    double toUnblockGiveOrTakePlus = funds + 0.011;
                    double toUnblockGiveOrTakeMinus = funds - 0.011;
                    for(BidPair pair : currentBlockedFunds){
                        if(pair.getKey().equalsIgnoreCase(itemID)){
                            if(pair.getValue() < toUnblockGiveOrTakePlus && pair.getValue() > toUnblockGiveOrTakeMinus){
                                currentBlockedFunds.remove(pair);
                                break;
                            }
                        }
                    }

                    accountToAccess.setBlockedFunds(currentBlockedFunds);
                    //message is of success
                    double newBal = ( accountToAccess.getAccountBalance() -
                            sumListOfFunds(accountToAccess.getBlockedFunds()) );
                    newBal = Math.floor(newBal * 100) / 100;
                    output = funds + " unblocked. New available balance: " + newBal;
                    break;
                }
            }
        }
        return output;
    }
    /**
     * Is the method used to transfer money from an agent's account to a house's
     * @param accountFromNumber is the number to withdraw from
     * @param toTransfer is the amount to transfer
     * @param accountToNumber is the number to deposit to
     * @param itemID is the name of the item being transferred for
     */
    public static String transfer(int accountFromNumber, double toTransfer, String itemID, int accountToNumber){
        String output = "";

        //get account accountNumber
        Account accountToTakeFrom = ACCOUNTS.get(accountFromNumber);
        synchronized (accountToTakeFrom) {
            //if it is ready for transfer, it is likely in list of blocked funds*/
            LinkedList<BidPair> currentBlockedFunds = new LinkedList<>(accountToTakeFrom.getBlockedFunds());
            boolean pairRemoved = false;
            //find this blocked fund. Having trouble getting doubles to be equal before and after parses
            double giveOrTakePlus = toTransfer + 0.011;
            double giveOrTakeMinus = toTransfer - 0.011;
            for(BidPair pair : currentBlockedFunds){
                //if the item ID matches and the blocked fund is +-0.009 from the toTransfer
                if(pair.getKey().equalsIgnoreCase(itemID) &&
                                pair.getValue() < giveOrTakePlus &&
                                    pair.getValue() > giveOrTakeMinus){
                    //remove the pair
                    currentBlockedFunds.remove(pair);
                    pairRemoved = true;
                    System.out.println(pair.getValue() + " " + pair.getKey() + " removed");
                }
            }
            //remove if it is
            if(pairRemoved){
                accountToTakeFrom.setBlockedFunds(currentBlockedFunds);
                //current balance
                double balance = accountToTakeFrom.getAccountBalance() - toTransfer;
                //new balance
                accountToTakeFrom.setAccountBalance(balance);

                output = String.format("WITHDRAW %.2f withdrawn. New balance: %.2f", toTransfer, balance);

                //get the account to deposit to
                Account accountToGiveTo = ACCOUNTS.get(accountToNumber);
                synchronized (accountToGiveTo){
                    double newBalance = accountToGiveTo.getAccountBalance();
                    newBalance += toTransfer;
                    accountToGiveTo.setAccountBalance(newBalance);

                    System.out.println("Withdrawn and deposited");
                }
            }
        }
        //return this message to the agent
        return output;
    }

    /**
     * Is the method that creates an account in the singleton pattern of a list of Accounts
     * @param name is the name on the account
     * @param balance is the initial balance
     * @param accountNumber is the unique connection number recycled as a bank account number
     */
    public static void createAccount(String name, double balance, int accountNumber){
        if(ACCOUNTS == null){
            ACCOUNTS = new HashMap<>();
        }
        /*Make new account*/
        Account newAccount = new Account(name,balance,accountNumber);

        ACCOUNTS.put(accountNumber, newAccount);
    }

    /**
     * Adds an auction house port and host name info to the list for the bank to share with the agents
     * @param port number of house
     * @param hostname of the house
     */
    public static void addAuctionHouse(int port, String hostname){
        if(AuctionHouse_Port_HostName == null){
            AuctionHouse_Port_HostName = new ArrayList<>();
        }

        //make sure this is not a house trying to reconnect
        boolean reconnect = false;
        for(AddressPair pair : AuctionHouse_Port_HostName){
            if(pair.getKey() == port){
                reconnect = true;
                break;
            }
        }
        //if not a reconnect
        if(!reconnect){
            AuctionHouse_Port_HostName.add(new AddressPair(port, hostname));
        }
    }

    /**
     * Gets the list of auction houses in a message form to parsed on the other side
     * @return list of houses
     */
    public static String getAuctionHouses(){
        //if there are no auction houses yet registered, return N
        StringBuilder sb = new StringBuilder("N");

        if(AuctionHouse_Port_HostName != null && AuctionHouse_Port_HostName.size() > 0){
            sb = new StringBuilder();
            for(AddressPair pair : AuctionHouse_Port_HostName){
                sb.append("Port: ").append(pair.getKey()).append(" | Host Name: ").append(pair.getValue()).append("#");
            }
        }
        return sb.toString();
    }

    /**
     * Sums the numbers in a list
     * @param list to sum the funds of
     * @return total sum
     */
    public static double sumListOfFunds(LinkedList<BidPair> list){
        double sum = 0.0;
        for(BidPair pair : list){
            sum += pair.getValue();
        }
        return sum;
    }

    /**
     * Returns the balance, both total and available of an account
     * @param accountNumber to check the balance of
     * @return String message of the balance
     */
    public static String accountBalance(int accountNumber){
        Account toCheck = ACCOUNTS.get(accountNumber);
        synchronized (toCheck){
            //total balance is total in account
            double balance = toCheck.getAccountBalance();
            //available is total - blocked funds
            double available = (balance - sumListOfFunds(toCheck.getBlockedFunds()));

            return String.format("Balance: $%.2f | Available: $%.2f", balance, available);
        }
    }

    /**
     * Closes out a particular bank account and its connection info
     * @param clientAccountNumber of account to close
     * @param toRemoveConnection connection to discard
     * @param toRemoveHouse House port and host name to remove
     * @return String that is ignored. By here
     */
    public static String closeAccount(int clientAccountNumber, BankConnection toRemoveConnection,
                                      AddressPair toRemoveHouse) {
        String output = "Cannot close account!";
        //make sure this account does not currently have any blocked funds
        if(ACCOUNTS != null && ACCOUNTS.get(clientAccountNumber).getBlockedFunds().size() == 0){
            output = "Before: " + ACCOUNTS.size() + " accounts. After: ";
            //remove it and the connection it was using from the list of connections
            ACCOUNTS.remove(clientAccountNumber);
            //if successful, then can close account
            output += ACCOUNTS.size() + " accounts.";

            System.out.println(output);//report if the account was closed/removed

            //to send back to the account closer
            output = "Account closed.";
            ACTIVE_CLIENTS.remove(toRemoveConnection);
            if(toRemoveHouse != null){
                for(AddressPair pair : AuctionHouse_Port_HostName){
                    //if this is the address pair to remove
                    if(pair.getKey().equals(toRemoveHouse.getKey())){
                        AuctionHouse_Port_HostName.remove(pair);
                        break;//can only be one
                    }
                }
            }
        }
        return output;
    }

    /**
     * Is a Bank_Pack.Bank Account Object
     * Inner Class
     */
    public static class Account {

        private double accountBalance;
        private LinkedList<BidPair>  blockedFunds = new LinkedList<>();

        /**
         * Creates a new account
         * @param name on the account -> Unused but left in in case needed it later
         * @param initialBalance to open the account with
         * @param accountNumber the number on this account -> Unused because of the way I stored the accounts, but
         *                      have left it in in case it is needed later
         */
        public Account(String name, double initialBalance, int accountNumber){
            //floor then re-multiply to remove any weird numbers like 12.3265
            accountBalance = Math.floor(initialBalance * 100) / 100;
        }

        /**
         * Gets the account balance
         * @return the balance
         */
        public double getAccountBalance() {
            return accountBalance;
        }

        /**
         * Set the account balance
         * @param accountBalance is the new balance
         */
        public void setAccountBalance(double accountBalance) {
            this.accountBalance = Math.floor(accountBalance * 100) / 100;
        }

        /**
         * Gets the list of amounts currently unavailable from total balance
         * @return the list
         */
        public LinkedList<BidPair>  getBlockedFunds() {
            return blockedFunds;
        }

        /**
         * Used for setting new blocked funds
         * @param blockedFunds is the new list of blocked funds
         */
        public void setBlockedFunds(LinkedList<BidPair> blockedFunds) {
            this.blockedFunds = blockedFunds;
        }

        //the way i store the accounts made it so that i did not need to have a getAccountNumber method
    }

}
