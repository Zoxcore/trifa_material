package com.notification.types;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JTextArea;

import com.theme.TextTheme;
import com.theme.WindowTheme;

/**
 * A text notification which will display a title and a subtitle.
 */
public class TextNotification extends BorderLayoutNotification {
	protected JLabel m_titleLabel;
	protected JTextArea m_subtitleArea;

	private TextTheme m_textTheme;

	public TextNotification() {
		m_titleLabel = new JLabel();
		m_subtitleArea = new JTextArea();

		this.addComponent(m_titleLabel, BorderLayout.NORTH);
		this.addComponent(m_subtitleArea, BorderLayout.CENTER);
	}

	public String getTitle() {
		return m_titleLabel.getText();
	}

	public void setTitle(String title) {
		m_titleLabel.setText(title);
	}

	public String getSubtitle() {
		return m_subtitleArea.getText();
	}

	public void setSubtitle(String subtitle) {
		m_subtitleArea.setText(subtitle);
	}

	protected TextTheme getTextTheme() {
		return m_textTheme;
	}

	/**
	 * @param theme
	 *            the two Fonts that should be used.
	 */
	public void setTextTheme(TextTheme theme) {
		m_textTheme = theme;
		m_titleLabel.setFont(theme.title);
		m_subtitleArea.setFont(theme.subtitle);
		m_titleLabel.setForeground(theme.titleColor);
		m_subtitleArea.setForeground(theme.subtitleColor);
	}

	@Override
	public void setWindowTheme(WindowTheme theme) {
		super.setWindowTheme(theme);

		if (m_textTheme != null) {
			m_titleLabel.setForeground(m_textTheme.titleColor);
			m_subtitleArea.setForeground(m_textTheme.subtitleColor);
		}
	}
}
