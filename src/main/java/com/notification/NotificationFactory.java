package com.notification;

import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.exception.NotificationException;
import com.notification.types.AcceptNotification;
import com.notification.types.IconNotification;
import com.notification.types.ProgressBarNotification;
import com.notification.types.TextNotification;
import com.theme.TextTheme;
import com.theme.ThemePackage;
import com.theme.ThemePackagePresets;
import com.theme.WindowTheme;

/**
 * Creates Notifications using a ThemePackage. It is possible to add custom Notifications by adding
 * NotificationBuilders.
 */
public final class NotificationFactory {
	private ThemePackage m_pack;
	private HashMap<Class<? extends Notification>, NotificationBuilder<? extends Notification>> m_builders;

	public enum Location {
		NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST
	}

	{
		m_builders = new HashMap<Class<? extends Notification>, NotificationBuilder<? extends Notification>>();
		m_builders.put(TextNotification.class, new TextNotificationBuilder());
		m_builders.put(AcceptNotification.class, new AcceptNotificationBuilder());
		m_builders.put(IconNotification.class, new IconNotificationBuilder());
		m_builders.put(ProgressBarNotification.class, new ProgressBarNotificationBuilder());
	}

	public NotificationFactory() {
		setThemePackage(ThemePackagePresets.cleanLight());
	}

	public NotificationFactory(ThemePackage pack) {
		setThemePackage(pack);
	}

	/**
	 * Sets the themes that the factory should use when creating notifications. See ThemePackagePresets for some default
	 * themes.
	 *
	 * @param pack
	 *            the ThemePackage to set
	 */
	public void setThemePackage(ThemePackage pack) {
		m_pack = pack;
	}

	/**
	 * Adds a custom NotificationBuilder. Notifications can then be built using build(Class notificationClass).
	 *
	 * @param <T>
	 *            the type of Notification of the NotificationBuilder
	 * @param notificationClass
	 *            the Class of the Notification that should be built
	 * @param builder
	 *            the NotificationBuilder that builds the notificationClass
	 */
	public <T extends Notification> void addBuilder(Class<T> notificationClass, NotificationBuilder<T> builder) {
		m_builders.put(notificationClass, builder);
	}

	/**
	 * Removes the NotificationBuilder associated with this notification class.
	 *
	 * @param <T>
	 *            the type of Notification of the NotificationBuilder
	 * @param notificationClass
	 *            the Class of the Notification built by the NotificationBuilder that should be removed
	 */
	public <T extends Notification> void removeBuilder(Class<T> notificationClass) {
		m_builders.remove(notificationClass);
	}

	/**
	 * Builds a Notification using the NotificationBuilder associated with the notification class.
	 *
	 * @param <T>
	 *            the type of Notification of the NotificationBuilder
	 * @param notificationClass
	 *            the Class of the Notification to build
	 * @return the built Notification
	 */
	public <T extends Notification> T build(Class<T> notificationClass) {
		if (!m_builders.containsKey(notificationClass))
			throw new RuntimeException("No NotificationBuilder for: " + notificationClass);

		@SuppressWarnings("unchecked")
		T note = (T) m_builders.get(notificationClass).buildNotification(m_pack, new Object[0]);
		return note;
	}

	/**
	 * Builds a Notification using the NotificationBuilder associated with the notification class.
	 *
	 * @param <T>
	 *            the type of Notification of the NotificationBuilder
	 * @param notificationClass
	 *            the class of the Notification to build
	 * @param args
	 *            the args passed to the NotificationBuilder
	 * @return the built Notification
	 */
	public <T extends Notification> T build(Class<T> notificationClass, Object... args) {
		if (!m_builders.containsKey(notificationClass))
			throw new RuntimeException("No NotificationBuilder for: " + notificationClass);

		@SuppressWarnings("unchecked")
		T note = (T) m_builders.get(notificationClass).buildNotification(m_pack, args);
		return note;
	}

	/**
	 * Builds a TextNotification.
	 *
	 * @param title
	 *            the title to display on the TextNotification
	 * @param subtitle
	 *            the subtitle to display on the TextNotification
	 * @return the built TextNotification
	 */
	public TextNotification buildTextNotification(String title, String subtitle) {
		return build(TextNotification.class, title, subtitle);
	}

