package frc283.lib.term;

import java.util.Scanner;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Do not change the name of this class.
 * Do not change the name of this class.
 * Do not change the name of this class.
 * 
 * A class that is used laptop-side to execute phantomdriver robot functions
 */
public final class PhantomTerminal
{
	public static final String TABLE_NAME = "phantom_terminal";

	//The networktable keys
	public static final String CODE_KEY = "code";
	public static final String ARGS_KEY = "args";
	public static final String COMMAND_RECEIVED_KEY = "command_received";
	public static final String REPLY_KEY = "reply";
	
	public static final String INPUT_PROMPT = ">";
	
	public static final String START_PROMPT = "Input your command. Type 'help' to view commands. Type 'exit' or close the window to kill the terminal.";
	
	//If the robot doesnt respond in 10 seconds, it must not have received the command
	public static final double TIMEOUT_DURATION = 10.0;
	
	//private static NetworkTableEntry codeEntry;
	//private static NetworkTableEntry argsEntry;
	//private static NetworkTableEntry commandReceivedEntry;
	//private static NetworkTableEntry replyEntry;
	
	//Used to watch for command timeouts
	private static long previousMilliseconds;
	
	private static Scanner keyboard;
	
	/** Prevents instantiation */
	private PhantomTerminal() {}
	
	public static void main(String... strings)
	{
		keyboard = new Scanner(System.in);
		
		//Initialize the networktable helper variable
		//NetworkTable nTable = NetworkTableInstance.getDefault().getTable(TABLE_NAME);
		
		//Set default entry values
		/*codeEntry = nTable.getEntry(CODE_KEY);
		codeEntry.setDefaultString("");
		argsEntry = nTable.getEntry(ARGS_KEY);
		argsEntry.setDefaultStringArray(new String[0]);
		
		//True to begin with because the program will wait until the last command has been received before sending a new one
		commandReceivedEntry = nTable.getEntry(COMMAND_RECEIVED_KEY);
		commandReceivedEntry.setDefaultBoolean(true);
		replyEntry = nTable.getEntry(REPLY_KEY);
		replyEntry.setDefaultString("");*/
		
		System.out.println(START_PROMPT);
		
		String currentLine;
		boolean continueLoop = true;
		do
		{
			System.out.print(INPUT_PROMPT);
			
			currentLine = keyboard.nextLine();
			
			currentLine = currentLine.trim();
			
			//Split the line into commands and arguments
			String[] sections = currentLine.split(" ");
			
			PhantomCommandType command = interpret(sections[0]);
			
			if (command == null)
			{
				System.out.println("'" + sections[0] + "' was not recognized as a command.");
			}
			else
			{
				switch (command)
				{
					default:
						//Not a recognized command
					break;
					case HELP:
						doHelp(sections);
					break;
					case DELETE:
						doDelete(sections);
					break;
					case SET:
						doSet(sections);
					break;
					case GET:
						doGet(sections);
					break;
					case CREATE:
						doCreate(sections);
					break;
					case RECORD:
						doRecord(sections);
					break;
					case OVERVIEW:
						doOverview(sections);
					break;
					case SAVE:
						doSave(sections);
					break;
					case COPY:
						doCopy(sections);
					break;
					case EXIT:
						continueLoop = doExit();
					break;
				}
			}
		}
		while (continueLoop);
	}
	
	private static PhantomCommandType interpret(String code)
	{
		//Go through each possible command type
		for (PhantomCommandType c : PhantomCommandType.values())
		{
			//If its code matches matches the passed string
			if (c.getCode().equals(code))
			{
				//Return that command
				return c;
			}
		}
		
		//Otherwise return null
		return null;
	}
	
	private static void doCommand(PhantomCommandType type, String[] args)
	{
		//codeEntry.setString(type.getCode());
		//argsEntry.setStringArray(args);
		
		//Becomes set to true by the robot end when the command is received
		//commandReceivedEntry.setBoolean(false);
		
		//Start the timer - a new command has just been "sent"
		previousMilliseconds = System.currentTimeMillis();
		
		//Gets set to true when the command times out
		boolean timeout = false;
		
		//Wait for the command_received key to be set to true
		//while (commandReceivedEntry.getBoolean(true) == false)
		{
			//If more than X seconds have passed
			if ((System.currentTimeMillis() - previousMilliseconds) > (TIMEOUT_DURATION * 1000))
			{
				timeout = true;
				//break;
			}
		}
		
		if (timeout == true)
		{
			System.out.println("Timeout. The robot failed to respond within " + TIMEOUT_DURATION + " seconds.");
			System.out.println("The command '" + type.getCode() + "' failed. Please enter a new command.");
		}
		else
		{
			//When the received key is set to true, the reply has been updated to the proper value
			//The program can now continue
		}
	}
	
	private static void doHelp(String[] args)
	{
		
	}
	
	private static void doDelete(String[] args)
	{
		
	}
	
	private static void doSet(String[] args)
	{
		
	}
	
	private static void doGet(String[] args)
	{
		
	}
	
	private static void doCreate(String[] args)
	{
		
	}
	
	private static void doRecord(String[] args)
	{
		
	}
	
	private static void doOverview(String[] args)
	{
		
	}
	
	private static void doSave(String[] args)
	{
		
	}
	
	private static void doCopy(String[] args)
	{
		
	}
	
	private static boolean doExit()
	{
		return false;
	}
}
