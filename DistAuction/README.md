---------------------------------------Author-----------------------------------------------------
Michael Nafe | nafem@unm.edu | CS351


------------------------------------Introduction---------------------------------------------------
This program is made up of 3 parts, the Bank, AuctionHouse and Agent, each having their own pack.
The Bank is a server to both the AuctionHouse and Agents, while the AuctionHouse is a client to 
the bank, as well as a server to the Agent. Agent is only a client. The server information for
the Bank is known by all parts of the program, known as in it is given to each part of the 
program via command line arguments.


-----------------------------------Files_Involved---------------------------------------------------
Agent_Pack
	Agent.jar			-> The jar file for the Agent program
	Agent.java			-> The main class for this program
	AgentInbox.java		-> The thread that listens for input from either the Bank or an AuctionHouse
	ConnectionPair.java -> Is a Pair<> object I made since Pair<> is not part of CS machines java 8
	
Bank_Pack
	Bank.jar			-> The jar file for the Bank program
	AddressPair.java	-> Is a Pair<> object I made since Pair<> is not part of CS machines java 8
	Bank.java			-> The main class for this program
	BankConnection.java	-> The thread that maintains a connection with either an AuctionHouse or Agent 
	BidPair.java		-> Is a Pair<> object I made since Pair<> is not part of CS machines java 8
	
House_Pack
	AuctionHouse.jar	-> The jar file for the AuctionHouse program. Does not work because I cannot
						   get it to read from a file properly. The AuctionHouse program works if
						   you runs it by doing java AuctionHouse, rather than java -jar AuctionHouse.jar
	AuctionHouse.java	-> The main class for this program
	HouseConnection.java-> The thread that maintains a connection with an agent
	Item.java			-> Is the item object that an AuctionHouse auctions off
	items.txt			-> The file from where items are randomly generated. This file is just a line
						   by line list of Item_Name Description
						   
						   
--------------------------------------To_Run----------------------------------------------------------
Must start that Bank first, either with the jar or doing javac then java, with the command line args: 
Port_Number_For_Bank localhost.

Then, can start either the AuctionHouse or Agent. 

If starting the AuctionHouse, must have the House_Pack folder as it is laid out in the LoboGit in order 
for the items.txt file to be found and must do:
javac AuctionHouse.java
java AuctionHouse Port_Number_For_Bank Host_Name_For_Bank Port_Number_For_This_Auction_House

If starting the Agent, can use either the jar or do javac then jav, with the command line args:
Port_Number_For_Bank Host_Name_For_Bank


-------------------------------------------Bugs--------------------------------------------------------
The jar file does not work for the AuctionHouse because it cannot find the items.txt file. I ran out
of time to figure it out. When I do tests locally and I disconnect an AuctionHouse via just 'X'ing out,
the Bank detects this disconnection and removes the disconnected AuctionHouse from the list of valid 
houses to connect to, but when I disconnect an AuctionHouse on the CS machines, the Bank does not detect
this disconnection and the Port and Address info remains at the Bank as a possible AuctionHouse to 
connect to.

	