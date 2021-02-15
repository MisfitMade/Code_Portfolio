--------------------------Author-----------------------------------
Michael Nafe | nafem@unm.edu | CS351

------------------------Introduction-------------------------------
This program, MexicanTrain, is the domino game Mexican Train. 
This version uses standard in/out to play, no GUI.

------------------------Files Involved------------------------------
Main.java				-> The file with the main method. Handles
						   some of the recieving and parsing of user
						   input. Also calculates the winner.

Player.java				-> This is the object that represents a player,
						   both humans and computers.
						  
Domino.java				-> This is the domino object, having a left and 
						   right side, plus some other features.

Table.java				-> This is the gameboard/table which holds all
						   train statuses, as well as most of the logic
						   involved for both the humans and computers 
						   when they attempt to make a play.
	
Controller.java			-> This is the file that controls the game loops,
						   both between players and between rounds. Also 
						   hndles alot receiving, parsing and validating
						   user input.
						
Boneyard.java			-> This is the boneyard object. Generates all the
						   dominoes. All dominoes in the game are funneled
						   through the boneyard.
						   
						   

-----------------------------Bugs------------------------------------
Surely something, but none known.


-------------------------Enhancements-------------------------------
I know that it could be faster to do:
When the computer has no play, draws a domino and then tries to play
again, they only check the valid trains with the newly drawn domino
instead of rechecking its entire hand. If there is time, I will
implement this.
						   