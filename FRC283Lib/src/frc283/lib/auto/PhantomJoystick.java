package frc283.lib.auto;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Timer;

/**
 * Second version, using the PhantomRoute wrapper class, as well as gson data encoding
 * 
 * This class is a jukebox. It contains all the discs, and can play the discs, as well as record new discs and record over old discs
 * When you create a PhantomJoystick, it will find all .route files stored on the system, not just the designated save folder
 * 
 * Note: nothing is static on this class because we trawl for Routes when constructing a joystick
 *
 * Terms:
 *     Active Route: The PhantomRoute currently being played and/or recorded over. "Route" (singular) in function names usually refers to this
 *     Stored Route: All PhantomRoutes that were found on the file system or just created this session. Includes the active route. "Routes" (plural) usually refers to these
 *     Playback: The process of playing back all the joystick data
 *     Recording: The process of actually recording the joystick data
 *     
 * TODO; prevent duplicate routes
 * TODO: enable/disable printsouts. For real. nEEDED
 * TODO: reversing function
 */
public class PhantomJoystick
{
	/** The folder where all NEW routes are saved. It's possible that some old routes fell outside this folder. Should not end with a slash */
	public final static String routeFolder = "/home/lvuser/frc/routes";
	
	/** The folder that is searched for all .route files. Should be as high up in the file system as possible */
	public final static String ROOT_SEARCH_FOLDER = "/home";
	
	/** True when playing back the data */
	private boolean playback = false;
	
	/** True when recording the data */
	private boolean recording = false;
	
	/** Allows printouts when functions execute. Can disable with disablePrintouts() */
	private boolean allowPrintouts = true;
	
	/** Used to mete out recording and playback */
	private Timer timer;
	
	/** The route currently being written/read to/from] */
	private PhantomRoute activeRoute;
	
	/** Used to control indexing during playback */
	private int playbackIndex = 0;
	
	/** Joystick where values are watched during recording */
	private Joystick recordingJoystick;
	
	/** Contains all PhantomRoutes found all the system */
	private HashMap<String, PhantomRoute> storedRoutes;
	
	public PhantomJoystick(Joystick recordingJoystick)
	{
		storedRoutes = new HashMap<String, PhantomRoute>();
		
		timer = new Timer();
		
		this.recordingJoystick = recordingJoystick;
		
		//Create a directory representation, and start iterating through it for .route files
		createPhantomRoutes(new File(PhantomJoystick.ROOT_SEARCH_FOLDER).listFiles());
	}
	
	/**
	 * Searches a directory for .route files then makes PhantomRoute wrappers for them
	 * @param files
	 */
	private void createPhantomRoutes(File[] files)
	{
		if (files != null)
		{
			for (File singleFile : files)
			{
				if (singleFile.isDirectory())
				{
					//If it's a directory, then make another call to this function to also iterate through THAT directory
					createPhantomRoutes(singleFile.listFiles());
				}
				else
				{
					//Position of the "." in the file name
					int dotIndex = singleFile.getName().lastIndexOf(".") + 1; 
					
					//Grab the "route" part of "file.route" (or any other other file extension, like "txt")
					String extension = singleFile.getName().substring(dotIndex, singleFile.getName().length());
					if (extension.equalsIgnoreCase("route"))
					{
						//Create a PhantomRoute for this .route file
						PhantomRoute newPhantomRoute = new PhantomRoute(singleFile.getAbsolutePath());
						
						//Push that route onto the storedRoutes
						storedRoutes.put(newPhantomRoute.getName(), newPhantomRoute);
					}
				}
			}
		}
		else
		{
			print("No routes found. Create a route or most functions will not work correctly.");
		}
	}
	
	/**
	 * Helps control printouts and standardize them
	 * Not every printout in this class needs to use this
	 * @param input - will print this with some sort of fixture in front or behind it
	 */
	private void print(String input)
	{
		if (allowPrintouts == true)
		{
			System.out.println("PhantomJoystick: " + input);
		}
	}
	
	/**
	 * Stops functions from echoing when called.
	 * Printouts are enabled by default and cannot be re-enabled after calling this
	 */
	public void disablePrintouts()
	{
		allowPrintouts = false;
	}
	
