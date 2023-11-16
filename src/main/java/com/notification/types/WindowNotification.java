package com.notification.types;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JWindow;

import com.notification.Notification;
import com.theme.WindowTheme;
import com.utils.MathUtils;

/**
 * A Notification which displays in a JWindow, handles click events, and allows subclasses to supply a JPanel. The
 * default Notification dimensions are set; if subclasses want to override this, they can do so in their constructors.
 */
public abstract class WindowNotification extends Notification {
	private JWindow m_window;
	private JPanel m_panel;
	private boolean m_closeOnClick;
	private MouseAdapter m_listener;

	private WindowTheme m_theme;

	private static final int DEFAULT_WIDTH = 300;
	private static final int DEFAULT_HEIGHT = 100;
	public static final String CLICKED = "clicked";
	public static final String SHOWN = "shown";
	public static final String HIDDEN = "hidden";

	public WindowNotification() {
		m_window = new JWindow();
		m_window.setAlwaysOnTop(true);

		m_listener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				fireListeners(CLICKED);
				if (m_closeOnClick)
					removeFromManager();
			}
		};

		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setPanel(new JPanel());
	}

	protected JWindow getWindow() {
		return m_window;
	}

	protected void setPanel(JPanel panel) {
		if (m_panel != null) {
			m_window.remove(m_panel);
			m_panel.removeMouseListener(m_listener);
		}

		m_panel = panel;

		m_window.add(m_panel);
		m_panel.addMouseListener(m_listener);
	}

	/**
	 * @return whether or not the Notification should close when it's clicked
	 */
	public boolean isCloseOnClick() {
		return m_closeOnClick;
	}

	/**
	 * @param close
	 *
	 *            whether or not the Notification should close when it's clicked
	 */
	public void setCloseOnClick(boolean close) {
		m_closeOnClick = close;
	}

	protected WindowTheme getWindowTheme() {
		return m_theme;
	}

	/**
	 * Sets the theme of the WindowNotification. It is up to the subclasses how they want to interpret the "image"
	 * attribute of the theme.
	 *
	 * @param theme
	 *            the WindowTheme to set
	 */
	public void setWindowTheme(WindowTheme theme) {
		m_theme = theme;

		m_window.setBackground(theme.background);
		m_window.setForeground(theme.foreground);
		m_window.setOpacity((float) theme.opacity);
		m_window.setSize(theme.width, theme.height);

		m_panel.setBackground(theme.background);
		m_panel.setForeground(theme.foreground);

		for (Component comp : m_panel.getComponents()) {
			recursiveSetTheme(theme, comp);
		}
	}

	private void recursiveSetTheme(WindowTheme theme, Component comp) {
		comp.setBackground(theme.background);
		comp.setForeground(theme.foreground);

		if (comp instanceof Container) {
			Container container = (Container) comp;
			for (Component component : container.getComponents()) {
				recursiveSetTheme(theme, component);
			}
		}
	}

	@Override
	public int getX() {
		return m_window.getX();
	}

	@Override
	public int getY() {
		return m_window.getY();
	}

	@Override
	public void setLocation(int x, int y) {
		m_window.setLocation(x, y);
	}

	@Override
	public int getWidth() {
		return m_window.getWidth();
	}

	@Override
	public int getHeight() {
		return m_window.getHeight();
	}

	@Override
	public void setSize(int width, int height) {
		m_window.setSize(width, height);
	}

	/**
	 * Gets the opacity of the window between 0 and 1.
	 *
	 * @return the opacity of the window
	 */
	@Override
	public double getOpacity() {
		return m_window.getOpacity();
	}

	/**
	 * Sets the opacity, overriding the value given in the window theme.
	 *
	 * @param opacity
	 *            the opacity (clamped to between 0 and 1)
	 */
	@Override
	public void setOpacity(double opacity) {
		m_window.setOpacity((float) MathUtils.clamp(opacity, 0, 1));
	}

	@Override
	public void show() {
		m_window.setVisible(true);
		fireListeners(SHOWN);
	}

	@Override
	public void hide() {
		m_window.dispose();
		fireListeners(HIDDEN);
	}

	@Override
	public boolean isShown() {
		return m_window.isVisible();
	}
}
