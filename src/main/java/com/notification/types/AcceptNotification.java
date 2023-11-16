package com.notification.types;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.theme.TextTheme;
import com.theme.WindowTheme;

/**
 * This is a Notification that will ask the user to accept of decline a certain action.
 */
public class AcceptNotification extends TextNotification {
	private JButton m_accept;
	private JButton m_decline;

	private boolean m_accepted;

	public AcceptNotification() {
		m_accept = new JButton("Accept");
		m_decline = new JButton("Decline");
		m_accepted = false;

		m_accept.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_accepted = true;
				removeFromManager();
			}
		});
		m_decline.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				m_accepted = false;
				removeFromManager();
			}
		});

		setButtonDimensions(new Dimension(100, 22));

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(m_decline);
		buttonPanel.add(m_accept);
		this.addComponent(buttonPanel, BorderLayout.SOUTH);
	}

	/**
	 * Will wait for the user to click a button (if the Notification hides, this method will act as if the user clicked
	 * deny).
	 *
	 * @return the user's response
	 */
	public boolean blockUntilReply() {
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		return m_accepted;
	}

	/**
	 * Sets the preferred size of the buttons.
	 *
	 * @param d
	 *            sets the dimensions of the accept and decline buttons
	 */
	public void setButtonDimensions(Dimension d) {
		m_accept.setPreferredSize(d);
		m_decline.setPreferredSize(d);
	}

	/**
	 * @return the text on the accept button
	 */
	public String getAcceptText() {
		return m_accept.getText();
	}

	/**
	 * Sets the text on the accept button.
	 *
	 * @param acceptText
	 *            the text on the accept button
	 */
	public void setAcceptText(String acceptText) {
		m_accept.setText(acceptText);
	}

	/**
	 * @return the text on the decline button
	 */
	public String getDeclineText() {
		return m_decline.getText();
	}

	/**
	 * Sets the text on the decline button.
	 *
	 * @param declineText
	 *            the text on the decline button
	 */
	public void setDeclineText(String declineText) {
		m_decline.setText(declineText);
	}

	@Override
	public void hide() {
		super.hide();

		synchronized (this) {
			this.notifyAll();
		}
	}

	@Override
	public void setTextTheme(TextTheme theme) {
		super.setTextTheme(theme);

		m_accept.setForeground(theme.subtitleColor);
		m_decline.setForeground(theme.subtitleColor);
	}

	@Override
	public void setWindowTheme(WindowTheme theme) {
		super.setWindowTheme(theme);

		if (getTextTheme() != null) {
			m_accept.setForeground(getTextTheme().subtitleColor);
			m_decline.setForeground(getTextTheme().subtitleColor);
		}
	}
}
