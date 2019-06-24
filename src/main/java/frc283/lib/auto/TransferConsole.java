package frc283.lib.auto;

import java.util.function.Function;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

public class TransferConsole 
{
	public enum PCode
	{
		SAVE("save", "Save all phantom routes to the os, not just the active route."),
		DELETE("delete", "Deletes the route with the passed name. E.g: delete napalm_left_side."),
		COPY("copy", "Creates a copy of the passed route. E.g: copy napalm_left_side - this makes napalm_left_side_v2."),
		SET("set", "Sets the active route. Takes one parameter, the name of the route to be the new active route."),
		GET("get", "Returns the name of the active route."),
		CREATE("create", "Creates a new route. The parameters are [title - short description] [robot - intended bot] [desc - in-depth description of route] [role - driver/operator] [spacing - number of ms between measurements]"),
		START_RECORD("startrec", "Starts recording. Make sure you set a route first."),
		STOP_RECORD("stoprec", "Stops the recording."),
		OVERVIEW("overview", "Prints an overview of the given route. Pass 'all' to see an overview of all available routes. E.g: overview guillotine_center OR overview all."),
		HELP("help", "Describes all the available codes to pass."),
		STOP("stop", "Stops the console.");
		
		/** The code you type in to execute this function */
		private String codeStr;
		/** Short description printed when help is requested */
		private String desc;
		PCode(String codeStr, String desc)
		{
			this.codeStr = codeStr;
			this.desc = desc;
		}
		public String get()
		{
			return this.codeStr;
		}
		public String desc()
		{
			return this.desc;
		}
	}
	
	/** Name of the networktable used to transfer data to the robot */
	public static final String TABLE_NAME = "phantom_joystick";
	
	/** Printed to indicate that the console is ready to receive a command */
	private static final String OPEN_LINE_INDICATOR = "> ";
	
	/** Keyboard input */
	private Scanner keyboard;
	
	/**
	 * Ok, here's a breakdown on the structure of this data
	 * +---------------------+
	 * | SAVE | {f1, f2}     |
	 * | COPY | {f3}         |
	 * | SEND | {f4, f5, f6} |
	 * | .... | ...          |
	 * +---------------------+
	 * Where each f1, f2 is a function that takes a string[] of arguments, and returns a string to print to the console
	 */
	HashMap<PCode, ArrayList<Function<String[], String>>> listeners;
	
	/** Used to transfer all data over to the robot */
	private static NetworkTable nTable;
	
	public TransferConsole(boolean robotSide)
	{
		NetworkTableInstance nTableInst = NetworkTableInstance.getDefault();
		nTable = nTableInst.getTable(TABLE_NAME);
		keyboard = new Scanner(System.in);
		
		if (robotSide == true)
		{
			//
			
		}
		else //Laptop Side
		{
			//Open the console
			openStream();
		}
	}
	
	/**
	 * Triggers the passed code, executing all functions registered for it
	 * @param code - code to trigger functions for
	 * @param args - arguments collected from the command line
	 */
	private void trigger(PCode code, String[] args)
	{
		ArrayList<Function<String[], String>> listenFuncs = listeners.get(code);
		for (Function<String[], String> f : listenFuncs)
		{
			f.apply(args);
		}
	}
	
	/**
	 * Register a function to listen for a code
	 * @param code - The TransferConsole.code value to listen for to be executed
	 * @param listener - Function that executes with the passed arguments when the code is received
	 */
	public void register(PCode code, Function<String[], String> listener)
	{
		listeners.get(code).add(listener);
	}
	
	/**
	 * Prints a summary of function purposes to the system.out
	 */
	private void printHelp()
	{
		System.out.println("Here's a summary of codes and their purposes.");
		for (PCode s : PCode.values())
		{
			//20 dashes
			System.out.println("--------------------");
			System.out.println("| " + s.get() + " - " + s.desc());
		}
		System.out.println("--------------------");
	}
	
	private void openStream()
	{
		System.out.println("Console Open");
		readCodes();
	}
	
	private void readCodes()
	{
		boolean returnVal;
		do
		{
			System.out.println(OPEN_LINE_INDICATOR);
			returnVal = interpret(keyboard.nextLine());
		}
		while (returnVal == true);
	}
	
	private boolean interpret(String consoleInput)
	{
		String trimmedInput; //Input without start and end space
		String functionWord; //Word that indicates desired command
		String argWords;     //All args that follow are args
		
		//Remove leading and trailing spacing
		trimmedInput = consoleInput.trim();
		
		//Find the first space after a function keyword.
		int spaceIndex = trimmedInput.indexOf(" ");
		
		//IndexOf returns -1 if not found. Means it's a 1-word function like "stop"
		if (spaceIndex != -1)
		{
			//Grab everything from the start to the first space if it exists
			functionWord = trimmedInput.substring(0, spaceIndex);	
			
			//argWords are all the words that come after the main functionWord
			argWords = trimmedInput.substring(spaceIndex, trimmedInput.length());
		}
		else
		{
			//One one functions will just be the trimmed input
			functionWord = trimmedInput;
			
			//If it's a one-word function, no args
			argWords = "";
		}			
		
		//Trim out any excess middle space
		argWords = argWords.trim();
		
		//Stop all console looping if the code is stop
		if (functionWord == PCode.STOP.get())
		{
			System.out.println("Console Closed.");
			//Can any further looping
			return false;
		}
		else if(functionWord == PCode.HELP.get())
		{
			printHelp();
			return true;
		}
		else
		{
			//Go through each possible value of the enum
			for (PCode s : PCode.values())
			{
				//Gets set to true if the functionWord matches at least one function
				boolean aMatch = false;
				
				//If the functionWord matches one the commands
				if (functionWord == s.get())
				{
					aMatch = true;
					//System.out.println("Detected " + s.get());
					sendCode(s, new String[] {argWords});
				}
				
				//Warning if nothing matches. Is a little redundant with the above return statement, but eh
				if (aMatch == false)
				{
					System.out.println("Unknown code.");
				}
			}
			return true;
		}
	}
	
	/**
	 * Sends a code to the other end of the console
	 * Holds up the stream until a response is returned, then prints it
	 * 
	 * @param code - code to send
	 * @param args - arguments
	 */
	private void sendCode(PCode code, String[] args)
	{
		
	}
}