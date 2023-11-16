package com.notification.types;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.theme.WindowTheme;

/**
 * Lays out Swing Components in a BorderLayout.
 */
public class BorderLayoutNotification extends WindowNotification {
	private JPanel m_panel;

	public static final int PANEL_PADDING = 10;

	public BorderLayoutNotification() {
		super();

		m_panel = new JPanel(new BorderLayout());
		m_panel.setBorder(new EmptyBorder(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING));
		setPanel(m_panel);
	}

	/**
	 * Adds a Component to the Notification.
	 *
	 * @param comp
	 *            the Component to add
	 * @param borderLayout
	 *            the BorderLayout String, e.g. BorderLayout.NORTH
	 */
	public void addComponent(Component comp, String borderLayout) {
		m_panel.add(comp, borderLayout);

		WindowTheme theme = this.getWindowTheme();
		if (theme != null) {
			comp.setBackground(theme.background);
			comp.setForeground(theme.foreground);
		}
		getWindow().revalidate();
		getWindow().repaint();
	}

	/**
	 * Removes a component.
	 *
	 * @param comp
	 *            the Component to remove
	 */
	public void removeComponent(Component comp) {
		m_panel.remove(comp);

		getWindow().revalidate();
		getWindow().repaint();
	}
}
