package frc283.lib.ref;


public class Constants 
{
	//Computer Ports
		public static final int DRIVER_CONTROLLER_PORT = 0; //Logitech 
		public static final int OPERATOR_CONTROLLER_PORT = 1; //xbox
	//Joystick Ports
		//Buttons
			public static final int A = 1;
			public static final int B = 2;
			public static final int X = 3;
			public static final int Y = 4;
			public static final int LEFT_BUMPER = 5;
			public static final int RIGHT_BUMPER = 6;
			public static final int BACK = 7;
			public static final int START = 8;
			public static final int LEFT_STICK_BUTTON = 9;
			public static final int RIGHT_STICK_BUTTON = 10;
		//Sticks
			public static final int LEFT_X = 0;
			public static final int LEFT_Y = 1;
			public static final int LEFT_TRIGGER = 2;
			public static final int RIGHT_TRIGGER = 3;
			public static final int RIGHT_X = 4;
			public static final int RIGHT_Y = 5;
	//Robot Ports
		//PWM
			public static final int LEFT_DRIVE_CONTROLLER_PORT = 0;
			public static final int RIGHT_DRIVE_CONTROLER_PORT = 3;
			public static final int WINCH_CONTROLLER_PORT = 5;
			public static final int LIFT_CONTROLLER_PORT = 9;
			public static final int LEFT_ARM_CONTROLLER_PORT = 6;
			public static final int RIGHT_ARM_CONTROLLER_PORT = 8;
		//DIO
			//TODO: Change these to the right values
			//Left Drive
			public static final int LEFT_DRIVE_ENCODER_PORT_A = 2;
			public static final int LEFT_DRIVE_ENCODER_PORT_B = 3;
			//Right Drive
			public static final int RIGHT_DRIVE_ENCODER_PORT_A = 0;
			public static final int RIGHT_DRIVE_ENCODER_PORT_B = 1;
			//Winch
			public static final int WINCH_ENCODER_PORT_A = 6;
			public static final int WINCH_ENCODER_PORT_B = 7;
			//Lift
			public static final int LIFT_ENCODER_PORT_A = 4;
			public static final int LIFT_ENCODER_PORT_B = 5;
			public static final int LIFT_LIMIT_PORT = 8;
		//Pneumatics
			public static final int DRIVE_GEARSHIFT_PORT = 2;
			public static final int ARM_SOLENOID_PORT = 1;
}
