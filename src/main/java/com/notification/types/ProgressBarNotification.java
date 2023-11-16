package com.notification.types;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.theme.TextTheme;

public class ProgressBarNotification extends BorderLayoutNotification {
	private JLabel m_label;
	private JProgressBar m_progress;

	public ProgressBarNotification() {
		m_label = new JLabel();
		m_progress = new JProgressBar();

		this.addComponent(m_label, BorderLayout.NORTH);

		JPanel progressPanel = new JPanel(new BorderLayout());
		progressPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
		progressPanel.add(m_progress, BorderLayout.CENTER);
		this.addComponent(progressPanel, BorderLayout.CENTER);
	}

	/**
	 * This will set the text font to that of the title font.
	 *
	 * @param theme
	 *            the TextTheme to set
	 */
	public void setTextTheme(TextTheme theme) {
		m_label.setFont(theme.title);
		m_label.setForeground(theme.titleColor);
	}

	public String getTitle() {
		return m_label.getText();
	}

	public void setTitle(String title) {
		m_label.setText(title);
	}

	/**
	 * Sest the progress of the progress bar, from 0 to 100.
	 *
	 * @param progress
	 *            the progress to set
	 */
	public void setProgress(int progress) {
		m_progress.setValue(progress);
	}

	/**
	 * @return the progress of the progress bar, from 0 to 100
	 */
	public int getProgress() {
		return m_progress.getValue();
	}
}