	/**
	 * Save each PhantomRoute
	 */
	public void saveRoutes()
	{
		print("Saving routes.");
		//Iterates through each PhantomRoute and saves it
		for (PhantomRoute pr : storedRoutes.values())
		{
			pr.save();
		}
	}
	
	/**
	 * 
	 * @param title
	 * @param robot
	 * @param desc
	 * @param role
	 * @param timeSpacing - Must be at least 30ms
	 */
	public void createRoute(String title, String robot, String desc, String role)
	{
		//Ensures that the route folder exists
		File folder = new File(routeFolder);
		//Create a new PhantomRoute file
		PhantomRoute newPhantomRoute = new PhantomRoute(title, robot, desc, role, routeFolder);
		//Add this new route to the index
		storedRoutes.put(newPhantomRoute.getName(), newPhantomRoute);
	}
	
	/**
	 * Cannot be used during playback or recording
	 * @param routeName - name of the route to set to being active
	 */
	public void setActiveRoute(String routeName)
	{
		activeRoute = storedRoutes.get(routeName);
		//print("Active route is now " + routeName + ".");
	}
	
	/**
	 * @return - the name of the current active string
	 */
	public String getActiveRouteName()
	{
		return activeRoute.getName();
	}
	
	/**
	 * @param channel - the axis number to get the value for
	 * @return - the most appropriate value for the current time since playback started
	 */
	public double getRawAxis(int channel)
	{
		if (playback == true)
		{
			//Convert the playbackTime to an index value for that time
			int playbackIndex = activeRoute.indexFromTime((int)(timer.get() * 1000));
			
			//If its a valid index
			if (playbackIndex <= activeRoute.lastIndex())
			{
				//Return the data at that index
				return activeRoute.getAnalog(channel, playbackIndex);
			}
			else
			{
				System.out.println("Playback has ended.");
				playbackStop();
				return 0;
			}
		}
		else
		{
			//System.err.println("PhantomJoystick.getRawAxis: Playback has ended. You must call playbackInit to initiate playback");
			return 0;
		}
	}
	
	/**
	 * @param channel - the button number to get the value for
	 * @return - the most appropriate value for the current time since playback started
	 */
	public boolean getRawButton(int channel)
	{
		if (playback == true)
		{
			//Convert the playbackTime to an index value for that time
			int playbackIndex = activeRoute.indexFromTime((int)(timer.get() * 1000));
			
			//Return the data at that index
			return activeRoute.getDigital(channel, playbackIndex);
		}
		else
		{
			//System.err.println("PhantomJoystick.getRawAxis: Playback has ended. You must call playbackInit to initiate playback");
			return false;
		}
	}
	
	/**
	 * Initiates recording. Values from the passed joystick will be watched
	 * @param override - Clears out the data first if passed
	 */
	public void recordInit(boolean override)
	{
		if (playback == false)
		{
			if (override)
			{
				this.clearRoute();
			}
			print("Recording started.");
			timer.reset();
			timer.start();
			recording = true;
		}
	}
	
	/**
	 * Initiates recording. Values from the passed joystick will be watched
	 * WILL OVERRIDE EXSITING DATA
	 * This is the same as calling recordInit(true). See function definition
	 */
	public void recordInit()
	{
		this.recordInit(true);
	}
	
	/**
	 * Records joystick values at proper times. Must be called rapidly and periodically to function
	 * This function appends data onto the end of the timelines
	 */
	public void recordPeriodic()
	{
		//System.out.println("PhantomJoystick.recording: " + recording);
		if (recording == true)
		{
			//Contains all the analog values from this measurement cycle
			Double[] analogValues = new Double[RouteData.analogChannelCount];
			
			//For each possible analog input
			for (int a = 0; a < analogValues.length; a++)
			{
				//Populate the array with axis values
				System.out.println("joystick@axis:" + a + ": " + recordingJoystick.getRawAxis(a));
				analogValues[a] = recordingJoystick.getRawAxis(a);
			}
			
			//Contains all the digital values from this measurement cycle
			Boolean[] digitalValues = new Boolean[RouteData.digitalChannelCount];
			
			//For each possible digital input
			for (int d = 0; d < digitalValues.length; d++)
			{
				//Populate the array with button values
				digitalValues[d] = recordingJoystick.getRawButton(d + 1);
			}
			
			//The milliseconds that have passed since last measurement
			int msSinceLastMeasurement = (int)(timer.get() * 1000);
			
			//Add the newly recorded values
			activeRoute.add(analogValues, digitalValues, msSinceLastMeasurement);
			
			//IMPORTANT: reset the timer.
			timer.reset();
		}
	}
	
