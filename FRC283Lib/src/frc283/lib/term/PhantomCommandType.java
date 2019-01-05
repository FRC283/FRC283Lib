package frc283.lib.term;

public enum PhantomCommandType 
{
	HELP("help", "Print the description of a command.", "command name:Name of the command to print for, blank for reflexive call.", "all:Pass 'a' to print the help for all commands. You can also call help with no arguments to print all helps."),
	DELETE("delete", "Delete the current route."),
	SET("set", "Set the active route, then prints the active route name.", "route name:Name of the route to set to. You can also pass through a route number."),
	GET("get", "Returns the name of the active route."),
	CREATE("create", "Creates a new route.", "title:Title of the new route", "robot:Name of the robot intended for use.", "description:Short description of the route."),
	RECORD("record", "Starts or stop recording", "start/stop:'start' to start recording. 'stop' to stop recording. Pass no arguments to toggle the recording."),
	OVERVIEW("overview", "Prints the overview of every route. Each route is given an ID number that can be used to refer to that route."),
	SAVE("save", "Save all routes.", "all:Pass 'a' to save all routes."),
	COPY("copy", "Copies the active route to make its v2 version. Then sets the active route to the copy."),
	EXIT("exit", "Stop the console. You can also close the window.");
	
	private String code;
	private String desc;
	private String[] args;
	
	/**
	 * @param code - What text invokes the code e.g. "help" for help
	 * @param desc
	 * @param argNames - Each argument, separated by its description by a colon. E.g. "command name:The command to use."
	 */
	PhantomCommandType(String code, String desc, String... args)
	{
		this.code = code;
		this.desc = desc;
		this.args = args;
	}
	
	public String getCode()
	{
		return this.code;
	}
	
	public String getShortDescription()
	{
		return this.desc;
	}
	
	public String getHelp()
	{
		String returnValue = "";
		returnValue += "COMMAND \"" + this.code + "\"\n";
		returnValue += "	" + this.desc + "\n";
		if (this.args.length == 0)
		{
			returnValue += "	(NO ARGUMENTS)\n";
		}
		else
		{
			returnValue += "	ARGUMENTS:\n";
			for (String s : this.args)
			{
				returnValue += "	[" + s.split(":")[0] + "] - " + s.split(":")[1] + "\n";
			}
		}
		return returnValue;
	}
}
