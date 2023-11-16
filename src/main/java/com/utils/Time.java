package com.utils;

/**
 * Represents a time, which can be used for the duration of a Notification, fade times, etc.
 */
public final class Time {
	private int m_milliseconds;
	private boolean m_infinite;

	private Time(int milliseconds, boolean infinite) {
		m_milliseconds = milliseconds;
		m_infinite = infinite;
	}

	/**
	 * @param seconds
	 *            the number of seconds. This is truncated at the millisecond.
	 * @return a Time of length seconds
	 */
	public static Time seconds(double seconds) {
		return new Time((int) (seconds * 1000), false);
	}

	/**
	 * @param milliseconds
	 *            the number of milliseconds
	 * @return a Time of length milliseconds
	 */
	public static Time milliseconds(int milliseconds) {
		return new Time(milliseconds, false);
	}

	/**
	 * Specifies an infinite length of Time.
	 *
	 * @return a Time representing infinity
	 */
	public static Time infinite() {
		return new Time(-1, true);
	}

	/**
	 * @param time
	 *            the Time to add to
	 * @return the sum of the two times
	 */
	public Time add(Time time) {
		return new Time(m_milliseconds + time.getMilliseconds(), m_infinite || time.isInfinite());
	}

	/**
	 * @return the number of seconds, of -1 if it is infinite
	 */
	public double getSeconds() {
		if (m_infinite)
			return -1;
		return (double) m_milliseconds / 1000;
	}

	/**
	 * @return the number of milliseconds, or -1 if it is infinite
	 */
	public int getMilliseconds() {
		if (m_infinite)
			return -1;
		return m_milliseconds;
	}

	/**
	 * @return whether or not the Time is infinite
	 */
	public boolean isInfinite() {
		return m_infinite;
	}
}
