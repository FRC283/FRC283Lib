package frc283.lib.auto;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * A class that manages all the data associated with a single saved autonomous route
 * 
 * Does not handle playback, that's the role of the PhantomJoystick
 * 
 * Manages the its own file on the file system
 * 
 * Has two constructors
 * - One initializes from a full file path to an existing route file
 *     - Useful for accessing preexisting routes or older routes with different file naming schemes or similar
 * - One initializes using some basic initial data about the class and creates a new route at the specified location
 *     - If the file already exists there, it reads from it instead (the passed data must patch the file found there)
 *                      
 * 
 * Example Usage:
 * 	PhantomRoute pr = new PhantomRoute(100, "left_side_autonomous", "napalm")
 *  pr.getIsEmpty() //Must be true since this is a new route
 *  pr.addAnalog(0, 643.34);
 *  pr.save();
 *  
 *  or
 *  
 *  PhantomRoute pr = new PhantomRoute("root\\routes\\napalm_left_side_autonomous.route") //Throws an error if it doesn't exist
 *  pr.getIsEmpty() //False, has a route here
 *  pr.clear()      //Deleted the old routing values, not the file
 *  pr.getIsEmpty() //True
 *  pr.addAnalog(0, 674.23)
 *  pr.save()
 *  pr.delete()     //Deletes the file
 *  
 *  
 *  TODO: auto-save after operations?
 *  TODO: function to cut-out starting and ending blank values (to cut delays from the start of the autonomous)
 */
public class PhantomRoute 
{	
	//All newly created route files end with this file type/extension
	public final static String EXTENSION = "route";
	
	/** The minimum ms needed for proper measurement */
	public final static int MIN_TIME_SPACING = 30;
	
	//Object that contains all actual data describing route of robot
	public RouteData routeData;
	
	//The file on the RoboRIO that contains this route's data
	protected File file;
	
	//
	protected FileReader fileReader;
	
	//
	protected BufferedReader bufferedReader;
	
	//
	protected FileWriter fileWriter;
	
	//
	protected BufferedWriter bufferedWriter;
	
	//Google-developed library for turning java objects into json and back
	protected Gson gson;
	
	/**
	 * Used to create entirely new routes
	 * CAN be used to access old routes
	 * Will always override any previous version with this name and robot
	 * @param timeSpacing - milliseconds between recorded values for this file
	 * @param title - brief overview of the route like "left_side_high"
	 * @param desc - detailed overview of the route
	 * @param folder - folder to create the file in
	 * @param robot - name of the robot to use with route with
	 * @param role - e.g. "Operator" or "Driver"
	 */
	public PhantomRoute(String title, String robot, String desc, String role, int timeSpacing, String folder)
	{
		this.routeData = new RouteData();
		
		//Last modified initially starts as the time of creation 
		this.routeData.lastModified = new Date().getTime();
		
		//Time spacing must be a certain minimum
		this.routeData.timeSpacing = (timeSpacing < MIN_TIME_SPACING) ? MIN_TIME_SPACING : timeSpacing;
		
		this.routeData.title = title.toLowerCase().replace(" ", "_");
		
		this.routeData.description = desc.toLowerCase();
		
		this.routeData.robot = robot.toLowerCase().replace(" ", "_");
		
		this.routeData.role = role.toLowerCase().replace(" ", "_");
		
		this.routeData.version = 1;
		
		//Using a GsonBuilder allows pretty printing to be set to true, meaning the output file will be more human-friendly to read
		this.gson = new GsonBuilder().create();
		
		//There are 6 analog inputs on the robot
		this.routeData.analog = (new ArrayList[6]);
		this.routeData.analogSpacing = new ArrayList[6];
		
		//Initialize analog array
		for (int j = 0; j < this.routeData.analog.length; j++)
		{
			//Each array value is a boolean ArrayList
			this.routeData.analog[j] = new ArrayList<Double>(0);
			this.routeData.analogSpacing[j] = new ArrayList<Integer>(0);
		}
		
		//There are 10 digital inputs on the robot
		this.routeData.digital = new ArrayList[10];
		this.routeData.digitalSpacing = new ArrayList[10];
		
		//Initialize analog array
		for (int h = 0; h < this.routeData.digital.length; h++)    
		{
			//Each array value is a boolean ArrayList
			this.routeData.digital[h] = new ArrayList<Boolean>(0);
			this.routeData.digitalSpacing[h] = new ArrayList<Integer>(0);
		}
		
		//E.g. root\routes\2018_napalm_left_side.route
		String fullPath = folder.toLowerCase() + File.separator + this.getName() + "." + PhantomRoute.EXTENSION;

		//Access the file or the location where the file will be
		this.file = new File(fullPath);
		
		System.out.println("PhantomRoute: Created a new route file at " + fullPath);
		
		//If the file and route is already in existance
		if (file.exists())
		{
			this.initializeFromPath(fullPath);
		}
	}
	