	/**
	 * Builds an AcceptNotification with "Accept" and "Decline" as the button messages.
	 *
	 * @param title
	 *            the title to display on the AcceptNotification
	 * @param subtitle
	 *            the subtitle to display on the AcceptNotification
	 * @return the built AcceptNotification
	 */
	public AcceptNotification buildAcceptNotification(String title, String subtitle) {
		return build(AcceptNotification.class, title, subtitle);
	}

	/**
	 * Builds an AcceptNotification with the specified Strings.
	 *
	 * @param title
	 *            the title to display on the AcceptNotification
	 * @param subtitle
	 *            the subtitle to display on the AcceptNotification
	 * @param accept
	 *            the text on the accept button
	 * @param decline
	 *            the text on the decline button
	 * @return the built AcceptNotification
	 */
	public AcceptNotification buildAcceptNotification(String title, String subtitle, String accept, String decline) {
		return build(AcceptNotification.class, title, subtitle, accept, decline);
	}

	/**
	 * Builds an IconNotification.
	 *
	 * @param title
	 *            the title to display on the IconNotification
	 * @param subtitle
	 *            the subtitle to display on the IconNotification
	 * @param icon
	 *            the icon on the IconNotification
	 * @return the built IconNotification
	 */
	public IconNotification buildIconNotification(String title, String subtitle, ImageIcon icon) {
		return build(IconNotification.class, title, subtitle, icon);
	}

	/**
	 * Builds a ProgressBarNotification.
	 *
	 * @param title
	 *            the title to display on the ProgressBarNotification
	 * @return the built ProgressBarNotification
	 */
	public ProgressBarNotification buildProgressBarNotification(String title) {
		return build(ProgressBarNotification.class, title);
	}

	private class TextNotificationBuilder implements NotificationBuilder<TextNotification> {
		@Override
		public TextNotification buildNotification(ThemePackage pack, Object... args) {
			if (args.length != 2)
				throw new NotificationException("TextNotifications need two arguments: title, subtitle!");

			TextNotification note = new TextNotification();
			note.setWindowTheme(pack.getTheme(WindowTheme.class));
			note.setTextTheme(pack.getTheme(TextTheme.class));
			note.setTitle((String) args[0]);
			note.setSubtitle((String) args[1]);
			return note;
		}
	}

	private class AcceptNotificationBuilder implements NotificationBuilder<AcceptNotification> {
		@Override
		public AcceptNotification buildNotification(ThemePackage pack, Object... args) {
			if (args.length != 2 && args.length != 4)
				throw new NotificationException(
						"AcceptNotifications need two or four arguments: title, subtitle, accept text, decline text!");

			AcceptNotification note = new AcceptNotification();
			note.setWindowTheme(pack.getTheme(WindowTheme.class));
			note.setTextTheme(pack.getTheme(TextTheme.class));
			note.setTitle((String) args[0]);
			note.setSubtitle((String) args[1]);
			if (args.length == 4) {
				note.setAcceptText((String) args[2]);
				note.setDeclineText((String) args[3]);
			}

			return note;
		}
	}

	private class IconNotificationBuilder implements NotificationBuilder<IconNotification> {
		@Override
		public IconNotification buildNotification(ThemePackage pack, Object... args) {
			if (args.length != 3)
				throw new NotificationException("IconNotifications need three arguments: title, subtitle, icon!");

			IconNotification note = new IconNotification();
			note.setWindowTheme(pack.getTheme(WindowTheme.class));
			note.setTextTheme(pack.getTheme(TextTheme.class));
			note.setTitle((String) args[0]);
			note.setSubtitle((String) args[1]);
			note.setIcon((Icon) args[2]);
			return note;
		}
	}

	private class ProgressBarNotificationBuilder implements NotificationBuilder<ProgressBarNotification> {
		@Override
		public ProgressBarNotification buildNotification(ThemePackage pack, Object... args) {
			if (args.length != 1)
				throw new NotificationException("ProgressBarNotifications need one argument: title!");

			ProgressBarNotification note = new ProgressBarNotification();
			note.setWindowTheme(pack.getTheme(WindowTheme.class));
			note.setTextTheme(pack.getTheme(TextTheme.class));
			note.setTitle((String) args[0]);
			return note;
		}
	}
}
