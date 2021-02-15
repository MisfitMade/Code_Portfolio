import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Is the Auction house object
 */
public class AuctionHouse {

    //the list of all the agents connected to this house
    private static final ArrayList<HouseConnection> ACTIVE_CLIENTS = new ArrayList<>();
    //the list of items currently up for auction
    private static ArrayList<Item> Items_Up_For_Auction;
    //the list of items that have been won but not paid for yet
    private static ArrayList<Item> Items_Finalizing;
    //the array of timers that tracks when a bid has timed out
    private static Timer[] itemTimers;
    //the account number for this house
    private static int houseAccountNumber;

    /**
     * The main method for House_Pack.AuctionHouse
     * @param args = Bank_Port Bank_Host_Name Port_Number_For_This_House
     * @throws InterruptedException when a connection error
     */
    public static void main(String[] args) throws InterruptedException{

        //the timer objects
        itemTimers = new Timer[3];
        itemTimers[0] = null; itemTimers[1] = null; itemTimers[2] = null;

        if(args.length == 3){

            int bank_port = Integer.parseInt(args[0]);
            String bank_host_name = args[1];
            int house_port = Integer.parseInt(args[2]);
            

            InetAddress ip = null;
            try {
                ip  = InetAddress.getLocalHost();
            }
            catch (IOException e){
                System.out.println("Connection Failed");
                System.exit(1);
            }

            String house_host_name = ip.getHostName();

            TimeUnit.SECONDS.sleep(10);
            //Try connecting to the bank
            try(Socket socketToBank = new Socket(bank_host_name, bank_port);
                PrintWriter out_To_Bank = new PrintWriter(socketToBank.getOutputStream(), true);
                BufferedReader bank_in = new BufferedReader(new InputStreamReader(socketToBank.getInputStream()))) {

                //report that there is a connection
                System.out.println(bank_in.readLine());
                //give A.H. info to the bank
                out_To_Bank.println("Address " + house_port + " " + house_host_name);
                System.out.println( bank_in.readLine());

                //create an account for this A.H.
                out_To_Bank.println("Create 0.0 "+ house_host_name);
                String returned = bank_in.readLine();
                System.out.println("New Account info\n" + returned);

                String[] returnedToParse = returned.split(" ");
                //get this house's account number
                houseAccountNumber = Integer.parseInt(returnedToParse[2]);

                /*then prepare for an agent to connect*/
                ServerSocket serverSocket = new ServerSocket(house_port);
                boolean keepRunning;
                do{
                    Socket clientSocket = serverSocket.accept();

                    keepRunning = clientSocket != null;//exit on error

                    if(keepRunning){
                        HouseConnection hc = new HouseConnection(clientSocket,houseAccountNumber,out_To_Bank,bank_in);
                        ACTIVE_CLIENTS.add(hc);

                        Thread agentConnection = new Thread(hc);
                        agentConnection.start();
                    }

                }while(keepRunning);
            }
            catch (IOException | NumberFormatException e){
                System.out.println("Connection lost. Please start over");
                System.exit(0);
            }
        }
        else{
            System.out.println("Must provide the bank port number, host name and an auction house port number, in that " +
                    "order.\n" +
                    "For example: java -jar AuctionHouse.jar 7999 b146-12 4500");
        }
    }

    /**
     * Produces a string of the Items currently up for auction and returns it
     * @return a String presentation of the items up for auction
     * @throws FileNotFoundException when it cannot find the file from which it generates items
     */
    public static String presentCurrentItemsForAuction() throws FileNotFoundException {
        //if there is no list of items yet
        if(Items_Up_For_Auction == null){
            Items_Up_For_Auction = new ArrayList<>();
        }
        //if items finalizing is null
        if(Items_Finalizing == null){
            Items_Finalizing = new ArrayList<>();
        }

        //if there is not currently 3 items up for auction + being finalized
        SecureRandom random = new SecureRandom();
        while(Items_Up_For_Auction.size() + Items_Finalizing.size() < 3){
            //add an item at random
            Items_Up_For_Auction.add(new Item(random.nextInt(83)));
            System.out.println("Item made");
        }

        //display the items to the agent
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < Items_Up_For_Auction.size(); i++){
            Item item = Items_Up_For_Auction.get(i);
            sb.append("Item ").append(i).append(") ").append(item.getItemID()).append(" | Description: ").
                    append(item.getDescription()).append(" | Minimum bid: ")
                    .append(String.format("%.2f | ", item.getMinimumBid())).append("Bidding state: ");

            String state = (item.isNeedsFinalized()) ? "Purchased, awaiting payment" : "Open";

            sb.append(state);

            sb.append("#");//tokenizer character
        }

