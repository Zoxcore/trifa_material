package com.platform;

/**
 * Detects platform-specific modifications.
 */
public class Platform {
	private OperatingSystem m_operatingSystem;
	private boolean m_used = false;

	public Platform() {
		String os = System.getProperty("os.name").toLowerCase();
		m_operatingSystem = new DefaultOperatingSystem();
		if (os.indexOf("win") >= 0)
			m_operatingSystem = new Windows();
		if (os.indexOf("mac") >= 0)
			m_operatingSystem = new Mac();
		if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0)
			m_operatingSystem = new Unix();
	}

	/**
	 * @return the detected OperatingSystem
	 */
	public OperatingSystem getOperatingSystem() {
		return m_operatingSystem;
	}

	/**
	 * @param feature
	 *            the feature to query
	 * @return whether or not a certain feature is supported for the platform.
	 */
	public boolean isSupported(String feature) {
		return m_operatingSystem.isSupported(feature);
	}

	/**
	 * @return whether or not the Notifications should be adjusted to best fit the platform.
	 */
	public boolean isUsed() {
		return m_used;
	}

	/**
	 * Sets whether or not the Notifications should be adjusted to best fit the platform.
	 *
	 * @param used
	 *            whether or not the Notifications should be adjusted
	 */
	public void setAdjustForPlatform(boolean used) {
		m_used = used;
	}

	private static Platform INSTANCE;

	public static Platform instance() {
		if (INSTANCE == null)
			INSTANCE = new Platform();

		return INSTANCE;
	}
}
