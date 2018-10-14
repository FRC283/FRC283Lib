package frc283.lib.auto;

import java.util.ArrayList;

/**
 * Just the raw data for the timeline
 */
public class RouteData 
{
	/** Identifies the robot intended to be used with this route. E.g. "Guillotine"
	Appears at start of name */
	public String robot;
	
	/** Gives a broad name to the route idea. E.g. "left_side_high_goal"
	Appears at middle of name */
	public String title;
	
	/** Describes the intended path taken. E.g. "Starting from the baseline, travel about 12ft forwards, then turn and shoot at the goal" */
	public String description;
	
	/** A number that determines which iteration this is of the original.
	Appears on end of name if greater than 1 */
	public int version;
	
	/** A small description of the intended role e.g. "operator" or "driver" */
	public String role;
	
	/** A number that can be used to see when this route's timeline data was last modified */
	public long lastModified;
	
	/** An arraylist containing arrays of doubles
	Length 6 - 6 different channels
	These are the analog "timelines" */
	public ArrayList<Double[]> analog;
	
	/** Number of analog input timelines */
	public final static int analogChannelCount = 6;
	
	/** An arraylist containingarrays of booleans
	Length 10 - 10 different channels
	These are the digital "timelines" */
	public ArrayList<Boolean[]> digital;
	
	/** Number of digital input timelines */
	public final static int digitalChannelCount = 10;
	
	/**
	 * Timeline of spacing values: 
	 * so the spacing value at 1 is the time that passed between measurement of value 0 and value 1 for either the digital or analog array
	 */
	public ArrayList<Integer> spacing;
}