        return sb.toString();
    }

    /**
     * Is the method that runs when a agent tries to bid on an item
     * @param itemNumber is the number of the item in the list
     * @param bid is the amount to bid
     * @param currentBidderAccountNumber is the account number of the bidder trying to make this bid
     * @param out_to_bank is the printer out to the bank
     * @return the response
     */
    public static String bidOnItem(int itemNumber, double bid, int currentBidderAccountNumber,
                                                PrintWriter out_to_bank){
        String output = "Failure. Bid too low or item has been purchased already. Review the items.";
        //first check if the bid is >= minimum bid
        Item toBidOn = Items_Up_For_Auction.get(itemNumber);
        synchronized (toBidOn){
            //in case the user gave a janky number like 11.054594598
            bid = Math.floor(bid * 100) / 100;
            //now
            System.out.println("Attempting to place bid on " + toBidOn.getItemID());
            //if the bid is enough and this item is not in a pending purchase state
            if(bid >= (Math.floor(toBidOn.getMinimumBid() * 100) / 100)
                    && !toBidOn.isNeedsFinalized()){

                System.out.println(bid + " is an eligible amount.");
                //Then this bid is okay

                //if there is already a bidder for this item, must inform them that they have been overtaken
                if(toBidOn.getCurrentBidHolder() != -1){
                    int theCurrentHolder = toBidOn.getCurrentBidHolder();
                    //find the connection for the agent that just lost the bid
                    for(HouseConnection hc : ACTIVE_CLIENTS){
                        if(hc.getAgentAccountNumber() == theCurrentHolder){
                            hc.printMessageToAgent("Outbid - Some other agent has placed a higher bid than you " +
                                    "on " + toBidOn.getItemID() + " from Auction House " + houseAccountNumber + "!!!");
                            //also, release the blocked funds for this agent for this item
                            hc.printMessageToBank("Unblock " + toBidOn.getItemID() + " " + toBidOn.getCurrentBid() +
                                    " " + theCurrentHolder);
                            break;//there can only be one
                        }
                    }
                }
                //if this agent already holds the highest bid
                if(toBidOn.getCurrentBidHolder() == currentBidderAccountNumber){
                    output = "Your bid is already the highest bid on " + toBidOn.getCurrentBid();
                }
                //else, make this agent the highest bid
                else{
                    toBidOn.setCurrentBid(bid);
                    //now this agent is the current bid holder
                    toBidOn.setCurrentBidHolder(currentBidderAccountNumber);
                    //make a new next minimum bid. Lets say, the current bid + 10 percent of how much the current bid is
                    double newMin = bid + (bid*0.1);
                    newMin = Math.floor(newMin * 100) / 100;
                    toBidOn.setMinimumBid(newMin);

                    //stop then start the timer for this items bid
                    if(itemTimers[itemNumber] != null){
                        itemTimers[itemNumber].cancel();
                    }

                    itemTimers[itemNumber] = new Timer();
                    FinalizeAuction timer = new FinalizeAuction(itemNumber);
                    itemTimers[itemNumber].schedule(timer, 30000);

                    output = "Success! Your bid of " + bid + " is now the highest on Item " +
                            itemNumber + ") " + toBidOn.getItemID();
                }
            }
            //else the bid is not enough, unblock this agent's funds
            else{
                out_to_bank.println("Unblock "+toBidOn.getItemID()+" "+bid+" "+currentBidderAccountNumber);
            }
        }

        return output;
    }

    /**
     * Adds an item to the list that holds items that have been won, but not yet paid for
     * @param buying is the item being paid for
     */
   public static void addAFinalizingItem(Item buying){
        if(Items_Finalizing == null){
            Items_Finalizing = new ArrayList<>();
        }
        if(!Items_Finalizing.contains(buying)){
            Items_Finalizing.add(buying);
        }
   }

    /**
     * Checks if this agent has any active bids
     * @param agentAccountNumber is the account to match against
     * @return the response
     */
   public static String checkThisAgentsBids(int agentAccountNumber){

        if(Items_Up_For_Auction == null){
            Items_Up_For_Auction = new ArrayList<>();
        }
        StringBuilder outputBuilder = new StringBuilder();
        for (Item currentItem : Items_Up_For_Auction) {
           if (currentItem.getCurrentBidHolder() == agentAccountNumber) {
               if (currentItem.isNeedsFinalized()) {
                   outputBuilder.append("Won ").append(currentItem.getItemID()).append("!! Authorize a transfer of ")
                           .append(currentItem.getCurrentBid()).append(" to account ID ").append(houseAccountNumber)
                            .append("#");//tokenizer char
               }
               else {
                   outputBuilder.append("A Bid on: ").append(currentItem.getItemID()).append(" of ").
                           append(currentItem.getCurrentBid()).append("#");//tokenizer char
               }
           }
       }
        //if there are bids for this agent at this house, return them, other wise
        return (outputBuilder.length()>0) ? outputBuilder.toString() :
                ("No current bids for this house#");//tokenizer char = #
   }

    /**
     * Figures out if an agent has won an item but not yet paid
     * @param agentAccountNumber is the account to check if it has won any bids, but not yet paid
     * @return the response
     */
   public static String doesThisAgentNeedToAuthorizeTransfer(int agentAccountNumber){
        //if there are items in the Items in need of finalizing list
       StringBuilder toFinalize = new StringBuilder("InNeedOf ");
       //guard with a null check
       if(Items_Finalizing != null && Items_Finalizing.size() > 0){
           for (Item currentItem : Items_Finalizing) {
               if (currentItem.getCurrentBidHolder() == agentAccountNumber) {
                   toFinalize.append(currentItem.getItemID()).append(" ").append(currentItem.getCurrentBid()).append(" ");
               }
           }
       }

       //if there are items that this agent needs finalizing, return them, else return V
       if(toFinalize.length() > 9){
           //append this houses account number onto the end
           toFinalize.append(houseAccountNumber);
           return toFinalize.toString();
       }

       return "V";
   }

    /**
     * After an item is paid for, the item is removed from all the places it is and replaced by a new item
     * in the Items_Up_For_Auction list
     * @param paid is the amount paid
     * @param buyerAccount is the account that purchased the item
     * @return the item that has been bought
     * @throws FileNotFoundException when the file to generate a new item from is not found
     */
   public static Item finalizeItem(double paid, int buyerAccount) throws FileNotFoundException{

        Item confirmed = null;
        //the item should be in Items_Finalizing
       for(Item confirmedBuy : Items_Finalizing){
           if(confirmedBuy.getCurrentBid() == paid && confirmedBuy.getCurrentBidHolder() == buyerAccount){
               confirmed = confirmedBuy;
               Items_Finalizing.remove(confirmedBuy);
               for(int i = 0; i < Items_Up_For_Auction.size(); i++){
                   if(Items_Up_For_Auction.get(i).equals(confirmedBuy)){
                       Items_Up_For_Auction.remove(confirmedBuy);
                       //add an item bak in this spot
                       SecureRandom secureRandom = new SecureRandom();
                       Items_Up_For_Auction.add(i, new Item(secureRandom.nextInt(83)));
                       itemTimers[i] = null;
                   }
               }
               break;
           }
       }
       return confirmed;
   }

    /**
     * Returns a particular House_Pack.Item up for auction name
     * @param itemNumber is the item number
     * @return the item's name
     */
    public static String getItemId(int itemNumber){
        return Items_Up_For_Auction.get(itemNumber).getItemID();
    }

    /**
     * Timer task that runs when an item is won
     */
    private static class FinalizeAuction extends TimerTask{

        private final int ITEM_TO_FINALIZE;

        /**
         * Constructor for timer task
         * @param ITEM_TO_FINALIZE is the item number
         */
        public FinalizeAuction(int ITEM_TO_FINALIZE){
            this.ITEM_TO_FINALIZE = ITEM_TO_FINALIZE;
        }

        /**
         * The procedure that runs when the timer expires
         */
        @Override
        public void run() {
            Item bought = Items_Up_For_Auction.get(ITEM_TO_FINALIZE);
            synchronized (bought){
                addAFinalizingItem(bought);
                bought.setNeedsFinalized(true);
                //inform the winner
                int agentAccountNumber = bought.getCurrentBidHolder();
                for(HouseConnection hc : ACTIVE_CLIENTS){
                    if(hc.getAgentAccountNumber() == agentAccountNumber){
                        hc.printMessageToAgent("You won Item " + ITEM_TO_FINALIZE + ") " + bought.getItemID() +
                                ". Authorize a fund transfer via the user options.");
                        break;//can only be one
                    }
                }
            }
        }
    }

}
