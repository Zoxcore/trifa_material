package com.utils;

public class MathUtils {
	/**
	 * @param number
	 *            the number to find the sign of
	 * @return the sign of the number
	 */
	public static int sign(double number) {
		return (int) Math.signum(number);
	}

	/**
	 * Clamps the number to be between the min and the max.
	 *
	 * @param num
	 *            the number to clamp
	 * @param min
	 *            the minimum value
	 * @param max
	 *            the maximum value
	 * @return the clamped number between min and max
	 */
	public static double clamp(double num, double min, double max) {
		if (num < min)
			return min;
		if (num > max)
			return max;
		return num;
	}
}