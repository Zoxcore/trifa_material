package com.notification.manager;

import java.util.ArrayList;
import java.util.List;

import com.notification.Notification;
import com.notification.NotificationFactory.Location;
import com.notification.NotificationListener;
import com.notification.types.WindowNotification;
import com.utils.Time;

/**
 * Displays Notifications one after another in a certain location. As soon as the current Notification is hidden, a new
 * one will appear in its place.
 */
public class SequenceManager extends SimpleManager {
	private List<NotificationShowTime> m_sequence;
	private Notification m_currentNotification;

	{
		m_sequence = new ArrayList<NotificationShowTime>();
	}

	public SequenceManager() {
		super();
	}

	public SequenceManager(Location loc) {
		super(loc);
	}

	@Override
	public void notificationAdded(Notification notification, Time time) {
		notification.addNotificationListener(new CloseListener());
		if (m_currentNotification == null) {
			m_currentNotification = notification;
			superAdded(notification, time);
		} else {
			m_sequence.add(new NotificationShowTime(notification, time));
		}
	}

	private void superAdded(Notification notification, Time time) {
		super.notificationAdded(notification, time);
	}

	private class CloseListener implements NotificationListener {
		@Override
		public void actionCompleted(Notification notification, String action) {
			if (action.equals(WindowNotification.HIDDEN)) {
				m_currentNotification.removeNotificationListener(this);
				m_currentNotification = null;
				if (!m_sequence.isEmpty()) {
					NotificationShowTime showing = m_sequence.remove(0);
					m_currentNotification = showing.notification;
					superAdded(showing.notification, showing.time);
				}
			}
		}
	}

	private class NotificationShowTime {
		public Notification notification;
		public Time time;

		public NotificationShowTime(Notification notification, Time time) {
			this.notification = notification;
			this.time = time;
		}
	}
}
