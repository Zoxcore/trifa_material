package com.notification.types;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * An IconNotification displays text, but with an icon.
 */
public class IconNotification extends TextNotification {
	private JLabel m_iconLabel;

	public static final int ICON_PADDING = 10;

	public IconNotification() {
		super();
		m_iconLabel = new JLabel();

		this.removeComponent(m_titleLabel);
		this.removeComponent(m_subtitleArea);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(m_titleLabel, BorderLayout.NORTH);
		panel.add(m_subtitleArea, BorderLayout.CENTER);
		panel.setBorder(new EmptyBorder(0, ICON_PADDING, 0, 0));

		this.addComponent(m_iconLabel, BorderLayout.WEST);
		this.addComponent(panel, BorderLayout.CENTER);
	}

	/**
	 * Sets the icon to use.
	 *
	 * @param icon
	 *            the icon to use
	 */
	public void setIcon(Icon icon) {
		m_iconLabel.setIcon(icon);
	}

	/**
	 * @return the icon to use
	 */
	public Icon getIcon() {
		return m_iconLabel.getIcon();
	}
}
