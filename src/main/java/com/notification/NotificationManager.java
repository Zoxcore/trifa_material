package com.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import com.utils.Time;

/**
 * Manages the creation and movement of Notifications. Once a Notification is added, all aspects of it except for click
 * handeling are managed by the NotificationManager. This includes things such as showing and hiding.
 */
public abstract class NotificationManager {
	private List<Notification> m_notifications;

	public NotificationManager() {
		m_notifications = new ArrayList<Notification>();
	}

	/**
	 * @return all the Notifications being managed by the NotificationManager
	 */
	public final List<Notification> getNotifications() {
		return m_notifications;
	}

	/**
	 * Adds a Notification and will also make it visible.
	 *
	 * @param note
	 *            the Notification to be added
	 * @param time
	 *            the amount of time the Notification should display (e.g., Time.seconds(1) will make the Notification
	 *            display for one second).
	 */
	public final void addNotification(Notification note, Time time) {
		if (!m_notifications.contains(note)) {
			note.setNotificationManager(this);
			m_notifications.add(note);
			notificationAdded(note, time);
		}
	}

	/**
	 * Removes a Notification and will also hide it.
	 *
	 * @param note
	 *            the Notification to be removed
	 */
	public final void removeNotification(Notification note) {
		if (m_notifications.contains(note)) {
			m_notifications.remove(note);
			notificationRemoved(note);
			note.setNotificationManager(null);
		}
	}

	protected abstract void notificationAdded(Notification note, Time time);

	protected abstract void notificationRemoved(Notification note);

	protected void scheduleRemoval(Notification note, Time time) {
		if (!time.isInfinite()) {
			java.util.Timer removeTimer = new java.util.Timer();
			removeTimer.schedule(new RemoveTask(note), time.getMilliseconds());
		}
	}

	private class RemoveTask extends TimerTask {
		private Notification m_note;

		public RemoveTask(Notification note) {
			m_note = note;
		}

		@Override
		public void run() {
			NotificationManager.this.removeNotification(m_note);
		}
	}
}
