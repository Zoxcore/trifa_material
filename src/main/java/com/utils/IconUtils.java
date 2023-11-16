package com.utils;

import java.awt.Image;

import javax.swing.ImageIcon;

public class IconUtils {
	private IconUtils() {

	}

	/**
	 * Creates an ImageIcon of the specified path with a given width and height.
	 *
	 * @param path
	 *            the classpath, starting with the root (e.g., /com/demo/exclamation.png)
	 * @param width
	 *            the width in pixels
	 * @param height
	 *            the height in pixels
	 * @return the created ImageIcon
	 */
	public static ImageIcon createIcon(String path, int width, int height) {
		java.net.URL imgURL = IconUtils.class.getResource(path);
		if (imgURL != null) {
			ImageIcon icon = new ImageIcon(imgURL);
			icon = new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));

			return icon;
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}
}
