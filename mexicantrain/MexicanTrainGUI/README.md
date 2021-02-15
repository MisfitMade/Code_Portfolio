--------------------------Author-----------------------------------
Michael Nafe | nafem@unm.edu | CS351

------------------------Introduction-------------------------------
This program, MexicanTrainGUI, is the domino game Mexican Train. 
This version provides a GUI to play the game through.

------------------------Files Involved------------------------------
Main.java					-> Has the main method and makes the "How 
							   Many Players" window pop up.
					 
GetNumberOfPlayers.java		-> Pop-up window at the start of the program
							   that gets the number of humans and number
							   of computers specs from the user.
							  
Controller.java				-> Defines all the EventHandlers used by the
							   buttons in the display. Also, controls the 
							   transition between players and rounds.
							   
Display.java				-> Is the GUI. Has some logic for 
							   opening/closing trains and showing human's 
							   trays and setting the labels that inform
							   the user of game actions.
							   
Table.java					-> This is the gameboard/table which holds all
							   train statuses, as well as most of the logic
							   involved for the computers when they attempt 
							   to make a play. Also the logic that determines
							   if a human has a play.
							   
Player.java					-> This is the object that represents a player,
							   both humans and computers.
							   
Boneyard.java				-> This is the boneyard object. Generates all the
							   dominoes. All dominoes in the game are funneled
						       through the boneyard.
							  
Domino.java					-> This is the domino object, having a left and 
							   right side, a graphic, plus some other features.
							   The graphic consists of two 'DominoHalf's.
							  
DominoHalf.java				-> This is an oject that generates a half of a Domino
							   for the Domino's graphic. Each Domino graphic has 
							   two of these.

---------------------To Work User Interface-------------------------
It is pretty intuitive. The tray has dominoes which are buttons. 
Click a domino you want to interact with, then click one of the 
action buttons: Flip, Play Here. 
In the display, a blue Play Here button means that the player 
whose turn it is must play there next as a double was just played there.
A yellow train means that train is open. 
The info on the blue buttons and yellow trains is displayed on the GUI.

-----------------------------Bugs-----------------------------------
1) 	Perhaps it is a bug that I cannot 
	get the Human's tray's buttons, the toggle buttons, to be sized
	more closely to the size of the dominoes which are their graphics.
	
2) 	Perhaps it is a bug that sometimes a toggle button in the Human's 
	trays looks selected even though it is not. If it looks selected 
	and a player tries to make a move with it, the GUI informs them 
	to "make sure you have a domino selected."


-------------------------Enhancements-------------------------------
I know that it could be faster to do:
When the computer has no play, draws a domino and then tries to play
again, they only check the valid trains with the newly drawn domino
instead of rechecking its entire hand. If there is time, I will
implement this.