	/**
	 * Used to re-wrap previously-saved routes
	 * CANNOT be used to make new routes
	 * @param path - absolute file path to the saved route
	 */
	public PhantomRoute(String path)
	{
		this.initializeFromPath(path);
	}
	
	/**
	 * Creates a new PhantomRoute as a copy of the passed PhantomRoute
	 * @param phantomRoute - the PhantomRoute to be copied
	 */
	public PhantomRoute(PhantomRoute phantomRoute)
	{
		this.gson = new GsonBuilder().create();
		
		this.routeData = new RouteData();
		
		this.routeData.lastModified = new Date().getTime();
		
		this.routeData.timeSpacing = phantomRoute.getTimeSpacing();
		
		this.routeData.title = phantomRoute.getTitle();
		
		this.routeData.description = phantomRoute.getDescription();
		
		this.routeData.robot = phantomRoute.getRobot();
		
		this.routeData.version = phantomRoute.getVersion() + 1;
		
		this.file = new File(phantomRoute.getFolder() + File.pathSeparator + this.getName() + "." + EXTENSION);
	}
	
	/**
	 * Sets the values of data in this wrapper to be what's contained in the specified file
	 * @param path - full file path to the data file
	 */
	private void initializeFromPath(String path)
	{
		this.gson = new GsonBuilder().create();
		
		file = new File(path);
		try 
		{
			fileReader = new FileReader(file);
		} 
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		bufferedReader = new BufferedReader(fileReader);
		routeData = gson.fromJson(bufferedReader, RouteData.class);
		try 
		{
			bufferedReader.close();
			fileReader.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param index - The index of analog data timeline to be retrieved
	 * @return - An ArrayList of the history of all analog values on the requested port
	 */
	public ArrayList<Double> getAnalog(int index)
	{
		return routeData.analog[index];
	}
	
	/**
	 * @param index - The index of analog data timeline to be retrieved
	 * @return - An ArrayList of the history of all analog spacing values on the requested port
	 */
	public ArrayList<Integer> getAnalogSpacing(int index)
	{
		return routeData.analogSpacing[index];
	}
	
	/**
	 * 
	 * @param index - The index of digital data timeline to be retrieved
	 * @return - An ArrayList of the history of all digital values on the requested port
	 */
	public ArrayList<Boolean> getDigital(int index)
	{
		return routeData.digital[index];
	}
	
	/**
	 * @param index - The index of digital data timeline to be retrieved
	 * @return - An ArrayList of the history of all digital spacing values on the requested port
	 */
	public ArrayList<Integer> getDigitalSpacing(int index)
	{
		return routeData.digitalSpacing[index];
	}
	
	/**
	 * Counts as a modification
	 * @param inputIndex - Index of the analog input to set (e.g. the joystick left y axis button might be 3, or something)
	 * @param recordingIndex - On that input's timeline, this is the index of the value you intend to change (e.g. the fourth recorded value is index=3)
	 * @param value - A double, the value to record
	 */
	public void setAnalog(int inputIndex, int recordingIndex, double value)
	{
		routeData.analog[inputIndex].set(recordingIndex, value);
		routeData.lastModified = new Date().getTime();
	}
	
	/**
	 * Counts as a modification
	 * @param inputIndex - Index of the analog input to set (e.g. the joystick left y axis button might be 3, or something)
	 * @param recordingIndex - On that input's timeline, this is the index of the value you intend to change (e.g. the fourth recorded value is index=3)
	 * @param value - milliseconds since last measurement
	 */
	public void setAnalogSpacing(int inputIndex, int recordingIndex, int value)
	{
		routeData.analogSpacing[inputIndex].set(recordingIndex, value);
		routeData.lastModified = new Date().getTime();
	}
	
	/**
	 * Counts as a modification
	 * @param inputIndex - Index of the digital input to set (e.g. the joystick left bumper button might be 3, or something)
	 * @param recordingIndex - On that input's timeline, this is the index of the value you intend to change (e.g. the fourth recorded value is index=3)
	 * @param value - True or false, the value to record
	 */
	public void setDigital(int inputIndex, int recordingIndex, boolean value)
	{
		routeData.digital[inputIndex].set(recordingIndex, value);
		routeData.lastModified = new Date().getTime();
	}
	
	/**
	 * Counts as a modification
	 * @param inputIndex - Index of the digital input to set (e.g. the joystick left bumper button might be 3, or something)
	 * @param recordingIndex - On that input's timeline, this is the index of the value you intend to change (e.g. the fourth recorded value is index=3)
	 * @param value - milliseconds since last measurement
	 */
	public void setDigitalSpacing(int inputIndex, int recordingIndex, int value)
	{
		routeData.digitalSpacing[inputIndex].set(recordingIndex, value);
		routeData.lastModified = new Date().getTime();
	}
	
	/**
	 * Pushes the value onto the end of the analog timeline specified by the index
	 * @param index - which timeline to push to
	 * @param value - value to be added
	 */
	public void addAnalog(int index, double value)
	{
		routeData.analog[index].add(value);
		routeData.lastModified = new Date().getTime();
	}
	
	/**
	 * Pushes the value onto the end of the analog spacing timeline specified by the index
	 * @param index - which timeline to push to
	 * @param value - value to be added in ms
	 */
	public void addAnalogSpacing(int index, int value)
	{
		routeData.analogSpacing[index].add(value);
		routeData.lastModified = new Date().getTime();
	}
	
	/**
	 * Pushes the value onto the end of the digital timeline specified by the index
	 * @param index - which timeline to push to
	 * @param value - value to be added
	 */
	public void addDigital(int index, boolean value)
	{
		routeData.digital[index].add(value);
		routeData.lastModified = new Date().getTime();
	}
	
	/**
	 * Pushes the value onto the end of the digital spacing timeline specified by the index
	 * @param index - which timeline to push to
	 * @param value - value to be added in ms
	 */
	public void addDigitalSpacing(int index, int value)
	{
		routeData.digitalSpacing[index].add(value);
		routeData.lastModified = new Date().getTime();
	}
	
	/**
	 * If this is a new route, saves the route to the file system.
	 * If this was a previous route that was re-contructed, then this updates the file, overwriting the new one
	 */
	public void save()
	{
		try 
		{
			fileWriter = new FileWriter(file);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		bufferedWriter = new BufferedWriter(fileWriter);
		try 
		{
			//ArrayLists are turned into regular arrays when jsonified. Just a a note
			//However, when fromJson cast into a RouteData object, they will be magically converted to ArrayLists. Pretty amazing.
			bufferedWriter.write(gson.toJson(routeData));
			bufferedWriter.close();
			fileWriter.close();
			System.out.println("PhantomRoute: saved " + getName());
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes all timeline content. Does not delete the file or data about the name, robot, etc
	 * You must save after this operation
	 */
	public void clear()
	{
		//Clear out each analog array
		for (int a = 0; a < routeData.analog.length; a++)
		{
			routeData.analog[a].clear();
			routeData.analogSpacing[a].clear();
		}
		
		//Clear out each digital array
		for (int b = 0; b < routeData.digital.length; b++)
		{
			routeData.digital[b].clear();
			routeData.analogSpacing[b].clear();
		}
		
		//Counts as a modification
		routeData.lastModified = new Date().getTime();
	}
	
	/**
	 * Deletes this file representation on the system
	 * Do not use this PhantomRoute after calling this
	 * TODO: have this object delete itself
	 */
	public void delete()
	{
		this.file.delete();
	}
	
	/**
	 * @return - true if this PhantomRoute contains no timeline data
	 */
	public boolean getIsEmpty()
	{
		//Set to false if any ArrayList in this route is not empty
		boolean isEmpty = true;
		
		for (ArrayList<Double> a : routeData.analog)     
		{
			if (a.size() != 0)
			{
				isEmpty = false;
			}
		}
		for (ArrayList<Integer> c : routeData.analogSpacing)     
		{
			if (c.size() != 0)
			{
				isEmpty = false;
			}
		}
		for (ArrayList<Boolean> b : routeData.digital)     
		{
			if (b.size() != 0)
			{
				isEmpty = false;
			}
		}
		for (ArrayList<Integer> d : routeData.digitalSpacing)     
		{
			if (d.size() != 0)
			{
				isEmpty = false;
			}
		}
		
		return isEmpty;
	}
	
	/**
	 * @return - a nice little table thing that gives an overview to this PhantomRoute
	 */
	public String getOverview()
	{
		//If the version is 1, then add a little "(v1)" reminder next to the name. Otherwise add "" (blank)
		String versionStr = (this.getVersion() == 1) ? " (v" + this.getVersion() + ")" : "";
		
		String tableStr = "";
		tableStr += "| \"" + this.getName() + "\"" + versionStr + "\n";
		tableStr += "|    Description: \"" + this.getDescription() + "\"\n";
		tableStr += "|    Saved at " + this.getPath() + "\n";
		tableStr += "|    Last Modified " + this.getLastModified() + " (24-h Clock) \n";
		tableStr += "|    Time Spacing: " + this.getTimeSpacing() + "ms";
		return tableStr;
	}
	
	/**
	 * @return - a string representation of the time and day this route was last modified. E.g. 7-24-2018 13:43
	 */
	public String getLastModified()
	{
		Date d = new Date(routeData.lastModified);
		return (d.getMonth() + "-" + d.getDate() + "-" + d.getYear() + " " + d.getHours() + ":" + d.getMinutes());
	}
	
	/**
	 * @return - the number of millliseconds between recorded values
	 */
	public int getTimeSpacing()
	{
		return routeData.timeSpacing;
	}
	
	/**
	 * @return - description of the route, e.g. "left_side_high_goal"
	 */
	public String getDescription()
	{
		return routeData.description;
	}
	
	/**
	 * @return - the robot intended for use
	 */
	public String getRobot()
	{
		return routeData.robot;
	}
	
	/**
	 * @return - the role intended for use e.g. "operator"
	 */
	public String getRole()
	{
		return routeData.role;
	}
	
	/**
	 * @return - the title of the route, which appears in the file name. E.g. "left_side_high_goal"
	 */
	public String getTitle()
	{
		return routeData.title;
	}
	
	/**
	 * @return - the version number for this route
	 */
	public int getVersion()
	{
		return routeData.version;
	}
	
	/**
	 * @return - the name of this route. Does not include full path or extension. E.g. "napalm_left_side_high_v2". Constructed from properties, not pulled from file system.
	 */
	public String getName()
	{
		//If the version is greater than 1, will add _v2, _v3 onto the end. If it's v1, nothing is added
		String versionAddendum = (routeData.version > 1 ? ("_v" + routeData.version) : "");
		String roleString = (routeData.role.equals("") ? "" : "_" + routeData.role);
		return routeData.robot + roleString + "_" + routeData.title + versionAddendum;
	}
	
	/**
	 * @return - the file extension. e.g. ".route"
	 */
	public String getExtension()
	{
		return PhantomRoute.EXTENSION;
	}
	
	/**
	 * @return - the absolute file path and file name + extension on the roboRIO
	 */
	public String getPath()
	{
		return file.getAbsolutePath();
	}
	
	/**
	 * @return - the absolute file path to folder this resides in
	 */
	public String getFolder()
	{
		return file.getParentFile().getAbsolutePath();
	}
	
	public String toString()
	{
		String returnValue = this.getOverview() + "\n";
		for (int a = 0; a < routeData.analog[0].size(); a++)
		{
			returnValue = returnValue + ("Analog Value Set #" + a + ": " + routeData.analog[0]) + "\n";
		}
		for (int b = 0; b < routeData.analogSpacing[0].size(); b++)
		{
			returnValue = returnValue + ("Analog Spacing Value Set #" + b + ": " + routeData.analogSpacing[0]) + "\n";
		}
		for (int c = 0; c < routeData.digital[0].size(); c++)
		{
			returnValue = returnValue + ("Digital Value Set #" + c + ": " + routeData.digital[0]) + "\n";
		}
		for (int d = 0; d < routeData.digitalSpacing[0].size(); d++)
		{
			returnValue = returnValue + ("Digital Spacing Value Set #" + d + ": " + routeData.digitalSpacing[0]) + "\n";
		}
		return returnValue;
	}
}
