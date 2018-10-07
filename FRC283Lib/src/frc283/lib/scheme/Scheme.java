package frc283.lib.scheme;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Date;

import javax.imageio.ImageIO;

public abstract class Scheme
{
	
	public static void main(String[] args)
	{
		Scheme.generateControls("Power Up", "org.usfirst.frc.team283.robot.DriveSubsystem", "org.usfirst.frc.team283.robot.LiftSubsystem");
	}
	
	//Logitech Ports (Default)
		//Digital
			public static final int LOGITECH_A = 0;
			public static final int LOGITECH_B = 1;
			public static final int LOGITECH_X = 2;
			public static final int LOGITECH_Y = 3;
			public static final int LOGITECH_LEFT_BUMPER = 4;
			public static final int LOGITECH_RIGHT_BUMPER = 5;
			public static final int LOGITECH_BACK = 6;
			public static final int LOGITECH_START = 7;
			public static final int LOGITECH_LEFT_STICK_BUTTON = 8;
			public static final int LOGITECH_RIGHT_STICK_BUTTON = 9;
		//Analog
			public static final int LOGITECH_LEFT_X = 10;
			public static final int LOGITECH_LEFT_Y = 11;
			public static final int LOGITECH_LEFT_TRIGGER = 12;
			public static final int LOGITECH_RIGHT_TRIGGER = 13;
			public static final int LOGITECH_RIGHT_X = 14;
			public static final int LOGITECH_RIGHT_Y = 15;
	//Xbox Ports
		//Digital
			public static final int XBOX_A = 16;
			public static final int XBOX_B = 17;
			public static final int XBOX_X = 18;
			public static final int XBOX_Y = 19;
			public static final int XBOX_LEFT_BUMPER = 20;
			public static final int XBOX_RIGHT_BUMPER = 21;
			public static final int XBOX_BACK = 22;
			public static final int XBOX_START = 23;
			public static final int XBOX_LEFT_STICK_BUTTON = 24;
			public static final int XBOX_RIGHT_STICK_BUTTON = 25;
		//Analog
			public static final int XBOX_LEFT_X = 26;
			public static final int XBOX_LEFT_Y = 27;
			public static final int XBOX_LEFT_TRIGGER = 28;
			public static final int XBOX_RIGHT_TRIGGER = 29;
			public static final int XBOX_RIGHT_X = 30;
			public static final int XBOX_RIGHT_Y = 31;	
	//Left Physical Joystick
		//Digital
		//Analog
	//Right Physical Joystick
		//Digital
		//Analog
	
	/** What kind of controller does the driver and operator use? */
	public enum driverMode
	{
		logitech;
	}
	
	@Repeatable(Schemas.class)
	@Retention(RetentionPolicy.RUNTIME)
	/**
	 * Place this annotation in front of "action" functions like so:
	 * (at)Schema(JoystickSchema.BUTTON)
	 * and the JoystickSchema class will read this and put that function with that button on the controls image
	 * See JoystickSchema for a complete list of buttons and axes
	 */
	@interface Schema
	{
		/**
		 * Stores the button/axis that the function is assigned to 
		 * Possible Values: are above
		 */
		int value(); //Since this is named value, you can simply input it next to the annotation
		String desc() default ""; //Possibly consider including this in a later version. Purpose would be to have better naming
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	/**
	 * Container annotation for schemas. Do not use.
	 */
	@interface Schemas
	{
		Schema[] value();
	}
	
	/**
	 * Generates a visual guide to the controls scheme of a robot
	 * NOTE: Requires base controls image to be in source folder
	 * @param title - Name of the Robot
	 * @param classNames - List of classes where the Scheme annotation can be found
	 */
	public static void generateControls(String title, String... classNames)
	{
		//Following: some values for image positioning
		final int LABEL_BASE_X = 835;
		final int LABEL_BASE_Y = 139;
		final int LABEL_INCR = 42;
		final int TITLE_X = 36;
		final int TITLE_Y = 78;
		/** Printed at the top of the Schema */
		String robotName = title;
		/** Our stored references to all classes in this project. Holds a max of 20 for now */
		Class<?>[] classes = new Class[20];
		for (int i = 0; i < classNames.length; i++)
		{
			try 
			{
				classes[i] = Class.forName(classNames[i]);
			} 
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		System.out.println("<=== Generating Controls Image ===>");
		BufferedImage img = null;
		try 
		{
		    img = ImageIO.read(new File("ControlsSchemaBase.png"));
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		Graphics g = img.getGraphics();
	    g.setFont(new Font("Consolas", Font.BOLD, 35));
	    g.setColor(Color.BLACK);
		g.drawString(robotName +  " Auto-Generated Controls - " + new Date(), TITLE_X, TITLE_Y);
		g.setFont(new Font("Consolas", Font.PLAIN, 30));
		for (Class<?> c : classes)
		{
			if (c != null)
			{
				System.out.println("Detected class : \"" + c.getName() + "\"");

				Method[] methods = c.getDeclaredMethods();
				for (Method m : methods)
				{
					System.out.println("	Method Detected: " + m.getName());
					Schema singleSchema = m.getAnnotation(Schema.class); //Only returns non-null for methods with ONE @Schema marker
					Schemas allSchemas = m.getAnnotation(Schemas.class); //Only methods with multiple @Schema markers have "@SchemaS"
					if (allSchemas != null)
					{
						for (Schema s : allSchemas.value())
						{
							System.out.println("		Annotation found at function \"" + m.getName() + "\" in class " + c.getName());
							if (s.desc().equals(""))
							{
								g.drawString(m.getName(), LABEL_BASE_X, LABEL_BASE_Y + ((s.value()) * LABEL_INCR));
							}
							else
							{
								g.drawString(s.desc(), LABEL_BASE_X, LABEL_BASE_Y + ((s.value()) * LABEL_INCR));
							}
						}
					}
					else if (singleSchema != null)
					{
						System.out.println("		Annotation found at function \"" + m.getName() + "\" in class " + c.getName());
						if (singleSchema.desc().equals(""))
						{
							g.drawString(m.getName(), LABEL_BASE_X, LABEL_BASE_Y + ((singleSchema.value()) * LABEL_INCR));
						}
						else
						{
							g.drawString(singleSchema.desc(), LABEL_BASE_X, LABEL_BASE_Y + ((singleSchema.value()) * LABEL_INCR));
						}
					}
				}
			}
		}
		
		
		g.dispose();
	    try 
	    {
			ImageIO.write(img, "png", new File("ControlsImage.png"));
		} 
	    catch (IOException e) 
	    {
			e.printStackTrace();
		}
	    System.out.println("<=== End Generation ===>");
	}
}