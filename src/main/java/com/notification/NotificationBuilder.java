package com.notification;

import com.theme.ThemePackage;

/**
 * This interface is implemented for building custom Notifications.
 *
 * @param <T>
 *            the type to build
 */
public interface NotificationBuilder<T extends Notification> {
	/**
	 * Builds a Notification in accordance with the ThemePackage.
	 *
	 * @param pack
	 *            the ThemePackage to apply to the Notification
	 * @param args
	 *            optional additional arguments passed to the NotificationBuilder
	 * @return the built Notification
	 */
	public T buildNotification(ThemePackage pack, Object... args);
}
