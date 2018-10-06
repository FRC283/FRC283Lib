package frc283.lib.util;

public class Rescaler 
{
	/**
	 * Shortcut for using the rescaler with a deadzone
	 * @param value - value to be rescaled
	 * @param deadzone - abs of deadzone e.g. 0.1
	 * @return - new value
	 */
	public static double deadzone(double value, double deadzone)
	{
		return rescale(deadzone, 1, 0, 1, value);
	}
	
	/**
	 * A function that changes scales, cutting out outlying values and allowing negatives
	 * @param lowero - The lower end of the old scale
	 * @param uppero - The upper end of the old scale
	 * @param lowern - The lower end of the new scale
	 * @param uppern - The upper end of the new scale
	 * @param value - The value, on the old scale, to be returned as its equivalent on the new scale
	 * @return
	 */
	public static double rescale(double lowero, double uppero, double lowern, double uppern, double value)
	{
		boolean neg = false;
		double rescaledValue = 0;	//Rescaled Value = number to be returned
		if (value < 0)
		{
			neg = true;
			value *= -1;
		}
		double oldscale = uppero - lowero;
		double newscale = uppern - lowern;

		rescaledValue = value - lowero;
		rescaledValue /= oldscale;
		rescaledValue *= newscale;
		rescaledValue += lowern;

		if (rescaledValue < 0)
		{
			rescaledValue = 0;
		}
		if (neg == true)
		{
			rescaledValue *= -1;
		}
		return rescaledValue;
	}
}
