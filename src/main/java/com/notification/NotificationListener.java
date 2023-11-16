package com.notification;

public interface NotificationListener {
	/**
	 * Called when an action is completed on the Notification (e.g., a click).
	 *
	 * @param notification
	 *            the Notification on which the action was completed
	 * @param action
	 *            a String identifying what the action was
	 */
	public void actionCompleted(Notification notification, String action);
}