	/**
	 * Stops recording
	 * Saves all PhantomRoutes
	 */
	public void recordStop()
	{
		if (recording == true)
		{
			recording = false;
			timer.stop();
			timer.reset();
			saveRoutes();
		}
	}
	
	/**
	 * Initiate playback, to allow using getAxis and getButton
	 */
	public void playbackInit()
	{
		if (recording == false)
		{
			print("Playback initiated.");
			timer.reset();
			timer.start();
			playbackIndex = 0;
			playback = true;
		}
	}
	
	/**
	 * Stop playback
	 */
	public void playbackStop()
	{
		if (playback == true)
		{
			print("Playback stopped.");
			playback = false;
			timer.stop();
			timer.reset();
		}
	}
	
	/**
	 * @return - true if currently playing back. false when playback hasn't started or has ended.
	 */
	public boolean getPlaybackState()
	{
		return playback;
	}
	
	/**
	 * Creates a copy of the route in the system. Will have name changed to _v2, _v3, etc
	 * This is the only way to modify the version number
	 * @param routeName - name of route to be copied
	 */
	public void copyRoute(String routeName)
	{
		PhantomRoute copy = new PhantomRoute(storedRoutes.get(routeName));
		storedRoutes.put(copy.getName(), copy);
		print("Copied route " + routeName + " to route " + copy.getName() + ".");
	}
	
	/**
	 * Deletes the active route from the system
	 * You must set the active route to something else before you do pretty much anything else after this
	 */
	public void deleteRoute()
	{
		activeRoute.delete();
		storedRoutes.remove(activeRoute.getName());
		print("Removed route " + activeRoute + ".");
	}
	
	/**
	 * Clears all timeline data inside the active route
	 */
	public void clearRoute()
	{
		activeRoute.clear();
		activeRoute.save();
	}
	
	/**
	 * Get the nicely-formatted overview of the active phantomRoute
	 * @return - nicely formatted table string
	 */
	public String getRouteOverview()
	{
		return activeRoute.getOverview();
	}
	
	/**
	 * Print an overview of the active route
	 */
	public void printRouteOverview()
	{
		//No print() function used because it has enough preface already, and also doesnt need to be enabled/disabled
		System.out.println(getRouteOverview());
	}
	
	/**
	 * @return - A multiline string, formatted as a table, that describes all routes stored on the system
	 */
	public String getAllOverviews()
	{
		String tableStr = "";
		tableStr += "+------------------------------------------------------------------------+" + "\n";
		tableStr += "|                           # " + "Phantom Routes" + " #                           |" + "\n";
		tableStr += "+------------------------------------------------------------------------+" + "\n";
		
		//Go through each stored PhantomRoute
		for (PhantomRoute pr : storedRoutes.values())
		{
			tableStr += pr.getOverview() + "\n";
			tableStr += "+------------------------------------------------------------------------+" + "\n";
			
			//If active route and the the iterated route reference the same object
			if (pr == activeRoute)
			{
				tableStr += "|                      ^ ^ ^ ^ " + "Active Route " + " ^ ^ ^ ^                      |" + "\n";
				tableStr += "+------------------------------------------------------------------------+" + "\n";
			}
		}
		return tableStr;
	}
	
	/**
	 * Prints getRouteTable()
	 */
	public void printAllOverviews()
	{
		//No print() function used because it has enough preface already, and also doesnt need to be enabled/disabled
		System.out.println(getAllOverviews());
	}
}
