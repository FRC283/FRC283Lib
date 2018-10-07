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
 * TODO: enable/disable printsouts
 * TODO: change everything to javadoc comments
 * TODO: maybe have the constructor not have everry parameter in it, i.e optional apramers
 * TODO: have a version number system in the constructor
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
	
	/** The string name of the route currently being written/read to/from
	Why is this a string and not a PhantomRoute? Because java passes objects strangely, so we dont want to make a bunch of dupes */
	private String activeRoute;
	
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
	 * Whenever the timer is rolling for playback or recording, this function helps translate that
	 * time value into a useful integer for accessing the arrays that describe the routes
	 * 
	 * First, converts the timer value into milliseconds
	 * Then divides that 1000 milliseconds into a number of steps based on the time spacing
	 * E.g. if the time spacing is 100, then 1 second will be divided into 10 saved values
	 * The typecast to int acts as truncation e.g. 127 milliseconds would become 1.27 which is cast to 1.00
	 *		
	 * @param timeValue - the number of seconds on the timer.
	 * @return - the time index for the current timer value
	 */
	private int getTimeIndex(double timeValue)
	{
		return (int)(timeValue * 1000 / storedRoutes.get(activeRoute).getTimeSpacing());
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
	public void createRoute(String title, String robot, String desc, String role, int timeSpacing)
	{
		//Ensures that the route folder exists
		File folder = new File(routeFolder);
		//Create a new PhantomRoute file
		PhantomRoute newPhantomRoute = new PhantomRoute(title, robot, desc, role, timeSpacing, routeFolder);
		//Add this new route to the index
		storedRoutes.put(newPhantomRoute.getName(), newPhantomRoute);
	}
	
	/**
	 * Cannot be used during playback or recording
	 * @param routeName - name of the route to set to being active
	 */
	public void setActiveRoute(String routeName)
	{
		activeRoute = routeName;
		//print("Active route is now " + routeName + ".");
	}
	
	/**
	 * @return - the name of the current active string
	 */
	public String getActiveRouteName()
	{
		return storedRoutes.get(activeRoute).getName();
	}
	
	/**
	 * @param number - the axis number to get the value for
	 * @return - the most appropriate value for the current time since playback started
	 */
	public double getRawAxis(int number)
	{
		if (playback == true)
		{
			
		}
		else
		{
			//System.err.println("PhantomJoystick.getRawAxis: Playback has ended. You must call playbackInit to initiate playback");
			return 0;
		}
	}
	
	/**
	 * @param number - the button number to get the value for
	 * @return - the most appropriate value for the current time since playback started
	 */
	public boolean getRawButton(int number)
	{
		if (playback == true)
		{
			int timeIndex = getTimeIndex(timer.get());
			ArrayList<Boolean> timeline = storedRoutes.get(activeRoute).getDigital(number);
			if (timeIndex < timeline.size())
			{
				return timeline.get(timeIndex);
			}
			else
			{
				playbackStop();
				return false;
			}
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
				System.out.println("Inside recordInit: boolean arraylist 0 -> " + storedRoutes.get(activeRoute).getDigital(0));
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
		if (recording == true)
		{
			//First, converts the timer value into milliseconds
			//Then divides that 1000 milliseconds into a number of steps based on the time spacing
			//E.g. if the time spacing is 100, then 1 second will be divided into 10 saved values
			//The typecast to int acts as truncation e.g. 127 milliseconds would become 1.27 which is cast to 1.00
			int timeIndex = getTimeIndex(timer.get());
			
			if (timeIndex >= 1) //If at least one time-step has passed since last recording
			{
				System.out.println((int)(timer.get() * 1000) + "ms since last record.");
			
				//For each possible analog input
				for (int a = 0; a < 6; a++)
				{
					//Get analog input #a, set its value at the timeIndex to be the current joystick axis value for axis #a
					storedRoutes.get(activeRoute).getAnalog(a).add(recordingJoystick.getRawAxis(a));
					//Push time since last recording
					storedRoutes.get(activeRoute).getAnalogSpacing(a).add((int)(timer.get() * 1000));
				}
				
				//For each possible digital input
				for (int d = 0; d < 10; d++)
				{
					//Get digital input #d, set its value at the timeIndex to be the current joystick button value for digital #d
					storedRoutes.get(activeRoute).getDigital(d).add(recordingJoystick.getRawButton(d));
					//Push the time since last recording
					storedRoutes.get(activeRoute).getDigitalSpacing(d).add((int)(timer.get() * 1000));
				}
				
				//IMPORTANT: reset the timer.
				timer.reset();
			}
			else //If one time-step hasn't passed
			{
				//Do nothing
			}
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
			print("Recording stopped.");
			System.out.println("active: " + activeRoute);
			System.out.println("stored: " + storedRoutes.toString());
			System.out.println("Analog Arrays: ");
			for (int a = 0; a < 6; a++)
			{
				PhantomRoute pr = storedRoutes.get(activeRoute);
				System.out.println("index: " + a);
				System.out.println("	>" + pr.getAnalog(a));
			}
			System.out.println("Digital Arrays: ");
			for (int d = 0; d < 10; d++)
			{
				PhantomRoute pr = storedRoutes.get(activeRoute);
				System.out.println("index: " + d);
				System.out.println("	>" + pr.getDigital(d));
			}
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
			int est = storedRoutes.get(activeRoute).getTimeSpacing() * storedRoutes.get(activeRoute).getAnalog(0).size();
			System.out.println("Total time it took: " + timer.get() + " theoretical value: " + est);
			playback = false;
			timer.stop();
			timer.reset();
		}
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
	 * Deletes the route from the system
	 * @param routeName - name of route to be deleted
	 */
	public void deleteRoute(String routeName)
	{
		storedRoutes.get(routeName).delete();
		storedRoutes.remove(routeName);
		print("Removed route " + routeName + ".");
	}
	
	/**
	 * Clears all timeline data inside the active route
	 */
	public void clearRoute()
	{
		storedRoutes.get(activeRoute).clear();
		storedRoutes.get(activeRoute).save();
		System.out.println("Inside clearRoute: boolean arraylist 0 -> " + storedRoutes.get(activeRoute).getDigital(0));
	}
	
	/**
	 * @param routeName - name of route who's data will be printed
	 * @return - nicely formatted table string
	 */
	public String getRouteOverview(String routeName)
	{
		return storedRoutes.get(routeName).getOverview();
	}
	
	/**
	 * @param routeName - name of route to print data of
	 */
	public void printRouteOverview(String routeName)
	{
		//No print() function used because it has enough preface already, and also doesnt need to be enabled/disabled
		System.out.println(getRouteOverview(routeName));
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
